package de.knallisworld.aoc2024.day06;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.Direction;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import de.knallisworld.aoc2024.support.puzzle.InputReader;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;

@Log4j2
public class Day06 {

	public static void main(String[] args) {
		printHeader(6);
		printSolution(1, () -> "Visited: %d".formatted(countDistinctVisited(readInput(InputReader.readInputLines(6, "part1")))));
		printSolution(2, () -> "Block candidates: %d".formatted(countBlockingSituations(readInput(InputReader.readInputLines(6, "part1")))));
	}

	static int countDistinctVisited(final State state) {
		processPatrol(state);
		return state.visited.size();
	}

	static long countBlockingSituations(final State state) {
		final var temp = state.copy();
		processPatrol(temp);

		return temp.visited
				.keySet()
				.stream()
				.filter(candidate -> {
					final var copy = state.copy();
					copy.grid.setValue(candidate, Tile.BOX);
					try {
						processPatrol(copy);
					} catch (final Exception e) {
						return true;
					}
					return false;
				})
				.count();
	}

	static void processPatrol(final State state) {
		while (state.grid.hasValue(state.guard.position)) {
			// part2 option
			if (state.visited.get(state.guard.position) == state.guard.direction) {
				throw new IllegalStateException("Loop detected!");
			}
			state.visited.put(state.guard.position, state.guard.direction);
			var nextPosition = state.guard.position.add(state.guard.direction.offset());
			while (state.grid.getValue(nextPosition).orElse(Tile.EMPTY) != Tile.EMPTY) {
				state.guard = state.guard.rotateRight();
				nextPosition = state.guard.position.add(state.guard.direction.offset());
			}
			state.guard = new Guard(nextPosition, state.guard.direction);
		}
	}

	static State readInput(final List<String> lines) {
		final var grid = new FixGrid<>(Tile.class, lines.size(), lines.getFirst().length());
		final var state = new State(grid);
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  final var value = switch (line.charAt(x)) {
									  case '.', '^', 'v', '<', '>' -> Tile.EMPTY;
									  case '#' -> Tile.BOX;
									  default -> throw new IllegalStateException("Unexpected value: " + line.charAt(x));
								  };
								  grid.setValue(x, y, value);
								  // guard
								  final var guard = switch (line.charAt(x)) {
									  case '<' -> new Guard(Point2D.create(x, y), Direction.West);
									  case '>' -> new Guard(Point2D.create(x, y), Direction.East);
									  case '^' -> new Guard(Point2D.create(x, y), Direction.North);
									  case 'v' -> new Guard(Point2D.create(x, y), Direction.South);
									  default -> null;
								  };
								  if (guard != null) {
									  state.setGuard(guard);
								  }
							  });
				 });
		return state;
	}

	enum Tile {
		EMPTY,
		BOX
	}

	record Guard(Point2D<Integer> position,
				 Direction direction) {

		public Guard rotateRight() {
			return new Guard(
					position,
					direction.right()
			);
		}

	}

	@Data
	static class State {

		private final FixGrid<Tile> grid;
		private Guard guard;
		private final Map<Point2D<Integer>, Direction> visited = new HashMap<>();

		public State copy() {
			final var result = new State(
					FixGrid.copy(grid)
			);
			result.setGuard(new Guard(guard.position, guard.direction));
			result.visited.putAll(visited);
			return result;
		}

	}

}

