(ns aoc25.day11
  (:require
   [aoc25.util :as u]
   [ubergraph.core :as uber]
   [clojure.string :as str]))

(defn make-pairs
  [p]
  (for [d (rest p)]
    [(first p) d]))

(defn read-data
  [f]
  (->> f
       slurp
       str/split-lines
       (map #(str/split % #":?\s"))
       (map make-pairs)
       (apply concat)))

(defn all-paths
  "Find all paths from start to end in a directed graph."
  [g start end]
  (letfn [(path-finder [current-path]
            (let [current-node (last current-path)]
              (if (= current-node end)
                [current-path]
                (->> (uber/successors g current-node)
                     (remove (set current-path))  ; Prevent cycles
                     (mapcat #(path-finder (conj current-path %)))
                     seq))))]
    (path-finder [start])))

(defn part1
  [f]
  (let [devs (read-data f)]
    (-> (uber/digraph)
        (uber/add-directed-edges* devs)
        (all-paths "you" "out")
        count)))

(defn part2
  [f])

(comment
  (def testf "data/day11-test.txt")
  (def inputf "data/day11-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (time (part2 inputf)))

;; The End