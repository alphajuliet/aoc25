(ns aoc25.day08
  (:require [aoc25.util :as u]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]))

(defn read-data
  [f]
  (->> f
       slurp
       str/split-lines
       (map #(str/split % #","))
       (u/mapmap edn/read-string)))

(defn dist3d
  "Straight-line distance between two 3D coordinates."
  [c1 c2]
  (->> (mapv - c1 c2)
       (mapv #(* % %))
       (apply +)
       Math/sqrt))

(defn all-dists
  "Measure all the distances between pairs of points"
  [coll]
  (let [ncoords (count coll)]
    (for [c1 (range ncoords)
          c2 (range ncoords)
          :when (< c1 c2)]
      [[c1 c2] (dist3d (nth coll c1) (nth coll c2))])))

(defn connect-boxes
  [n pairs]
  (let [connections (take n pairs)
        nodes (->> pairs (drop n) flatten distinct)]
    (-> (uber/graph)
        (uber/add-nodes* nodes)
        (uber/add-edges* connections))))

(defn connect-all-boxes
  "Keep connecting boxes until all are connected in a single circuit"
  [pairs]
  (let [nodes (->> pairs flatten distinct)
        graph (-> (uber/graph) (uber/add-nodes* nodes))]
    (reduce
     (fn [g pair]
       (let [g' (uber/add-edges g pair)]
         (if (alg/strongly-connected? g')
           (reduced pair)
           g')))
     graph
     pairs)))

(defn part1
  [n f]
  (let [coords (read-data f)
        pairs (->> coords
                   all-dists
                   (sort-by second)
                   (map first))]
    (->> pairs
         (connect-boxes n)
         (alg/connected-components)
         (map distinct)
         (map count)
         (sort >)
         (take 3)
         (apply *))))

(defn part2
  [f]
  (let [coords (read-data f)
        pairs (->> coords
                   all-dists
                   (sort-by second)
                   (map first))]
    (->> pairs
         connect-all-boxes
         (mapv #(nth coords %))
         (map first)
         (apply *))))

(comment
  (def testf "data/day08-test.txt")
  (def inputf "data/day08-input.txt")

  (part1 10 testf)
  (part1 1000 inputf)

  (part2 testf)
  (part2 inputf))

;; The End