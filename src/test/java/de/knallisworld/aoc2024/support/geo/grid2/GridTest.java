package de.knallisworld.aoc2024.support.geo.grid2;

import org.junit.jupiter.api.Test;

class GridTest {

	@Test
	void x(){
		final var grid = new FixGrid<>(String.class, 2, 2);
		grid.setValue(1, 1, "2");
	}

}
