package com.codex.minecartboost.mixin;

import com.codex.minecartboost.MinecartBoostRules;
import com.codex.minecartboost.MinecartRailBoostConfig;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperimentalMinecartController.class)
public abstract class ExperimentalMinecartControllerMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void minecartRailBoost$boostExperimentalSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) (Object) this).minecart();
        MinecartBoostRules.speedLimitFor(minecart, world).ifPresent(configuredLimit -> {
            if (configuredLimit > cir.getReturnValue()) {
                cir.setReturnValue(configuredLimit);
            }
        });
    }
}
