package com.appiemon.wonso.data;

import com.appiemon.wonso.WonsoMod;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WonsoData {
	private static final Gson GSON = new Gson();
	private static final List<ElementRecord> ELEMENTS = new ArrayList<>();
	private static final Map<Integer, ElementRecord> BY_Z = new LinkedHashMap<>();
	private static final Map<String, ElementRecord> BY_SYMBOL = new LinkedHashMap<>();
	private static final Map<Integer, AtomRecord> ATOMS = new LinkedHashMap<>();
	private static final List<MoleculeRecord> MOLECULES = new ArrayList<>();
	private static final Map<String, MoleculeRecord> MOLECULE_BY_ID = new LinkedHashMap<>();
	private static JsonArray SYNTHESIS = new JsonArray();
	private static JsonArray ENERGY_ABILITIES = new JsonArray();
	private static JsonArray CRUSH = new JsonArray();
	private static JsonArray DISSOLVE = new JsonArray();
	private static JsonArray CRUDE = new JsonArray();
	private static JsonArray PURIFY = new JsonArray();
	private static JsonArray QUIZ = new JsonArray();
	private static JsonArray SYNERGY = new JsonArray();

	private WonsoData() {
	}

	public static void load() {
		ELEMENTS.clear();
		BY_Z.clear();
		BY_SYMBOL.clear();
		ATOMS.clear();
		MOLECULES.clear();
		MOLECULE_BY_ID.clear();

		JsonArray elArr = readArray("elements.json");
		for (JsonElement el : elArr) {
			JsonObject o = el.getAsJsonObject();
			ElementRecord rec = new ElementRecord(
					o.get("z").getAsInt(),
					o.get("symbol").getAsString(),
					o.get("nameKo").getAsString(),
					o.get("nameEn").getAsString(),
					o.has("atomicMass") && !o.get("atomicMass").isJsonNull() ? o.get("atomicMass").getAsDouble() : null,
					o.get("period").getAsInt(),
					o.has("group") && !o.get("group").isJsonNull() ? o.get("group").getAsInt() : null,
					o.get("category").getAsString(),
					o.get("stateAtRoomTemp").getAsString(),
					o.has("electronConfiguration") && !o.get("electronConfiguration").isJsonNull()
							? o.get("electronConfiguration").getAsString() : null,
					o.has("electronegativity") && !o.get("electronegativity").isJsonNull()
							? o.get("electronegativity").getAsDouble() : null,
					o.get("displayRow").getAsInt(),
					o.get("displayColumn").getAsInt(),
					o.get("hasKnownMolecules").getAsBoolean(),
					o.has("descriptionKo") ? o.get("descriptionKo").getAsString() : "",
					stringList(o, "relatedMoleculeIds"),
					toMap(o.getAsJsonObject("card")),
					toMap(o.getAsJsonObject("battle"))
			);
			ELEMENTS.add(rec);
			BY_Z.put(rec.z(), rec);
			BY_SYMBOL.put(rec.symbol(), rec);
		}

		for (JsonElement el : readArray("atoms.json")) {
			JsonObject o = el.getAsJsonObject();
			List<Integer> shells = new ArrayList<>();
			for (JsonElement s : o.getAsJsonArray("shellDistribution")) {
				shells.add(s.getAsInt());
			}
			AtomRecord rec = new AtomRecord(
					o.get("z").getAsInt(),
					o.get("defaultProtons").getAsInt(),
					o.get("defaultNeutrons").getAsInt(),
					o.get("defaultElectrons").getAsInt(),
					shells,
					o.get("valenceElectrons").getAsInt()
			);
			ATOMS.put(rec.z(), rec);
		}

		for (JsonElement el : readArray("molecules.json")) {
			JsonObject o = el.getAsJsonObject();
			List<MoleculeRecord.Part> parts = new ArrayList<>();
			for (JsonElement p : o.getAsJsonArray("elements")) {
				JsonObject po = p.getAsJsonObject();
				parts.add(new MoleculeRecord.Part(po.get("symbol").getAsString(), po.get("count").getAsInt()));
			}
			List<Integer> related = new ArrayList<>();
			for (JsonElement z : o.getAsJsonArray("relatedElements")) {
				related.add(z.getAsInt());
			}
			MoleculeRecord rec = new MoleculeRecord(
					o.get("id").getAsString(),
					o.get("formula").getAsString(),
					o.get("formulaDisplay").getAsString(),
					o.get("nameKo").getAsString(),
					o.get("nameEn").getAsString(),
					o.get("category").getAsString(),
					o.get("descriptionKo").getAsString(),
					o.get("tier").getAsString(),
					o.get("difficulty").getAsInt(),
					o.get("energy").getAsInt(),
					parts,
					related
			);
			MOLECULES.add(rec);
			MOLECULE_BY_ID.put(rec.id(), rec);
		}

		SYNTHESIS = readArray("synthesis.json");
		ENERGY_ABILITIES = readArray("energy-abilities.json");
		CRUSH = readArray("vanilla_refining/crush_recipes.json");
		DISSOLVE = readArray("vanilla_refining/dissolve_recipes.json");
		CRUDE = readArray("vanilla_refining/crude_refine.json");
		PURIFY = readArray("vanilla_refining/purify_recipes.json");
		QUIZ = readArray("quiz/all.json");
		SYNERGY = readArray("group_synergy.json");
		WonsoMod.LOGGER.info("Loaded shared-data from {}", resolve("elements.json"));
	}

	private static List<String> stringList(JsonObject o, String key) {
		List<String> list = new ArrayList<>();
		if (!o.has(key) || !o.get(key).isJsonArray()) {
			return list;
		}
		for (JsonElement e : o.getAsJsonArray(key)) {
			list.add(e.getAsString());
		}
		return list;
	}

	private static Map<String, Object> toMap(JsonObject o) {
		Map<String, Object> map = new LinkedHashMap<>();
		if (o == null) {
			return map;
		}
		for (String k : o.keySet()) {
			JsonElement v = o.get(k);
			if (v.isJsonPrimitive()) {
				if (v.getAsJsonPrimitive().isNumber()) {
					map.put(k, v.getAsNumber());
				} else {
					map.put(k, v.getAsString());
				}
			}
		}
		return map;
	}

	private static JsonArray readArray(String name) {
		Path path = resolve(name);
		try {
			if (path != null && Files.exists(path)) {
				return GSON.fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), JsonArray.class);
			}
			try (InputStream in = WonsoData.class.getClassLoader().getResourceAsStream("data/wonso/shared/" + name)) {
				if (in == null) {
					throw new IllegalStateException("Missing shared-data: " + name);
				}
				return GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonArray.class);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to read " + name, e);
		}
	}

	private static Path resolve(String name) {
		Path dev = FabricLoader.getInstance().getGameDir().resolve("../../shared-data").resolve(name).normalize();
		if (Files.exists(dev)) {
			return dev;
		}
		Path sibling = Path.of("").toAbsolutePath().resolve("../shared-data").resolve(name).normalize();
		if (Files.exists(sibling)) {
			return sibling;
		}
		return FabricLoader.getInstance().getModContainer(WonsoMod.MOD_ID)
				.flatMap(c -> c.findPath("data/wonso/shared/" + name))
				.orElse(null);
	}

	public static List<ElementRecord> elements() {
		return ELEMENTS;
	}

	public static ElementRecord byZ(int z) {
		return BY_Z.get(z);
	}

	public static ElementRecord bySymbol(String symbol) {
		return BY_SYMBOL.get(symbol);
	}

	public static AtomRecord atom(int z) {
		return ATOMS.get(z);
	}

	public static List<MoleculeRecord> molecules() {
		return MOLECULES;
	}

	public static MoleculeRecord molecule(String id) {
		return MOLECULE_BY_ID.get(id);
	}

	public static JsonArray synthesis() {
		return SYNTHESIS;
	}

	public static JsonArray energyAbilities() {
		return ENERGY_ABILITIES;
	}

	public static JsonArray crush() {
		return CRUSH;
	}

	public static JsonArray dissolve() {
		return DISSOLVE;
	}

	public static JsonArray crude() {
		return CRUDE;
	}

	public static JsonArray purify() {
		return PURIFY;
	}

	public static JsonArray quiz() {
		return QUIZ;
	}

	public static JsonArray synergy() {
		return SYNERGY;
	}
}
