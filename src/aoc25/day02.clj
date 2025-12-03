(ns aoc25.day02
  (:require [clojure.string :as str]
            [aoc25.util :as util]))
           
(defn read-data
 "Read the input file as a matrix of integers"
 [f]
 (->> f
      slurp
      str/split-lines
      (map #(str/split % #""))
      (util/mapmap Integer/parseInt)))

(defn largest-pair
 "Return the largest number formed by the two largest digits in order in the vector"
 [v]
 (let [a (apply max (butlast v))
       a-pos (.indexOf v a)
       b (apply max (subvec v (inc a-pos)))]
   (+ (* a 10) b)))

(defn part1
 [f]
 (let [m (read-data f)]
    (->> m
         (map largest-pair)
         (apply +))))

(comment
  (def testf "data/day02-test.txt")
  (def inputf "data/day02-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))