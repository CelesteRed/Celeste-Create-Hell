package com.celestered.serverfixes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Shared logic for the water mixins. */
public final class WaterRules {
    public static final TagKey<Fluid> SPLASHING_FLUIDS =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("create", "fan_processing_catalysts/splashing"));
    public static final TagKey<Block> SPLASHING_BLOCKS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("create", "fan_processing_catalysts/splashing"));

    private static final Map<Level, Map<BlockPos, Integer>> EXPOSURE = new WeakHashMap<>();

    private WaterRules() {}

    /** Water only counts for fan washing as a real water block, not water held inside another block. */
    public static Boolean splashingOverride(Level level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.isEmpty()) return null;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof LiquidBlock) return null;
        // waterlogged blocks, kelp, seagrass, bubble columns...: only a catalyst if the block itself is one
        return state.is(SPLASHING_BLOCKS);
    }

    /** Called every 20 ticks per running fan with the positions its air current passes through. */
    public static void exposeToFan(Level level, BlockPos origin, Direction dir, int distance) {
        int lifetime = ServerFixesConfig.fanWaterLifetimeSeconds * 20;
        if (lifetime <= 0 || !(level instanceof ServerLevel server)) return;
        Map<BlockPos, Integer> exposure = EXPOSURE.computeIfAbsent(level, l -> new HashMap<>());
        for (int i = 1; i <= distance; i++) {
            BlockPos pos = origin.relative(dir, i);
            if (!level.isLoaded(pos)) break;
            BlockState state = level.getBlockState(pos);
            FluidState fluid = state.getFluidState();
            boolean liquid = state.getBlock() instanceof LiquidBlock && !fluid.isEmpty() && fluid.is(SPLASHING_FLUIDS);
            String into = liquid ? null : ServerFixesConfig.evaporatesInto.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            if (!liquid && into == null) continue;
            int step = liquid ? Math.max(20, lifetime / 8) : lifetime;
            int t = exposure.getOrDefault(pos, 0) + 20;
            if (t < step) { exposure.put(pos.immutable(), t); continue; }
            exposure.remove(pos);
            if (liquid) {
                int amount = fluid.getAmount() - 1;
                BlockState next = amount <= 0 || !(fluid.getType() instanceof FlowingFluid flowing)
                        ? Blocks.AIR.defaultBlockState()
                        : flowing.getFlowing(amount, false).createLegacyBlock();
                level.setBlockAndUpdate(pos, next);
            } else {
                Block target = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(into));
                if (target == Blocks.AIR && !"minecraft:air".equals(into)) continue;
                level.setBlockAndUpdate(pos, copyProperties(state, target.defaultBlockState()));
            }
            server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 1.8f);
            server.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 4, 0.25, 0.1, 0.25, 0.01);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState copyProperties(BlockState from, BlockState to) {
        for (Property p : from.getProperties()) {
            if (to.hasProperty(p)) to = to.setValue(p, from.getValue(p));
        }
        return to;
    }

    /** Dripstone dripped into a cauldron at {@code cauldronPos}: use up the source block feeding it. */
    public static void consumeDripSource(Level level, BlockPos cauldronPos, Fluid fluid) {
        if (!ServerFixesConfig.dripstoneConsumesSource || level.isClientSide) return;
        boolean water = fluid.isSame(Fluids.WATER);
        if (!water && !fluid.isSame(Fluids.LAVA)) return;
        // a full cauldron is 3 water drips (one bucket = one source) but 1 lava drip
        if (water && level.random.nextInt(3) != 0) return;
        BlockPos tip = PointedDripstoneBlock.findStalactiteTipAboveCauldron(level, cauldronPos);
        if (tip == null) return;
        BlockPos p = tip;
        int guard = 0;
        while (level.getBlockState(p.above()).is(Blocks.POINTED_DRIPSTONE) && guard++ < 64) p = p.above();
        BlockPos source = p.above().above();
        BlockState state = level.getBlockState(source);
        FluidState fs = state.getFluidState();
        if (fs.isEmpty() || !fs.isSource() || !fs.getType().isSame(fluid)) return;
        if (state.getBlock() instanceof LiquidBlock) {
            level.setBlockAndUpdate(source, Blocks.AIR.defaultBlockState());
        } else if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            level.setBlockAndUpdate(source, state.setValue(BlockStateProperties.WATERLOGGED, false));
        }
    }
}
