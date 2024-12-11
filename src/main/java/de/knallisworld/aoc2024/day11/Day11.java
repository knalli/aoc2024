package de.knallisworld.aoc2024.day11;

import lombok.extern.log4j.Log4j2;

import java.util.*;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputFirstLine;
import static java.lang.Long.parseLong;

@Log4j2
public class Day11 {

	public static void main(String[] args) {
		printHeader(11);
		printSolution(1, () -> "Stone count: %d".formatted(solve(readInput(readInputFirstLine(11, "part1")), 25)));
		printSolution(2, () -> "Stone count: %d".formatted(solve(readInput(readInputFirstLine(11, "part1")), 75)));
	}

	record Key(long n, int round) {
	}

	static long solve(final List<Long> stones, final int goalRounds) {
		return solve0(new HashMap<>(), stones, goalRounds);
	}

	static long solve0(final Map<Key, Long> cache, final List<Long> stones, final int roundsLeft) {
		if (roundsLeft == 0) {
			return stones.size();
		}
		return stones.stream()
					 .mapToLong(n -> {
						 final var key = new Key(n, roundsLeft);
						 if (n == 0) {
							 if (cache.containsKey(key)) {
								 return cache.get(key);
							 }
							 final var r = solve0(
								 cache,
								 List.of(1L),
								 roundsLeft - 1
							 );
							 cache.put(key, r);
							 return r;
						 } else {
							 final var valueStr = Long.toString(n);
							 if (valueStr.length() % 2 == 0) {
								 if (cache.containsKey(key)) {
									 return cache.get(key);
								 }
								 final var r = solve0(
									 cache,
									 List.of(
										 parseLong(valueStr.substring(0, valueStr.length() / 2)),
										 parseLong(valueStr.substring(valueStr.length() / 2))
									 ),
									 roundsLeft - 1
								 );
								 cache.put(key, r);
								 return r;
							 } else {
								 if (cache.containsKey(key)) {
									 return cache.get(key);
								 }
								 final var r = solve0(
									 cache,
									 List.of(n * 2024),
									 roundsLeft - 1
								 );
								 cache.put(key, r);
								 return r;
							 }
						 }
					 })
					 .sum();
	}

	static List<Long> readInput(final String str) {
		final var result = new LinkedList<Long>();
		Arrays.stream(str.split(" "))
			  .map(Long::parseLong)
			  .forEach(result::add);
		return result;
	}

}

