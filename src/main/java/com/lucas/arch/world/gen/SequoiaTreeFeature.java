package com.lucas.arch.world.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SequoiaTreeFeature {

    public static boolean generate(ServerLevel level, BlockPos pos, RandomSource random) {
        int height = 52 + random.nextInt(16);

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        BlockState logState = Blocks.OAK_LOG.defaultBlockState();
        BlockState leavesState = Blocks.OAK_LEAVES.defaultBlockState();

        int spireHeight = 8; 
        int trunk3x3Height = height - spireHeight;

        for (int y = 0; y < height; y++) {
            if (y < 4) {
                drawCross5x5(level, pos.above(y), logState);
            } 
            else if (y < trunk3x3Height) {
                drawTrunk3x3(level, pos.above(y), logState);
            } 
            else {
                level.setBlock(pos.above(y), logState, 3);
            }

            float progress = (float) y / height;
            if (progress > 0.35f && y < height - 3) {
                float crownFactor = 1.0f - ((progress - 0.35f) / 0.65f); 
                
                if (random.nextFloat() < 0.40f) {
                    Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    int startOffset = (y >= trunk3x3Height) ? 1 : 2;
                    int branchLength = Math.round(4 + (6 * crownFactor));

                    generateBranch(level, pos.above(y), dir, startOffset, branchLength, random, logState, leavesState);
                }
            }
        }

        generateRoots(level, pos, logState, random);

        for (int topY = trunk3x3Height - 2; topY <= height + 2; topY++) {
            int radius = Math.max(1, 4 - ((topY - trunk3x3Height) / 2));
            generateFoliagePad(level, pos.above(topY), radius, 1, leavesState);
        }

        return true;
    }

    private static void drawCross5x5(ServerLevel level, BlockPos center, BlockState state) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 && Math.abs(z) == 2) continue; 
                level.setBlock(center.offset(x, 0, z), state, 3);
            }
        }
    }

    private static void drawTrunk3x3(ServerLevel level, BlockPos center, BlockState state) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(center.offset(x, 0, z), state, 3);
            }
        }
    }

    private static void generateRoots(ServerLevel level, BlockPos centerPos, BlockState logState, RandomSource random) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int rootLength = 4 + random.nextInt(2);
            int rootHeight = 4 + random.nextInt(2);

            BlockPos rootStart = centerPos.relative(dir, 2);

            for (int h = 0; h < rootHeight; h++) {
                int dist = rootLength - h;
                for (int d = 0; d < dist; d++) {
                    BlockPos p = rootStart.relative(dir, d).above(h);
                    level.setBlock(p, logState, 3);
                    if (h == 0) {
                        level.setBlock(p.below(), logState, 3);
                    }
                }
            }
        }
    }

    private static void generateBranch(ServerLevel level, BlockPos centerAtY, Direction dir, int startOffset, int length, RandomSource random, BlockState baseLogState, BlockState leavesState) {
        BlockPos current = centerAtY.relative(dir, startOffset);

        BlockState branchLogState = baseLogState.hasProperty(RotatedPillarBlock.AXIS)
            ? baseLogState.setValue(RotatedPillarBlock.AXIS, dir.getAxis())
            : baseLogState;

        for (int i = 0; i < length; i++) {
            level.setBlock(current, branchLogState, 3);

            if (i % 2 == 1) {
                current = current.above();
            }

            if (i > 1 && i % 2 == 0) {
                generateFoliagePad(level, current, 2, 1, leavesState);
            }

            current = current.relative(dir);
        }

        generateFoliagePad(level, current, 3, 1, leavesState);
    }

    private static void generateFoliagePad(ServerLevel level, BlockPos center, int radiusXZ, int radiusY, BlockState leavesState) {
        for (int x = -radiusXZ; x <= radiusXZ; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusXZ; z <= radiusXZ; z++) {
                    double normX = (double) x / radiusXZ;
                    double normY = (double) y / radiusY;
                    double normZ = (double) z / radiusXZ;

                    if ((normX * normX + normY * normY + normZ * normZ) <= 1.25) {
                        BlockPos leafPos = center.offset(x, y, z);
                        if (level.getBlockState(leafPos).isAir()) {
                            level.setBlock(leafPos, leavesState, 3);
                        }
                    }
                }
            }
        }
    }
}