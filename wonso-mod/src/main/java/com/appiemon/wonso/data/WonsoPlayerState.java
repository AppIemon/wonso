package com.appiemon.wonso.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class WonsoPlayerState {
	public static final Codec<WonsoPlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("energy").forGetter(s -> s.energy),
			Codec.STRING.listOf().fieldOf("molecules").forGetter(s -> s.discoveredMolecules),
			Codec.STRING.listOf().fieldOf("elements").forGetter(s -> s.discoveredElements),
			Codec.INT.listOf().fieldOf("party").forGetter(s -> s.party),
			Codec.BOOL.fieldOf("atomViewed").forGetter(s -> s.atomViewed),
			Codec.BOOL.fieldOf("tableOpened").forGetter(s -> s.tableOpened)
	).apply(instance, WonsoPlayerState::new));

	public float energy;
	public final List<String> discoveredMolecules;
	public final List<String> discoveredElements;
	public final List<Integer> party;
	public boolean atomViewed;
	public boolean tableOpened;

	public WonsoPlayerState() {
		this(0f, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false, false);
	}

	public WonsoPlayerState(float energy, List<String> molecules, List<String> elements, List<Integer> party,
							boolean atomViewed, boolean tableOpened) {
		this.energy = energy;
		this.discoveredMolecules = new ArrayList<>(molecules);
		this.discoveredElements = new ArrayList<>(elements);
		this.party = new ArrayList<>(party);
		this.atomViewed = atomViewed;
		this.tableOpened = tableOpened;
	}

	public void addEnergy(float amount) {
		energy = Math.max(0f, energy + amount);
	}

	public boolean discoverElement(String symbol) {
		if (discoveredElements.contains(symbol)) {
			return false;
		}
		discoveredElements.add(symbol);
		return true;
	}

	public boolean discoverMolecule(String id) {
		if (discoveredMolecules.contains(id)) {
			return false;
		}
		discoveredMolecules.add(id);
		return true;
	}

	public boolean setParty(List<Integer> zs) {
		if (zs.size() > 6) {
			return false;
		}
		party.clear();
		for (int z : zs) {
			if (!party.contains(z)) {
				party.add(z);
			}
		}
		return true;
	}
}
