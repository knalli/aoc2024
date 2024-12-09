package de.knallisworld.aoc2024.day09;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.knallisworld.aoc2024.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2024.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2024.support.puzzle.InputReader.readInputFirstLine;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

@Log4j2
public class Day09 {

	public static void main(String[] args) {
		printHeader(9);
		printSolution(1, () -> "Checksum: %s".formatted(part1(readFilesystem(readInputFirstLine(9, "part1")))));
		printSolution(2, () -> "Checksum: %s".formatted(part2(readFilesystem(readInputFirstLine(9, "part1")))));
	}

	static long part1(final Filesystem fs) {
		while (fs.hasGaps()) {
			fs.stream()
			  .filter(ordered -> ordered.block.free)
			  .findFirst()
			  .ifPresent(ordered -> {
				  // remove empty blocks (without length)
				  if (ordered.block.length == 0) {
					  fs.remove(ordered.index);
					  return; // positions changed, restart
				  }
				  // cleanup empty blocks at the end
				  if (fs.getLast().free) {
					  while (fs.getLast().free) {
						  fs.remove(fs.size() - 1);
					  }
					  return; // positions changed, restart
				  }
				  final var free = ordered.block;
				  final var last = fs.getLast();
				  final var used = Math.min(last.length, free.length);
				  // remove last or cut down
				  if (last.length == used) {
					  fs.remove(fs.size() - 1);
				  } else {
					  fs.set(fs.size() - 1, new Block(false, last.length - used, last.fileId));
				  }
				  // update "current"
				  for (var i = 0; i < used; i++) {
					  if (i == 0) {
						  fs.set(ordered.index, new Block(false, 1, last.fileId));
					  } else {
						  fs.append(ordered.index, new Block(false, 1, last.fileId));
					  }
				  }
				  if (free.length - used > 0) {
					  fs.append(ordered.index + used - 1, new Block(true, free.length - used, -1));
				  }
			  });
		}
		return fs.checksum();
	}

	static long part2(final Filesystem fs) {
		fs.files()
		  .toList()
		  .reversed()
		  .forEach(fileBlock -> {
			  final var fileBlockIndex = fs.getIndexOfBlock(fileBlock);
			  fs.stream()
				.filter(o -> o.block.free && o.block.length >= fileBlock.length && o.index < fileBlockIndex)
				.findFirst()
				.ifPresent(o -> {
					fs.set(fileBlockIndex, new Block(true, fileBlock.length, -1));
					final var left = o.block.length - fileBlock.length;
					fs.set(o.index, fileBlock);
					if (left > 0) {
						final var next = fs.get(o.index + 1);
						if (next.free) {
							fs.set(o.index + 1, new Block(true, next.length + left, -1));
						} else {
							fs.append(o.index, new Block(true, left, -1));
						}
					}
				});
		  });
		return fs.checksum();
	}

	static Filesystem readFilesystem(final String line) {
		return new Filesystem(
			IntStream.range(0, line.length())
					 .boxed()
					 .map(i -> {
						 final var ch = line.charAt(i);
						 final var free = i % 2 == 1;
						 return new Block(free, ch - 48, free ? -1 : i / 2);
					 })
					 .collect(toCollection(ArrayList::new))
		);
	}

	@SuppressWarnings("preview")
	@Data
	static class Filesystem {

		private final List<Block> data;

		public long checksum() {
			long sum = 0;
			long position = 0;

			for (final var block : data) {
				if (!block.free()) {
					for (var i = 0; i < block.length(); i++) {
						sum += (position + i) * block.fileId();
					}
				}
				position += block.length();
			}

			return sum;
		}

		public boolean hasGaps() {
			for (final var block : data) {
				if (block.free() && block.length() > 0) {
					return true;
				}
			}
			return false;
		}

		public Stream<Ordered> stream() {
			return IntStream
				.range(0, data.size())
				.boxed()
				.map(i -> new Ordered(i, data.get(i)));
		}

		public int getIndexOfBlock(final Block block) {
			return data.indexOf(block);
		}

		public Block get(final int index) {
			return data.get(index);
		}

		public Stream<Block> files() {
			return data.stream()
					   .filter(not(Block::free));
		}

		public Block getLast() {
			return data.getLast();
		}

		public int size() {
			return data.size();
		}

		public void set(int idx, Block block) {
			data.set(idx, block);
		}

		public void remove(int idx) {
			data.remove(idx);
		}

		public void append(int idx, Block block) {
			data.add(idx + 1, block);
		}

		record Ordered(int index, Block block) {
		}

		@Override
		public String toString() {
			return stream()
				.map(i -> {
					final var length = i.block.length;
					if (i.block.free) {
						return ".".repeat(length);
					}
					return Long.valueOf(i.block.fileId).toString().repeat(length);
				})
				.collect(joining(""));
		}

	}

	record Block(boolean free, int length, int fileId) {
	}

}

