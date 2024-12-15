package de.knallisworld.aoc2024.day15;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.Direction;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day15 {

	public static void main(String[] args) {
		printHeader(15);
		printSolution(1, () -> "Sum of all boxes' GPS coordinates: %d".formatted(part1(readInput(readInputLines(15, "part1")))));
		printSolution(2, () -> "Sum of all boxes' GPS coordinates: %d".formatted(part2(readInput(readInputLines(15, "part1")))));
	}

	enum Tile {
		ROBOT,
		WALL,
		EMPTY,
		BOX
	}

	record GridWrapper(FixGrid<Tile> grid, Map<Point2D<Integer>, Object> boxIds) {
	}

	record Input(FixGrid<Tile> grid, List<Direction> moves) {
	}

	static long part1(final Input input) {
		return partX(input.grid, input.moves, Map.of());
	}

	static long part2(final Input input) {
		final var expanded = expandGrid(input.grid);
		return partX(expanded.grid, input.moves, expanded.boxIds);
	}

	static long partX(final FixGrid<Tile> grid, final List<Direction> moves, final Map<Point2D<Integer>, Object> boxIds) {
		//System.out.println(renderGrid(grid, null));

		// get current robot position and replace position with empty
		// (we dont need to update the position on grid)
		var current = grid.fields()
						  .stream()
						  .filter(f -> f.value() == Tile.ROBOT)
						  .map(FixGrid.FieldsView.Field::pos)
						  .findFirst()
						  .orElseThrow();
		grid.setValue(current, Tile.EMPTY);

		for (var move : moves) {
			//System.out.println("Move " + move);

			final var next = current.add(move.offset());
			final var nextValue = grid.getValueRequired(next);
			if (nextValue == Tile.WALL) {
				// not possible
				continue;
			} else if (nextValue == Tile.BOX) {
				// try to push
				final List<Point2D<Integer>> nexts;
				if (!boxIds.containsKey(next)) {
					// part1
					nexts = List.of(next);
				} else {
					// part2
					final var boxId = boxIds.get(next);
					nexts = boxIds.entrySet()
								  .stream()
								  .filter(e -> e.getValue() == boxId)
								  .map(Map.Entry::getKey)
								  .toList();
				}
				if (!pushBoxes(grid, nexts, move, boxIds)) {
					// not possible
					continue;
				}
			}
			current = next;
			//System.out.println(renderGrid(grid, current));
		}

		//System.out.println(renderGrid(grid, current));

		return grid.fields()
				   .stream()
				   .filter(f -> f.value() == Tile.BOX)
				   .map(FixGrid.FieldsView.Field::pos)
				   .collect(Collectors.groupingBy(p -> boxIds.getOrDefault(p, "NONE")))
				   .entrySet()
				   .stream()
				   .<Point2D<Integer>>mapMulti((p, downstream) -> {
					   if ("NONE".equals(p.getKey())) {
						   // part 1
						   p.getValue().forEach(downstream);
					   } else {
						   // part 2
						   final Comparator<Point2D<Integer>> cmpX = Comparator
							   .comparing(Point2D::getX);
						   final Comparator<Point2D<Integer>> cmpY = Comparator
							   .comparing(Point2D::getY);
						   p.getValue().stream()
							.min(cmpX.thenComparing(cmpY))
							.ifPresent(downstream);
					   }
				   })
				   // part2
				   .distinct()
				   .mapToLong(p -> 100L * p.getY() + p.getX())
				   .sum();
	}

	static String renderGrid(final FixGrid<Tile> grid, @Nullable final Point2D<Integer> robotPosition) {
		return grid.toString((p, c) -> {
			if (p.equals(robotPosition)) {
				return "@";
			}
			return switch (c) {
				case Tile.ROBOT -> "@";
				case Tile.WALL -> "#";
				case Tile.EMPTY -> ".";
				case Tile.BOX -> "O";
			};
		});
	}

	static boolean pushBoxes(final FixGrid<Tile> grid,
							 final List<Point2D<Integer>> positions,
							 final Direction move,
							 final Map<Point2D<Integer>, Object> boxIds) {
		final var pushable = pushBox0(grid, positions, move, boxIds, new HashSet<>());
		if (pushable.isEmpty()) {
			return false;
		}

		final var oldBoxIdPositions = boxIds.isEmpty()
			// part 1
			? Map.of()
			// part 2
			: pushable.stream().collect(toMap(identity(), boxIds::remove));
		pushable.forEach(position -> grid.setValue(position, Tile.EMPTY));
		pushable.forEach(position -> {
			final var next = position.add(move.offset());
			grid.setValue(next, Tile.BOX);
			// part2
			if (!oldBoxIdPositions.isEmpty()) {
				boxIds.put(next, oldBoxIdPositions.get(position));
			}
		});

		return true;
	}

	static Set<Point2D<Integer>> pushBox0(final FixGrid<Tile> grid,
										  final List<Point2D<Integer>> positions,
										  final Direction move,
										  final Map<Point2D<Integer>, Object> boxIds,
										  final Set<Point2D<Integer>> visited) {

		/*
		System.out.println(positions);
		if (!boxIds.isEmpty()) {
			positions.forEach(p -> System.out.println("%s = %s".formatted(p, requireNonNull(boxIds.get(p)))));
		}
		 */

		record Field(Point2D<Integer> position, Point2D<Integer> next, Tile nextValue) {
		}

		final var nexts = positions
			.stream()
			.map(pos -> {
				final var next = pos.add(move.offset());
				return new Field(
					pos,
					next,
					grid.getValueRequired(next)
				);
			})
			.filter(not(f -> positions.contains(f.next)))
			.filter(not(f -> visited.contains(f.next)))
			.toList();
		if (nexts.isEmpty()) {
			final var result = new HashSet<>(visited);
			result.addAll(positions);
			return result;
		}

		if (nexts.stream().allMatch(f -> f.nextValue() == Tile.EMPTY)) {
			final var result = new HashSet<>(visited);
			result.addAll(positions);
			return result;
		}

		if (nexts.stream().anyMatch(f -> f.nextValue() == Tile.WALL)) {
			return Set.of();
		}

		if (nexts.stream().anyMatch(f -> f.nextValue() == Tile.BOX)) {
			final var nextPositions = nexts
				.stream()
				.filter(f -> f.nextValue() == Tile.BOX)
				.flatMap(f -> {
					if (boxIds.containsKey(f.next)) {
						// part2
						final var boxId = boxIds.get(f.next);
						return boxIds.entrySet()
									 .stream()
									 // by object-id
									 .filter(e -> e.getValue() == boxId)
									 .map(Map.Entry::getKey)
									 // avoid recursion of already visited/checked points
									 .filter(not(positions::contains))
									 .filter(not(visited::contains));
					} else {
						// part1
						return Stream.of(f.next);
					}
				})
				.distinct()
				.toList();

			if (nextPositions.isEmpty()) {
				final var result = new HashSet<>(visited);
				result.addAll(positions);
				return result;
			}

			final var visited2 = new HashSet<>(visited);
			visited2.addAll(positions);
			return pushBox0(grid, nextPositions, move, boxIds, visited2);
		}

		return Set.of();
	}

	static Input readInput(final List<String> lines) {
		return new Input(
			readGrid(
				lines.stream()
					 .takeWhile(not(String::isBlank))
					 .toList()
			),
			lines.stream()
				 .dropWhile(not(String::isBlank))
				 .skip(1)
				 .flatMap(line -> line.chars().boxed())
				 .map(ch -> switch (ch) {
					 case (int) '>' -> Direction.East;
					 case (int) '<' -> Direction.West;
					 case (int) '^' -> Direction.North;
					 case (int) 'v' -> Direction.South;
					 default -> throw new IllegalStateException("Unexpected value: " + ch);
				 })
				 .toList()
		);
	}

	static FixGrid<Tile> readGrid(final List<String> lines) {
		final var grid = new FixGrid<>(Tile.class, lines.size(), lines.getFirst().length());
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  final var ch = line.charAt(x);
								  final var tile = switch (ch) {
									  case '#' -> Tile.WALL;
									  case '.' -> Tile.EMPTY;
									  case '@' -> Tile.ROBOT;
									  case 'O' -> Tile.BOX;
									  default -> throw new IllegalStateException("Unexpected value: " + ch);
								  };
								  grid.setValue(x, y, tile);
							  });
				 });
		return grid;
	}

	// part 2
	static GridWrapper expandGrid(final FixGrid<Tile> grid) {
		final var result = FixGrid.create(Tile.class, grid.getHeight(), grid.getWidth() * 2);
		final var boxIds = new HashMap<Point2D<Integer>, Object>();
		grid.fields().forEach(field -> {
			final var x = field.pos().getX() * 2;
			final var y = field.pos().getY();
			switch (field.value()) {
				case EMPTY -> {
					result.setValue(x, y, Tile.EMPTY);
					result.setValue(x + 1, y, Tile.EMPTY);
				}
				case WALL -> {
					result.setValue(x, y, Tile.WALL);
					result.setValue(x + 1, y, Tile.WALL);
				}
				case BOX -> {
					result.setValue(x, y, Tile.BOX);
					result.setValue(x + 1, y, Tile.BOX);
					final var id = new Object(); // simple obj, no logic
					boxIds.put(Point2D.create(x, y), id);
					boxIds.put(Point2D.create(x + 1, y), id);
				}
				case ROBOT -> {
					result.setValue(x, y, Tile.ROBOT);
					result.setValue(x + 1, y, Tile.EMPTY);
				}
			}
		});
		return new GridWrapper(result, boxIds);
	}

}

