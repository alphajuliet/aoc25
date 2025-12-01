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

(defn part1
  [f]
  (let [lines (str/split-lines (slurp f))]
    (->> lines
         (map parse-instruction)
         (reduce
          (fn [acc x] (conj acc (mod (+ (first acc) x) 100)))
          (list 50))
         (util/count-if zero?))))

(defn part2
  [f]
  (let [lines (str/split-lines (slurp f))]
    (->> lines
         (map parse-instruction))))
    
(comment
  (def testf "data/day01-test.txt")
  (def inputf "data/day01-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End