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
  "Measure all the distances between the points"
  [coll]
  (let [ncoords (count coll)]
    (for [c1 (range ncoords)
           c2 (range ncoords)
           :when (< c1 c2)]
       [[c1 c2] (dist3d (nth coll c1) (nth coll c2))])))

(defn connect-boxes
  [n dists]
  (let [connections (take n dists)
        nodes (->> dists (drop n) flatten distinct)]
    (-> (uber/graph)
        (uber/add-nodes* nodes)
        (uber/add-edges* connections))))

(defn part1
  [n f]
  (let [coords (read-data f)
        dists (->> coords
                   all-dists
                   (sort-by second)
                   (map first))] 
    (->> dists
         (connect-boxes n)
         (alg/connected-components)
         (map distinct)
         (map count)
         (sort >)
         (take 3)
         (apply *))))
         

(defn part2
  [f])

(comment
  (def testf "data/day08-test.txt")
  (def inputf "data/day08-input.txt")

  (part1 10 testf)
  (part1 1000 inputf)

  (part2 testf)
  (part2 inputf))

;; The End