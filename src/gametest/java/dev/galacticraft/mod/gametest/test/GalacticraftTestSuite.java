/*
 * Copyright (c) 2019-2021 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.gametest.test;

import dev.galacticraft.mod.block.GalacticraftBlock;
import dev.galacticraft.mod.block.decoration.GratingBlock;
import net.minecraft.block.Blocks;
import net.minecraft.tag.FluidTags;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

/**
 * Miscellaneous tests.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class GalacticraftTestSuite implements GalacticraftGameTest {
	@GameTest(structureName = EMPTY_STRUCTURE)
	public void gratingWaterFlowTest(TestContext context) {
        final var pos4 = new BlockPos(0, 4, 0);
        final var pos3 = new BlockPos(0, 3, 0);
        final var pos2 = new BlockPos(0, 2, 0);
        final var pos1 = new BlockPos(0, 1, 0);
        final var mutable = new BlockPos.Mutable();
        context.setBlockState(pos2, GalacticraftBlock.GRATING.getDefaultState().with(GratingBlock.GRATING_STATE, GratingBlock.GratingState.LOWER));
        if (!context.getBlockState(pos2).getFluidState().isEmpty()) {
            context.throwPositionedException(String.format("Expected grating to not be filled with fluid but found %s instead!", Registry.FLUID.getId(context.getBlockState(pos2).getFluidState().getFluid())), pos2);
        } else {
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    if (mutable.set(x, 4, z).equals(pos4)) {
                        continue;
                    }
                    context.setBlockState(mutable, Blocks.GLASS);
                }
            }
            context.setBlockState(pos4, Blocks.WATER);
            context.runAtTick(context.getTick() + 40L, () -> {
                if (!context.getBlockState(pos3).getFluidState().isIn(FluidTags.WATER)) {
                    context.throwPositionedException(String.format("Expected water to flow downward but found %s instead!", Registry.FLUID.getId(context.getBlockState(pos3).getFluidState().getFluid())), pos3);
                } else if (!context.getBlockState(pos2).getFluidState().isIn(FluidTags.WATER)) {
                    context.throwPositionedException(String.format("Expected grating to be filled with water but found %s instead!", Registry.FLUID.getId(context.getBlockState(pos2).getFluidState().getFluid())), pos2);
                } else if (!context.getBlockState(pos1).getFluidState().isIn(FluidTags.WATER)) {
                    context.throwPositionedException(String.format("Expected water to be found below grating but found %s instead!", Registry.FLUID.getId(context.getBlockState(pos1).getFluidState().getFluid())), pos1);
                } else {
                    context.setBlockState(pos4, Blocks.AIR);
                    context.runAtTick(context.getTick() + 50L, () -> context.addInstantFinalTask(() -> {
                        if (!context.getBlockState(pos3).getFluidState().isEmpty()) {
                            context.throwPositionedException(String.format("Expected water to drain itself but found %s instead!", Registry.FLUID.getId(context.getBlockState(pos3).getFluidState().getFluid())), pos3);
                        } else if (!context.getBlockState(pos2).getFluidState().isEmpty()) {
                            context.throwPositionedException(String.format("Expected grating to not be filled with fluid but found %s instead!", Registry.FLUID.getId(context.getBlockState(pos2).getFluidState().getFluid())), pos2);
                        } else if (!context.getBlockState(pos1).getFluidState().isEmpty()) {
                            context.throwPositionedException(String.format("Expected no fluid to be found below grating but found %s instead!", Registry.FLUID.getId(context.getBlockState(pos1).getFluidState().getFluid())), pos1);
                        }
                    }));
                }
            });
        }
    }
}