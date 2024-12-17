package de.knallisworld.aoc2024.day17;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day17 {

	public static void main(String[] args) {
		printHeader(17);
		printSolution(1, () -> "Result = %s".formatted(part1(readComputer(readInputLines(17, "part1")))));
		printSolution(2, () -> "Result = %d".formatted(part2(readComputer(readInputLines(17, "part1")))));
	}

	static String part1(final Computer computer) {
		return computer.executeAndReturnAsString();
	}

	static long part2(final Computer vintage) {

		final Function<Long, List<Short>> executor = registerA -> {
			final var computer = new Computer(
				new Registers(
					vintage.registers.getIp(),
					registerA,
					vintage.registers.getB(),
					vintage.registers.getC()
				),
				vintage.instructions
			);
			return computer.executeAndReturnAsList();
		};

		final var remaining = new ArrayList<Short>(vintage.instructions);
		final var result = new ArrayList<Short>();

		long a = 0L;
		while (!remaining.isEmpty()) {
			--a;
			result.addFirst(remaining.removeLast());

			do {
				++a;
			} while (!executor.apply(a).equals(result));
			if (!remaining.isEmpty()) {
				a = a << 3;
			}
		}

		return a;
	}

	static Computer readComputer(final List<String> lines) {
		final var registersData = lines
			.stream()
			.filter(line -> line.startsWith("Register "))
			.map(line -> line.split("Register ")[1].split(": "))
			.collect(toMap(s -> s[0], s -> Long.parseLong(s[1])));
		final var instructions = lines
			.stream()
			.filter(line -> line.startsWith("Program: "))
			.flatMap(line -> Arrays.stream(line.split("Program: ")[1].split(",")))
			.map(Short::parseShort)
			.toList();
		return new Computer(
			new Registers(
				0,
				registersData.get("A"),
				registersData.get("B"),
				registersData.get("C")
			),
			instructions
		);
	}

	@SuppressWarnings("ClassCanBeRecord")
	@Data
	static class Computer {

		private final Registers registers;
		private final List<Short> instructions;

		private boolean printStatementsEnabled;
		private boolean debugStatementsEnabled;

		String executeAndReturnAsString() {
			final var result = new ArrayList<Short>();
			execute(result::add, _ -> false);
			return result.stream().map(v -> "" + v).collect(joining(","));
		}

		List<Short> executeAndReturnAsList() {
			final var result = new ArrayList<Short>();
			execute(result::add, _ -> false);
			return result;
		}

		void startDebuggingInterface(final Predicate<Short> stopFilter) {
			execute(
				_ -> {
				},
				stopFilter
			);
		}

		void execute(final Consumer<Short> out, final Predicate<Short> stopFilter) {
			final var stopRequired = new AtomicBoolean();
			while (registers.ip < instructions.size() - 1) {
				execute0(out, value -> {
					final var stopReceived = stopFilter.test(value);
					if (stopReceived) {
						stopRequired.set(true);
					}
					return stopReceived;
				});
				if (stopRequired.get()) {
					return;
				}
			}
			//System.out.println("HALT");
		}

		void execute0(final Consumer<Short> out, final Predicate<Short> stopFilter) {
			final var opCode = OpCode.values()[instructions.get(registers.ip)];
			final var operandLiteral = instructions.get(registers.ip + 1);
			if (printStatementsEnabled) {
				System.err.printf(
					"%s %x (%x)   [a=%x, b=%x, c=%xd]%n",
					opCode,
					operandLiteral,
					getOperandCombo(operandLiteral),
					registers.getA(),
					registers.getB(),
					registers.getC()
				);
			}
			switch (opCode) {
				case adv -> {
					// division (=> a)
					var x = registers.a;
					var y = getOperandCombo(operandLiteral);
					final var r = (x >> y);
					if (debugStatementsEnabled) {
						System.err.printf("    A=%x >> %x = %x => A%n", x, y, r);
					}
					registers.a = r;
					registers.ip += 2;
				}
				case bxl -> {
					// bitwise XOR
					var x = registers.b;
					var y = operandLiteral;
					final var r = x ^ y;
					if (debugStatementsEnabled) {
						System.err.printf("    %x ^ %x = %x ==> B%n", x, y, r);
					}
					registers.b = r;
					registers.ip += 2;
				}
				case bst -> {
					// modulo
					final var x = getOperandCombo(operandLiteral);
					final var r = x % 8;
					if (debugStatementsEnabled) {
						System.err.printf("    %x %% %x = %x => B%n", x, 8, r);
					}
					registers.b = r;
					registers.ip += 2;
				}
				case jnz -> {
					// jump if not zero
					if (registers.a != 0) {
						registers.ip = operandLiteral;
					} else {
						registers.ip += 2;
					}
					if (debugStatementsEnabled) {
						System.err.println("    if A != 0 restart");
					}
				}
				case bxc -> {
					// bitwise XOR
					final var x = registers.b;
					final var y = registers.c;
					final var r = x ^ y;
					if (debugStatementsEnabled) {
						System.err.printf("    %x >> %x = %x => B%n", x, y, r);
					}
					registers.b = r;
					registers.ip += 2;
				}
				case out -> {
					// output
					final var x = getOperandCombo(operandLiteral);
					final var value = (short) (x % 8);
					if (stopFilter.test(value)) {
						// part2 optimization fast exiting
						return;
					}
					out.accept(value);
					if (debugStatementsEnabled) {
						System.err.printf("    print %x %% %x%n", x, 8);
					}
					registers.ip += 2;
				}
				case bdv -> {
					// division (=> b)
					var x = registers.a;
					var y = getOperandCombo(operandLiteral);
					final var r = (x >> y);
					if (debugStatementsEnabled) {
						System.err.printf("    A=%x >> %x = %x => B%n", x, y, r);
					}
					registers.b = r;
					registers.ip += 2;
				}
				case cdv -> {
					// division (=> c)
					var x = registers.a;
					var y = getOperandCombo(operandLiteral);
					final var r = (x >> y);
					if (debugStatementsEnabled) {
						System.err.printf("    A=%x >> %x = %x => C%n", x, y, r);
					}
					registers.c = r;
					registers.ip += 2;
				}
			}
		}

		private long getOperandCombo(Short operandLiteral) {
			return switch (operandLiteral) {
				case 0, 1, 2, 3 -> operandLiteral;
				case 4 -> registers.a;
				case 5 -> registers.b;
				case 6 -> registers.c;
				default -> throw new IllegalStateException("Unexpected value: " + operandLiteral);
			};
		}

	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Registers {

		private int ip;
		private long a;
		private long b;
		private long c;

	}

	enum OpCode {
		adv,
		bxl,
		bst,
		jnz,
		bxc,
		out,
		bdv,
		cdv
	}

}

