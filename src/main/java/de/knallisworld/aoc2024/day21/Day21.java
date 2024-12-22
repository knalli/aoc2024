package de.knallisworld.aoc2024.day21;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.Direction;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.lang.Math.abs;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;

@Log4j2
public class Day21 {

	public static void main(String[] args) {
		printHeader(21);
		printSolution(1, () -> "Result = %d".formatted(partX(readInputLines(21, "part1"), 2)));
		printSolution(1, () -> "Result = %d".formatted(partX(readInputLines(21, "part1"), 25)));
	}

	enum Tile {
		ONE,
		TWO,
		THREE,
		FOUR,
		FIVE,
		SIX,
		SEVEN,
		EIGHT,
		NINE,
		ZERO,
		ACTIVATE,
		NORTH,
		EAST,
		SOUTH,
		WEST,
		EMPTY;

		public static Tile ofDirection(Direction direction) {
			return switch (direction) {
				case North -> NORTH;
				case East -> EAST;
				case South -> SOUTH;
				case West -> WEST;
			};
		}

		public static String renderList(List<Tile> tiles) {
			return tiles.stream()
						.map(t -> switch (t) {
							case NORTH -> "^";
							case EAST -> ">";
							case SOUTH -> "v";
							case WEST -> "<";
							case ACTIVATE -> "A";
							default -> throw new IllegalStateException("Unexpected value: " + t);
						})
						.collect(joining(""));
		}
	}

	@Data
	static class Device {

		private final FixGrid<Tile> grid;

		public Point2D<Integer> positionOf(Tile tile) {
			return grid.fields()
					   .stream()
					   .filter(f -> f.value() == tile)
					   .findFirst()
					   .map(FixGrid.FieldsView.Field::pos)
					   .orElseThrow();
		}

	}

	static long partX(final List<String> lines, final int robots) {
		final var costCache = new HashMap<CacheKey, Long>();
		return lines
			.stream()
			.mapToLong(line -> {
				final var devices = Stream.of(
											  Stream.of(createNumberPd()),
											  IntStream.range(0, robots)
													   .mapToObj(_ -> createRobot())
										  )
										  .flatMap(identity())
										  .toList();
				final var number = Long.parseLong(line.substring(0, line.length() - 1));
				final var cost = encodeKeys(decodeString(line), devices, costCache);
				final var l = cost * number;
				//System.out.printf("%d * %d\n", cost, number);
				return l;
			})
			.sum();
	}

	record CacheKey(Tile from, Tile to, int depth) {
	}

	private static long encodeKeys(final List<Tile> keys, final List<Device> devices, final Map<CacheKey, Long> costCache) {
		if (devices.isEmpty()) {
			return keys.size();
		}
		var currentKey = Tile.ACTIVATE;
		var length = 0L;
		for (final var nextKey : keys) {
			length += encodeKey(currentKey, nextKey, devices, costCache);
			currentKey = nextKey;
		}
		return length;
	}

	private static long encodeKey(final Tile currentKey, final Tile nextKey, final List<Device> devices, final Map<CacheKey, Long> costCache) {
		final var key = new CacheKey(currentKey, nextKey, devices.size());
		// costCache.computeIfAbsent not possible due CME
		return Optional
			.ofNullable(costCache.get(key))
			.orElseGet(() -> {
				final var device = devices.getFirst();
				final var from = device.positionOf(currentKey);
				final var to = device.positionOf(nextKey);

				final var dx = to.getX() - from.getX();
				final var dy = to.getY() - from.getY();

				var cost = Long.MAX_VALUE;
				final var actionsX = (dx < 0 ? "<" : ">").repeat(abs(dx));
				final var actionsY = (dy < 0 ? "^" : "v").repeat(abs(dy));
				if (device.grid.getValueRequired(from.getX(), to.getY()) != Tile.EMPTY) {
					final var actions = decodeString(
						actionsY + actionsX + "A"
					);
					cost = Math.min(cost, encodeKeys(actions, devices.subList(1, devices.size()), costCache));
				}
				if (device.grid.getValueRequired(to.getX(), from.getY()) != Tile.EMPTY) {
					final var actions = decodeString(
						actionsX + actionsY + "A"
					);
					cost = Math.min(cost, encodeKeys(actions, devices.subList(1, devices.size()), costCache));
				}
				costCache.put(key, cost);
				return cost;
			});
	}

	private static Device createNumberPd() {
		final var grid = FixGrid.create(Tile.class, 4, 3);
		grid.setValue(0, 0, Tile.SEVEN);
		grid.setValue(1, 0, Tile.EIGHT);
		grid.setValue(2, 0, Tile.NINE);
		grid.setValue(0, 1, Tile.FOUR);
		grid.setValue(1, 1, Tile.FIVE);
		grid.setValue(2, 1, Tile.SIX);
		grid.setValue(0, 2, Tile.ONE);
		grid.setValue(1, 2, Tile.TWO);
		grid.setValue(2, 2, Tile.THREE);
		grid.setValue(0, 3, Tile.EMPTY);
		grid.setValue(1, 3, Tile.ZERO);
		grid.setValue(2, 3, Tile.ACTIVATE);

		return new Device(grid);
	}

	static Device createRobot() {
		final var grid = FixGrid.create(Tile.class, 2, 3);
		grid.setValue(0, 0, Tile.EMPTY);
		grid.setValue(1, 0, Tile.NORTH);
		grid.setValue(2, 0, Tile.ACTIVATE);
		grid.setValue(0, 1, Tile.WEST);
		grid.setValue(1, 1, Tile.SOUTH);
		grid.setValue(2, 1, Tile.EAST);

		return new Device(grid);
	}

	static List<Tile> decodeString(final String input) {
		return input.chars()
					.mapToObj(ch -> switch (ch) {
						case (int) '>' -> Tile.EAST;
						case (int) '<' -> Tile.WEST;
						case (int) '^' -> Tile.NORTH;
						case (int) 'v' -> Tile.SOUTH;
						case (int) '0' -> Tile.ZERO;
						case (int) '1' -> Tile.ONE;
						case (int) '2' -> Tile.TWO;
						case (int) '3' -> Tile.THREE;
						case (int) '4' -> Tile.FOUR;
						case (int) '5' -> Tile.FIVE;
						case (int) '6' -> Tile.SIX;
						case (int) '7' -> Tile.SEVEN;
						case (int) '8' -> Tile.EIGHT;
						case (int) '9' -> Tile.NINE;
						case (int) 'A' -> Tile.ACTIVATE;
						default -> throw new IllegalStateException("Unexpected value: " + ch);
					})
					.toList();
	}

}

