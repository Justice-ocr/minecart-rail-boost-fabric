package com.codex.minecartboost;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public final class MinecartBoostRules {
    private MinecartBoostRules() {
    }

    public static boolean shouldBoost(AbstractMinecartEntity minecart, ServerWorld world) {
        if (!hasRealPlayerPassenger(minecart)) {
            return false;
        }

        BlockPos supportPos = minecart.getBlockPos().down();
        BlockState supportState = world.getBlockState(supportPos);
        return MinecartRailBoostConfig.get().boostBlocks().contains(supportState.getBlock());
    }

    public static boolean hasRealPlayerPassenger(AbstractMinecartEntity minecart) {
        for (Entity passenger : minecart.getPassengerList()) {
            if (passenger instanceof ServerPlayerEntity player && isRealPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRealPlayer(ServerPlayerEntity player) {
        String className = player.getClass().getName().toLowerCase(Locale.ROOT);
        String simpleName = player.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        if (className.contains("fake") || simpleName.contains("fake")) {
            return false;
        }
        return player.getServer() != null && !player.isRemoved();
    }
}
