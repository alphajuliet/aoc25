# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Advent of Code 2025 solutions in Clojure. Each day has a source file in `src/aoc25/dayNN.clj` with paired data files `data/dayNN-test.txt` and `data/dayNN-input.txt`.

## Running Code

Solutions are run via REPL (Calva/nREPL). Each day file ends with a `(comment ...)` block for interactive evaluation:

```clojure
(comment
  (def testf "data/dayNN-test.txt")
  (def inputf "data/dayNN-input.txt")
  (part1 testf)
  (part2 inputf))
```

**Run tests:** `clj -M:test` (cognitect test-runner, tests live in `test/aoc25/`)

**Dev REPL with extra heap:** `clj -M:dev`

## Day Solution Structure

Every day file follows this pattern:
1. Namespace with `aoc25.util` and other required deps
2. `read-data` function that takes a filename, slurps and parses it
3. Helper functions specific to the day's problem
4. `part1` and `part2` functions that each take a filename
5. `(comment ...)` block for REPL-driven testing

## Shared Utilities (`aoc25.util`)

Key functions used across solutions: `diff` (pairwise differences), `count-if`, `read-lines`, `T` (transpose), `manhattan`, `mat-find-all`, `safe-mget`, `argmax`/`argmin`, `rotate`, `str->num`, `take-until`/`take-upto`.

## Key Libraries

- **ubergraph** - Graph algorithms (connected components, path finding, strongly-connected checks)
- **core.matrix + vectorz-clj** - Grid/matrix problems (neighbor access, element operations)
- **instaparse** - Parser generator for complex input formats
- **math.combinatorics** - Exhaustive search patterns
- **data.priority-map** - Priority queues for pathfinding
- **core.logic / core.match** - Logic programming and pattern matching

## Data Files

Data files are gitignored. Test data (`dayNN-test.txt`) is the example from the problem statement; input data (`dayNN-input.txt`) is the personal puzzle input.
