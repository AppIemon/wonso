package com.appiemon.wonso.world;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.registry.WonsoBlocks;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Periodite / nether salt / end crystal ore — documented in MINECRAFT_MOD_SPEC §5.
 * Placement JSON lives under data/wonso/worldgen when the 26.2 feature codec is wired;
 * this registers the biome injection keys.
 */
public final class PerioditeOreFeature {
	private PerioditeOreFeature() {
	}

	public static void initialize() {
		if (WonsoBlocks.PERIODITE_ORE == null) {
			WonsoMod.LOGGER.warn("Periodite ore missing, skip biome injection");
			return;
		}
		try {
			BiomeModifications.addFeature(
					BiomeSelectors.foundInOverworld(),
					GenerationStep.Decoration.UNDERGROUND_ORES,
					ResourceKey.create(Registries.PLACED_FEATURE, WonsoMod.id("periodite_ore"))
			);
		} catch (Exception e) {
			WonsoMod.LOGGER.warn("Worldgen injection skipped: {}", e.toString());
		}
	}
}
