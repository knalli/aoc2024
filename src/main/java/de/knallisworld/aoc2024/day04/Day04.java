package de.knallisworld.aoc2024.day04;

import de.knallisworld.aoc2024.support.geo.Point2D;
import de.knallisworld.aoc2024.support.geo.grid2.FixGrid;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputLines;

@Log4j2
public class Day04 {

	public static void main(String[] args) {
		printHeader(4);
		//printSolution(1, () -> "Word count: %d".formatted(countWordsOld(readGrid(readInputLines(4, "part1")))));
		printSolution(1, () -> "Word count: %d".formatted(countWordsPart1(readGrid(readInputLines(4, "part1")))));
		printSolution(2, () -> "Word count: %d".formatted(countWordsPart2(readGrid(readInputLines(4, "part1")))));
	}

	static FixGrid<Tile> readGrid(final List<String> lines) {
		final var grid = FixGrid.create(Tile.class, lines.size(), lines.getFirst().length());
		IntStream.range(0, lines.size())
				 .forEach(y -> {
					 final var line = lines.get(y);
					 IntStream.range(0, line.length())
							  .forEach(x -> {
								  grid.setValue(x, y, Tile.valueOf(String.valueOf(line.charAt(x))));
							  });
				 });
		return grid;
	}

	static int countWordsOld(final FixGrid<Tile> grid) {
		return grid.fields()
				   .stream()
				   .filter(field -> field.value() == Tile.X)
				   .mapToInt(field -> countWordsOld(grid, field.pos()))
				   .sum();
	}

