(ns aoc25.day03
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

(defn largest-12
  "Return the largest number formed by any twelve digits in order of appearance in the vector.
   Uses a greedy approach: at each step, pick the largest digit that leaves enough remaining."
  [v]
  (when (>= (count v) 12)
    (loop [remaining v
           needed 12
           result []]
      (if (zero? needed)
        (BigInteger. (str/join (map str result)))
        ;; Find the largest digit in the window where we can still pick enough digits after it
        (let [window-size (inc (- (count remaining) needed))  ; How far we can look ahead?
              window (subvec remaining 0 window-size)
              max-digit (apply max window)
              max-pos (.indexOf window max-digit)]
          (recur (subvec remaining (inc max-pos))
                 (dec needed)
                 (conj result max-digit)))))))

(defn part1
  [f]
  (let [m (read-data f)]
    (->> m
         (map largest-pair)
         (apply +))))

(defn part2
  [f]
  (let [m (read-data f)]
    (->> m
         (map largest-12)
         (apply +))))

(comment
  (def testf "data/day03-test.txt")
  (def inputf "data/day03-input.txt")

  (part1 testf)
  (part1 inputf)

  (time (part2 testf))
  (time (part2 inputf)))
  
# The End