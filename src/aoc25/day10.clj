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
     :joltage joltage}))

(defn read-data
 [f]
 (->> f
      slurp
      str/split-lines
      (map parse-line)))

(defn shortest-path
  "Find shortest path from 0 to target using XOR switches."
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
(defn part1
  [f]
  (let [machines (read-data f)]
    (->> machines
         (map shortest-path)
         (map count)
         (apply +))))

(defn part2
 [f])

(comment
  (def testf "data/day10-test.txt")
  (def inputf "data/day10-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (time (part2 inputf)))

;; The End