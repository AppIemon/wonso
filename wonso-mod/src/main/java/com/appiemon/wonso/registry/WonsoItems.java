package com.appiemon.wonso.registry;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.data.ElementRecord;
import com.appiemon.wonso.data.MoleculeRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.item.ChemistryJournalItem;
import com.appiemon.wonso.item.ElementCardItem;
import com.appiemon.wonso.item.GuideBookItem;
import com.appiemon.wonso.item.MoleculeCardItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class WonsoItems {
	public static final Map<String, Item> ELEMENT_CARDS = new LinkedHashMap<>();
	public static final Map<String, Item> MOLECULE_CARDS = new LinkedHashMap<>();
	public static final Map<String, Item> INTERMEDIATES = new LinkedHashMap<>();
	public static final Map<String, Item> IMPURE = new LinkedHashMap<>();

	public static Item CHEMISTRY_JOURNAL;
	public static Item GUIDE_BOOK;
	public static Item REFINING_CATALYST;
	public static Item STELLAR_CORE;
	public static Item NEUTRON_STAR_FRAGMENT;
	public static Item BLACK_HOLE_ESSENCE;

	private static final List<String> INTERMEDIATE_IDS = List.of(
			"silica_grit", "sand_dust", "stone_powder", "metal_trace", "quartz_dust",
			"iron_ore_powder", "rock_slag", "nether_quartz_dust", "murky_silicon_slurry",
			"waste_sludge", "iron_slurry", "sulfur_trace", "carbon_residue", "ash",
			"electrolyzed_hydrogen", "oxygen_bubble", "salt_brine", "slag", "toxic_waste",
			"wrong_element_chip", "calcium_phosphate", "silicon_dioxide", "sodium_chloride",
			"pure_Si", "pure_Fe", "pure_C"
	);

	private WonsoItems() {
	}

	public static void initialize() {
		CHEMISTRY_JOURNAL = register("chemistry_journal", ChemistryJournalItem::new, new Item.Properties().stacksTo(1));
		GUIDE_BOOK = register("guide_book", GuideBookItem::new, new Item.Properties().stacksTo(1));
		REFINING_CATALYST = register("refining_catalyst", Item::new, new Item.Properties());
		STELLAR_CORE = register("stellar_core", Item::new, new Item.Properties().stacksTo(16));
		NEUTRON_STAR_FRAGMENT = register("neutron_star_fragment", Item::new, new Item.Properties());
		BLACK_HOLE_ESSENCE = register("black_hole_essence", Item::new, new Item.Properties());

		for (ElementRecord element : WonsoData.elements()) {
			Item item = register(element.cardId(), props -> new ElementCardItem(props, element), new Item.Properties());
			ELEMENT_CARDS.put(element.symbol(), item);
			IMPURE.put(element.symbol(), register("impure_" + element.symbol().toLowerCase(), Item::new, new Item.Properties()));
		}
		for (MoleculeRecord molecule : WonsoData.molecules()) {
			Item item = register(molecule.cardId(), props -> new MoleculeCardItem(props, molecule), new Item.Properties());
			MOLECULE_CARDS.put(molecule.id(), item);
		}
		for (String id : INTERMEDIATE_IDS) {
			INTERMEDIATES.put(id, register(id, Item::new, new Item.Properties()));
		}

		ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), WonsoMod.id("main"));
		CreativeModeTab tab = FabricCreativeModeTab.builder()
				.icon(() -> new ItemStack(CHEMISTRY_JOURNAL))
				.title(Component.translatable("itemGroup.wonso"))
				.displayItems((params, output) -> {
					output.accept(CHEMISTRY_JOURNAL);
					output.accept(GUIDE_BOOK);
					output.accept(REFINING_CATALYST);
					ELEMENT_CARDS.values().forEach(output::accept);
					MOLECULE_CARDS.values().forEach(output::accept);
					INTERMEDIATES.values().forEach(output::accept);
				})
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, tab);
	}

	public static Item elementCard(String symbol) {
		return ELEMENT_CARDS.get(symbol);
	}

	public static Item moleculeCard(String id) {
		return MOLECULE_CARDS.get(id);
	}

	public static Item intermediate(String id) {
		return INTERMEDIATES.get(id);
	}

	public static Item impure(String symbol) {
		return IMPURE.get(symbol);
	}

	public static String symbolOfCard(ItemStack stack) {
		if (stack.getItem() instanceof ElementCardItem card) {
			return card.element().symbol();
		}
		return null;
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WonsoMod.id(name));
		Item item = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
