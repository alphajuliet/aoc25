# Advent of Code 2025

My advent-ures in code in AOC 2025, predominantly in my favourite language Clojure, but occasionally in something different if I'm feeling advent-urous.

Below is some commentary on the challenges. Look at the source(s) for more detail.

## Day 1

Welcome to AoC, and here's a nice little exercise to warm up. Parse the commands, do some additions modulo 100 as a `scan` operation, and count up the zeroes.

For part 2, we need to keep track of how many time we've gone past zero. My solution looks sounds but it's not working. On day 1?? Need to sort this out.

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