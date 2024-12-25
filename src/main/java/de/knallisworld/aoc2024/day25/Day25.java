package de.knallisworld.aoc2024.day25;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.stream.Gatherers.windowFixed;

@SuppressWarnings("preview")
@Log4j2
public class Day25 {

	public static void main(String[] args) {
		printHeader(25);
		printSolution(1, () -> "Result = %d".formatted(part1(readInputLines(25, "part1"))));
		printSolution(2, () -> "x");
	}

	static long part1(final List<String> lines) {
		final var keys = readKeys(lines);
		final var locks = readLocks(lines);

		//System.out.println(keys);
		//System.out.println(locks);

		//System.out.printf("Keys: %d%n", keys.size());
		//System.out.printf("Locks: %d%n", locks.size());

		var counter = 0;
		for (final var key : keys) {
			for (final var lock : locks) {
				if (testLockAndKey(lock, key)) {
					counter++;
				}
			}
		}

		//System.out.printf("Keys: %d%n", keys2.size());
		//System.out.printf("Locks: %d%n", locks2.size());
		//System.out.println(counter);

		return counter;
	}

	private static boolean foundMatch(final List<List<Integer>> keys,
									  final List<List<Integer>> locks,
									  final List<Pair<List<Integer>, List<Integer>>> matched) {
		for (final var key : keys) {
			for (final var lock : locks) {
				final var pair = new Pair<>(key, lock);
				if (matched.contains(pair)) {
					continue;
				}
				matched.add(pair);
				if (testLockAndKey(lock, key)) {
					keys.remove(key);
					locks.remove(lock);
					return true;
				}
			}
		}
		return false;
	}

	static boolean testLockAndKey(final List<Integer> lock, final List<Integer> key) {
		if (lock.size() != key.size()) {
			return false;
		}
		return IntStream.range(0, lock.size())
						.map(i -> lock.get(i) + key.get(i))
						.allMatch(r -> r <= 5);
	}

	static List<List<Integer>> readKeys(final List<String> lines) {
		return lines.stream()
					.gather(windowFixed(8))
					.filter(block -> block.getFirst().equals("....."))
					.map(block -> block.subList(0, 7))
					.map(block -> {
						return IntStream.range(0, block.getFirst().length())
										.map(i -> {
											return IntStream.range(0, block.size())
															.filter(j -> block.get(j).charAt(i) == '#')
															.map(j -> block.size() - 1 - j)
															.findFirst()
															.orElse(0);
										})
										.boxed()
										.toList();
					})
					.toList();
	}

	static List<List<Integer>> readLocks(final List<String> lines) {
		return lines.stream()
					.gather(windowFixed(8))
					.filter(block -> block.getFirst().equals("#####"))
					.map(block -> block.subList(0, 7))
					.map(block -> {
						return IntStream.range(0, block.getFirst().length())
										.map(i -> {
											return IntStream.range(0, block.size())
															.filter(j -> block.get(j).charAt(i) == '.')
															.map(j -> j - 1)
															.findFirst()
															.orElse(0);
										})
										.boxed()
										.toList();
					})
					.toList();
	}

}

