(ns aoc25.day09
  (:require [aoc25.util :as u]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn read-data
 [f]
 (->> f
      slurp
      str/split-lines
      (map #(str/split % #","))
      (u/mapmap edn/read-string)))

(defn rect-area
  "Calculate the area of the rectangle with the given opposite corners."
  [[x1 y1] [x2 y2]]
  (* (inc (abs (- x1 x2)))
     (inc (abs (- y1 y2)))))

(defn part1
  [f]
  (let [coords (read-data f)]
    (->> (for [p coords
               q coords
               :when (< (first p) (first q))]
           (rect-area p q))
         (sort >)
         first)))

(defn part2
 [f])

(comment
  (def testf "data/day09-test.txt")
  (def inputf "data/day09-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End