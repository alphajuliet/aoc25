(ns aoc25.day01
  (:require [clojure.string :as str]
            [aoc25.util :as util]))

(defn parse-instruction
  "Parse a single instruction as a positive or negative number"
  [s]
  (let [dir (first s)
        clicks (Integer/parseInt (subs s 1))]
    (cond
      (= dir \L) (- clicks)
      (= dir \R) clicks)))

(defn read-data
  [f]
  (->> f
       slurp
       str/split-lines
       (map parse-instruction)))

(defn idiv [a b] (int (Math/floor (/ a b))))

(defn part1
  [f]
  (->> f
       read-data
       (reductions + 50) ; Add up all the movements, starting at position 50
       (map #(mod % 100))
       (util/count-if zero?)))

(defn zero-crossings
  "Count how many times the dial reads zero when moving from
   unwrapped position a to unwrapped position b."
  [a b]
  (if (>= b a)
    (- (idiv b 100) (idiv a 100))              ; right: multiples in (a, b]
    (- (idiv (dec a) 100) (idiv (dec b) 100)))) ; left:  multiples in [b, a)

(defn part2
  "Count every time the dial passes through zero during all movements."
  [f]
  (->> f
       read-data
       (reductions + 50)
       (partition 2 1)
       (map (fn [[a b]] (zero-crossings a b)))
       (apply +)))

(comment
  (def testf "data/day01-test.txt")
  (def inputf "data/day01-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; Part 2 answer is not: 6697, 6698, 6696

;; The End