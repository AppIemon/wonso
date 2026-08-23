package com.appiemon.wonso.world;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.registry.WonsoBlocks;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;

import java.util.function.Predicate;

/**
 * Periodite / nether salt / end crystal ore — MINECRAFT_MOD_SPEC §5.
 * Configured + placed features live under data/wonso/worldgen.
 */
public final class PerioditeOreFeature {
	private PerioditeOreFeature() {
	}

	public static void initialize() {
		inject("periodite_ore", BiomeSelectors.foundInOverworld(), WonsoBlocks.PERIODITE_ORE != null);
		inject("deep_periodite", BiomeSelectors.foundInOverworld(), WonsoBlocks.DEEP_PERIODITE != null);
		inject("nether_salt", BiomeSelectors.foundInTheNether(), WonsoBlocks.NETHER_SALT != null);
		inject("end_crystal_ore", BiomeSelectors.foundInTheEnd(), WonsoBlocks.END_CRYSTAL_ORE != null);
	}

	private static void inject(String path, Predicate<BiomeSelectionContext> selector, boolean present) {
		if (!present) {
			WonsoMod.LOGGER.warn("{} missing, skip biome injection", path);
			return;
		}
		try {
			BiomeModifications.addFeature(
					selector,
					GenerationStep.Decoration.UNDERGROUND_ORES,
					ResourceKey.create(Registries.PLACED_FEATURE, WonsoMod.id(path))
			);
		} catch (Exception e) {
			WonsoMod.LOGGER.warn("Worldgen injection skipped for {}: {}", path, e.toString());
		}
	}
}
