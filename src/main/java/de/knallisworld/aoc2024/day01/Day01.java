package de.knallisworld.aoc2024.day01;

import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLinesAsStream;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Log4j2
public class Day01 {

	public static void main(String[] args) {
		printHeader(1);

		final var listL = readInputLinesAsStream(1, "part1", s -> Integer.parseInt(s.split(" +")[0]))
				.sorted()
				.toList();
		final var listR = readInputLinesAsStream(1, "part1", s -> Integer.parseInt(s.split(" +")[1]))
				.sorted()
				.toList();

		printSolution(1, () -> "Sum of distances = %d".formatted(sumOfDistances(listL, listR)));
		printSolution(2, () -> "Similarity score = %d".formatted(getSimilarityScore(listL, listR)));
	}

	static int sumOfDistances(final List<Integer> listL, final List<Integer> listR) {
		assert listL.size() == listR.size();
		return IntStream.range(0, listL.size())
		                .map(i -> Math.abs(listL.get(i) - listR.get(i)))
		                .sum();
	}

	static long getSimilarityScore(final List<Integer> listL, final List<Integer> listR) {
		final var map = listR.stream()
		                     .collect(groupingBy(a -> a, counting()));
		return listL.stream()
		            .mapToLong(n -> n * map.getOrDefault(n, 0L))
		            .sum();
	}

}
