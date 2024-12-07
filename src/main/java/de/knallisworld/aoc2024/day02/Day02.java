package de.knallisworld.aoc2024.day02;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLinesMulti;

@Log4j2
public class Day02 {

	public static void main(String[] args) {
		printHeader(2);
		printSolution(1, () -> {
			final var filter = buildIsValidFilter();
			final var ll = readInput(2, "part1");
			return "Safe reports = %d".formatted(countValid(ll, filter));
		});
		printSolution(2, () -> {
			final var filter = buildIsValidFilter();
			final var ll = applyDampenerAlgo(readInput(2, "part1"), filter);
			return "Safe reports = %d".formatted(countValid(ll, filter));
		});
	}

	@SuppressWarnings("SameParameterValue")
	static List<List<Integer>> readInput(final int day,
										 final String name) {
		return readInputLinesMulti(
			day,
			name,
			line -> Arrays.stream(line.split(" "))
						  .map(Integer::parseInt)
		);
	}

	static Predicate<List<Integer>> buildIsValidFilter() {
		return list -> {
			var prev = list.getFirst();
			var order = Order.NONE;
			for (var i = 1; i < list.size(); i++) {
				final var current = list.get(i);
				// determine initial order
				if (order == Order.NONE) {
					if (current < prev) {
						order = Order.DESC;
					} else if (current > prev) {
						order = Order.ASC;
					} else {
						// yes, this may be also your forgotten case
						return false;
					}
				}
				// with the applied order, check the next
				switch (order) {
					case ASC -> {
						final var delta = current - prev;
						if (1 <= delta && delta <= 3) {
							prev = current;
						} else {
							return false;
						}
					}
					case DESC -> {
						final var delta = prev - current;
						if (1 <= delta && delta <= 3) {
							prev = current;
						} else {
							return false;
						}
					}
				}
			}
			return true;
		};
	}

	static long countValid(final List<List<Integer>> input,
						   final Predicate<List<Integer>> filter) {
		return input
			.stream()
			.filter(filter)
			.count();
	}

	static List<List<Integer>> applyDampenerAlgo(final List<List<Integer>> input,
												 final Predicate<List<Integer>> filter) {
		return input
			.stream()
			.mapMulti((List<Integer> list, Consumer<List<Integer>> next) -> {
				if (filter.test(list)) {
					next.accept(list);
					return;
				}
				// brute-forcing: remove each level/index and try again with the filter
				for (int i = 0; i < list.size(); i++) {
					final var candidate = new ArrayList<>(list);
					//noinspection SuspiciousListRemoveInLoop
					candidate.remove(i);
					if (filter.test(candidate)) {
						next.accept(candidate);
						return;
					}
				}
			})
			.toList();
	}

	enum Order {
		ASC, DESC, NONE
	}

}

