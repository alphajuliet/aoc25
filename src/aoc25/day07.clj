(ns aoc25.day07
  (:require [aoc25.util :as u]
            [clojure.set :as set]
            [clojure.string :as str]))

(defn read-data
  [f]
  (->> f
       slurp
       str/split-lines
       (take-nth 2)))

(defn splitter-locs
  [s]
  (->> s
       (map-indexed #(vector %1 %2))
       (filter #(= (second %) \^))
       (map first)))

(defn split-beam
  "Split a beam if it hits the location of the splitter"
  [beams loc]
  (if (contains? beams loc)
    (-> beams
        (set/union (set [(dec loc) (inc loc)]))
        (set/difference (set (list loc))))
    beams))

(defn row-splits
  [beams splitter-locs]
  (reduce split-beam beams splitter-locs))

(defn generate-beams
  [start splitters]
  (reductions row-splits #{start} splitters))

(defn split-beam-multiple
  "Split a beam if it hits the location of the splitter. Record
   all beams as a map of multiplicities. Capture multiplicities, i.e. if 2 beams hit
   the splitter then 4 beams are generated: 2 on each side."
  [beams loc]
  (if (contains? beams loc)
    (let [n (get beams loc)]
      (-> beams
          (update (dec loc) #(if % (+ % n) n))
          (update (inc loc) #(if % (+ % n) n))
          (assoc loc 0))) 
              
    beams))

(defn row-splits-multiple
  [beams splitter-locs]
  (reduce split-beam-multiple beams splitter-locs))

(defn generate-beams-multiple
  [start splitters]
  (reduce row-splits-multiple (hash-map start 1) splitters))

(defn part1
  [f]
  (let [mf (read-data f)
        splitters (->> mf (map splitter-locs) rest)
        midpoint (int (/ (count (first mf)) 2))
        beams (generate-beams midpoint splitters)]
    (->> beams
         (map set/intersection (map set splitters))
         (map count)
         (apply +))))

(defn part2
  [f]
  (let [mf (read-data f)
        splitters (->> mf (map splitter-locs) rest)
        midpoint (-> mf first count (/ 2) int)
        beams (generate-beams-multiple midpoint splitters)]
    (->> beams
         vals
         (apply +))))

(comment
  (def testf "data/day07-test.txt")
  (def inputf "data/day07-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (part2 inputf))

;; The End