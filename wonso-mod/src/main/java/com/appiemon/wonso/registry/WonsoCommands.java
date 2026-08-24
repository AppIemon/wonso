package com.appiemon.wonso.registry;

import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.gameplay.BattleManager;
import com.appiemon.wonso.gameplay.DiscoveryManager;
import com.appiemon.wonso.gameplay.ElementEnergyManager;
import com.appiemon.wonso.gameplay.FusionManager;
import com.appiemon.wonso.gameplay.RefiningManager;
import com.appiemon.wonso.gameplay.StarManager;
import com.appiemon.wonso.gameplay.SynthesisRecipeManager;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class WonsoCommands {
	private WonsoCommands() {
	}

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("wonso")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
						.then(Commands.literal("refine")
								.then(Commands.argument("action", StringArgumentType.word())
										.executes(ctx -> {
											boolean ok = RefiningManager.handle(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "action"));
											return ok ? 1 : 0;
										})))
						.then(Commands.literal("fusion")
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(ctx -> FusionManager.fuse(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")) ? 1 : 0)))
						.then(Commands.literal("star")
								.then(Commands.argument("id", StringArgumentType.word())
										.executes(ctx -> StarManager.ignite(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")) ? 1 : 0)))
						.then(Commands.literal("battle")
								.executes(ctx -> BattleManager.startShrine(ctx.getSource().getPlayerOrException()) ? 1 : 0)
								.then(Commands.argument("command", StringArgumentType.greedyString())
										.executes(ctx -> BattleManager.handle(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "command")) ? 1 : 0)))
						.then(Commands.literal("party")
								.then(Commands.literal("clear")
										.executes(ctx -> {
											BattleManager.setParty(ctx.getSource().getPlayerOrException(), List.of());
											return 1;
										}))
								.then(Commands.literal("add")
										.then(Commands.argument("z", IntegerArgumentType.integer(1, 118))
												.executes(ctx -> {
													ServerPlayer player = ctx.getSource().getPlayerOrException();
													List<Integer> zs = new ArrayList<>(ElementEnergyManager.state(player).party);
													int z = IntegerArgumentType.getInteger(ctx, "z");
													if (!zs.contains(z) && zs.size() < 6) {
														zs.add(z);
													}
													BattleManager.setParty(player, zs);
													return 1;
												})))
								.executes(ctx -> {
									ServerPlayer player = ctx.getSource().getPlayerOrException();
									ctx.getSource().sendSuccess(() -> Component.literal("party=" + ElementEnergyManager.state(player).party), false);
									return 1;
								}))
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
