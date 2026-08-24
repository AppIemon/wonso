package com.appiemon.wonso.registry;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.block.ElementRefineryBlock;
import com.appiemon.wonso.block.MachineBlock;
import com.appiemon.wonso.block.SynthesisLabBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class WonsoBlocks {
	public static final Map<String, Block> ALL = new LinkedHashMap<>();

	public static Block SYNTHESIS_LAB;
	public static Block ELEMENT_REFINERY;
	public static Block ENERGY_CONVERTER;
	public static Block PERIODITE_ORE;
	public static Block DEEP_PERIODITE;
	public static Block NETHER_SALT;
	public static Block END_CRYSTAL_ORE;
	public static Block FUSION_CHAMBER;
	public static Block ELEMENT_SHRINE;
	public static Block RESEARCH_TABLE;
	public static Block CRUSHER;
	public static Block REACTION_VAT;
	public static Block EVAPORATION_PAN;
	public static Block ELECTROLYZER;
	public static Block DISTILLER;
	public static Block REDUCTION_FURNACE;
	public static Block WASTE_BIN;
	public static Block SLAG_BIN;

	private WonsoBlocks() {
	}

	public static void initialize() {
		SYNTHESIS_LAB = register("synthesis_lab", SynthesisLabBlock::new, machine());
		ELEMENT_REFINERY = register("element_refinery", ElementRefineryBlock::new, machine());
		ENERGY_CONVERTER = register("energy_converter", p -> new MachineBlock(p, "energy_converter"), machine());
		PERIODITE_ORE = register("periodite_ore", p -> new MachineBlock(p, "periodite_ore"),
				BlockBehaviour.Properties.of().strength(3.0f, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());
		DEEP_PERIODITE = register("deep_periodite", p -> new MachineBlock(p, "deep_periodite"),
				BlockBehaviour.Properties.of().strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());
		NETHER_SALT = register("nether_salt", p -> new MachineBlock(p, "nether_salt"),
				BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.NETHERRACK));
		END_CRYSTAL_ORE = register("end_crystal_ore", p -> new MachineBlock(p, "end_crystal_ore"),
				BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.STONE));
		FUSION_CHAMBER = register("fusion_chamber", p -> new MachineBlock(p, "fusion_chamber"), machine());
		ELEMENT_SHRINE = register("element_shrine", p -> new MachineBlock(p, "element_shrine"), machine());
		RESEARCH_TABLE = register("research_table", p -> new MachineBlock(p, "research_table"), machine());
		CRUSHER = register("crusher", p -> new MachineBlock(p, "crusher"), machine());
		REACTION_VAT = register("reaction_vat", p -> new MachineBlock(p, "reaction_vat"), machine());
		EVAPORATION_PAN = register("evaporation_pan", p -> new MachineBlock(p, "evaporation_pan"), machine());
		ELECTROLYZER = register("electrolyzer", p -> new MachineBlock(p, "electrolyzer"), machine());
		DISTILLER = register("distiller", p -> new MachineBlock(p, "distiller"), machine());
		REDUCTION_FURNACE = register("reduction_furnace", p -> new MachineBlock(p, "reduction_furnace"), machine());
		WASTE_BIN = register("waste_bin", p -> new MachineBlock(p, "waste_bin"), machine());
		SLAG_BIN = register("slag_bin", p -> new MachineBlock(p, "slag_bin"), machine());

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> {
			for (Block block : ALL.values()) {
				tab.accept(block.asItem());
			}
		});
	}

	private static BlockBehaviour.Properties machine() {
		return BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.METAL);
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		Identifier id = WonsoMod.id(name);
		BlockItemId keys = BlockItemId.create(id, id);
		Block block = factory.apply(properties.setId(keys.block()));
		Registry.register(BuiltInRegistries.BLOCK, keys.block(), block);
		BlockItem item = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(keys.item()));
		Registry.register(BuiltInRegistries.ITEM, keys.item(), item);
		ALL.put(name, block);
		return block;
	}
}
