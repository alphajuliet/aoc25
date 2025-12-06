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

(defn part2
  [f])

(comment
  (def testf "data/day05-test.txt")
  (def inputf "data/day05-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End