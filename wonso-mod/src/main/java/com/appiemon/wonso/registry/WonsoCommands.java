package com.appiemon.wonso.registry;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.gameplay.DiscoveryManager;
import com.appiemon.wonso.gameplay.ElementEnergyManager;
import com.appiemon.wonso.gameplay.SynthesisRecipeManager;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WonsoCommands {
	private WonsoCommands() {
	}

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("wonso")
						.then(Commands.literal("card")
								.then(Commands.argument("symbol", StringArgumentType.word())
										.executes(ctx -> {
											ServerPlayer player = ctx.getSource().getPlayerOrException();
											String symbol = StringArgumentType.getString(ctx, "symbol");
											var item = WonsoItems.elementCard(symbol);
											if (item == null) {
												ctx.getSource().sendFailure(Component.literal("Unknown element " + symbol));
												return 0;
											}
											player.addItem(new ItemStack(item));
											DiscoveryManager.discoverElement(player, symbol);
											return 1;
										})))
						.then(Commands.literal("energy")
								.then(Commands.argument("amount", FloatArgumentType.floatArg())
										.executes(ctx -> {
											ServerPlayer player = ctx.getSource().getPlayerOrException();
											ElementEnergyManager.addEnergy(player, FloatArgumentType.getFloat(ctx, "amount"));
											ctx.getSource().sendSuccess(() -> Component.literal("⚡ " + ElementEnergyManager.energy(player)), false);
											return 1;
										})))
						.then(Commands.literal("skill")
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(ctx -> {
											ElementEnergyManager.useSkill(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id"));
											return 1;
										})))
						.then(Commands.literal("synth")
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(ctx -> {
											SynthesisRecipeManager.tryCraft(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id"));
											return 1;
										})))
						.then(Commands.literal("journal")
								.executes(ctx -> {
									ctx.getSource().getPlayerOrException().addItem(new ItemStack(WonsoItems.CHEMISTRY_JOURNAL));
									return 1;
								}))
						.then(Commands.literal("info")
								.executes(ctx -> {
									ctx.getSource().sendSuccess(() -> Component.literal(
											"wonso elements=" + WonsoData.elements().size()
													+ " molecules=" + WonsoData.molecules().size()
													+ " ⚡=" + (ctx.getSource().getPlayer() == null ? 0 : ElementEnergyManager.energy(ctx.getSource().getPlayer()))
									), false);
									return 1;
								}))
		));
	}
}
