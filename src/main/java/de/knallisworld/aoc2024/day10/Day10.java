package de.knallisworld.aoc2024.day10;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Gatherer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

@SuppressWarnings("preview")
@Log4j2
public class Day10 {

	public static void main(String[] args) {
		printHeader(10);
		printSolution(1, () -> "Scores of all trailheads = %d".formatted(part1(readGrid(readInputLines(10, "part1")))));
		printSolution(2, () -> "Rating of all trailheads = %d".formatted(part2(readGrid(readInputLines(10, "part1")))));
	}

	static long part1(final FixGrid<Integer> grid) {
		return findTrailHeads(grid)
			//.flatMap(start -> findHikes_Stream(grid, start, 9))
			.gather(findHikes_Gatherer(grid, 9))
			// scoring
			.collect(groupingBy(Trail::start, mapping(Trail::end, toSet())))
			.values()
			.stream()
			.mapToLong(Set::size)
			.sum();
	}

	static long part2(final FixGrid<Integer> grid) {
		return findTrailHeads(grid)
			//.flatMap(start -> findHikes_Stream(grid, start, 9))
			.gather(findHikes_Gatherer(grid, 9))
			// rating
			.collect(groupingBy(Trail::start, mapping(Trail::end, counting())))
			.values()
			.stream()
			.mapToLong(Long::longValue)
			.sum();
	}

	static Stream<Point2D<Integer>> findTrailHeads(final FixGrid<Integer> grid) {
		return grid.fields()
				   .stream()
				   .filter(f -> f.value() == 0)
				   .map(FixGrid.FieldsView.Field::pos);
	}

	record Trail(List<Point2D<Integer>> path) {

		public Point2D<Integer> start() {
			return path.getFirst();
		}

		public Point2D<Integer> end() {
			return path.getLast();
		}

	}

	static Gatherer<Point2D<Integer>, FixGrid<Integer>, Trail> findHikes_Gatherer(final FixGrid<Integer> grid,
																				  final int goalHeight) {

		record Item(List<Point2D<Integer>> path, int height) implements Comparable<Item> {
			@Override
			public int compareTo(final Item o) {
				return this.length() - o.length();
			}

			public Point2D<Integer> pos() {
				return path.getLast();
			}

			public int length() {
				return path.size();
			}
		}

		return Gatherer.ofSequential(
			() -> grid,
			(g, start, downstream) -> {
				final var q = new PriorityQueue<Item>();
				{
					final var path = new ArrayList<Point2D<Integer>>();
					path.add(start);
					q.add(new Item(path, grid.getValueRequired(start)));
				}

				while (!q.isEmpty()) {
					final var current = q.poll();

					if (current.height == goalHeight) {
						downstream.push(new Trail(current.path));
						continue;
					}

					current.pos()
						   .getAdjacents4()
						   .filter(grid::hasValue)
						   .filter(not(current.path::contains))
						   .forEach(adjacent -> {
							   final var nextHeight = grid.getValueRequired(adjacent);
							   if (current.height + 1 == nextHeight) {
								   final var nextPath = new ArrayList<>(current.path);
								   nextPath.add(adjacent);
								   q.add(new Item(nextPath, nextHeight));
							   }
						   });
				}
				return true;
			}
		);
	}


	static Stream<Trail> findHikes_Stream(final FixGrid<Integer> grid,
										  final Point2D<Integer> start,
										  final int goalHeight) {

		record Item(List<Point2D<Integer>> path, int height) implements Comparable<Item> {
			@Override
			public int compareTo(final Item o) {
				return this.length() - o.length();
			}

			public Point2D<Integer> pos() {
				return path.getLast();
			}

			public int length() {
				return path.size();
			}
		}

		final var result = new HashSet<Trail>();

		final var q = new PriorityQueue<Item>();
		{
			final var path = new ArrayList<Point2D<Integer>>();
			path.add(start);
			q.add(new Item(path, grid.getValueRequired(start)));
		}

		while (!q.isEmpty()) {
			final var current = q.poll();

			if (current.height == goalHeight) {
				result.add(new Trail(current.path));
				continue;
			}

			current.pos()
				   .getAdjacents4()
				   .filter(grid::hasValue)
				   .filter(not(current.path::contains))
				   .forEach(adjacent -> {
					   final var nextHeight = grid.getValueRequired(adjacent);
					   if (current.height + 1 == nextHeight) {
						   final var nextPath = new ArrayList<>(current.path);
						   nextPath.add(adjacent);
						   q.add(new Item(nextPath, nextHeight));
					   }
				   });
		}

		return result.stream();
	}

	static FixGrid<Integer> readGrid(final List<String> lines) {
		final var grid = FixGrid.create(Integer.class, lines.size(), lines.getFirst().length());
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  grid.setValue(x, y, line.charAt(x) - 48);
							  });
				 });
		return grid;
	}

}

