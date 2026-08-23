package com.appiemon.wonso.client;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.item.ChemistryJournalItem;
import com.appiemon.wonso.item.GuideBookItem;
import com.appiemon.wonso.network.WonsoPackets;
import com.appiemon.wonso.registry.WonsoItems;
import com.appiemon.wonso.screen.EnergyPanelScreen;
import com.appiemon.wonso.screen.PeriodicTableScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class WonsoClient implements ClientModInitializer {
	public static float clientEnergy;
	public static int discoveredElements;
	public static int discoveredMolecules;
	public static String mode = "element";
	public static int selectedZ = 1;

	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(WonsoMod.id("keys"));

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

		ClientPlayNetworking.registerGlobalReceiver(WonsoPackets.StateS2C.TYPE, (payload, context) -> {
			clientEnergy = payload.energy();
			discoveredElements = payload.elements();
			discoveredMolecules = payload.molecules();
		});
	}

	public static void openPeriodic(Minecraft client, String reason) {
		ClientPlayNetworking.send(new WonsoPackets.OpenUiC2S(reason));
		client.gui.setScreen(new PeriodicTableScreen());
	}
}
