package com.codex.minecartboost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public final class MinecartRailBoostConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("minecart-rail-boost.json");
    private static volatile RuntimeConfig cached = RuntimeConfig.defaults();

    private MinecartRailBoostConfig() {
    }

    public static RuntimeConfig get() {
        return cached;
    }

    public static synchronized RuntimeConfig load() {
        RuntimeConfig loaded = readOrCreate();
        cached = loaded;
        MinecartRailBoostMod.LOGGER.info(
                "Loaded minecart boost config: defaultMaxSpeed={}, blockSpeeds={}",
                loaded.defaultMaxSpeed(),
                loaded.blockSpeeds().size()
        );
        return loaded;
    }

    private static RuntimeConfig readOrCreate() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                RuntimeConfig defaults = RuntimeConfig.defaults();
                write(defaults);
                return defaults;
            }

            String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
            FileConfig fileConfig = GSON.fromJson(json, FileConfig.class);
            if (fileConfig == null) {
                return RuntimeConfig.defaults();
            }
            return fileConfig.toRuntime();
        } catch (Exception e) {
            MinecartRailBoostMod.LOGGER.warn("Failed to load config, using defaults.", e);
            return RuntimeConfig.defaults();
        }
    }

    private static void write(RuntimeConfig config) {
        FileConfig fileConfig = FileConfig.fromRuntime(config);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(fileConfig), StandardCharsets.UTF_8);
        } catch (IOException e) {
            MinecartRailBoostMod.LOGGER.warn("Failed to write default config.", e);
        }
    }

    public record RuntimeConfig(double defaultMaxSpeed, Map<Block, Double> blockSpeeds) {
        public static RuntimeConfig defaults() {
            return new RuntimeConfig(0.4D, Map.of(Blocks.GOLD_BLOCK, 1.0D));
        }

        public double speedFor(Block block) {
            return blockSpeeds.getOrDefault(block, defaultMaxSpeed);
        }
    }

    private record FileConfig(
            @SerializedName("defaultMaxSpeed") Double defaultMaxSpeed,
            @SerializedName("blockSpeeds") Map<String, Double> blockSpeeds,
            @SerializedName("boostedMaxSpeed") Double legacyBoostedMaxSpeed,
            @SerializedName("boostBlocks") List<String> legacyBoostBlocks
    ) {
        private static FileConfig fromRuntime(RuntimeConfig config) {
            Map<String, Double> blockSpeeds = new LinkedHashMap<>();
            config.blockSpeeds().forEach((block, speed) -> blockSpeeds.put(Registries.BLOCK.getId(block).toString(), speed));
            return new FileConfig(config.defaultMaxSpeed(), blockSpeeds, null, null);
        }

        private RuntimeConfig toRuntime() {
            double parsedDefaultMaxSpeed = defaultMaxSpeed != null
                    ? defaultMaxSpeed
                    : (legacyBoostedMaxSpeed != null ? legacyBoostedMaxSpeed : RuntimeConfig.defaults().defaultMaxSpeed());
            if (parsedDefaultMaxSpeed <= 0.0D) {
                parsedDefaultMaxSpeed = RuntimeConfig.defaults().defaultMaxSpeed();
            }

            Map<Block, Double> parsedBlockSpeeds = new LinkedHashMap<>();
            if (blockSpeeds != null) {
                for (Map.Entry<String, Double> entry : blockSpeeds.entrySet()) {
                    String rawId = entry.getKey();
                    Double rawSpeed = entry.getValue();
                    if (rawSpeed == null || rawSpeed <= 0.0D) {
                        MinecartRailBoostMod.LOGGER.warn("Ignoring non-positive speed for block {}: {}", rawId, rawSpeed);
                        continue;
                    }

                    Identifier id = Identifier.tryParse(rawId);
                    if (id == null) {
                        MinecartRailBoostMod.LOGGER.warn("Ignoring invalid block id in config: {}", rawId);
                        continue;
                    }

                    Block block = Registries.BLOCK.get(id);
                    if (!Registries.BLOCK.getId(block).equals(id)) {
                        MinecartRailBoostMod.LOGGER.warn("Ignoring unknown block id in config: {}", rawId);
                        continue;
                    }
                    parsedBlockSpeeds.put(block, rawSpeed);
                }
            } else if (legacyBoostBlocks != null) {
                for (String rawId : legacyBoostBlocks) {
                    Identifier id = Identifier.tryParse(rawId);
                    if (id == null) {
                        MinecartRailBoostMod.LOGGER.warn("Ignoring invalid block id in config: {}", rawId);
                        continue;
                    }

                    Block block = Registries.BLOCK.get(id);
                    if (!Registries.BLOCK.getId(block).equals(id)) {
                        MinecartRailBoostMod.LOGGER.warn("Ignoring unknown block id in config: {}", rawId);
                        continue;
                    }
                    parsedBlockSpeeds.put(block, legacyBoostedMaxSpeed != null && legacyBoostedMaxSpeed > 0.0D
                            ? legacyBoostedMaxSpeed
                            : RuntimeConfig.defaults().blockSpeeds().getOrDefault(block, 1.0D));
                }
            }

            if (parsedBlockSpeeds.isEmpty()) {
                parsedBlockSpeeds = new LinkedHashMap<>(RuntimeConfig.defaults().blockSpeeds());
            }
            return new RuntimeConfig(parsedDefaultMaxSpeed, Map.copyOf(parsedBlockSpeeds));
        }
    }
}
