(ns aoc25.day10
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

(defn bits->number
  "Convert a collection of bit positions into a integer
   e.g. `(bits->number '(1 3)) => 10`"
  ;; bits->number : List Integer -> Integer
  [bcoll]
  (reduce #(+ %1 (bit-shift-left 1 %2))
          0
          bcoll))

(defn parse-line
  "Parse the machine specification line and convert to useful data."
  [machine]
  (let [target (re-find #"^\[.+\]" machine)
        switches (re-seq #"\([\d\s,]+\)" machine)
        joltage (re-find #"\{.+\}" machine)]
    {:target (-> target
                 str/reverse
                 (subs 1 (dec (count target)))
                 (str/replace #"\." "0")
                 (str/replace #"#" "1")
                 (Integer/parseInt 2))
     :switches (->> switches
                    (map edn/read-string)
                    (map bits->number))
     :counters (->> switches
                    (map edn/read-string))
     :joltage (-> joltage
                  (str/replace #"\{" "(")
                  (str/replace #"\}" ")")
                  edn/read-string)}))

(defn read-data
  [f]
  (->> f
       slurp
       str/split-lines
       (map parse-line)))

(defn shortest-path
  "Find shortest path using BFS from 0 to target using XOR switches."
  [{:keys [target switches]}]
  (loop [queue (conj clojure.lang.PersistentQueue/EMPTY [0 []])
         visited #{0}]
    (if (empty? queue)
      nil  ; goal not reachable
      (let [[curr path] (peek queue)
            queue' (pop queue)]
        (if (= curr target)
          path
          (let [neighbors (for [g switches
                                :let [next-v (bit-xor curr g)]
                                :when (not (visited next-v))]
                            [next-v g])
                new-entries (map (fn [[v g]] [v (conj path g)]) neighbors)]
            (recur (into queue' new-entries)
                   (into visited (map first neighbors)))))))))

(defn counter->vec
  "Convert a counter to a vector, with length n.
  e.g. (counter->vec 4 '(2 3)) => [0 0 1 1]"
  [n c]
  (let [counter-set (set c)]
    (vec (for [i (range n)]
           (if (counter-set i) 1 0)))))

(defn transpose
  "Transpose a matrix"
  [m]
  (apply mapv vector m))

(defn rref
  "Reduce matrix to reduced row echelon form using Gaussian elimination.
   Matrix is [A|b] where we're solving Ax = b.
   Returns [reduced-matrix, pivot-cols]"
  [matrix]
  (let [rows (count matrix)
        cols (count (first matrix))
        m (mapv vec matrix)]
    (loop [mat m
           row 0
           col 0
           pivots []]
      (if (or (>= row rows) (>= col (dec cols))) ; dec cols because last col is augmented
        [mat pivots]
        (let [;; Find pivot (non-zero element in current column)
              pivot-row (first (filter #(not= 0 (get-in mat [% col]))
                                       (range row rows)))]
          (if (nil? pivot-row)
            ;; No pivot in this column, move to next column
            (recur mat row (inc col) pivots)
            (let [;; Swap rows if needed
                  mat' (if (= pivot-row row)
                         mat
                         (assoc mat row (mat pivot-row) pivot-row (mat row)))
                  pivot-val (get-in mat' [row col])
                  ;; Eliminate all other rows
                  mat'' (reduce
                         (fn [m r]
                           (if (= r row)
                             m
                             (let [factor (/ (get-in m [r col]) pivot-val)]
                               (assoc m r (mapv - (m r) (mapv #(* factor %) (m row)))))))
                         mat'
                         (range rows))]
              (recur mat'' (inc row) (inc col) (conj pivots col)))))))))

(defn find-particular-solution
  "Given RREF matrix and pivot columns, extract a particular solution"
  [rref-mat pivots]
  (let [rows (count rref-mat)
        cols (dec (count (first rref-mat))) ; exclude augmented column
        solution (vec (repeat cols 0))]
    (reduce
     (fn [sol row-idx]
       (if (< row-idx (count pivots))
         (let [pivot-col (nth pivots row-idx)
               pivot-val (get-in rref-mat [row-idx pivot-col])
               rhs (get-in rref-mat [row-idx (dec (count (first rref-mat)))])]
           (assoc sol pivot-col (/ rhs pivot-val)))
         sol))
     solution
     (range rows))))

(defn solve-linear-system
  "Solve Ax = b for non-negative integer solutions minimizing sum(x).
   x is the transposed counter matrix (rows are counters, cols are positions)
   Returns the minimum sum or nil if no solution exists."
  [x b]
  (let [x-cols (transpose x) ; Now each row is a position equation
        cols (count x)
        ;; Build augmented matrix [A|b] where A is transposed
        aug-matrix (mapv (fn [row target]
                           (conj (vec row) target))
                         x-cols b)
        [rref-mat pivots] (rref aug-matrix)

        ;; Check for inconsistency (0 = non-zero in any row)
        inconsistent? (some (fn [row]
                              (and (every? zero? (take cols row))
                                   (not (zero? (last row)))))
                            rref-mat)]

    (if inconsistent?
      nil ; No solution exists
      (let [pivot-set (set pivots)
            free-vars (vec (remove pivot-set (range cols)))

            ;; Get particular solution (set free variables to 0)
            particular (find-particular-solution rref-mat pivots)]

        (if (empty? free-vars)
          ;; Unique solution - check if it's valid (non-negative integers)
          (if (every? #(and (integer? %) (>= % 0)) particular)
            (reduce + particular)
            nil)

          ;; Multiple solutions - need to search over free variables
          ;; For each free variable, find how changing it affects pivot variables
          (let [;; Build null space basis vectors
                null-basis (for [free-var free-vars]
                             (let [basis-vec (vec (repeat cols 0))]
                               (reduce
                                (fn [vec row-idx]
                                  (if (< row-idx (count pivots))
                                    (let [pivot-col (nth pivots row-idx)
                                          coeff (get-in rref-mat [row-idx free-var])
                                          pivot-val (get-in rref-mat [row-idx pivot-col])]
                                      (assoc vec pivot-col (- (/ coeff pivot-val))))
                                    vec))
                                (assoc basis-vec free-var 1)
                                (range (count rref-mat)))))

                ;; Find bounds on free variables to keep all variables non-negative
                max-free-vals (mapv (fn [_] 100) free-vars) ; Start with reasonable bound

                ;; Try to find minimum by searching over free variable space
                best (atom nil)]

            (letfn [(search [free-idx free-vals]
                      (if (= free-idx (count free-vars))
                        ;; Compute solution for these free variable values
                        (let [solution (reduce
                                        (fn [sol [basis-vec val]]
                                          (mapv + sol (mapv #(* val %) basis-vec)))
                                        particular
                                        (map vector null-basis free-vals))]
                          (when (every? #(and (>= % 0) (integer? %)) solution)
                            (let [sum (reduce + solution)]
                              (swap! best (fn [b] (if (or (nil? b) (< sum b)) sum b))))))

                        ;; Try different values for current free variable
                        (doseq [val (range (inc (nth max-free-vals free-idx)))]
                          (search (inc free-idx) (conj free-vals val)))))]

              (search 0 []))
            @best))))))

(defn solve-backtrack [x b]
  (let [x (mapv vec x)
        n (count x)
        m (count (first x))
        b (vec b)
        bounds (mapv (fn [row]
                       (let [active (keep-indexed (fn [j v] (when (= v 1) (nth b j))) row)]
                         (if (seq active) (apply min active) (apply max b))))
                     x)
        max-remaining (vec (for [k (range (inc n))]
                             (mapv (fn [j]
                                     (reduce + 0 (for [i (range k n)]
                                                   (* (nth bounds i) (get-in x [i j])))))
                                   (range m))))
        best-so-far (atom Long/MAX_VALUE)
        ;; Greedy initial solution to get a good bound
        _ (loop [k 0, sums (vec (repeat m 0)), total 0]
            (when (< k n)
              (let [row-k (nth x k)
                    ;; Find minimum v that satisfies constraints
                    v (first (for [v (range (inc (nth bounds k)))
                                   :let [new-sums (mapv + sums (map #(* v %) row-k))
                                         remaining (nth max-remaining (inc k))]
                                   :when (every? (fn [j]
                                                   (>= (+ (new-sums j) (remaining j)) (b j)))
                                                 (range m))]
                               v))]
                (when v
                  (let [new-sums (mapv + sums (map #(* v %) row-k))
                        new-total (+ total v)]
                    (if (= (inc k) n)
                      (when (= new-sums b)
                        (reset! best-so-far new-total))
                      (recur (inc k) new-sums new-total)))))))
        search (fn search [k sums current-sum]
                 (if (= k n)
                   (when (= sums b)
                     (swap! best-so-far min current-sum))
                   (let [row-k (nth x k)]
                     (doseq [v (range (inc (nth bounds k)))]
                       (let [new-sum (+ current-sum v)
                             new-sums (mapv + sums (map #(* v %) row-k))
                             remaining (nth max-remaining (inc k))]
                         (when (and (< new-sum @best-so-far)
                                    (every? (fn [j]
                                              (and (<= (new-sums j) (b j))
                                                   (>= (+ (new-sums j) (remaining j)) (b j))))
                                            (range m)))
                           (search (inc k) new-sums new-sum)))))))]
    (search 0 (vec (repeat m 0)) 0)
    @best-so-far))

(defn solve-counters
 [{:keys [counters joltage]}]
 (let [n (count joltage)
       x (mapv (partial counter->vec n) counters)]
   (or (solve-linear-system x joltage)
       (solve-backtrack x joltage))))

(defn part1
  [f]
  (let [machines (read-data f)]
    (->> machines
         (map shortest-path)
         (map count)
         (apply +))))

(defn part2
  [f]
  (let [machines (read-data f)]
    (->> machines
         #_(take 5)
         (pmap solve-counters)
         (apply +))))

(comment
  (def testf "data/day10-test.txt")
  (def inputf "data/day10-input.txt")

  (part1 testf)
  (part1 inputf)

  (time (part2 testf))
  (time (part2 inputf)))

;; The End
