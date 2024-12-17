package de.knallisworld.aoc2024.day17;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Day17Test {

	@Nested
	class ForPart2Demo {

		@SuppressWarnings("OctalInteger")
		@Test
		void run01() {
			final var computer = new Day17.Computer(
				new Day17.Registers(
					0,
					0_004,
					0,
					0
				),
				Stream.of(2, 4, 1, 1, 7, 5, 1, 5, 4, 2, 5, 5, 0, 3, 3, 0)
					  .map(Integer::shortValue)
					  .toList()
			);
			assertThat(computer.executeAndReturnAsString())
				.isEqualTo("0");
		}

		@SuppressWarnings("OctalInteger")
		@Test
		void run02() {
			final var computer = new Day17.Computer(
				new Day17.Registers(
					0,
					0_000_014,
					0,
					0
				),
				Stream.of(2, 4, 1, 1, 7, 5, 1, 5, 4, 2, 5, 5, 0, 3, 3, 0)
					  .map(Integer::shortValue)
					  .toList()
			);
			assertThat(computer.executeAndReturnAsString())
				.isEqualTo("0,4");
		}

		@SuppressWarnings("OctalInteger")
		@Test
		void run03() {
			final var computer = new Day17.Computer(
				new Day17.Registers(
					0,
					0_000_414,
					0,
					0
				),
				Stream.of(2, 4, 1, 1, 7, 5, 1, 5, 4, 2, 5, 5, 0, 3, 3, 0)
					  .map(Integer::shortValue)
					  .toList()
			);
			assertThat(computer.executeAndReturnAsString())
				.isEqualTo("0,4,0");
		}

		@SuppressWarnings("OctalInteger")
		@Test
		void run04() {
			final var computer = new Day17.Computer(
				new Day17.Registers(
					0,
					0_001_004,
					0,
					0
				),
				Stream.of(2, 4, 1, 1, 7, 5, 1, 5, 4, 2, 5, 5, 0, 3, 3, 0)
					  .map(Integer::shortValue)
					  .toList()
			);
			assertThat(computer.executeAndReturnAsString())
				.isEqualTo("0,4,0,4");
		}

	}

	@Test
	void check01() {
		final var computer = new Day17.Computer(
			new Day17.Registers(
				0,
				0,
				0,
				9
			),
			Stream.of(2, 6)
				  .map(Integer::shortValue)
				  .toList()
		);
		assertThat(computer.executeAndReturnAsString())
			.isEqualTo("");
		assertThat(computer.getRegisters().getB())
			.isEqualTo(1);
	}

	@Test
	void check02() {
		final var computer = new Day17.Computer(
			new Day17.Registers(
				0,
				10,
				0,
				0
			),
			Stream.of(5, 0, 5, 1, 5, 4)
				  .map(Integer::shortValue)
				  .toList()
		);
		assertThat(computer.executeAndReturnAsString())
			.isEqualTo("0,1,2");
	}

	@Test
	void check03() {
		final var computer = new Day17.Computer(
			new Day17.Registers(
				0,
				2024,
				0,
				0
			),
			Stream.of(0, 1, 5, 4, 3, 0)
				  .map(Integer::shortValue)
				  .toList()
		);
		assertThat(computer.executeAndReturnAsString())
			.isEqualTo("4,2,5,6,7,7,7,7,3,1,0");
		assertThat(computer.getRegisters().getA())
			.isEqualTo(0);
	}

	@Test
	void check04() {
		final var computer = new Day17.Computer(
			new Day17.Registers(
				0,
				0,
				29,
				0
			),
			Stream.of(1, 7)
				  .map(Integer::shortValue)
				  .toList()
		);
		computer.executeAndReturnAsString();
		assertThat(computer.getRegisters().getB())
			.isEqualTo(26);
	}

	@Test
	void check05() {
		final var computer = new Day17.Computer(
			new Day17.Registers(
				0,
				0,
				2024,
				43690
			),
			Stream.of(4, 0)
				  .map(Integer::shortValue)
				  .toList()
		);
		computer.executeAndReturnAsString();
		assertThat(computer.getRegisters().getB())
			.isEqualTo(44354);
	}

}
