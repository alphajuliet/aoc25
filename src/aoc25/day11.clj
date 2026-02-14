(ns aoc25.day11
  (:require [aoc25.util :as u]
            [clojure.string :as str]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg]))

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

(defn count-paths-dag
  "Count all paths from start to end in a DAG using DP.
   Processes nodes in reverse topological order."
  [g start end]
  (let [topo (alg/topsort g)]
    (-> (reduce
         (fn [counts node]
           (if (= node end)
             (assoc counts node 1N)
             (assoc counts node
                    (->> (uber/successors g node)
                         (map #(get counts % 0N))
                         (reduce +' 0N)))))
         {}
         (reverse topo))
        (get start 0N))))

(defn paths-through-nodes
  "Count paths from start to end passing through all required nodes.
   Optimized: breaks into segments (start->n1->n2->end) instead of enumerating all paths."
  [g start end required-nodes]
  (if (= (count required-nodes) 2)
    ;; For 2 nodes: count paths through both orderings
    ;; Path ordering 1: svr -> n1 -> n2 -> out
    ;; Path ordering 2: svr -> n2 -> n1 -> out
    (let [[n1 n2] (vec required-nodes)
          paths1 (*' (count-paths-dag g start n1)
                     (count-paths-dag g n1 n2)
                     (count-paths-dag g n2 end))
          paths2 (*' (count-paths-dag g start n2)
                     (count-paths-dag g n2 n1)
                     (count-paths-dag g n1 end))]
      (+' paths1 paths2))
    ;; General fallback (not optimized)
    (throw (ex-info "Only 2 required nodes supported" {:count (count required-nodes)}))))

(defn part1
  [f]
  (let [conns (read-data f)]
    (-> (uber/digraph)
        (uber/add-directed-edges* conns)
        (all-paths "you" "out")
        count)))

(defn part2
  [f]
  (let [conns (read-data f)
        g (-> (uber/digraph)
              (uber/add-directed-edges* conns))]
    (paths-through-nodes g "svr" "out" #{"fft" "dac"}))) 

(comment
  (def testf "data/day11-test.txt")
  (def test2f "data/day11-test2.txt")
  (def inputf "data/day11-input.txt")
 
  (part1 testf)
  (part1 inputf)

  (part2 test2f)
  (time (part2 inputf)))

;; The End