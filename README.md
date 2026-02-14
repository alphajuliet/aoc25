# Advent of Code 2025

My advent-ures in code in [AOC 2025](https://adventofcode.com/2025), predominantly in my favourite language Clojure, but occasionally in something different if I'm feeling advent-urous.

Below is some commentary on the challenges. Look at the source(s) for more detail.

## Day 1

Welcome to AoC, and here's a nice little exercise to warm up. Parse the commands, do some additions modulo 100 as a `scan` operation, and count up the zeroes.

For part 2, we need to keep track of how many time we've gone past zero. My solution looks sounds but it's not working. On day 1?? Need to sort this out.

And it's sorted with a minor change to call zero-crossings for each move.

## Day 2

Part 1 is ridiculously easy: just compare the two halves of a string once you get the all the numbers in the given range. For part 2, work out the divisors and loop over the bits and see if they're the same. Nice opportunity to use `(apply = coll)` to do a bunch of equality checking in one go.

## Day 3

The first part is simple enough, find the biggest digit before the last, and then the biggest next digit. Turn that into a number. Voilà. Part 2 is a bit tedious because we need to search through a bunch of combinations. Thanks to Claude for enumerating this once I'd explained properly what I wanted.

## Day 4

Part 1 is a straightforward matrix neighbour count thing, like we've done many times before in AoC. I converted the symbols into numbers to make it easier to calculate but it could all be done as characters. And on to part 2, where we need to iterate on removing rolls until we can't remove any more. Bring on my lucky `reduce/reduced` function.

## Day 5

Again, part 1 can be solved with an obvious solution, which is to count each number that sits within the lower and upper bounds of one of the ranges. Sorting the ranges first reduces the effort slightly. Part 2 is the usual problem of scale where we are dealing with very large ranges and we want to avoid enumerating them, and instead just keep a running list of the lower and upper bounds of all the ranges and adjusting for full or partial overlaps.

## Day 6

This was an amusing little exercise in parsing formatted text data. Part 1 was trivial, but part 2 required a lot of splitting and converting vertical digits into numbers. Clojure has a powerful set of functions and predicates for manipulating data; one of the reasons I like it. Not the most elegant code I've written but it got us there. 

## Day 7

This was the first puzzle that got me really thinking, having been down several intuitive but quite wrong rabbit holes. In part 1 I ended up using sets to track the beams as they traversed all the splitters, and this worked well. In part 2, after some sketching and thinking I thought: ah, multisets are the answer, but of course ran out of memory on the input data. Then I finally realised we just need to track multiplicities in a map. I'm sometimes a bit slow but we got to the right answer.

## Day 8

Finally, we get to exercise the graph network library, and this puzzle is made for it. We sort the distances between all the pairs and use that to load in all the nodes and then a bunch of edges. In day one, just the first 1000, and see how many circuits we can form using the `alg/connected-components` call in `Ubergraph`. For part 2, we keep adding edges until the graph is `strongly-connected`. Nice.

## Day 9

As usual, part 1 was straightforward -- generate all the rectangles, find their area, and find the biggest. Not so simple for part 2. My algorithm was sound but it was not completing in any reasonable time. I got Claude to help me optimise the code but it was still taking too long. More analysis with Claude helped to identify a sampling approach rather than brute force, and this got the compute time down to nearly 7 seconds. I'll take some credit for the algorithm but props to Claude for the analysis that got a solution in acceptable time.

## Day 10

Some discussion with Claude up front about this puzzle, before touching any code, was useful in understanding how this related to geometry and groups under $\mathbb{Z}/2\mathbb{Z}$. We then discussed approaches and decided that simple BFS was the most efficient in this situation. I coded up the parser and structured the code, and Claude wrote the BFS function, which I can't be arsed doing. Teamwork. 

Part 2 is in some ways a different problem and it has me stuck because the search space is huge for many of the machines in the input data. Claude and I have tried some different approaches but we're still blowing the compute on the input data. After reluctantly checking out Reddit, I can see that there is a clever recursive bifurcation approach that is computationally feasible, but I'm not going to code it up.

## Day 11

So, the first part is another excuse to roll out `Ubergraph` and count all the paths. Fortunately, we've seen this before so I have my `all-paths` function ready to go, and it did. Part 2, however, has us starting at a different point in the graph and is, so far, resistant to computation with my algorithms in any reasonable time. I need to think of a smarter way to do this.

So, Claude has identified that using dynamic programming using a reverse search is more appropriate here than DFS, and coded up a better approach that solved it within tens of milliseconds. Nice work, Claude.

## Day 12

This is a partial covering problem, and there the classic approach is with Knuth's DLX algorithm. Claude coded up most of part 1 for me but it's not yet giving me reliable answers. I'm wondering whether to pursue this one, I'm not excited by search algorithms.
