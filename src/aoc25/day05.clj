(ns aoc25.day05
  (:require [aoc25.util :as u]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.edn :as edn]))

(defn to-range
  "Convert a range string to a vector tuple."
  [s]
  (let [r (str/split s #"-")]
    (mapv edn/read-string r)))

(defn read-data
  [f]
  (let [[fresh avail] (->> f
                           slurp
                           str/split-lines
                           (split-with (comp not empty?)))]
    {:fresh (sort-by first (mapv to-range fresh))
     :avail (mapv edn/read-string (drop 1 avail))}))

(defn in-range? [[a b] x] (<= a x b))

(defn in-ranges?
  "Is the value x in any of the collection of ranges?"
  [ranges x]
  (some #(in-range? % x) ranges))

(defn part1
  [f]
  (let [{:keys [fresh avail]} (read-data f)]
    (->> avail
         (map #(in-ranges? fresh %))
         (u/count-if true?))))

(defn merge-ranges
  "Merge two ranges if they overlap or are adjacent, otherwise return nil."
  [[a b] [c d]]
  (if (or (<= a c (inc b))  ; r2 starts within or adjacent to r1
          (<= c a (inc d))) ; r1 starts within or adjacent to r2
    [(min a c) (max b d)]
    nil))

(defn merge-all-ranges
  "Merge all overlapping ranges in a collection of ranges."
  [ranges]
  (loop [remaining (rest (sort-by first ranges))
         current (first (sort-by first ranges))
         result []]
    (if (empty? remaining)
      (conj result current)
      (let [next-range (first remaining)
            merged (merge-ranges current next-range)]
        (if merged
          ;; Successfully merged, continue with merged range as current
          (recur (rest remaining) merged result)
          ;; No merge, save current and move to next
          (recur (rest remaining) next-range (conj result current)))))))

(defn part2
  [f]
  (let [{:keys [fresh]} (read-data f)
        merged (merge-all-ranges fresh)]
    ;; Sum up the size of all merged ranges
    (->> merged
         (map (fn [[a b]] (inc (- b a))))
         (apply +))))
      
(comment
  (def testf "data/day05-test.txt")
  (def inputf "data/day05-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End