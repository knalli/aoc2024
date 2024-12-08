package de.knallisworld.aoc2024.day08;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.stream.Collectors.*;

@Log4j2
public class Day08 {

	static final Frequency ANTINODE_FREQUENCY = new Frequency('#');

	public static void main(String[] args) {
		printHeader(8);
		printSolution(1, () -> "Unique locations of antinodes = %d".formatted(part1(readGrid(readInputLines(8, "part1")))));
		printSolution(2, () -> "Unique locations of antinodes = %d".formatted(part2(readGrid(readInputLines(8, "part1")))));
	}

	static long part1(final FixGrid<Location> grid) {
		return countUniqueAntinodeLocations(grid, false);
	}

	static long part2(final FixGrid<Location> grid) {
		return countUniqueAntinodeLocations(grid, true);
	}

	static long countUniqueAntinodeLocations(final FixGrid<Location> grid, final boolean resonanceEffect) {
		record IntermediateLocation(Point2D<Integer> position, Frequency frequency) {
		}

		grid
			.fields()
			.stream()
			.flatMap(field -> field
				.value()
				.frequencies()
				.stream()
				.map(frequency -> new IntermediateLocation(field.pos(), frequency))
			)
			.collect(groupingBy(
				IntermediateLocation::frequency,
				mapping(IntermediateLocation::position, toList())
			))
			.forEach((_, points) -> {
				final var antinodes = new HashSet<Point2D<Integer>>();
				// "that appear on every antenna"
				if (resonanceEffect) {
					antinodes.addAll(points);
				}
				for (var pointA : points) {
					for (var pointB : points) {
						if (pointA.equals(pointB)) {
							continue;
						}
						final var vec = Point2D.create(
							pointB.getX() - pointA.getX(),
							pointB.getY() - pointA.getY()
						);
						var before = pointA.add(vec.negative());
						antinodes.add(before);
						var after = pointB.add(vec);
						antinodes.add(after);
						// just loop until out-of-scope
						if (resonanceEffect) {
							while (grid.hasValue(before)) {
								before = before.add(vec.negative());
								antinodes.add(before);
							}
							while (grid.hasValue(after)) {
								after = after.add(vec);
								antinodes.add(after);
							}
						}
					}
				}
				antinodes.forEach(antinode -> {
					grid.getValue(antinode)
						// ignore out-of-bound
						.ifPresent(location -> {
							location.frequencies().add(ANTINODE_FREQUENCY);
						});
				});
			});

		//printGrid(grid);

		return grid.fields()
				   .stream()
				   .filter(f -> f.value().frequencies().contains(ANTINODE_FREQUENCY))
				   .count();
	}

	private static void printGrid(FixGrid<Location> grid) {
		System.out.println(grid.toString((p, f) -> {
			return " <%4s>".formatted(
				f.frequencies()
				 .stream()
				 .map(d -> String.valueOf(d.ch()))
				 .collect(joining(""))
			);
		}));
	}

	static FixGrid<Location> readGrid(final List<String> lines) {
		final var grid = FixGrid.create(Location.class, lines.size(), lines.getFirst().length());
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  final var frequencies = new ArrayList<Frequency>();
								  final var ch = line.charAt(x);
								  if (ch != '.') {
									  frequencies.add(new Frequency(ch));
								  }
								  grid.setValue(Point2D.create(x, y), new Location(frequencies));
							  });
				 });
		return grid;
	}

	record Frequency(char ch) {
	}

	record Location(List<Frequency> frequencies) {
	}

}

