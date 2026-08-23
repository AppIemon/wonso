package com.appiemon.wonso.world;

/**
 * Biome element weighting from MINECRAFT_MOD_SPEC §5.2 / VANILLA_REFINING_SPEC §6.
 */
public final class BiomeElementSpawns {
	private BiomeElementSpawns() {
	}

	public static String hintForBiome(String biomePath) {
		if (biomePath.contains("desert")) {
			return "Na, Mg, Si";
		}
		if (biomePath.contains("ocean") || biomePath.contains("beach")) {
			return "Cl, Br, H, O";
		}
		if (biomePath.contains("jungle")) {
			return "C, N, O";
		}
		if (biomePath.contains("nether")) {
			return "S, Fe, Si";
		}
		if (biomePath.contains("end")) {
			return "Xe, rare";
		}
		return "Si, Fe, O";
	}
}
