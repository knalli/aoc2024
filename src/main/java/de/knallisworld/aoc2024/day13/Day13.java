package de.knallisworld.aoc2024.day13;

import de.knallisworld.aoc2024.support.geo.Point2D;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day13 {

	public static void main(String[] args) {
		printHeader(13);
		printSolution(1, () -> "x: %d".formatted(part1(readCoordinates(readInputLines(13, "part1")), new Costs(3, 1))));
		printSolution(2, () -> "x: %d".formatted(part2(readCoordinates(readInputLines(13, "part1")), new Costs(3, 1))));

	}

	record Coordinates(Point2D<Long> buttonA, Point2D<Long> buttonB, Point2D<Long> price) {
	}

	record Costs(long a, long b) {
	}

	record Result(long a, long b) {
	}

	static List<Coordinates> bumpForPart2(final List<Coordinates> coordinates) {
		final var add = Point2D.create(10_000_000_000_000L, 10_000_000_000_000L);
		return coordinates
			.stream()
			.map(c -> new Coordinates(
				c.buttonA,
				c.buttonB,
				Point2D.create(c.price.getX() + add.getX(), c.price.getY() + add.getY())
			))
			.toList();
	}

	static List<Coordinates> readCoordinates(final List<String> lines) {
		final var pattern = Pattern.compile("(?:Button A: X\\+(\\d+), Y\\+(\\d+)\nButton B: X\\+(\\d+), Y\\+(\\d+)\nPrize: X=(\\d+), Y=(\\d+))\n*", Pattern.DOTALL | Pattern.MULTILINE);
		return pattern.matcher(String.join("\n", lines))
					  .results()
					  .map(matchResult -> {
						  return new Coordinates(
							  Point2D.create(
								  Long.parseLong(matchResult.group(1)),
								  Long.parseLong(matchResult.group(2))
							  ),
							  Point2D.create(
								  Long.parseLong(matchResult.group(3)),
								  Long.parseLong(matchResult.group(4))
							  ),
							  Point2D.create(
								  Long.parseLong(matchResult.group(5)),
								  Long.parseLong(matchResult.group(6))
							  )
						  );
					  })
					  .toList();
	}

	static long part1(final List<Coordinates> coordinates, final Costs costs) {
		return coordinates
			.stream()
			.flatMap(c -> {
				return solve(c).stream();
			})
			.mapToLong(r -> r.a * costs.a + r.b * costs.b)
			.sum();
	}

	static long part2(final List<Coordinates> coordinates, final Costs costs) {
		return part1(bumpForPart2(coordinates), costs);
	}

	// solve with linear equation
	static Optional<Result> solve(final Coordinates coordinates) {
		// Define the coefficients of the equations
		double[][] coefficients = {
			{coordinates.buttonA.getX(), coordinates.buttonB.getX()},
			{coordinates.buttonA.getY(), coordinates.buttonB.getY()}
		};
		// Define the constants on the right-hand side of the equations
		double[] constants = {coordinates.price.getX(), coordinates.price.getY()};

		// Create a RealMatrix from the coefficients
		var coefficientMatrix = new Array2DRowRealMatrix(coefficients);

		// Create a RealVector from the constants
		var constantVector = new ArrayRealVector(constants);

		// Solve the system of equations
		var solver = new LUDecomposition(coefficientMatrix).getSolver();
		var solution = solver.solve(constantVector);

		// Extract and print the results
		var a = Math.round(solution.getEntry(0));
		var b = Math.round(solution.getEntry(1));

		final var probe = Point2D.create(
			coordinates.buttonA.getX() * a + coordinates.buttonB.getX() * b,
			coordinates.buttonA.getY() * a + coordinates.buttonB.getY() * b
		);

		// check if result is matching
		if (probe.equals(coordinates.price)) {
			return Optional.of(new Result(a, b));
		}

		return Optional.empty();
	}

}

