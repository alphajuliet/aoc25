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

(defn remove-rolls
  "Remove all the rolls that have fewer than 4 neighbours"
  [mat]
  (let [ii (m/index-seq mat)
        cc (map (partial count-neighbours mat) ii)]
    (->> (zipmap ii cc)
         (remove #(or (nil? (val %))
                      (>= (val %) 4)))
         ;; Set those positions to 0
         (reduce (fn [acc e]
                   (apply m/mset acc (conj (first e) 0)))
                 mat))))

(defn part1
  [f]
  (let [mat (read-data f)]
    (->> mat
         m/index-seq
         (map #(count-neighbours mat %))
         (remove nil?)
         (filter #(< % 4))
         count)))

(defn part2
  [f]
  (let [mat (read-data f)]
    ;; Iteratively remove rolls until stable
    (->> (reduce
          (fn [curr-mat _]
            (let [new-mat (remove-rolls curr-mat)]
              (if (m/e= new-mat curr-mat)
                (reduced new-mat)
                new-mat)))
          mat
          (range 100))
         ;; Count how many were removed
         m/esum
         (- (m/esum mat)))))

(comment
  (def testf "data/day04-test.txt")
  (def inputf "data/day04-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End