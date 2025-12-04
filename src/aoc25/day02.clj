(ns aoc25.day02
  (:require [clojure.string :as str]
            [aoc25.util :as util]
            [clojure.edn :as edn]))

(defn read-data
  "Read the ranges as a collection of tuples of start and end integers"
  [f]
  (as-> f <>
    (slurp <>)
    (str/trim-newline <>)
    (str/split <> #",|-")
    (map edn/read-string <>)
    (partition 2 <>)
    (map vec <>)))

(defn invalid-id?
  "Given a number, is it made up only of a pair of repeated integers?
   Examples are: 11, 1212, 11221122, 222222"
  [n]
  (let [s (str n)
        len (count s)]
    (if (odd? len)
      false
      (let [half-len (/ len 2)
            h1 (subs s 0 half-len)
            h2 (subs s half-len)]
        (= h1 h2)))))

(defn invalid-ids-in-range
  "Return a collection of all invalid IDs in the given range (inclusive)"
  [[start end]]
  (filter invalid-id? (range start (inc end))))

(defn part1
  [f]
  (let [ranges (read-data f)]
     (->> ranges
          (map invalid-ids-in-range)
          (apply concat)
          (apply +))))

(comment
  (def testf "data/day02-test.txt")
  (def inputf "data/day02-input.txt")

  (part1 testf)
  (part1 inputf))

# The End