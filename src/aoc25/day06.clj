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

(defn parse-vector
  "Parse a vector of character digits and spaces into an integer or nil"
  [coll]
  (->> coll
       (map str)
       str/join
       edn/read-string))

(defn read-data-2
  "Parse the grid vertically"
  [f]
  (let [raw-data (->> f slurp str/split-lines)]
    {:grid (->> raw-data
                butlast
                u/T
                (map parse-vector)
                (partition-by nil?)
                (remove #(= '(nil) %))
                (map vec)
                vec)
     :ops (-> raw-data
              last
              str/trim
              (str/split #"\s+"))}))
               
(defn calc-expr
 [coll]
 (let [args (butlast coll)]
   (case (last coll)
     + (apply + args)
     * (apply * args)
     "+" (apply + args)
     "*" (apply * args)
     :else (throw (Exception. (str "Unknown operator: " (last coll)))))))

(defn part1
  [f]
  (let [data (read-data f)]
    (->> data
         (map calc-expr)
         (apply +))))

(defn part2
  [f]
  (let [{:keys [grid ops]} (read-data-2 f)]
    (->> ops
         (map conj grid)
         (map calc-expr)
         (apply +))))

(comment
  (def testf "data/day06-test.txt")
  (def inputf "data/day06-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End