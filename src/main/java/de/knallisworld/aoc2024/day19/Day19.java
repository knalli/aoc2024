package de.knallisworld.aoc2024.day19;

import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;

@Log4j2
public class Day19 {

	public static void main(String[] args) {
		printHeader(19);
		printSolution(1, () -> "Result = %d".formatted(part1(parseInput(readInputLines(19, "part1")))));
		printSolution(2, () -> "Result = %d".formatted(part2(parseInput(readInputLines(19, "part1")))));
	}

	static long part1(final Input input) {
		final var available = input.availableList()
								   .stream()
								   .sorted(comparing(s -> -s.length()))
								   .toList();

		return input.desiredList()
					.stream()
					.filter(desired -> findMatches(available, desired))
					.count();
	}

	static long part2(final Input input) {
		final var available = input.availableList()
								   .stream()
								   .sorted(comparing(s -> s.length()))
								   .toList();

		return input.desiredList()
					.stream()
					.filter(desired -> findMatches(available, desired))
					.mapToLong(desired -> countMatches(available, desired))
					.sum();
	}

	static boolean findMatches(final List<String> available, final String desired) {
		return findMatches0(available, desired);
	}

	static boolean findMatches0(final List<String> available, final String desired) {
		if (desired.isEmpty()) {
			return true;
		}
		for (final var a : available) {
			if (desired.startsWith(a)) {
				if (findMatches0(available, desired.substring(a.length()))) {
					return true;
				}
			}
		}
		return false;
	}

	static long countMatches(final List<String> available,
							 final String desired) {

		// init
		var map = new HashMap<String, Long>();
		map.put("", 1L);

		while (!map.isEmpty()) {
			final var matched = map.entrySet()
								   .stream()
								   .min(comparing(e -> e.getKey().length()))
								   .map(Map.Entry::getKey)
								   .orElseThrow();
			if (matched.equals(desired)) {
				return map.get(matched);
			}
			available.stream()
					 .filter(a -> desired.substring(matched.length()).startsWith(a))
					 .map(a -> matched + a)
					 .forEach(next -> map.merge(next, map.get(matched), Long::sum));
			map.remove(matched);
		}

		return 0;
	}

	record Input(
		List<String> availableList,
		List<String> desiredList
	) {
	}

	static Input parseInput(final List<String> lines) {
		return new Input(
			Arrays.stream(lines.getFirst().split(", "))
				  .map(String::strip)
				  .toList(),
			lines.stream()
				 .skip(2)
				 .map(String::strip)
				 .filter(not(String::isEmpty))
				 .toList()
		);
	}

}

