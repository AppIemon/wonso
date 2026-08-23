package com.appiemon.wonso.network;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.data.WonsoPlayerState;
import com.appiemon.wonso.gameplay.BattleManager;
import com.appiemon.wonso.gameplay.DiscoveryManager;
import com.appiemon.wonso.gameplay.ElementEnergyManager;
import com.appiemon.wonso.gameplay.FusionManager;
import com.appiemon.wonso.gameplay.GroupSynergyHandler;
import com.appiemon.wonso.gameplay.RefiningManager;
import com.appiemon.wonso.gameplay.StarManager;
import com.appiemon.wonso.gameplay.SynthesisRecipeManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class WonsoPackets {
	private WonsoPackets() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(OpenUiC2S.TYPE, OpenUiC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SynthC2S.TYPE, SynthC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SkillC2S.TYPE, SkillC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RefineC2S.TYPE, RefineC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(BattleCommandC2S.TYPE, BattleCommandC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PartyC2S.TYPE, PartyC2S.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(StateS2C.TYPE, StateS2C.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(BattleStateS2C.TYPE, BattleStateS2C.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(OpenUiC2S.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			if ("periodic".equals(payload.ui()) || "journal".equals(payload.ui())) {
				DiscoveryManager.markTableOpened(player);
			}
			if ("atom".equals(payload.ui())) {
				DiscoveryManager.markAtomViewed(player);
			}
			if ("shrine".equals(payload.ui())) {
				BattleManager.startShrine(player);
			}
			syncState(player);
		});
		ServerPlayNetworking.registerGlobalReceiver(SynthC2S.TYPE, (payload, context) -> {
			SynthesisRecipeManager.tryCraft(context.player(), payload.moleculeId());
			GroupSynergyHandler.check(context.player());
			syncState(context.player());
		});
		ServerPlayNetworking.registerGlobalReceiver(SkillC2S.TYPE, (payload, context) -> {
			ElementEnergyManager.useSkill(context.player(), payload.skillId());
		});
		ServerPlayNetworking.registerGlobalReceiver(RefineC2S.TYPE, (payload, context) -> {
			String action = payload.action();
			if (action.startsWith("fusion:")) {
				FusionManager.fuse(context.player(), action.substring("fusion:".length()));
			} else if (action.startsWith("star:")) {
				StarManager.ignite(context.player(), action.substring("star:".length()));
			} else {
				RefiningManager.handle(context.player(), action);
			}
			syncState(context.player());
		});
		ServerPlayNetworking.registerGlobalReceiver(BattleCommandC2S.TYPE, (payload, context) -> {
			BattleManager.handle(context.player(), payload.command());
			syncState(context.player());
		});
		ServerPlayNetworking.registerGlobalReceiver(PartyC2S.TYPE, (payload, context) -> {
			List<Integer> zs = new ArrayList<>();
			if (payload.zs() != null && !payload.zs().isBlank()) {
				for (String part : payload.zs().split(",")) {
					try {
						zs.add(Integer.parseInt(part.trim()));
					} catch (NumberFormatException ignored) {
					}
				}
			}
			BattleManager.setParty(context.player(), zs);
		});
	}

	public static void syncState(ServerPlayer player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		ServerPlayNetworking.send(player, new StateS2C(
				state.energy,
				state.discoveredElements.size(),
				state.discoveredMolecules.size(),
				encodeParty(state.party)
		));
	}

	private static String encodeParty(List<Integer> party) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < party.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(party.get(i));
		}
		return sb.toString();
	}

	public record OpenUiC2S(String ui) implements CustomPacketPayload {
		public static final Type<OpenUiC2S> TYPE = new Type<>(WonsoMod.id("open_ui"));
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenUiC2S> CODEC =
				StreamCodec.composite(ByteBufCodecs.STRING_UTF8, OpenUiC2S::ui, OpenUiC2S::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record SynthC2S(String moleculeId) implements CustomPacketPayload {
		public static final Type<SynthC2S> TYPE = new Type<>(WonsoMod.id("synth"));
		public static final StreamCodec<RegistryFriendlyByteBuf, SynthC2S> CODEC =
				StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SynthC2S::moleculeId, SynthC2S::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record SkillC2S(String skillId) implements CustomPacketPayload {
		public static final Type<SkillC2S> TYPE = new Type<>(WonsoMod.id("skill"));
		public static final StreamCodec<RegistryFriendlyByteBuf, SkillC2S> CODEC =
				StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SkillC2S::skillId, SkillC2S::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record RefineC2S(String action) implements CustomPacketPayload {
		public static final Type<RefineC2S> TYPE = new Type<>(WonsoMod.id("refine"));
		public static final StreamCodec<RegistryFriendlyByteBuf, RefineC2S> CODEC =
				StreamCodec.composite(ByteBufCodecs.STRING_UTF8, RefineC2S::action, RefineC2S::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record BattleCommandC2S(String command) implements CustomPacketPayload {
		public static final Type<BattleCommandC2S> TYPE = new Type<>(WonsoMod.id("battle_cmd"));
		public static final StreamCodec<RegistryFriendlyByteBuf, BattleCommandC2S> CODEC =
				StreamCodec.composite(ByteBufCodecs.STRING_UTF8, BattleCommandC2S::command, BattleCommandC2S::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record PartyC2S(String zs) implements CustomPacketPayload {
		public static final Type<PartyC2S> TYPE = new Type<>(WonsoMod.id("party"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PartyC2S> CODEC =
				StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PartyC2S::zs, PartyC2S::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record StateS2C(float energy, int elements, int molecules, String party) implements CustomPacketPayload {
		public static final Type<StateS2C> TYPE = new Type<>(WonsoMod.id("state"));
		public static final StreamCodec<RegistryFriendlyByteBuf, StateS2C> CODEC =
				StreamCodec.composite(
						ByteBufCodecs.FLOAT, StateS2C::energy,
						ByteBufCodecs.VAR_INT, StateS2C::elements,
						ByteBufCodecs.VAR_INT, StateS2C::molecules,
						ByteBufCodecs.STRING_UTF8, StateS2C::party,
						StateS2C::new
				);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record BattleStateS2C(String json) implements CustomPacketPayload {
		public static final Type<BattleStateS2C> TYPE = new Type<>(WonsoMod.id("battle_state"));
		public static final StreamCodec<RegistryFriendlyByteBuf, BattleStateS2C> CODEC =
				StreamCodec.composite(ByteBufCodecs.STRING_UTF8, BattleStateS2C::json, BattleStateS2C::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
