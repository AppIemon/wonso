package com.appiemon.wonso.client;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.block.MachineBlock;
import com.appiemon.wonso.item.ChemistryJournalItem;
import com.appiemon.wonso.item.GuideBookItem;
import com.appiemon.wonso.network.WonsoPackets;
import com.appiemon.wonso.registry.WonsoItems;
import com.appiemon.wonso.screen.BattleScreen;
import com.appiemon.wonso.screen.EnergyPanelScreen;
import com.appiemon.wonso.screen.FusionScreen;
import com.appiemon.wonso.screen.PartyScreen;
import com.appiemon.wonso.screen.PeriodicTableScreen;
import com.appiemon.wonso.screen.RefineScreen;
import com.appiemon.wonso.screen.SynthesisLabScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WonsoClient implements ClientModInitializer {
	public static float clientEnergy;
	public static int discoveredElements;
	public static int discoveredMolecules;
	public static String mode = "element";
	public static int selectedZ = 1;
	public static final List<Integer> party = new ArrayList<>();
	public static String battleJson = "";

	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(WonsoMod.id("keys"));

	private static final Set<String> ORES = Set.of("periodite_ore", "deep_periodite", "nether_salt", "end_crystal_ore");

	@Override
	public void onInitializeClient() {
		WonsoHudOverlay.initialize();

		KeyMapping openTable = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wonso.periodic", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, CATEGORY));
		KeyMapping skillHeal = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wonso.skill_heal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY));
		KeyMapping skillAtk = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wonso.skill_atk", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY));
		KeyMapping skillEnergy = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wonso.skill_energy", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));
		KeyMapping skillDraw = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wonso.skill_draw", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openTable.consumeClick()) {
				openPeriodic(client, "periodic");
			}
			while (skillHeal.consumeClick()) {
				ClientPlayNetworking.send(new WonsoPackets.SkillC2S("heal_all"));
			}
			while (skillAtk.consumeClick()) {
				ClientPlayNetworking.send(new WonsoPackets.SkillC2S("attack_up"));
			}
			while (skillEnergy.consumeClick()) {
				ClientPlayNetworking.send(new WonsoPackets.SkillC2S("energy_up"));
			}
			while (skillDraw.consumeClick()) {
				ClientPlayNetworking.send(new WonsoPackets.SkillC2S("draw_card"));
			}
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!world.isClientSide()) {
				return InteractionResult.PASS;
			}
			var stack = player.getItemInHand(hand);
			if (stack.getItem() instanceof ChemistryJournalItem || stack.is(WonsoItems.CHEMISTRY_JOURNAL)) {
				openPeriodic(Minecraft.getInstance(), "journal");
				return InteractionResult.SUCCESS;
			}
			if (stack.getItem() instanceof GuideBookItem) {
				Minecraft.getInstance().gui.setScreen(new EnergyPanelScreen());
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (!world.isClientSide()) {
				return InteractionResult.PASS;
			}
			var state = world.getBlockState(hit.getBlockPos());
			if (!(state.getBlock() instanceof MachineBlock machine)) {
				return InteractionResult.PASS;
			}
			openMachine(machine.machineId());
			return InteractionResult.SUCCESS;
		});

		ClientPlayNetworking.registerGlobalReceiver(WonsoPackets.StateS2C.TYPE, (payload, context) -> {
			clientEnergy = payload.energy();
			discoveredElements = payload.elements();
			discoveredMolecules = payload.molecules();
			party.clear();
			if (payload.party() != null && !payload.party().isBlank()) {
				for (String part : payload.party().split(",")) {
					try {
						party.add(Integer.parseInt(part.trim()));
					} catch (NumberFormatException ignored) {
					}
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(WonsoPackets.BattleStateS2C.TYPE, (payload, context) -> {
			battleJson = payload.json();
			Minecraft client = Minecraft.getInstance();
			if (!(client.gui.screen() instanceof BattleScreen)) {
				client.gui.setScreen(new BattleScreen());
			}
		});
	}

	public static void openPeriodic(Minecraft client, String reason) {
		ClientPlayNetworking.send(new WonsoPackets.OpenUiC2S(reason));
		client.gui.setScreen(new PeriodicTableScreen());
	}

	public static void openMachine(String machineId) {
		Minecraft client = Minecraft.getInstance();
		if (ORES.contains(machineId) || "waste_bin".equals(machineId) || "slag_bin".equals(machineId)) {
			return;
		}
		switch (machineId) {
			case "synthesis_lab" -> client.gui.setScreen(new SynthesisLabScreen());
			case "energy_converter" -> client.gui.setScreen(new EnergyPanelScreen());
			case "fusion_chamber" -> client.gui.setScreen(new FusionScreen());
			case "element_shrine" -> {
				ClientPlayNetworking.send(new WonsoPackets.OpenUiC2S("shrine"));
				client.gui.setScreen(new BattleScreen());
			}
			case "research_table" -> client.gui.setScreen(new PartyScreen());
			default -> client.gui.setScreen(new RefineScreen(machineId));
		}
	}
}
