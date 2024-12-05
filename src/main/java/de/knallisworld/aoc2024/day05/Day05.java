package de.knallisworld.aoc2024.day05;

import lombok.extern.log4j.Log4j2;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;

@Log4j2
public class Day05 {

	public static void main(String[] args) {
		printHeader(5);
		printSolution(1, () -> "Correct ordered: %d".formatted(solvePart1(readInput(readInputLines(5, "part1")))));
		printSolution(2, () -> "Correct ordered: %d".formatted(solvePart2(readInput(readInputLines(5, "part1")))));
	}

	static Input readInput(final List<String> lines) {
		final var rules = lines
				.stream()
				.takeWhile(not(String::isEmpty))
				.map(s -> {
					final var split = s.split("\\|");
					return new Pair<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
				})
				.toList();
		final var updates = lines.stream()
								 .skip(rules.size() + 1)
								 .map(s -> (List<Integer>) Arrays
										 .stream(s.split(","))
										 .map(Integer::parseInt)
										 .collect(toCollection(ArrayList::new))
								 )
								 .toList();
		return new Input(rules, updates);
	}

	static long solvePart1(final Input input) {
		return input.updates()
					.stream()
					.filter(buildOrderingCheckFilter(input.orderingRules()))
					.mapToLong(update -> update.get(update.size() / 2))
					.sum();
	}

	static Predicate<List<Integer>> buildOrderingCheckFilter(final List<Pair<Integer, Integer>> orderingRules) {
		return update -> {
			for (var rule : orderingRules) {
				final var fPos = update.indexOf(rule.getFirst());
				final var sPos = update.indexOf(rule.getSecond());
				// check if rule is applicable
				if (fPos < 0 || sPos < 0) {
					continue;
				}
				// check if rule is violated
				if (!(fPos < sPos)) {
					return false;
				}
			}
			return true;
		};
	}

	static long solvePart2(final Input input) {
		final var orderingRules = input.orderingRules();
		return input.updates()
					.stream()
					.filter(not(buildOrderingCheckFilter(orderingRules)))
					.peek(update -> {
						// ensure re-checking rules again until none is violated anymore
						while (true) {
							boolean any = false;
							for (var rule : orderingRules) {
								final var fPos = update.indexOf(rule.getFirst());
								final var sPos = update.indexOf(rule.getSecond());
								if (fPos < 0 || sPos < 0) {
									continue;
								}
								if (fPos > sPos) {
									update.set(fPos, rule.getSecond());
									update.set(sPos, rule.getFirst());
									any = true;
								}
							}
							if (!any) {
								break;
							}
						}
					})
					.mapToLong(update -> update.get(update.size() / 2))
					.sum();
	}

	record Input(
			List<Pair<Integer, Integer>> orderingRules,
			List<List<Integer>> updates
	) {
	}

}

