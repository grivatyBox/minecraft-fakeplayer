package net.minecraft.gametest.framework;

import java.util.stream.Stream;
import net.minecraft.core.BlockPosition;

@FunctionalInterface
public interface TestPosFinder {

    Stream<BlockPosition> findTestPos();
}
