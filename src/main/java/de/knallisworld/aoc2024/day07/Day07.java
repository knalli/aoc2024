package de.knallisworld.aoc2024.day07;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day07 {

	public static void main(String[] args) {
		printHeader(7);
		final var lines = readLines(readInputLines(7, "part1"));
		printSolution(1, () -> "Total calibration result = %d".formatted(getTotalCalibrationResult(
				lines,
				List.of(Operator.ADD, Operator.MUL)
		)));
		printSolution(2, () -> "Total calibration result = %d".formatted(getTotalCalibrationResult(
				lines,
				List.of(Operator.ADD, Operator.MUL, Operator.CONCAT)
		)));
	}

	static long getTotalCalibrationResult(final List<Line> lines,
										  final List<Operator> operators) {
		record State(long result, List<Long> numbers) {
		}
		return lines.stream()
					.filter(line -> {
						final var q = new ArrayDeque<State>();
						q.push(new State(
								line.numbers.getFirst(),
								line.numbers.stream()
											.skip(1)
											.toList()
						));
						while (!q.isEmpty()) {
							final var s = q.poll();
							if (s.numbers.isEmpty()) {
								if (s.result == line.test) {
									return true;
								}
								continue;
							}
							for (final var operator : operators) {
								q.push(new State(
										operator.fn.apply(
												s.result,
												s.numbers.getFirst()
										),
										s.numbers.stream()
												 .skip(1)
												 .toList()
								));
							}
						}
						return false;
					})
					.mapToLong(Line::test)
					.sum();
	}

	static List<Line> readLines(final List<String> input) {
		return input.stream()
					.map(Line::parse)
					.toList();
	}

	enum Operator {
		ADD(Long::sum),
		MUL((a, b) -> a * b),
		CONCAT((a, b) -> Long.valueOf(a.toString() + b.toString()));

		private final BiFunction<Long, Long, Long> fn;

		Operator(BiFunction<Long, Long, Long> fn) {
			this.fn = fn;
		}

		public BiFunction<Long, Long, Long> fn() {
			return fn;
		}
	}

	record Line(long test, List<Long> numbers) {

		public static Line parse(final String str) {
			final var split = str.split(":");
			return new Line(
					Long.parseLong(split[0].strip()),
					Arrays.stream(split[1].strip().split(" "))
						  .map(Long::parseLong)
						  .toList()
			);
		}

	}

}

