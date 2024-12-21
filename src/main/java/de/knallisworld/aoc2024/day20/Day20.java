package de.knallisworld.aoc2024.day20;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.Utils;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

@Log4j2
public class Day20 {

	public static void main(String[] args) {
		printHeader(20);
		printSolution(1, () -> "Result: %s".formatted(partX(readInput(readInputLines(20, "part0")), 2, 100)));
		printSolution(2, () -> "Result: %s".formatted(partX(readInput(readInputLines(20, "part0")), 20, 50)));
		printSolution(1, () -> "Result: %s".formatted(partX(readInput(readInputLines(20, "part1")), 2, 100)));
		printSolution(1, () -> "Result: %s".formatted(partX(readInput(readInputLines(20, "part1")), 20, 100)));
	}

	static String partX(final Input input, final int maxCheatDuration, final int minSkipping) {

		final var racePath = dfs0(input.grid, input.start, input.goal).getFirst().path(); // "Because there is only a single path from the start to the end"

		final var result = testForShortcuts(
			racePath,
			input.grid,
			maxCheatDuration,
			minSkipping
		);
		return "Normal run = %d, cheat max length = %d, min skipping %d = %d".formatted(
			racePath.size() - 1,
			maxCheatDuration,
			minSkipping,
			result
		);
	}

	record ResultItem(List<Point2D<Integer>> path,
					  List<List<Point2D<Integer>>> usedCheats) implements Comparable<ResultItem> {
		@Override
		public int compareTo(ResultItem o) {
			return Integer.compare(path.size(), o.path.size());
		}
	}

	static long testForShortcuts(final List<Point2D<Integer>> racePath,
								 final FixGrid<Tile> grid,
								 final int cheatDuration,
								 final int minSkipping) {
		return testForShortcuts0(racePath, grid, cheatDuration, minSkipping);
	}

	static List<ResultItem> dfs0(final FixGrid<Tile> grid,
								 final Point2D<Integer> start,
								 final Point2D<Integer> goal) {

		final var q = new PriorityQueue<ResultItem>();
		q.add(new ResultItem(List.of(start), List.of()));

		final var results = new ArrayList<ResultItem>();

		while (!q.isEmpty()) {
			final var current = q.poll();

			if (current.path().getLast().equals(goal)) {
				results.add(current);
				continue;
			}

			current.path()
				   .getLast()
				   .getAdjacents4()
				   .filter(grid::hasValue)
				   .filter(not(current.path()::contains))
				   .forEach(next -> {
					   if (grid.getValueRequired(next) == Tile.EMPTY) {// standard case
						   final var path = new ArrayList<>(current.path());
						   path.add(next);
						   q.add(new ResultItem(path, current.usedCheats()));
					   }
				   });
		}

		return results;
	}

	static long testForShortcuts0(final List<Point2D<Integer>> racePath,
								  final FixGrid<Tile> grid,
								  final long cheatDuration,
								  final int minSkipping) {

		record Path(Point2D<Integer> start, Point2D<Integer> end) {
		}

		record Cheat(Path path, long skipped) {
		}


		final var temp = IntStream
			.range(0, racePath.size())
			.boxed()
			.flatMap(i -> {
				final var p0 = racePath.get(i);
				return IntStream.range(i + 2, racePath.size())
								.boxed()
								.<Cheat>mapMulti((j, downstream) -> {
									final var p1 = racePath.get(j);
									final var dist = Utils.manhattenDistance(p0, p1);
									if (dist <= cheatDuration) {
										downstream.accept(new Cheat(new Path(p0, p1), j - i - dist));
									}
								});
			})
			.filter(c -> c.skipped() > 0)
			.collect(groupingBy(Cheat::path, toList()))
			.entrySet()
			.stream()
			.collect(groupingBy(a -> a.getValue().getFirst().skipped(), counting()));
		/*
		temp.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				System.out.printf("There are %d cheats that save %d picoseconds%n", entry.getValue(), entry.getKey());
			});
		 */
		return temp
			.entrySet()
			.stream()
			.filter(e -> e.getKey() >= minSkipping)
			.mapToLong(Map.Entry::getValue)
			.sum();
	}

	enum Tile {
		WALL,
		EMPTY
	}

	record Input(FixGrid<Tile> grid, Point2D<Integer> start, Point2D<Integer> goal) {
	}

	static Input readInput(final List<String> lines) {
		final var grid = new FixGrid<>(Tile.class, lines.size(), lines.getFirst().length());
		final var start = new AtomicReference<Point2D<Integer>>();
		final var goal = new AtomicReference<Point2D<Integer>>();
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  final var ch = line.charAt(x);
								  final var tile = switch (ch) {
									  case '#' -> Tile.WALL;
									  case '.' -> Tile.EMPTY;
									  case 'S' -> {
										  start.set(Point2D.create(x, y));
										  yield Tile.EMPTY;
									  }
									  case 'E' -> {
										  goal.set(Point2D.create(x, y));
										  yield Tile.EMPTY;
									  }
									  default -> throw new IllegalStateException("Unexpected value: " + ch);
								  };
								  grid.setValue(x, y, tile);
							  });
				 });
		return new Input(grid, requireNonNull(start.get()), requireNonNull(goal.get()));
	}

}

