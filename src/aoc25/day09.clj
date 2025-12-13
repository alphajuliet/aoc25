(ns aoc25.day09
  (:require [aoc25.util :as u]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn read-data
 [f]
 (->> f
      slurp
      str/split-lines
      (map #(str/split % #","))
      (u/mapmap edn/read-string)))

(defn rect-area
  "Calculate the area of the rectangle with the given opposite corners."
  [[x1 y1] [x2 y2]]
  (* (inc (abs (- x1 x2)))
     (inc (abs (- y1 y2)))))

(defn edges
  "Returns lazy seq of [a b] edge pairs from polygon vertices."
  [poly]
  (map vector poly (concat (rest poly) [(first poly)])))

(defn- on-segment?
  "Is point p on axis-aligned segment a-b?"
  [[px py] [[ax ay] [bx by]]]
  (and (<= (min ax bx) px (max ax bx))
       (<= (min ay by) py (max ay by))))

(defn- ray-crosses?
  "Does vertical edge cross horizontal ray from p to +∞?
   Half-open interval [minY, maxY) avoids double-counting vertices."
  [[px py] [[ax ay] [bx by]]]
  (and (= ax bx)                          ; vertical edge only
       (<= (min ay by) py)
       (< py (max ay by))
       (> ax px)))

(defn inside-orth?
  "Inside-or-boundary test for simple orthogonal polygon."
  [p poly-edges]
  (or (some #(on-segment? p %) poly-edges)
      (odd? (count (filter #(ray-crosses? p %) poly-edges)))))

(defn point-in-rect?
  "Is point strictly inside rectangle (not on boundary)?"
  [[px py] [x1 y1] [x2 y2]]
  (let [min-x (min x1 x2)
        max-x (max x1 x2)
        min-y (min y1 y2)
        max-y (max y1 y2)]
    (and (< min-x px max-x)
         (< min-y py max-y))))

(defn sample-rect-edge
  "Sample edge points of a rectangle. For large rectangles, sample every N points."
  [[x1 y1] [x2 y2]]
  (let [min-x (min x1 x2)
        max-x (max x1 x2)
        min-y (min y1 y2)
        max-y (max y1 y2)
        width (inc (- max-x min-x))
        height (inc (- max-y min-y))
        ;; Sample every N points for large rectangles
        x-step (max 1 (quot width 100))
        y-step (max 1 (quot height 100))
        x-samples (range min-x (inc max-x) x-step)
        y-samples (range min-y (inc max-y) y-step)]
    (concat
      ;; Always include corners
      [[min-x min-y] [min-x max-y] [max-x min-y] [max-x max-y]]
      ;; Sample top and bottom edges
      (for [x x-samples] [x min-y])
      (for [x x-samples] [x max-y])
      ;; Sample left and right edges
      (for [y y-samples] [min-x y])
      (for [y y-samples] [max-x y]))))

(defn covering-rect?
  "Does the bounding rectangle defined by v1 and v2 only cover the simple polygon?
   Uses sampling for large rectangles to avoid checking billions of points."
  [poly-edges vertices v1 v2]
  ;; Strategy: Sample rectangle boundary (not all points for huge rects)
  ;; Then ensure no polygon vertices are strictly inside rectangle
  (let [sample-pts (sample-rect-edge v1 v2)]
    (and (every? #(inside-orth? % poly-edges) sample-pts)
         (not-any? #(point-in-rect? % v1 v2) vertices))))

(defn all-rects
  "Find all the rectangles defined by pairs of vertices"
  [vertices]
  (for [p vertices
        q vertices
        :when (< (first p) (first q))]
    [p q]))

(defn bounding-box-area
  "Calculate area of polygon's bounding box"
  [vertices]
  (let [xs (map first vertices)
        ys (map second vertices)]
    (* (inc (- (apply max xs) (apply min xs)))
       (inc (- (apply max ys) (apply min ys))))))

(defn part1
  [f]
  (->> f
       read-data
       all-rects
       (map #(apply rect-area %))
       (sort >)
       first))

(defn part2
  [f]
  (let [vertices (read-data f)
        poly-edges (vec (edges vertices))
        max-area (bounding-box-area vertices)
        rects (all-rects vertices)]
    ;; Sort by area descending - check largest first, stop when we find a match
    (->> rects
         ;; Pre-filter: only rectangles <= bounding box area
         (filter #(<= (apply rect-area %) max-area))
         ;; Compute area and sort descending
         (map (fn [r] [r (apply rect-area r)]))
         (sort-by second >)
         ;; Find first valid rectangle (largest that covers polygon)
         (some (fn [[r area]]
                 (when (covering-rect? poly-edges vertices (first r) (second r))
                   area))))))

(comment
  (def testf "data/day09-test.txt")
  (def inputf "data/day09-input.txt")

  (part1 testf)
  (part1 inputf)

  (part2 testf)
  (time (part2 inputf)))
  
;; The End