(ns aoc25.day04
  (:require [clojure.string :as str]
            [aoc25.util :as util]
            [clojure.core.matrix :as m]
            [clojure.edn :as edn]))

(defn read-data
  "Read the data into a matrix of binary digits"
  [f]
  (->> f
       slurp
       str/split-lines
       (map #(str/replace % #"@" "1"))
       (map #(str/replace % #"\." "0"))
       (map #(str/split % #""))
       (util/mapmap edn/read-string)))

(defn neighbours
  "Return the values of neighbours in all 8 directions"
  [mat [i j]]
  (let [dirs [[-1 -1] [-1 0] [-1 1]
              [0 -1]         [0 1]
              [1 -1]  [1 0]  [1 1]]]
    (->> dirs
         (map #(util/safe-mget mat (map + [i j] %)))
         (remove nil?))))

(defn count-neighbours
  "Count the number of occupied neighbours"
  [mat coord]
  (if (pos-int? (util/safe-mget mat coord))
    (->> (neighbours mat coord)
         (filter #(= 1 %))
         count) 
    nil))
    
(defn part1
  [f]
  (let [mat (read-data f)]
    (->> mat
         m/index-seq
         (map #(count-neighbours mat %))
         (remove nil?)
         (filter #(< % 4))
         count)))

(comment
  (def testf "data/day04-test.txt")
  (def inputf "data/day04-input.txt")

  (part1 testf)
  (part1 inputf))

  ;; (part2 testf)
  ;; (part2 inputf))

;; The End