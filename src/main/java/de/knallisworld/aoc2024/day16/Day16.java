package de.knallisworld.aoc2024.day16;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.Direction;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.Objects.requireNonNull;

@Log4j2
public class Day16 {

	public static void main(String[] args) {
		printHeader(16);
		printSolution(1, () -> "Score: %d".formatted(part1(readGrid(readInputLines(16, "part1")))));
		printSolution(2, () -> "Best spot tiles: %d".formatted(part2(readGrid(readInputLines(16, "part1")))));
	}

	static long part1(final Input input) {
		return bfs(input.grid, input.start, input.goal).minScore();
	}

	static long part2(final Input input) {
		return bfs(input.grid, input.start, input.goal)
			.paths()
			.stream()
			.flatMap(Collection::stream)
			.distinct()
			.count();
	}

	record BfsResult(long minScore, List<List<Point2D<Integer>>> paths) {
	}

	static BfsResult bfs(final FixGrid<Tile> grid, Point2D<Integer> start, Point2D<Integer> goal) {

		record Item(
			List<Point2D<Integer>> path,
			Direction direction,
			long costs,
			boolean lastRotated
		) implements Comparable<Item> {

			@Override
			public int compareTo(Item o) {
				return Long.compare(this.costs, o.costs);
			}
		}

		record Next(
			Point2D<Integer> position,
			Direction direction,
			Point2D<Integer> test,
			long costs,
			boolean rotating
		) {
		}

		final var q = new PriorityQueue<Item>();
		q.add(new Item(List.of(start), Direction.East, 0, false));

		final var visited = new HashMap<Point2D<Integer>, Long>();

		final var min = new AtomicLong(Long.MAX_VALUE);
		final var minPaths = new ArrayList<List<Point2D<Integer>>>();

		while (!q.isEmpty()) {

			final var current = q.poll();
			final var currentPosition = current.path.getLast();

			//renderGrid(grid, goal, position);

			if (currentPosition.equals(goal)) {
				//System.out.println("Found goal with costs " + current.costs);
				if (current.costs < min.get()) {
					//System.out.println("New minimum");
					min.set(current.costs);
				}
				minPaths.add(current.path);
				continue;
			}

			// abort paths with costs already exceeding the known minimum
			if (min.get() <= current.costs) {
				continue;
			}

			// abort paths with a known sub-path of fewer costs
			if (visited.getOrDefault(currentPosition, Long.MAX_VALUE) < current.costs - 1000) {
				continue;
			}

			// optimization
			if (current.costs < visited.getOrDefault(currentPosition, Long.MAX_VALUE)) {
				visited.put(currentPosition, current.costs);
			}

			Stream.of(
					  new Next(
						  currentPosition,
						  current.direction.left(),
						  currentPosition.add(current.direction.left().offset()),
						  1000,
						  true
					  ),
					  new Next(
						  currentPosition,
						  current.direction.right(),
						  currentPosition.add(current.direction.right().offset()),
						  1000,
						  true
					  ),
					  new Next(
						  currentPosition.add(current.direction.offset()),
						  current.direction,
						  currentPosition.add(current.direction.offset()),
						  1,
						  false
					  )
				  )
				  // ignore any sub path which exceed the already known min costs
				  .filter(next -> next.costs < min.get())
				  // ignore double-rotating
				  .filter(next -> !next.rotating || !current.lastRotated)
				  // ensure visitable field
				  .filter(next -> grid
					  .getValue(next.test)
					  .map(Tile.EMPTY::equals)
					  .orElse(false)
				  )
				  .forEach(next -> {
					  final var l = new ArrayList<>(current.path);
					  if (!l.contains(next.position)) {
						  l.add(next.position);
					  }
					  q.add(new Item(l, next.direction, current.costs + next.costs, next.rotating));
				  });
		}

		//renderGrid(grid, goal, null);

		return new BfsResult(
			min.get(),
			minPaths
		);
	}

	private static void renderGrid(final FixGrid<Tile> grid,
								   @Nullable final Point2D<Integer> goal,
								   @Nullable final Point2D<Integer> current) {
		System.out.println(grid.toString((p, v) -> {
			if (p.equals(current)) {
				return "S";
			} else if (p.equals(goal)) {
				return "E";
			} else {
				return switch (v) {
					case WALL -> "#";
					case EMPTY -> ".";
				};
			}
		}));
	}

	enum Tile {
		WALL, EMPTY
	}

	record Input(FixGrid<Tile> grid, Point2D<Integer> start, Point2D<Integer> goal) {
	}

	static Input readGrid(final List<String> lines) {
		final var grid = new FixGrid<>(Tile.class, lines.size(), lines.getFirst().length());
		final var start = new AtomicReference<Point2D<Integer>>();
		final var goal = new AtomicReference<Point2D<Integer>>();
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  final var ch = line.charAt(x);
								  switch (ch) {
									  case '#' -> grid.setValue(x, y, Tile.WALL);
									  case '.' -> grid.setValue(x, y, Tile.EMPTY);
									  case 'S' -> {
										  start.set(Point2D.create(x, y));
										  grid.setValue(x, y, Tile.EMPTY);
									  }
									  case 'E' -> {
										  goal.set(Point2D.create(x, y));
										  grid.setValue(x, y, Tile.EMPTY);
									  }
									  default -> throw new IllegalStateException("Unexpected value: " + ch);
								  }
								  ;
							  });
				 });
		return new Input(grid, requireNonNull(start.get()), requireNonNull(goal.get()));
	}

}

