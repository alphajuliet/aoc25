(ns aoc25.day12 
  (:require [clojure.string :as str]
            [clojure.edn :as edn]))

;; --------------------------------
;; Read and do a first parse of the input data

(defn parse-region
 "Parse a region string into dimensions and quantities."
 [region]
 (let [[size qs] (str/split region #":\s")]
   [(map edn/read-string (re-seq #"\d+" size)) 
    (map edn/read-string (re-seq #"\d+" qs))]))

(defn parse-shape
  "Parse a shape"
  [shape]
  (let [num (edn/read-string (str/replace (first shape) ":" ""))]
    [num (rest shape)]))

(defn read-data
 [f]
 (let [raw (-> f slurp str/split-lines)
       [shapes regions] ((juxt filter remove) #(nil? (re-find #"^\d+x\d+:" %)) raw)]
   [(->> shapes
         (partition-by empty?)
         (remove #(= (list "") %))
         (map parse-shape))
    (map parse-region regions)]))

;; --------------------------------
;; Get all orientations of all the given tiles.

(defn parse-tile
  "Convert string representation to set of [x y] coordinates.
   Origin is top-left, y increases downward, x increases rightward.
   
   Example: (\"###\" \"##.\" \"##.\") 
         -> #{[0 0] [1 0] [2 0] [0 1] [1 1] [0 2] [1 2]}"
  [rows]
  (into #{}
        (for [[y row] (map-indexed vector rows)
              [x ch]  (map-indexed vector row)
              :when (= ch \#)]
          [x y])))

(defn rotate-90
  "Rotate coordinates 90° clockwise around origin.
   [x y] -> [y -x]"
  [coords]
  (into #{} (map (fn [[x y]] [y (- x)]) coords)))

(defn reflect-x
  "Reflect across the y-axis (flip horizontally).
   [x y] -> [-x y]"
  [coords]
  (into #{} (map (fn [[x y]] [(- x) y]) coords)))

(defn normalise
  "Translate coordinates so minimum x and y are both 0.
   This gives us a canonical position for comparison."
  [coords]
  (let [min-x (reduce min (map first coords))
        min-y (reduce min (map second coords))]
    (into #{} (map (fn [[x y]] [(- x min-x) (- y min-y)]) coords))))

(defn all-transforms
  "Generate all 8 orientations (4 rotations × 2 reflections).
   Returns a sequence of coordinate sets, not yet deduplicated."
  [coords]
  (let [rotations (take 4 (iterate rotate-90 coords))
        reflected (map reflect-x rotations)]
    (concat rotations reflected)))

(defn unique-orientations
  "Generate all unique orientations of a tile.
   Returns a set of normalised coordinate sets (1-8 depending on symmetry)."
  [coords]
  (->> (all-transforms coords)
       (map normalise)
       (into #{})))

(defn tile-orientations
  "Parse a tile and generate all its unique orientations.
   
   Input:  [id [\"###\" \"##.\" \"##.\"]]
   Output: [id #{#{[0 0] [1 0] ...} #{...} ...}]"
  [[id rows]]
  [id (unique-orientations (parse-tile rows))])

(defn generate-all-orientations
  "Process a collection of tile definitions into orientation lookup.
   
   Input:  ([0 (\"###\" \"##.\" \"##.\")] [1 (\"###\" \"##.\" \".##\")] ...)
   Output: {0 #{orientation1 orientation2 ...}
            1 #{...}
            ...}"
  [tile-defs]
  (into {} (map tile-orientations tile-defs)))

;; =============================================================================
;; Set up the DLX construction

(defn parse-problem
  "Parse the problem format into a structured map.
   
   Input:  [(4 4) (0 0 0 0 2 0)]
   Output: {:width 4
            :height 4
            :tiles {4 2}}  ; tile-id -> quantity (only non-zero)"
  [[[width height] quantities]]
  {:width  width
   :height height
   :tiles  (into {}
                 (comp (map-indexed vector)
                       (filter (fn [[_ qty]] (pos? qty))))
                 quantities)})

(defn in-bounds?
  "Check if all coordinates fit within grid dimensions."
  [coords width height]
  (every? (fn [[x y]]
            (and (<= 0 x (dec width))
                 (<= 0 y (dec height))))
          coords))

(defn translate
  "Translate a coordinate set by [dx dy]."
  [coords dx dy]
  (into #{} (map (fn [[x y]] [(+ x dx) (+ y dy)])) coords))

(defn valid-placements-for-orientation
  "Generate all valid positions for a single orientation on the grid."
  [orientation width height]
  (let [;; Find bounding box of the normalised orientation
        max-x (reduce max (map first orientation))
        max-y (reduce max (map second orientation))
        ;; Valid translation ranges
        x-range (range (- width max-x))
        y-range (range (- height max-y))]
    (for [dx x-range
          dy y-range
          :let [placed (translate orientation dx dy)]
          :when (in-bounds? placed width height)]
      placed)))

(defn valid-placements-for-tile
  "Generate all valid placements (position × orientation) for a tile."
  [tile-orientations width height]
  (into #{}  ; deduplicate in case translations create overlaps
        (mapcat #(valid-placements-for-orientation % width height))
        tile-orientations))

(defn generate-dlx-rows
  "Generate all DLX rows for the problem.
   
   Each row represents: 'place instance k of tile t at these cells'
   
   Returns: sequence of maps
     {:tile-instance [tile-id instance-idx]   ; primary column (must cover)
      :cells         #{[x y] ...}}            ; secondary columns (at most once)
   
   Input:
     - orientations: {tile-id -> #{orientation ...}}
     - problem: {:width :height :tiles {tile-id -> quantity}}"
  [orientations problem]
  (let [{:keys [width height tiles]} problem]
    (for [[tile-id quantity]  tiles
          instance-idx        (range quantity)
          :let [tile-orients (get orientations tile-id)]
          placement          (valid-placements-for-tile tile-orients width height)]
      {:tile-instance [tile-id instance-idx]
       :cells         placement})))

(defn total-cells-required
  "Calculate total cells needed to place all tiles."
  [tiles]
  (* 7 (reduce + (vals tiles))))

(defn feasible?
  "Quick check: do tiles even fit in grid area?"
  [problem]
  (<= (total-cells-required (:tiles problem))
      (* (:width problem) (:height problem))))

(defn all-tiles-placeable?
  "Check that every tile instance has at least one valid placement."
  [orientations problem]
  (let [{:keys [width height tiles]} problem]
    (every? (fn [[tile-id _quantity]]
              (seq (valid-placements-for-tile 
                     (get orientations tile-id) 
                     width height)))
            tiles)))

(defn build-dlx-problem
  "Build the complete DLX problem from raw inputs.
   
   Input:
     - tile-defs: ([0 (\"###\" \"##.\" ...)] [1 ...] ...)
     - problem-input: [(4 4) (0 0 0 0 2 0)]
   
   Output:
     {:problem     {:width 4 :height 4 :tiles {4 2}}
      :rows        [{:tile-instance [4 0] :cells #{...}} ...]
      :primary     #{[4 0] [4 1]}           ; tile instances (must cover)
      :secondary   #{[0 0] [0 1] ... [3 3]} ; grid cells (at most once)
      :feasible?   true/false}"
  [tile-defs problem-input]
  (let [orientations (generate-all-orientations tile-defs)
        problem      (parse-problem problem-input)
        {:keys [width height tiles]} problem]
    (if-not (feasible? problem)
      {:problem   problem
       :feasible? false
       :reason    :grid-too-small}
      
      (if-not (all-tiles-placeable? orientations problem)
        {:problem   problem
         :feasible? false
         :reason    :tile-cannot-fit}
        
        (let [rows      (generate-dlx-rows orientations problem)
              primary   (into #{} (for [[tid qty] tiles
                                        i (range qty)]
                                    [tid i]))
              secondary (into #{} (for [x (range width)
                                        y (range height)]
                                    [x y]))]
          {:problem   problem
           :rows      (vec rows)
           :primary   primary
           :secondary secondary
           :feasible? true})))))

;; =============================================================================
;; Debugging utilities
;; =============================================================================

(defn summarise-dlx-problem
  "Print a summary of the DLX problem for debugging."
  [dlx]
  (println "DLX Problem Summary")
  (println "-------------------")
  (println "Grid:" (:width (:problem dlx)) "×" (:height (:problem dlx)))
  (println "Tiles:" (:tiles (:problem dlx)))
  (println "Feasible:" (:feasible? dlx))
  (when (:feasible? dlx)
    (println "Primary columns (tile instances):" (count (:primary dlx)))
    (println "Secondary columns (grid cells):" (count (:secondary dlx)))
    (println "Total rows (placements):" (count (:rows dlx)))
    (println "Total cells needed:" (* 7 (reduce + (vals (:tiles (:problem dlx))))))
    (println "Total grid cells:" (* (:width (:problem dlx)) (:height (:problem dlx))))
    (println "\nRows per tile instance:")
    (doseq [[tile-instance cnt] (->> (:rows dlx)
                                     (group-by :tile-instance)
                                     (map (fn [[k v]] [k (count v)]))
                                     (sort))]
      (println " " tile-instance "->" cnt "placements"))))

;; =============================================================================
;; DLX Node Structure
;; =============================================================================
;;
;; We use a flat array-based representation for efficiency.
;; Each node has: left, right, up, down links + column header reference
;; Column headers additionally track: size (number of nodes) and column id
;;
;; Layout:
;;   - Node 0: root header
;;   - Nodes 1..num-cols: column headers
;;   - Nodes num-cols+1..: data nodes (one per 1-entry in the matrix)

(defrecord DLX [^longs left      ; left link
                ^longs right     ; right link  
                ^longs up        ; up link
                ^longs down      ; down link
                ^longs column    ; column header for each node
                ^longs size      ; size count (only meaningful for column headers)
                ^objects col-id  ; column identifier (only for headers)
                ^longs row-id    ; row index for data nodes
                num-cols         ; number of columns (primary + secondary)
                num-primary      ; number of primary columns
                primary-set      ; set of primary column ids (for quick lookup)
                rows])           ; original row data for solution reconstruction

;; =============================================================================
;; DLX Construction
;; =============================================================================

(defn- make-arrays [n]
  {:left   (long-array n)
   :right  (long-array n)
   :up     (long-array n)
   :down   (long-array n)
   :column (long-array n)
   :size   (long-array n)
   :col-id (object-array n)
   :row-id (long-array n)})

(defn build-dlx-matrix
  "Build the DLX data structure from the problem specification.
   
   Primary columns are linked into the header list (must be covered).
   Secondary columns exist but are not in the header list (optional coverage)."
  [{:keys [rows primary secondary]}]
  (let [;; Assign column indices: primary first, then secondary
        primary-vec   (vec (sort primary))
        secondary-vec (vec (sort secondary))
        all-cols      (into primary-vec secondary-vec)
        col->idx      (into {} (map-indexed (fn [i c] [c (inc i)]) all-cols))
        
        num-primary   (count primary-vec)
        num-secondary (count secondary-vec)
        num-cols      (+ num-primary num-secondary)
        
        ;; Calculate total nodes: root + column headers + data nodes
        ;; Each row covers 1 primary (tile-instance) + 7 secondary (cells)
        num-data-nodes (reduce + (map #(+ 1 (count (:cells %))) rows))
        total-nodes    (+ 1 num-cols num-data-nodes)
        
        ;; Allocate arrays
        {:keys [left right up down column size col-id row-id]} (make-arrays total-nodes)
        
        ;; Root node is index 0
        root 0]
    
    ;; Initialize column headers (indices 1 to num-cols)
    ;; Link primary columns into circular list with root
    ;; Secondary columns are self-linked (not in main list)
    (aset left root (long num-primary))
    (aset right root (long 1))
    
    (doseq [i (range 1 (inc num-cols))]
      (let [col-idx (long i)]
        ;; Vertical: self-linked initially
        (aset up col-idx col-idx)
        (aset down col-idx col-idx)
        (aset column col-idx col-idx)
        (aset size col-idx (long 0))
        (aset col-id col-idx (nth all-cols (dec i)))
        
        (if (<= i num-primary)
          ;; Primary: link horizontally into header list
          (do
            (aset left col-idx (long (if (= i 1) root (dec i))))
            (aset right col-idx (long (if (= i num-primary) root (inc i)))))
          ;; Secondary: self-linked horizontally (not in header list)
          (do
            (aset left col-idx col-idx)
            (aset right col-idx col-idx)))))
    
    ;; Add data nodes for each row
    (loop [rows-remaining rows
           row-idx        0
           next-node      (inc num-cols)]
      (if (empty? rows-remaining)
        ;; Return completed structure
        (map->DLX {:left        left
                   :right       right
                   :up          up
                   :down        down
                   :column      column
                   :size        size
                   :col-id      col-id
                   :row-id      row-id
                   :num-cols    num-cols
                   :num-primary num-primary
                   :primary-set (set primary-vec)
                   :rows        (vec rows)})
        
        (let [{:keys [tile-instance cells]} (first rows-remaining)
              ;; All columns this row covers: tile-instance + 7 cells
              row-cols (sort (map col->idx (cons tile-instance cells)))
              num-in-row (count row-cols)
              row-nodes (range next-node (+ next-node num-in-row))]
          
          ;; Create nodes for this row
          (doseq [[node-idx col-idx] (map vector row-nodes row-cols)]
            (let [node-idx   (long node-idx)
                  col-header (long col-idx)]
              ;; Set column reference and row id
              (aset column node-idx col-header)
              (aset row-id node-idx (long row-idx))
              
              ;; Link into column (insert above header = at bottom of column)
              (let [last-in-col (aget up col-header)]
                (aset down last-in-col node-idx)
                (aset up node-idx last-in-col)
                (aset down node-idx col-header)
                (aset up col-header node-idx))
              
              ;; Increment column size
              (aset size col-header (inc (aget size col-header)))))
          
          ;; Link row nodes horizontally (circular)
          (doseq [[i node-idx] (map-indexed vector row-nodes)]
            (let [node-idx   (long node-idx)
                  left-node  (long (nth row-nodes (mod (dec i) num-in-row)))
                  right-node (long (nth row-nodes (mod (inc i) num-in-row)))]
              (aset left node-idx left-node)
              (aset right node-idx right-node)))
          
          (recur (rest rows-remaining)
                 (inc row-idx)
                 (+ next-node num-in-row)))))))

;; =============================================================================
;; DLX Cover/Uncover Operations
;; =============================================================================

(defn- cover!
  "Cover a column: remove from header list and remove all rows containing it."
  [^DLX dlx ^long col]
  (let [left   (:left dlx)
        right  (:right dlx)
        up     (:up dlx)
        down   (:down dlx)
        column (:column dlx)
        size   (:size dlx)]
    
    ;; Remove column from header list
    (let [l (aget left col)
          r (aget right col)]
      (aset right l r)
      (aset left r l))
    
    ;; For each row in this column...
    (loop [row (aget down col)]
      (when-not (= row col)
        ;; For each other node in this row...
        (loop [node (aget right row)]
          (when-not (= node row)
            ;; Remove node from its column
            (let [u (aget up node)
                  d (aget down node)
                  c (aget column node)]
              (aset down u d)
              (aset up d u)
              (aset size c (dec (aget size c))))
            (recur (aget right node))))
        (recur (aget down row))))))

(defn- uncover!
  "Uncover a column: restore all rows and add back to header list."
  [^DLX dlx ^long col]
  (let [left   (:left dlx)
        right  (:right dlx)
        up     (:up dlx)
        down   (:down dlx)
        column (:column dlx)
        size   (:size dlx)]
    
    ;; For each row in this column (reverse order)...
    (loop [row (aget up col)]
      (when-not (= row col)
        ;; For each other node in this row (reverse order)...
        (loop [node (aget left row)]
          (when-not (= node row)
            ;; Restore node to its column
            (let [u (aget up node)
                  d (aget down node)
                  c (aget column node)]
              (aset down u node)
              (aset up d node)
              (aset size c (inc (aget size c))))
            (recur (aget left node))))
        (recur (aget up row))))
    
    ;; Restore column to header list
    (let [l (aget left col)
          r (aget right col)]
      (aset right l col)
      (aset left r col))))

;; =============================================================================
;; DLX Search
;; =============================================================================

(defn- choose-column
  "Choose the primary column with minimum size (MRV/S heuristic)."
  [^DLX dlx]
  (let [right (:right dlx)
        size (:size dlx)
        root 0]
    (loop [col (aget right root)
           best -1
           best-size Long/MAX_VALUE]
      (if (= col root)
        (if (not= best -1) best nil)
        (let [s (aget size col)]
          (if (< s best-size)
            (recur (aget right col) col s)
            (recur (aget right col) best best-size)))))))

(defn- search
  "Recursive DLX search. Returns first solution found or nil."
  [^DLX dlx ^java.util.ArrayList solution]
  (let [right  (:right dlx)
        down   (:down dlx)
        column (:column dlx)
        row-id (:row-id dlx)
        left-arr (:left dlx)
        size   (:size dlx)
        root   0]
    
    (if (= (aget right root) root)
        ;; All primary columns covered - found a solution!
        (vec (.toArray solution))
    
        ;; Choose column with fewest options
        (when-let [col (choose-column dlx)]
          (let [col (long col)]
            (if (zero? (aget size col))
              ;; Dead end - column has no rows
              nil
              (do
                (cover! dlx col)
                (loop [row (aget down col)]
                  (if (= row col)
                    ;; Tried all rows, no solution found
                    (do (uncover! dlx col) nil)
    
                    ;; Try this row
                    (do
                      (.add solution (aget row-id row))
    
                      ;; For other columns in this row:
                      ;; - If primary: cover them (won't happen as we only have 1 primary per row)
                      ;; - If secondary: just hide their column to prevent overlaps
                      (loop [node (aget right row)]
                        (when-not (= node row)
                          (let [col-to-cover (aget column node)]
                            ;; For secondary columns, we just hide this specific column
                            ;; (don't remove entire rows from matrix)
                            ;; For now, still call cover! but this is likely the bug
                            (cover! dlx col-to-cover))
                          (recur (aget right node))))
    
                      ;; Recurse
                      (if-let [result (search dlx solution)]
                        ;; Found solution!
                        result
    
                        ;; Backtrack: uncover columns and try next row
                        (do
                          (loop [node (aget left-arr row)]
                            (when-not (= node row)
                              (uncover! dlx (aget column node))
                              (recur (aget left-arr node))))
                          (.remove solution (dec (.size solution)))
                          (recur (aget down row))))))))))))))
    
    ;; =============================================================================
    ;; Public API
    ;; =============================================================================
    
(defn solve
      "Solve the DLX problem. Returns first solution found or nil.
       
       Solution is a vector of row indices that together cover all primary columns
       without overlapping on secondary columns."
      [^DLX dlx]
      (search dlx (java.util.ArrayList.)))
    
(defn solve-all
      "Find all solutions. Returns a vector of solutions."
      [^DLX dlx]
      (let [solutions (java.util.ArrayList.)
            right     (:right dlx)
            down      (:down dlx)
            left-arr  (:left dlx)
            column    (:column dlx)
            row-id    (:row-id dlx)
            size      (:size dlx)
            root      0]
    
        (letfn [(search-all [^java.util.ArrayList solution]
                  (if (= (aget right root) root)
                    (.add solutions (vec (.toArray solution)))
                    (when-let [col (choose-column dlx)]
                      (let [col (long col)]
                        (when (pos? (aget size col))
                          (cover! dlx col)
                          (loop [row (aget down col)]
                            (when-not (= row col)
                              (.add solution (aget row-id row))
                              (loop [node (aget right row)]
                                (when-not (= node row)
                                  (cover! dlx (aget column node))
                                  (recur (aget right node))))
                              (search-all solution)
                              (loop [node (aget left-arr row)]
                                (when-not (= node row)
                                  (uncover! dlx (aget column node))
                                  (recur (aget left-arr node))))
                              (.remove solution (dec (.size solution)))
                              (recur (aget down row))))
                          (uncover! dlx col))))))]
          (search-all (java.util.ArrayList.)))
    
        (vec (.toArray solutions))))
    
(defn solution->placements
      "Convert solution (row indices) to human-readable placements."
      [^DLX dlx solution]
      (mapv #(get (.rows dlx) %) solution))
    
    ;; =============================================================================
    ;; Main Entry Point
    ;; =============================================================================
    
(defn solve-heptomino-problem
      "Complete solver: from raw input to solution.
       
       Input:
         - tile-defs: ([0 (\"###\" \"##.\" ...)] ...)
         - problem-input: [(4 4) (0 0 0 0 2 0)]
       
       Output:
         {:solvable?   true/false
          :solution    [{:tile-instance [4 0] :cells #{...}} ...] or nil
          :row-count   count of valid placements explored
          :reason      :solved / :no-solution / :grid-too-small / :tile-cannot-fit}"
      [tile-defs problem-input]
      (let [dlx-problem (build-dlx-problem tile-defs problem-input)]
        (if-not (:feasible? dlx-problem)
          {:solvable? false
           :reason    (:reason dlx-problem)
           :solution  nil}
    
          (let [dlx-matrix (build-dlx-matrix dlx-problem)
                solution   (solve dlx-matrix)]
            (if solution
              {:solvable?  true
               :reason     :solved
               :solution   (solution->placements dlx-matrix solution)
               :row-count  (count (:rows dlx-problem))}
              {:solvable? false
               :reason    :no-solution
               :solution  nil
               :row-count (count (:rows dlx-problem))})))))
    
(defn print-solution
  "Pretty-print a solution as a grid.

   Takes either:
   - A result map from solve-heptomino-problem (extracts :solution automatically)
   - A vector of placements directly"
  [width height solution-or-result]
  (let [solution (if (map? solution-or-result)
                   (:solution solution-or-result)
                   solution-or-result)]
    (when solution
      (let [grid (vec (repeat height (vec (repeat width \.))))
            symbols "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            grid (reduce (fn [g [idx {:keys [tile-instance cells]}]]
                           (let [[tile-id _] tile-instance
                                 sym (nth symbols (mod tile-id (count symbols)))]
                             (reduce (fn [g [x y]]
                                       (assoc-in g [y x] sym))
                                     g cells)))
                         grid
                         (map-indexed vector solution))]
        (doseq [row grid]
          (println (apply str (interpose \space row))))))))

;; --------------------------------
(defn part1
      [f]
      (let [[shapes regions :as v] (read-data f)]
        (map (partial solve-heptomino-problem shapes) regions)))
    
(defn part2
      [f])
    
(comment
      (def testf "data/day12-test.txt")
      (def inputf "data/day12-input.txt")
    
      (part1 testf)
      (part1 inputf)
    
      (part2 testf)
      (time (part2 inputf)))
    
    ;; The End