package com.appiemon.wonso.network;

import com.appiemon.wonso.WonsoMod;
import com.appiemon.wonso.data.WonsoPlayerState;
import com.appiemon.wonso.gameplay.DiscoveryManager;
import com.appiemon.wonso.gameplay.ElementEnergyManager;
import com.appiemon.wonso.gameplay.GroupSynergyHandler;
import com.appiemon.wonso.gameplay.RefiningManager;
import com.appiemon.wonso.gameplay.SynthesisRecipeManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class WonsoPackets {
	private WonsoPackets() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(OpenUiC2S.TYPE, OpenUiC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SynthC2S.TYPE, SynthC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SkillC2S.TYPE, SkillC2S.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RefineC2S.TYPE, RefineC2S.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(StateS2C.TYPE, StateS2C.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(OpenUiC2S.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			if ("periodic".equals(payload.ui()) || "journal".equals(payload.ui())) {
				DiscoveryManager.markTableOpened(player);
			}
			if ("atom".equals(payload.ui())) {
				DiscoveryManager.markAtomViewed(player);
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
			if ("crush".equals(payload.action())) {
				RefiningManager.crushHeld(context.player());
			} else if (payload.action().startsWith("purify:")) {
				RefiningManager.purifyToCard(context.player(), payload.action().substring("purify:".length()));
			}
			syncState(context.player());
		});
	}

	public static void syncState(ServerPlayer player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		ServerPlayNetworking.send(player, new StateS2C(state.energy, state.discoveredElements.size(), state.discoveredMolecules.size()));
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

	public record StateS2C(float energy, int elements, int molecules) implements CustomPacketPayload {
		public static final Type<StateS2C> TYPE = new Type<>(WonsoMod.id("state"));
		public static final StreamCodec<RegistryFriendlyByteBuf, StateS2C> CODEC =
				StreamCodec.composite(
						ByteBufCodecs.FLOAT, StateS2C::energy,
						ByteBufCodecs.VAR_INT, StateS2C::elements,
						ByteBufCodecs.VAR_INT, StateS2C::molecules,
						StateS2C::new
				);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
