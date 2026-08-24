package com.appiemon.wonso.item;

import com.appiemon.wonso.data.ElementRecord;
import com.appiemon.wonso.gameplay.DiscoveryManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ElementCardItem extends Item {
	private final ElementRecord element;

	public ElementCardItem(Properties properties, ElementRecord element) {
		super(properties);
		this.element = element;
	}

	public ElementRecord element() {
		return element;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			DiscoveryManager.discoverElement(player, element.symbol());
		}
		return InteractionResult.SUCCESS;
	}
}