	// originally, solved part1
	static int countWordsOld(final FixGrid<Tile> grid, final Point2D<Integer> pos) {

		int result = 0;

		final var fitsRight = pos.getX() + 3 < grid.getWidth();
		final var fitsLeft = pos.getX() - 3 >= 0;
		final var fitsDown = pos.getY() + 3 < grid.getHeight();
		final var fitsUp = pos.getY() - 3 >= 0;

		// right
		if (fitsRight) {
			final var p1 = pos;
			final var p2 = p1.right();
			final var p3 = p2.right();
			final var p4 = p3.right();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		// left (backwards)
		if (fitsLeft) {
			final var p1 = pos;
			final var p2 = p1.left();
			final var p3 = p2.left();
			final var p4 = p3.left();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		// bottom
		if (fitsDown) {
			final var p1 = pos;
			final var p2 = p1.down();
			final var p3 = p2.down();
			final var p4 = p3.down();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		// up (backwards)
		if (fitsUp) {
			final var p1 = pos;
			final var p2 = p1.up();
			final var p3 = p2.up();
			final var p4 = p3.up();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		// diag: right/down
		if (fitsRight && fitsDown) {
			final var p1 = pos;
			final var p2 = p1.down().right();
			final var p3 = p2.down().right();
			final var p4 = p3.down().right();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		// diag: left/down
		if (fitsLeft && fitsDown) {
			final var p1 = pos;
			final var p2 = p1.down().left();
			final var p3 = p2.down().left();
			final var p4 = p3.down().left();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		// diag: right/up
		if (fitsRight && fitsUp) {
			final var p1 = pos;
			final var p2 = p1.up().right();
			final var p3 = p2.up().right();
			final var p4 = p3.up().right();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		// diag: left/up
		if (fitsLeft && fitsUp) {
			final var p1 = pos;
			final var p2 = p1.up().left();
			final var p3 = p2.up().left();
			final var p4 = p3.up().left();
			if (matchXms(grid, p1, p2, p3, p4)) {
				result++;
			}
		}
		return result;
	}

	static long countWordsPart1(final FixGrid<Tile> grid) {

		final var expectationVectors = List.of(
				// right
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(1, 0), Tile.M),
						new Expected(Point2D.create(2, 0), Tile.A),
						new Expected(Point2D.create(3, 0), Tile.S)
				),
				// left (backwards)
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(-1, 0), Tile.M),
						new Expected(Point2D.create(-2, 0), Tile.A),
						new Expected(Point2D.create(-3, 0), Tile.S)
				),
				// down
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(0, 1), Tile.M),
						new Expected(Point2D.create(0, 2), Tile.A),
						new Expected(Point2D.create(0, 3), Tile.S)
				),
				// up (backwards)
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(0, -1), Tile.M),
						new Expected(Point2D.create(0, -2), Tile.A),
						new Expected(Point2D.create(0, -3), Tile.S)
				),
				// right/down
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(1, 1), Tile.M),
						new Expected(Point2D.create(2, 2), Tile.A),
						new Expected(Point2D.create(3, 3), Tile.S)
				),
				// right/up
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(1, -1), Tile.M),
						new Expected(Point2D.create(2, -2), Tile.A),
						new Expected(Point2D.create(3, -3), Tile.S)
				),
				// left/down
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(-1, 1), Tile.M),
						new Expected(Point2D.create(-2, 2), Tile.A),
						new Expected(Point2D.create(-3, 3), Tile.S)
				),
				// left/up
				List.of(
						new Expected(Point2D.create(0, 0), Tile.X),
						new Expected(Point2D.create(-1, -1), Tile.M),
						new Expected(Point2D.create(-2, -2), Tile.A),
						new Expected(Point2D.create(-3, -3), Tile.S)
				)
		);

		return grid.fields()
				   .stream()
				   .mapToLong(field -> countWords(grid, expectationVectors, field.pos()))
				   .sum();
	}

	static long countWordsPart2(final FixGrid<Tile> grid) {

		// of A
		final var expectationVectors = List.of(
				List.of(
						// M.S
						// .A.
						// M.S
						new Expected(Point2D.create(-1, -1), Tile.M),
						new Expected(Point2D.create(1, -1), Tile.S),
						new Expected(Point2D.create(0, 0), Tile.A),
						new Expected(Point2D.create(-1, 1), Tile.M),
						new Expected(Point2D.create(1, 1), Tile.S)
				),
				List.of(
						// M.M
						// .A.
						// S.S
						new Expected(Point2D.create(-1, -1), Tile.M),
						new Expected(Point2D.create(1, -1), Tile.M),
						new Expected(Point2D.create(0, 0), Tile.A),
						new Expected(Point2D.create(-1, 1), Tile.S),
						new Expected(Point2D.create(1, 1), Tile.S)
				),
				List.of(
						// S.S
						// .A.
						// M.M
						new Expected(Point2D.create(-1, -1), Tile.S),
						new Expected(Point2D.create(1, -1), Tile.S),
						new Expected(Point2D.create(0, 0), Tile.A),
						new Expected(Point2D.create(-1, 1), Tile.M),
						new Expected(Point2D.create(1, 1), Tile.M)
				),
				List.of(
						// S.M
						// .A.
						// S.M
						new Expected(Point2D.create(-1, -1), Tile.S),
						new Expected(Point2D.create(1, -1), Tile.M),
						new Expected(Point2D.create(0, 0), Tile.A),
						new Expected(Point2D.create(-1, 1), Tile.S),
						new Expected(Point2D.create(1, 1), Tile.M)
				)
		);

		return grid.fields()
				   .stream()
				   //.filter(field -> field.value() == Tile.X)
				   .mapToLong(field -> countWords(grid, expectationVectors, field.pos()))
				   .sum();
	}

	static long countWords(final FixGrid<Tile> grid, List<List<Expected>> expectationVectorRules, final Point2D<Integer> pos) {
		return expectationVectorRules
				.stream()
				.filter(rule -> rule
						.stream()
						// resolve actual tile at grid[pos+vec]
						.allMatch(expected -> grid.getValue(pos.add(expected.vec())).orElse(null) == expected.value()))
				.count();
	}

	static boolean matchXms(final FixGrid<Tile> grid,
							final Point2D<Integer> p1,
							final Point2D<Integer> p2,
							final Point2D<Integer> p3,
							final Point2D<Integer> p4) {
		return grid.getValueRequired(p1) == Tile.X
				&& grid.getValueRequired(p2) == Tile.M
				&& grid.getValueRequired(p3) == Tile.A
				&& grid.getValueRequired(p4) == Tile.S;
	}

	enum Tile {
		X, M, A, S
	}

	record Expected(Point2D<Integer> vec, Tile value) {
	}

}

