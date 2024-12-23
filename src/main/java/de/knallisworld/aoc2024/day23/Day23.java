package de.knallisworld.aoc2024.day23;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.stream.Collectors.*;

@Log4j2
public class Day23 {

	public static void main(String[] args) {
		printHeader(23);
		printSolution(1, () -> "Result = %d".formatted(part1(readInputLines(23, "part1"))));
		printSolution(2, () -> "Result = %s".formatted(part2(readInputLines(23, "part1"))));
	}

	static Set<List<String>> cliques(final Map<String, Set<String>> connections,
									 final int limit) {
		Set<List<String>> result = new HashSet<>();
		cliques0(connections, limit, result);
		return result;
	}

	static void cliques0(final Map<String, Set<String>> connections,
						 final int limit,
						 final Set<List<String>> result) {
		for (final var key : connections.keySet()) {
			recurse0(result, connections, limit, key, new HashSet<>(List.of(key)));
		}
	}

	static void recurse0(final Set<List<String>> result,
						 final Map<String, Set<String>> connections,
						 final int limit,
						 final String node,
						 final Set<String> clique) {
		List<String> sortedClique = new ArrayList<>(clique);
		Collections.sort(sortedClique);

		if (!result.contains(sortedClique)) {
			result.add(sortedClique);
			if (sortedClique.size() >= limit) {
				return;
			}
		} else {
			return;
		}

		for (final var neighbor : connections.get(node)) {
			if (clique.contains(neighbor)) {
				continue;
			}
			if (!connections.get(neighbor).containsAll(clique)) {
				continue;
			}
			final var newClique = new HashSet<>(clique);
			newClique.add(neighbor);
			recurse0(result, connections, limit, neighbor, newClique);
		}
	}

	static Map<String, Set<String>> parse(final List<String> lines) {
		return lines.stream()
					.map(line -> line.split("-"))
					// both directions
					.flatMap(split -> Stream.of(List.of(split[0], split[1]), List.of(split[1], split[0])))
					.collect(groupingBy(
						List::getFirst,
						mapping(List::getLast, toSet())
					));
	}

	static long part1(final List<String> lines) {
		final var cliques = cliques(parse(lines), 3)
			.stream()
			.filter(l -> l.size() == 3)
			.filter(l -> l.stream().anyMatch(s -> s.startsWith("t")))
			.toList();
		return cliques.size();
	}

	static String part2(final List<String> lines) {
		final var parsed = parse(lines);
		return cliques(parsed, parsed.size())
			.stream()
			.max(Comparator.comparing(a -> a.size()))
			.stream()
			.flatMap(a -> a.stream())
			.collect(joining(","));
	}

}

