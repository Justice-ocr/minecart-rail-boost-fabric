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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
                "Loaded minecart boost config: boostedMaxSpeed={}, boostBlocks={}",
                loaded.boostedMaxSpeed(),
                loaded.boostBlocks().size()
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

    public record RuntimeConfig(double boostedMaxSpeed, Set<Block> boostBlocks) {
        public static RuntimeConfig defaults() {
            return new RuntimeConfig(1.0D, Set.of(Blocks.GOLD_BLOCK));
        }
    }

    private record FileConfig(
            @SerializedName("boostedMaxSpeed") double boostedMaxSpeed,
            @SerializedName("boostBlocks") List<String> boostBlocks
    ) {
        private static FileConfig fromRuntime(RuntimeConfig config) {
            return new FileConfig(
                    config.boostedMaxSpeed(),
                    config.boostBlocks().stream()
                            .map(Registries.BLOCK::getId)
                            .map(Identifier::toString)
                            .toList()
            );
        }

        private RuntimeConfig toRuntime() {
            Set<Block> blocks = new LinkedHashSet<>();
            if (boostBlocks != null) {
                for (String rawId : boostBlocks) {
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
                    blocks.add(block);
                }
            }

            double speed = boostedMaxSpeed > 0.0D ? boostedMaxSpeed : RuntimeConfig.defaults().boostedMaxSpeed();
            if (blocks.isEmpty()) {
                blocks = RuntimeConfig.defaults().boostBlocks();
            }
            return new RuntimeConfig(speed, Set.copyOf(blocks));
        }
    }
}
