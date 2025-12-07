(ns aoc25.day06
  (:require [aoc25.util :as u]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn read-data
 [f]
 (->> f
      slurp
      str/split-lines
      (map str/trim)
      (map #(str/split % #"\s+"))
      (u/mapmap edn/read-string)
      u/T))

(defn calc-expr
 [coll]
 (let [args (butlast coll)]
   (case (last coll)
     + (apply + args)
     * (apply * args)
     :else (throw (Exception. (str "Unknown operator: " (last coll)))))))

(defn part1
  [f]
  (let [data (read-data f)]
    (->> data
         (map calc-expr)
         (apply +))))

(defn part2
 [f])

(comment
  (def testf "data/day06-test.txt")
  (def inputf "data/day06-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End