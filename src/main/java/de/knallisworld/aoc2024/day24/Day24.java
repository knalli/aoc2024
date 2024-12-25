package de.knallisworld.aoc2024.day24;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.Objects.requireNonNullElse;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day24 {

	@SneakyThrows
	public static void main(String[] args) {
		printHeader(24);
		printSolution(1, () -> "Result = %d".formatted(part1(readInputLines(24, "part1"))));
		printSolution(2, () -> "Result = %s".formatted(part2(readInputLines(24, "part1"))));
	}

	@SneakyThrows
	static long part1(final List<String> lines) {

		final var wires = buildWires(lines);
		final var workers = buildWorkers(lines, wires);
		waitForAllWorkers(workers);

		//final var x = readNumberByPrefix(wires, "x");
		//System.out.printf("%d | %50s %n", x, Long.toBinaryString(x));
		//final var y = readNumberByPrefix(wires, "y");
		//System.out.printf("%d | %50s %n", y, Long.toBinaryString(y));

		final var z = readNumberByPrefix(wires, "z");
		//System.out.printf("%d | %50s %n", z, Long.toBinaryString(z));

		return z;
	}

	@SneakyThrows
	static String part2(final List<String> lines) {

		final var rules = lines.stream()
							   .filter(l -> l.contains("->"))
							   .map(Rule::parse)
							   .collect(Collectors.toCollection(ArrayList::new));
		final var lastGate = rules.stream()
								  .map(a -> a.out)
								  .max(Comparator.naturalOrder())
								  .orElseThrow();
		final var violated1 = rules.stream()
								   .filter(r -> r.out.startsWith("z"))
								   .filter(not(r -> r.out.equals(lastGate)))
								   .filter(not(r -> r.op.equals("XOR")))
								   .toList();
		final var violated2 = rules.stream()
								   .filter(not(r -> r.out.startsWith("z")))
								   .filter(r -> !r.in1.startsWith("x") && !r.in1.startsWith("y") && !r.in2.startsWith("x") && !r.in2.startsWith("y"))
								   .filter(r -> r.op.equals("XOR"))
								   .toList();

		for (final var violatingRule2 : violated2) {
			final var violatingRule1 = violated1
				.stream()
				.filter(r1 -> r1.out.equals(firstZThatUsesOut(rules, violatingRule2.out)))
				.findFirst()
				.orElseThrow();

			final var t = violatingRule2.out();
			violatingRule2.setOut(violatingRule1.out());
			violatingRule1.setOut(t);
		}

		final var wires = buildWires2(lines);
		final var x = readNumberByPrefix2(wires, "x");
		final var y = readNumberByPrefix2(wires, "y");

		run(rules, wires);
		final var z = readNumberByPrefix2(wires, "z");

		final var falseCarry = "" + (Long.numberOfTrailingZeros((x + y) ^ z));

		return Stream.of(
						 violated1.stream(),
						 violated2.stream(),
						 rules.stream()
							  .filter(r -> r.in1.endsWith(falseCarry) && r.in2.endsWith(falseCarry))
					 )
					 .flatMap(identity())
					 .map(r -> r.out)
					 .sorted()
					 .collect(joining(","));
	}

	static void run(final List<Rule> rules, Map<String, Integer> wires) {
		var exclude = new HashSet<Rule>();

		while (exclude.size() != rules.size()) {
			var available = rules.stream()
								 .filter(a -> !exclude.contains(a) && rules.stream()
																		   .noneMatch(b -> (a.in1.equals(b.out) || a.in2.equals(b.out)) && !exclude.contains(b)))
								 .toList();

			for (var rule : available) {
				int v1 = wires.getOrDefault(rule.in1, 0);
				int v2 = wires.getOrDefault(rule.in2, 0);
				wires.put(rule.out, switch (rule.op) {
					case "AND" -> v1 & v2;
					case "OR" -> v1 | v2;
					case "XOR" -> v1 ^ v2;
					default -> throw new IllegalStateException("Unexpected value: " + rule.op);
				});
			}
			exclude.addAll(available);
		}
	}

	@Nullable
	static String firstZThatUsesOut(final List<Rule> rules, final String out) {
		final var filtered = rules.stream()
								  .filter(r -> r.in1().equals(out) || r.in2().equals(out))
								  .toList();

		final var found = filtered.stream()
								  .filter(r -> r.out().startsWith("z"))
								  .findFirst()
								  .orElse(null);

		if (found != null) {
			return "z" + String.format("%02d", Integer.parseInt(found.out().substring(1)) - 1);
		}

		for (var rule : filtered) {
			final var result = firstZThatUsesOut(rules, rule.out());
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	static Map<String, Integer> buildWires2(final List<String> lines) {
		return new HashMap<>(
			lines.stream()
				 .filter(l -> l.contains(":"))
				 .map(l -> l.split(": "))
				 .collect(toMap(
					 split -> split[0],
					 split -> split[1].equals("1") ? 1 : 0
				 ))
		);
	}

	static long readNumberByPrefix2(final Map<String, Integer> wires,
									final String prefix) {
		final var finalWires = wires.keySet()
									.stream()
									.filter(w -> w.startsWith(prefix))
									.sorted()
									.toList();
		return IntStream.range(0, finalWires.size())
						.mapToLong(i -> {
							final var wireId = finalWires.get(i);
							final var value = requireNonNullElse(wires.get(wireId), 0);
							//System.out.println(wireId + " " + value);
							if (value == 1) {
								// from binary
								return (long) Math.pow(2, i);
							} else {
								return 0L;
							}
						})
						.sum();
	}

	@SneakyThrows
	static String part2Old(final List<String> lines) {

		final Predicate<Map<String, ArrayBlockingQueue<Boolean>>> tester = w -> {
			final var x = readNumberByPrefix(w, "x");
			final var y = readNumberByPrefix(w, "y");
			final var z = readNumberByPrefix(w, "z");
			return x + y == z;
		};

		final var shuffableGateIds = resolveShuffableGateIds(lines);
		System.out.println(shuffableGateIds);
		return SwapUtils.generateSwap(lines, shuffableGateIds, 4)
						//.peek(System.out::println)
						.filter(item -> {
							final var wires = buildWires(item.lines());
							final var workers = buildWorkers(item.lines(), wires);
							waitForAllWorkers(workers);

							return tester.test(wires);
						})
						.findFirst()
						.map(item -> item.swapped().stream().sorted().collect(joining(",")))
						.orElseThrow();
	}

	@SneakyThrows
	static Map<String, Set<String>> resolveShuffableGateIds(final List<String> lines) {

		final var wires = buildWires(lines);
		final var workers = buildWorkers(lines, wires);
		waitForAllWorkers(workers);

		final var x = readNumberByPrefix(wires, "x");
		final var y = readNumberByPrefix(wires, "y");
		final var z = readNumberByPrefix(wires, "z");

		final var shuffableGateIds = new HashMap<String, Set<String>>();
		int numBits = 65 - Long.numberOfLeadingZeros(z);
		int bitCarry = 0;
		for (int i = 0; i < numBits; i++) {
			var bitX = (x >> i) & 1;
			var bitY = (y >> i) & 1;
			var bitZ = (z >> i) & 1;

			var gateId = "z%02d".formatted(i);

			int oldBitCarry = bitCarry;
			bitCarry = 0;
			final var sum = bitX + bitY + oldBitCarry;
			if (sum == 3) {
				if (bitZ != 1) {
					shuffableGateIds.put(gateId, Set.of("AND", "OR"));
				}
				bitCarry = 1;
			} else if (sum == 2) {
				if (bitZ != 0) {
					if (oldBitCarry == 0) {
						shuffableGateIds.put(gateId, Set.of("XOR"));
					} else if (oldBitCarry == 1) {
						shuffableGateIds.put(gateId, Set.of("AND"));
					}
				}
				bitCarry = 1;
			} else if (sum == 1) {
				if (bitZ != 1) {
					if (oldBitCarry == 0) {
						shuffableGateIds.put(gateId, Set.of("OR"));
					} else if (oldBitCarry == 1) {
						// shuffableGateIds.put(gateId, Set.of("AND"));
						// HOW?
						System.out.println("Flaw");
					}
				}
			} else if (sum == 0) {
				if (bitZ != 0) {
					shuffableGateIds.put(gateId, Set.of("XOR"));
				}
			}
		}

		return shuffableGateIds;
	}

	static List<Thread> buildWorkers(final List<String> lines,
									 final ConcurrentHashMap<String, ArrayBlockingQueue<Boolean>> wires) {
		final var workers = new ArrayList<Thread>();

		lines.stream()
			 .filter(l -> l.contains("->"))
			 .forEach(line -> {
				 final var outGate = line.split("-> ")[1];
				 final var inGate1 = line.split("-> ")[0].split(" ")[0];
				 final var op = line.split("-> ")[0].split(" ")[1];
				 final var inGate2 = line.split("-> ")[0].split(" ")[2];
				 wires.computeIfAbsent(outGate, __ -> new ArrayBlockingQueue<>(1));
				 wires.computeIfAbsent(inGate1, __ -> new ArrayBlockingQueue<>(1));
				 wires.computeIfAbsent(inGate2, __ -> new ArrayBlockingQueue<>(1));
				 workers.add(Thread.ofVirtual().start(() -> {
					 // System.out.println(line + " ...");
					 final var wire1 = wires.get(inGate1);
					 final var wire2 = wires.get(inGate2);
					 switch (op) {
						 case "AND" -> {
							 final var result = receiveAndResend(wire1) && receiveAndResend(wire2);
							 wires.get(outGate).add(result);
						 }
						 case "OR" -> {
							 final var result = receiveAndResend(wire1) || receiveAndResend(wire2);
							 wires.get(outGate).add(result);
						 }
						 case "XOR" -> {
							 final var w1 = receiveAndResend(wire1);
							 final var w2 = receiveAndResend(wire2);
							 final var result = (w1 && !w2) || (!w1 && w2);
							 wires.get(outGate).add(result);
						 }
						 default -> throw new IllegalStateException("Unexpected op: " + op);
					 }
					 // System.out.println(line + " COMPLETED");
				 }));
			 });
		return workers;
	}

	static void waitForAllWorkers(final Collection<Thread> workers) {
		workers.forEach(t -> {
			try {
				t.join();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	static ConcurrentHashMap<String, ArrayBlockingQueue<Boolean>> buildWires(final List<String> lines) {
		return new ConcurrentHashMap<>(
			lines.stream()
				 .filter(l -> l.contains(":"))
				 .map(l -> l.split(": "))
				 .collect(toMap(
					 split -> split[0],
					 split -> {
						 final var queue = new ArrayBlockingQueue<Boolean>(1);
						 queue.add(split[1].equals("1"));
						 return queue;
					 }
				 ))
		);
	}


	static long readNumberByPrefix(final Map<String, ArrayBlockingQueue<Boolean>> wires,
								   final String prefix) {
		final var finalWires = wires.keySet()
									.stream()
									.filter(w -> w.startsWith(prefix))
									.sorted()
									.toList();
		return IntStream.range(0, finalWires.size())
						.mapToLong(i -> {
							final var wireId = finalWires.get(i);
							final var value = requireNonNullElse(wires.get(wireId).poll(), false);
							//System.out.println(wireId + " " + value);
							if (value) {
								// from binary
								return (long) Math.pow(2, i);
							} else {
								return 0L;
							}
						})
						.sum();
	}


	@SneakyThrows
	static <T> T receiveAndResend(final ArrayBlockingQueue<T> queue) {
		final var value = queue.take();
		queue.add(value);
		return value;
	}

	static class Rule {

		private final String in1;
		private final String op;
		private final String in2;
		private String out;

		public Rule(String in1, String op, String in2, String out) {
			this.in1 = in1;
			this.op = op;
			this.in2 = in2;
			this.out = out;
		}

		public String in1() {
			return in1;
		}

		public String op() {
			return op;
		}

		public String in2() {
			return in2;
		}

		public String out() {
			return out;
		}

		public void setOut(String out) {
			this.out = out;
		}

		@Override
		public String toString() {
			return "%s %s %s -> %s".formatted(in1, op, in2, out);
		}

		public static Rule parse(final String str) {
			final var split = str.split(" -> ");
			final var left = split[0].split(" ");
			return new Rule(
				left[0],
				left[1],
				left[2],
				split[1]
			);
		}
	}

	static class SwapUtils {

		record Swap(Rule from, Rule to) {
		}

		record Item(List<String> lines, List<String> swapped) {
		}

		// approach #2 manually
		public static Stream<Item> generateSwap(final List<String> lines,
												final Map<String, Set<String>> shuffableGateIds,
												final int swaps) {

			final var preable = lines.stream()
									 .filter(l -> l.contains(": "))
									 .toList();

			final var rules = lines.stream()
								   .filter(l -> l.contains("->"))
								   .map(Rule::parse)
								   .toList();

			final var staticOnes = rules.stream()
										.filter(not(r -> shuffableGateIds.containsKey(r.out())))
										.toList();

			final var shuffableOnes = rules.stream()
										   .filter(r -> shuffableGateIds.containsKey(r.out()))
										   .toList();

			return permutateRules(
				shuffableOnes,
				shuffableGateIds::containsKey,
				swaps
			)
				.entrySet()
				.stream()
				.map(e -> {
					final var shuffledKeys = e.getKey();
					final var shuffled = e.getValue();
					return new Item(
						Stream.concat(
								  preable.stream(),
								  Stream.concat(
											staticOnes.stream(),
											shuffled.stream()
										)
										.map(Rule::toString)
							  )
							  .toList(),
						shuffledKeys.stream()
									.flatMap(s -> Stream.of(s.from.out, s.to.out))
									.sorted()
									.toList()
					);
				});
		}

		public static Map<List<Swap>, List<Rule>> permutateRules(final List<Rule> input,
																 final Predicate<String> swappableFilter,
																 int swaps) {
			return permutateRules0(Map.of(List.of(), input), swappableFilter, swaps);
		}

		public static Map<List<Swap>, List<Rule>> permutateRules0(final Map<List<Swap>, List<Rule>> input,
																  final Predicate<String> swappableFilter,
																  int swaps) {
			if (swaps < 1) {
				return input;
			}
			final var result = new HashMap<List<Swap>, List<Rule>>();
			input.forEach((visited, rules) -> {
				rules.stream()
					 .filter(r -> swappableFilter.test(r.out))
					 .filter(not(r -> visited.stream().anyMatch(swap -> swap.from.equals(r) || swap.to.equals(r))))
					 .forEach(rule1 -> {
						 rules.stream()
							  .filter(r -> swappableFilter.test(r.out))
							  .filter(not(r -> visited.stream().anyMatch(swap -> swap.from.equals(r) || swap.to.equals(r))))
							  .filter(not(rule1::equals))
							  .forEach(rule2 -> {
								  final var vv = new ArrayList<>(visited);
								  vv.add(new Swap(rule1, rule2));
								  final var rr = new ArrayList<>(rules);
								  rr.remove(rule1);
								  rr.remove(rule2);
								  rr.add(new Rule(rule1.in1(), rule1.op(), rule1.in2(), rule2.out()));
								  rr.add(new Rule(rule2.in1(), rule2.op(), rule2.in2(), rule1.out()));
								  result.put(vv, rr);
							  });
					 });
			});
			return permutateRules0(result, swappableFilter, swaps - 1);
		}

		// approach #1 recursive
		public static Stream<Item> generateSwappedLines(final List<String> lines,
														final Map<String, Set<String>> shuffableGateIds,
														final int swaps) {
			return generateSwappedRuleSets(lines, shuffableGateIds, swaps)
				.stream()
				.map(e -> new Item(e.getKey(), e.getValue().stream().flatMap(Collection::stream).distinct().toList()));
		}

		public static List<Map.Entry<List<String>, List<List<String>>>> generateSwappedRuleSets(final List<String> rules,
																								final Map<String, Set<String>> shuffableGateIds,
																								int swaps) {
			var outputsAndOperators = rules
				.stream()
				.map(rule -> {
					String[] parts = rule.split(" -> ");
					String[] expression = parts[0].split(" ");
					String operator = expression[1];
					return Map.entry(parts[1], operator);
				})
				.toList();

			var swappableIndices = IntStream
				.range(0, rules.size())
				.filter(i -> {
					String output = outputsAndOperators.get(i).getKey();
					String operator = outputsAndOperators.get(i).getValue();
					return shuffableGateIds.containsKey(output) && shuffableGateIds.get(output).contains(operator);
				})
				.boxed()
				.toList();

			var swappableOutputs = swappableIndices
				.stream()
				.map(i -> outputsAndOperators.get(i).getKey())
				.toList();
			var swappedCombinations = generateSwappedCombinations(swappableOutputs, swaps);

			return swappedCombinations
				.stream()
				.map(entry -> {
					final List<String> newRules = new ArrayList<>(rules);
					for (int i = 0; i < swappableIndices.size(); i++) {
						final var index = swappableIndices.get(i);
						final var newOutput = entry.getKey().get(i);
						newRules.set(index, rules.get(index).split(" -> ")[0] + " -> " + newOutput);
					}
					return Map.entry(newRules, entry.getValue());
				})
				.toList();
		}

		static List<Map.Entry<List<String>, List<List<String>>>> generateSwappedCombinations(final List<String> list,
																							 final int swaps) {
			final var results = new ArrayList<Map.Entry<List<String>, List<List<String>>>>();
			generateSwaps(new ArrayList<>(list), 0, swaps, new ArrayList<>(), new HashSet<>(), results);
			return results;
		}

		static void generateSwaps(final List<String> list,
								  final int start,
								  final int swaps,
								  final List<List<String>> swapsMade,
								  final Set<String> swappedSet,
								  final List<Map.Entry<List<String>, List<List<String>>>> results) {
			if (swaps == 0) {
				results.add(Map.entry(new ArrayList<>(list), new ArrayList<>(swapsMade)));
				return;
			}

			for (int i = start; i < list.size() - 1; i++) {
				for (int j = i + 1; j < list.size(); j++) {
					if (swappedSet.contains(list.get(i)) || swappedSet.contains(list.get(j))) {
						continue;
					}

					// Swap elements
					Collections.swap(list, i, j);
					swapsMade.add(List.of(list.get(i), list.get(j)));
					swappedSet.add(list.get(i));
					swappedSet.add(list.get(j));

					// deeper
					generateSwaps(list, i + 1, swaps - 1, swapsMade, swappedSet, results);

					swapsMade.removeLast();
					swappedSet.remove(list.get(i));
					swappedSet.remove(list.get(j));
					Collections.swap(list, i, j);
				}
			}
		}
	}

}

