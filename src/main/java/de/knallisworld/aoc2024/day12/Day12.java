package de.knallisworld.aoc2024.day12;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.Direction;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

@Log4j2
public class Day12 {

	public static void main(String[] args) {
		printHeader(12);
		printSolution(1, () -> "Total price of fencing: %d".formatted(part1(readGrid(readInputLines(12, "part1")))));
		printSolution(2, () -> "Total price of fencing: %d".formatted(part2(readGrid(readInputLines(12, "part1")))));
	}

	record Region(Set<Point2D<Integer>> values) {
	}

	static long part1(final FixGrid<Character> grid) {
		return resolveRegions(grid)
			.mapToLong(region -> region.values.size() * calcPerimeter(grid, region))
			.sum();
	}

	static long part2(final FixGrid<Character> grid) {
		return resolveRegions(grid)
			.mapToLong(region -> region.values.size() * calkDiscount(grid, region))
			.sum();
	}

	static long calkDiscount(final FixGrid<Character> grid, final Region region) {
		final var xW = new HashSet<Point2D<Integer>>();
		final var xE = new HashSet<Point2D<Integer>>();
		final var yN = new HashSet<Point2D<Integer>>();
		final var yS = new HashSet<Point2D<Integer>>();
		region.values.forEach(p -> {
			final var value = grid.getValueRequired(p);
			Arrays.stream(Direction.values()).forEach(d -> {
				final var a = p.add(d.offset());
				final var fence = grid.getValue(a)
									  // inside: no fence if same value
									  .map(av -> av != value)
									  // outside: so there will be a fence
									  .orElse(true);
				if (fence) {
					switch (d) {
						case North -> yN.add(p);
						case South -> yS.add(p);
						case East -> xE.add(p);
						case West -> xW.add(p);
					}
				}
			});
		});

		return Stream.of(xE, xW)
					 .mapToLong(s -> s
						 .stream()
						 .collect(groupingBy(Point2D::getX, mapping(Point2D::getY, toList())))
						 .values()
						 .stream()
						 .mapToLong(l -> extractGroups(l.stream().sorted().toList()).size())
						 .sum()
					 )
					 .sum()
			+
			Stream.of(yN, yS)
				  .mapToLong(s -> s
					  .stream()
					  .collect(groupingBy(Point2D::getY, mapping(Point2D::getX, toList())))
					  .values()
					  .stream()
					  .mapToLong(l -> extractGroups(l.stream().sorted().toList()).size())
					  .sum()
				  )
				  .sum();
	}

	static List<List<Integer>> extractGroups(final List<Integer> numbers) {
		return IntStream.range(0, numbers.size())
						.boxed()
						.collect(groupingBy(
							i -> numbers.get(i) - i, // Group by the difference between the number and its index
							LinkedHashMap::new,      // Preserve order
							mapping(numbers::get, toList())
						))
						.values()
						.stream()
						.toList();
	}

	static long calcPerimeter(final FixGrid<Character> grid, final Region region) {
		return region
			.values
			.stream()
			.mapToLong(p -> {
				final var value = grid.getValueRequired(p);
				return p
					.getAdjacents4()
					.filter(a -> grid
						.getValue(a)
						// inside: no fence if same value
						.map(av -> av != value)
						// outside: so there will be a fence
						.orElse(true)
					)
					.count();
			})
			.sum();
	}

	static Stream<Region> resolveRegions(final FixGrid<Character> grid) {
		final var visited = new HashSet<Point2D<Integer>>();
		final var regions = new HashMap<Character, Set<Region>>();

		final Function<Point2D<Integer>, Region> store = current -> {
			final var value = grid.getValueRequired(current);
			final var parts = regions.computeIfAbsent(value, _ -> new HashSet<>());
			visited.add(current);
			return parts
				.stream()
				.filter(r -> current.getAdjacents4().anyMatch(r.values::contains))
				.findFirst()
				.map(region -> {
					region.values.add(current);
					return region;
				})
				.orElseGet(() -> {
					final var region = new Region(new HashSet<>(Set.of(current)));
					regions.get(value).add(region);
					return region;
				});
		};

		while (visited.size() < grid.size()) {
			final var current = grid.fields()
									.stream()
									.map(FixGrid.FieldsView.Field::pos)
									.filter(not(visited::contains))
									.findFirst()
									.orElseThrow();
			//System.out.println("Current " + current);
			final var value = grid.getValueRequired(current);
			final var region = store.apply(current);

			// add more
			final var q = new ArrayDeque<Point2D<Integer>>();
			q.add(current);
			while (!q.isEmpty()) {
				q.poll()
				 .getAdjacents4()
				 .filter(not(visited::contains))
				 .filter(p -> grid.hasValue(p) && grid.getValueRequired(p) == value)
				 //.peek(p -> System.out.println("Grab " + p + " = " + grid.getValueRequired(p)))
				 .peek(region.values::add)
				 .peek(visited::add)
				 .forEach(q::add);
			}
		}
		return regions.values()
					  .stream()
					  .flatMap(Collection::stream);
	}

	static FixGrid<Character> readGrid(final List<String> lines) {
		final var grid = FixGrid.create(Character.class, lines.size(), lines.getFirst().length());
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  grid.setValue(x, y, line.charAt(x));
							  });
				 });
		return grid;
	}

}

