package de.knallisworld.aoc2024.day03;

import lombok.extern.log4j.Log4j2;

import java.util.regex.Pattern;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.lang.String.join;

@Log4j2
public class Day03 {

	public static void main(String[] args) {
		printHeader(3);
		printSolution(1, () -> "Results of multiplications = %d".formatted(part1(join("", readInputLines(3, "part1")))));
		printSolution(2, () -> "Results of multiplications = %d".formatted(part2(join("", readInputLines(3, "part1")))));
	}

	static long part1(String input) {
		final var pattern = Pattern.compile("mul\\((\\d{1,3}),(\\d{1,3})\\)");
		final var matcher = pattern.matcher(input);

		int result = 0;
		while (matcher.find()) {
			final var n1 = Integer.parseInt(matcher.group(1));
			final var n2 = Integer.parseInt(matcher.group(2));
			result += (n1 * n2);
		}

		return result;
	}

	static long part2(String input) {
		final var pattern = Pattern.compile("(do|don't|mul)\\((?:(\\d{1,3}),(\\d{1,3}))?\\)");
		final var matcher = pattern.matcher(input);

		int result = 0;
		boolean enabled = true;
		while (matcher.find()) {
			switch (matcher.group(1)) {
				case "do" -> enabled = true;
				case "don't" -> enabled = false;
				case "mul" -> {
					if (enabled) {
						final var n1 = Integer.parseInt(matcher.group(2));
						final var n2 = Integer.parseInt(matcher.group(3));
						result += (n1 * n2);
					}
				}
				default -> throw new IllegalStateException("Unexpected value: " + matcher.group(1));
			}
		}

		return result;
	}

}

