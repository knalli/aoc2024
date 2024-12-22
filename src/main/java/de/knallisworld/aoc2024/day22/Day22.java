package de.knallisworld.aoc2024.day22;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.stream.Collectors.*;
import static java.util.stream.Gatherers.windowSliding;

@SuppressWarnings("preview")
@Log4j2
public class Day22 {

	public static void main(String[] args) {
		printHeader(22);
		printSolution(1, () -> "Result = %d".formatted(part1(readInputLines(22, "part1", Long::parseLong))));
		printSolution(1, () -> "Result = %d".formatted(part2(readInputLines(22, "part1", Long::parseLong))));
	}

	static long part1(final List<Long> input) {
		return input.stream()
					.mapToLong(i -> Day22
						.generateBuyerGenerator(i)
						.skip(2000)
						.findFirst()
						.orElseThrow()
					)
					.sum();
	}

	static long part2(final List<Long> input) {
		record Item0(int price, int delta) {
		}
		record Item(int price, List<Integer> deltas) {
		}
		final var data = input.stream()
							  .map(i -> Day22
								  .generateBuyerGenerator(i)
								  .limit(2000)
								  .map(n -> n % 10)
								  // sliding window for crafting delta
								  .gather(windowSliding(2))
								  .map(window -> new Item0(
									  window.getLast().intValue(),
									  window.getLast().intValue() - window.getFirst().intValue()
								  ))
								  // sliding window for crafting the 4 last delta ("deltas")
								  .gather(windowSliding(4))
								  .map(window -> new Item(
									  window.getLast().price(),
									  window.stream().map(Item0::delta).toList()
								  ))
								  // finally, for run-optimizations re-organize structure to
								  // a map of "deltas" => first found price
								  .collect(groupingBy(
									  Item::deltas,
									  collectingAndThen(
										  mapping(Item::price, toList()),
										  List::getFirst
									  )
								  ))
							  )
							  .toList();
		return generateCombinations(4, -9, 9)
			.stream()
			.parallel() // gears up
			.mapToLong(combo -> data
				.stream()
				.mapToLong(buyer -> buyer.getOrDefault(combo, 0))
				.sum()
			)
			.max()
			.orElseThrow();
	}

	static Stream<Long> generateBuyerGenerator(final long seed) {
		return Stream.iterate(seed, number -> {
			var r = number;
			r = ((r * 64) ^ r) % 16777216;
			r = ((r / 32) ^ r) % 16777216;
			r = ((r * 2048) ^ r) % 16777216;
			return r;
		});
	}

	@SuppressWarnings("SameParameterValue")
	static List<List<Integer>> generateCombinations(int n,
													int min,
													int max) {
		final var result = new ArrayList<List<Integer>>();
		generateCombinationsHelper(n, min, max, new ArrayList<>(), result);
		return result;
	}

	static void generateCombinationsHelper(int n,
										   int min,
										   int max,
										   List<Integer> current,
										   List<List<Integer>> result) {
		if (current.size() == n) {
			result.add(new ArrayList<>(current));
			return;
		}
		for (int i = min; i <= max; i++) {
			current.add(i);
			generateCombinationsHelper(n, min, max, current, result);
			current.removeLast();
		}
	}

}

