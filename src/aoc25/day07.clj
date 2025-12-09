(ns aoc25.day07
  (:require [aoc25.util :as u]
            [clojure.set :as set]
            [clojure.string :as str]))

(defn read-data
  [f]
  (->> f
       slurp
       str/split-lines
       (take-nth 2)))

(defn splitter-locs
  [s]
  (->> s
       (map-indexed #(vector %1 %2))
       (filter #(= (second %) \^))
       (map first)))

(defn split-beam
  "Split a beam if it hits the location of the splitter"
  [beams loc]
  (if (contains? beams loc)
    (-> beams
        (set/union (set [(dec loc) (inc loc)]))
        (set/difference (set (list loc))))
    beams))

(defn row-splits
  [beams splitter-locs]
  (reduce split-beam beams splitter-locs))

(defn generate-beams
 [start splitters]
 (reductions row-splits #{start} splitters))

(defn part1
  [f]
  (let [mf (read-data f)
        splitters (->> mf (map splitter-locs) rest)
        midpoint (int (/ (count (first mf)) 2))
        beams (generate-beams midpoint splitters)]
    (->> beams
         (map set/intersection (map set splitters))
         (map count)
         (apply +))))
    
(defn part2
  [f])

(comment
  (def testf "data/day07-test.txt")
  (def inputf "data/day07-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End