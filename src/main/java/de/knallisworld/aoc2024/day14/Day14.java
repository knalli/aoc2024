package de.knallisworld.aoc2024.day14;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.groupingBy;

@Log4j2
public class Day14 {

	public static void main(String[] args) {
		printHeader(14);
		printSolution(1, () -> {
			final var maxDimension = Point2D.create(101, 103);
			final var seconds = 100;
			return "Safety factor = %d".formatted(part1(readRobots(readInputLines(14, "part1")), maxDimension, seconds));
		});
		printSolution(1, () -> {
			final var maxDimension = Point2D.create(101, 103);
			return "Easter egg = %d".formatted(part2b(readRobots(readInputLines(14, "part1")), maxDimension));
		});
	}

	record Robot(Point2D<Integer> start, Point2D<Integer> velocity) {
	}

	enum Quadrant {
		TL, TR, BL, BR, UNDEFINED;
	}

	record QuadrantResult(Quadrant quadrant, long count) {
	}

	static List<Robot> readRobots(final List<String> lines) {
		final var pattern = Pattern.compile("p=(-?\\d+),(-?\\d+) v=(-?\\d+),(-?\\d+)");
		return lines
			.stream()
			.map(pattern::matcher)
			.filter(Matcher::find)
			.map(match -> new Robot(
				Point2D.create(parseInt(match.group(1)), parseInt(match.group(2))),
				Point2D.create(parseInt(match.group(3)), parseInt(match.group(4)))
			))
			.toList();
	}

	@SuppressWarnings("SameParameterValue")
	static long part1(final List<Robot> robots,
					  final Point2D<Integer> maxDimension,
					  final int iterations) {
		return findQuadrants(robots, maxDimension, iterations)
			// safety factor means multiplying each sum/count
			.mapToLong(QuadrantResult::count)
			.reduce(1L, (a, b) -> a * b);
	}

	static long part2b(final List<Robot> robots,
					   final Point2D<Integer> maxDimension) {
		return IntStream.iterate(1, i -> i + 1)
						//.parallel()
						.filter(seconds -> {
							/*
							if (seconds % 100000 == 0) {
								System.out.println(seconds);
							}
							 */
							return findQuadrants(robots, maxDimension, seconds)
								// "very rarely, most of the robots should arrange"
								// more luck at the end, but the idea was to find the first
								// occurrence where 3 quadrants have less than usual (20%),
								// so the remaining one contain more (40%).
								// which means the remaining one must contain a cluster (the tree)
								.filter(q -> q.count < 0.2 * robots.size())
								.count() == 3;
						})
						.peek(second -> {
							// visualize first spot
							System.out.println(renderRobotsAt(robots, maxDimension, second));
						})
						.findFirst()
						.orElse(-1);
	}

	static String renderRobotsAt(final List<Robot> robots,
								 final Point2D<Integer> maxDimension,
								 final int iteration) {
		final var grid = FixGrid.create(Character.class, maxDimension.getY(), maxDimension.getX());
		grid.fill(' ');
		robots.forEach(robot -> {
			var x = (robot.start.getX() + (robot.velocity.getX() * iteration)) % maxDimension.getX();
			var y = (robot.start.getY() + (robot.velocity.getY() * iteration)) % maxDimension.getY();
			grid.setValue(
				(maxDimension.getX() + x) % maxDimension.getX(),
				(maxDimension.getY() + y) % maxDimension.getY(),
				'#'
			);
		});
		return grid.toString((_, v) -> v.toString());
	}

	// extracted from part1, useful for part2 as utility
	static Stream<QuadrantResult> findQuadrants(final List<Robot> robots,
												final Point2D<Integer> maxDimension,
												final int iterations) {
		assert maxDimension.getX() % 2 == 1;
		assert maxDimension.getY() % 2 == 1;
		final var borderX = maxDimension.getX() / 2;
		final var borderY = maxDimension.getY() / 2;

		return robots
			.stream()
			.map(robot -> {
				var x = (robot.start.getX() + (robot.velocity.getX() * iterations)) % maxDimension.getX();
				var y = (robot.start.getY() + (robot.velocity.getY() * iterations)) % maxDimension.getY();
				return Point2D.create(
					(maxDimension.getX() + x) % maxDimension.getX(),
					(maxDimension.getY() + y) % maxDimension.getY()
				);
			})
			.collect(groupingBy(p -> {
				// 4 quadrant + UNDEFINED
				if (p.getX() < borderX) {
					if (p.getY() < borderY) {
						return Quadrant.TL;
					} else if (p.getY() > borderY) {
						return Quadrant.BL;
					} else {
						return Quadrant.UNDEFINED;
					}
				} else if (p.getX() > borderX) {
					if (p.getY() < borderY) {
						return Quadrant.TR;
					} else if (p.getY() > borderY) {
						return Quadrant.BR;
					} else {
						return Quadrant.UNDEFINED;
					}
				} else {
					return Quadrant.UNDEFINED;
				}
			}))
			.entrySet()
			.stream()
			.filter(e -> e.getKey() != Quadrant.UNDEFINED)
			.map(e -> new QuadrantResult(e.getKey(), e.getValue().size()));
	}

}

