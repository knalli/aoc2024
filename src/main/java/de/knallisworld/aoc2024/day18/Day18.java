package de.knallisworld.aoc2024.day18;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.lang.Integer.min;
import static java.lang.Integer.parseInt;
import static java.util.function.Predicate.not;

@Log4j2
public class Day18 {

	public static void main(String[] args) {
		printHeader(18);
		printSolution(1, () -> "Result = %d".formatted(part1(readInput(readInputLines(18, "part1")), 71, 1024)));
		printSolution(1, () -> {
			final var p = part2_bs(readInput(readInputLines(18, "part1")), 71, 1024);
			return "Result = %d,%d".formatted(p.getX(), p.getY());
		});
		printSolution(2, () -> "x");
	}

	static long part1(final List<Point2D<Integer>> input, final int size, final int round) {

		final var start = Point2D.create(0, 0);
		final var goal = Point2D.create(size - 1, size - 1);

		record Item(Point2D<Integer> pos, long distance) implements Comparable<Item> {

			@Override
			public int compareTo(Item o) {
				//return Integer.compare(this.path.size(), o.path.size());
				return Long.compare(this.distance, o.distance);
			}

		}

		enum Tile {
			CORRUPTED,
			SAFE
		}
		final var grid = FixGrid.create(Tile.class, size, size);
		grid.fill(Tile.SAFE);
		input.stream()
			 .limit(round)
			 .forEach(p -> grid.setValue(p, Tile.CORRUPTED));

		final var q = new PriorityQueue<Item>();
		q.add(new Item(start, 0));

		var minD = Long.MAX_VALUE;
		var visited = new HashMap<Point2D<Integer>, Long>();

		while (!q.isEmpty()) {
			final var current = q.poll();
			if (current.pos.equals(goal)) {
				minD = Math.min(minD, current.distance);
				if (minD > current.distance()) {
					System.out.println("New min D = " + current.distance());
					minD = current.distance();
					continue;
				}
			}
			if (visited.getOrDefault(current.pos, Long.MAX_VALUE) < current.distance()) {
				continue;
			}
			current.pos
				.getAdjacents4()
				.filter(not(visited::containsKey))
				.filter(a -> visited.getOrDefault(a, Long.MAX_VALUE) > current.distance + 1)
				.filter(a -> grid.getValue(a).map(Tile.SAFE::equals).orElse(false))
				.forEach(a -> {
					q.add(new Item(a, current.distance() + 1));
					visited.computeIfAbsent(a, _ -> Long.MAX_VALUE);
				});
		}

		return minD;
	}

	static Point2D<Integer> part2_bs(final List<Point2D<Integer>> input, final int size, final int minRound) {
		final var maxRound = input.size();

		var left = minRound;
		var right = maxRound;
		var result = Long.MAX_VALUE;

		while (left <= right) {
			var mid = left + (right - left) / 2;
			var attempt = part1(input, size, mid);

			if (attempt < Long.MAX_VALUE) {
				// valid
				result = mid;
				left = mid + 1;
			} else {
				// invalid
				right = mid - 1;
			}
		}

		// at least the min will be returned
		return input.get((int) result);
	}

	static long part1_Dynamic(final List<Point2D<Integer>> input, final int size) {
		final var start = Point2D.create(0, 0);
		final var goal = Point2D.create(size - 1, size - 1);
		final var grid = FixGrid.create(Boolean.class, size, size);
		grid.fill(false);

		record Item(List<Point2D<Integer>> path, int round) implements Comparable<Item> {

			@Override
			public int compareTo(Item o) {
				return Integer.compare(this.path.size(), o.path.size());
			}
		}
		final var q = new PriorityQueue<Item>();
		q.add(new Item(List.of(start), 0));

		var minD = Integer.MAX_VALUE;

		while (!q.isEmpty()) {
			final var current = q.poll();
			if (current.path.getLast().equals(goal)) {
				if (minD > current.round) {
					minD = current.round;
					continue;
				}
			}
			current.path.getLast().getAdjacents4()
						.filter(not(current.path::contains))
						.filter(grid::hasValue)
						.filter(not(a -> input.subList(0, min(current.round + 1, input.size())).contains(a)))
						.forEach(a -> {
							final var pp = new ArrayList<>(current.path);
							pp.add(a);
							q.add(new Item(pp, current.round + 1));
						});
		}

		return minD;
	}

	static List<Point2D<Integer>> readInput(final List<String> lines) {
		return lines.stream()
					.map(str -> Point2D.create(
						parseInt(str.split(",")[0]),
						parseInt(str.split(",")[1])
					))
					.toList();
	}

}

