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

(defn prime-factors
  "Return the prime factors of n as a vector"
  [n]
  (loop [num n
         factor 2
         factors []]
    (cond
      (= num 1) factors
      (zero? (mod num factor)) (recur (/ num factor) factor (conj factors factor))
      :else (recur num (inc factor) factors))))

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

(defn invalid-id-2?
  "Given a number, is it made up of any number of repeated sequences?
   Examples are: 11, 222, 34343434, 5555555"
  [n]
  ;; Find the prime factors of the length of the number string, including 1
  (let [s (str n)
        len (count s)
        factors (prime-factors len)
        unique-factors (set factors)]
    (some
      (fn [f]
        (let [sub-len (/ len f)
              sub-str (subs s 0 sub-len)
              repeated-str (apply str (repeat f sub-str))]
          (= s repeated-str)))
      unique-factors)))
  

(defn invalid-ids-in-range
  "Return a collection of all invalid IDs in the given range (inclusive)"
  [f [start end]]
  (filter f (range start (inc end))))

(defn part1
  [f]
  (let [ranges (read-data f)]
    (->> ranges
         (map (partial invalid-ids-in-range invalid-id?))
         (apply concat)
         (apply +))))

(defn part2
  [f]
  (let [ranges (read-data f)]
    (->> ranges
         (map (partial invalid-ids-in-range invalid-id-2?))
         (apply concat)
         (apply +))))

(comment
  (def testf "data/day02-test.txt")
  (def inputf "data/day02-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

#The End