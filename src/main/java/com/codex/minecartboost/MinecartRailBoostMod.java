package com.codex.minecartboost;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecartRailBoostMod implements ModInitializer {
    public static final String MOD_ID = "minecart_rail_boost";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MinecartRailBoostConfig.load();
        LOGGER.info("Minecart Rail Boost loaded.");
    }
}
