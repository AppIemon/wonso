package com.appiemon.wonso.gameplay;

import com.appiemon.wonso.data.ElementRecord;
import com.appiemon.wonso.data.WonsoData;
import com.appiemon.wonso.data.WonsoPlayerState;
import com.appiemon.wonso.network.WonsoPackets;
import com.appiemon.wonso.registry.WonsoItems;
import com.google.gson.JsonArray;
import net.minecraft.world.item.Item;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class BattleManager {
	private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
	private static final Set<String> NOBLE = Set.of("Au", "Ag", "Pt", "Pd", "Cu");

	private BattleManager() {
	}

	public static void initialize() {
	}

	public static boolean handle(ServerPlayer player, String command) {
		if (command == null || command.isBlank()) {
			return false;
		}
		if (command.startsWith("start")) {
			return startShrine(player);
		}
		Session session = SESSIONS.get(player.getUUID());
		if (session == null || session.done) {
			player.sendOverlayMessage(Component.translatable("message.wonso.battle_none"));
			return false;
		}
		if (command.startsWith("skill:")) {
			turn(player, session, "skill", command.substring(6), null);
		} else if (command.startsWith("switch:")) {
			turn(player, session, "switch", command.substring(7), null);
		} else if (command.startsWith("tool:")) {
			turn(player, session, "tool", command.substring(5), null);
		} else if ("pass".equals(command)) {
			turn(player, session, "pass", "", null);
		} else if ("flee".equals(command)) {
			session.log.add("도주했다.");
			session.done = true;
			session.result = "flee";
		} else {
			return false;
		}
		sync(player, session);
		return true;
	}

	public static boolean startShrine(ServerPlayer player) {
		ensureParty(player);
		WonsoPlayerState state = ElementEnergyManager.state(player);
		if (state.party.isEmpty()) {
			player.sendOverlayMessage(Component.translatable("message.wonso.party_empty"));
			return false;
		}
		Session session = new Session();
		for (int z : state.party) {
			session.player.add(Fighter.from(z));
		}
		session.waves.add(List.of(randZ(1, 10), randZ(1, 10)));
		session.waves.add(List.of(randZ(11, 20), randZ(11, 20)));
		session.waves.add(List.of(randZ(21, 26)));
		session.loadWave();
		session.log.add("원소 신전 — 라운드 1. 플레이어는 명령만 내린다.");
		SESSIONS.put(player.getUUID(), session);
		sync(player, session);
		player.sendOverlayMessage(Component.translatable("message.wonso.battle_start"));
		return true;
	}

	public static boolean setParty(ServerPlayer player, List<Integer> zs) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		List<Integer> clean = new ArrayList<>();
		for (int z : zs) {
			if (z < 1 || z > 118 || clean.contains(z) || clean.size() >= 6) {
				continue;
			}
			clean.add(z);
		}
		state.setParty(clean);
		player.setAttached(ElementEnergyManager.STATE, state);
		WonsoPackets.syncState(player);
		player.sendOverlayMessage(Component.literal("파티 " + clean.size() + "/6"));
		return true;
	}

	public static void ensureParty(ServerPlayer player) {
		WonsoPlayerState state = ElementEnergyManager.state(player);
		if (!state.party.isEmpty()) {
			return;
		}
		List<Integer> zs = new ArrayList<>();
		for (String symbol : state.discoveredElements) {
			var el = WonsoData.bySymbol(symbol);
			if (el != null && !zs.contains(el.z())) {
				zs.add(el.z());
			}
			if (zs.size() >= 6) {
				break;
			}
		}
		if (zs.isEmpty()) {
			var inv = player.getInventory();
			for (int i = 0; i < inv.getContainerSize() && zs.size() < 6; i++) {
				String symbol = WonsoItems.symbolOfCard(inv.getItem(i));
				if (symbol == null) {
					continue;
				}
				var el = WonsoData.bySymbol(symbol);
				if (el != null && !zs.contains(el.z())) {
					zs.add(el.z());
				}
			}
		}
		if (zs.isEmpty()) {
			zs.add(1);
		}
		state.setParty(zs);
		player.setAttached(ElementEnergyManager.STATE, state);
	}

	private static void turn(ServerPlayer player, Session session, String kind, String arg, String ignored) {
		Fighter mine = session.activePlayer();
		Fighter foe = session.activeFoe();
		if (mine == null || foe == null) {
			return;
		}
		if ("switch".equals(kind)) {
			int z = parseInt(arg, -1);
			int idx = session.indexOf(session.player, z);
			if (idx < 0 || session.player.get(idx).hp <= 0) {
				session.log.add("교체 실패.");
				return;
			}
			session.pActive = idx;
			session.log.add("교체 → " + session.player.get(idx).symbol);
			enemyAct(session);
			endChecks(player, session);
			return;
		}
		if ("tool".equals(kind)) {
			if (!applyTool(player, session, mine, arg)) {
				session.log.add("분자 도구 없음: " + arg);
				return;
			}
			session.log.add(mine.symbol + " 분자 도구 " + arg);
			enemyAct(session);
			endChecks(player, session);
			return;
		}
		if ("pass".equals(kind)) {
			session.log.add("턴 종료.");
			enemyAct(session);
			endChecks(player, session);
			return;
		}
		int slot = parseInt(arg, 1);
		Skill skill = skillOf(mine, slot);
		if (skill.cost > 0 && !ElementEnergyManager.spend(player, skill.cost)) {
			session.log.add("⚡ 부족 — " + skill.name);
			return;
		}
		boolean playerFirst = mine.speed >= foe.speed;
		if (playerFirst) {
			resolveSkill(session, mine, foe, skill, true);
			if (foe.hp > 0) {
				enemyAct(session);
			}
		} else {
			enemyAct(session);
			if (mine.hp > 0) {
				resolveSkill(session, mine, foe, skill, true);
			}
		}
		endChecks(player, session);
	}

	private static void enemyAct(Session session) {
		Fighter foe = session.activeFoe();
		Fighter mine = session.activePlayer();
		if (foe == null || mine == null || foe.hp <= 0 || mine.hp <= 0) {
			return;
		}
		Skill skill = skillOf(foe, 1 + ThreadLocalRandom.current().nextInt(2));
		resolveSkill(session, foe, mine, skill, false);
	}

	private static void resolveSkill(Session session, Fighter atk, Fighter def, Skill skill, boolean playerSide) {
		if (skill.heal) {
			int heal = Math.max(4, atk.maxHp / 8);
			atk.hp = Math.min(atk.maxHp, atk.hp + heal);
			session.log.add(atk.symbol + " " + skill.name + " 회복 +" + heal);
			return;
		}
		double mult = typeMult(atk, def) * (skill.stab ? 1.2 : 1.0) * skill.power;
		if (sameGroupAlly(session, atk, playerSide) && skill.synergy) {
			mult *= 2.0;
		}
		int atkStat = Math.max(1, atk.attack + atk.atkStage * 2);
		int defStat = Math.max(1, def.defense + def.defStage * 2);
		int dmg = Math.max(1, (int) Math.round((atkStat * 1.4 + 4) * mult / Math.max(1.0, defStat * 0.35)));
		if (skill.pierce) {
			dmg = Math.max(1, (int) Math.round(atkStat * 0.9 * skill.power));
		}
		def.hp = Math.max(0, def.hp - dmg);
		session.log.add(atk.symbol + " " + skill.name + " → " + def.symbol + " -" + dmg
				+ (mult >= 1.9 ? " 효과는 굉장했다!" : mult <= 0.6 ? " 효과가 별로인 듯하다…" : ""));
		if (skill.poison && def.poisonTurns <= 0) {
			def.poisonTurns = 3;
			session.log.add(def.symbol + " 독성 잔류.");
		}
		if (def.poisonTurns > 0) {
			int dot = Math.max(1, def.maxHp / 16);
			def.hp = Math.max(0, def.hp - dot);
			def.poisonTurns -= 1;
			session.log.add(def.symbol + " 독 -" + dot);
		}
	}

	private static boolean applyTool(ServerPlayer player, Session session, Fighter mine, String moleculeId) {
		Item card = WonsoItems.moleculeCard(moleculeId);
		if (card == null || InventoryUtil.count(player, card) < 1) {
			return false;
		}
		InventoryUtil.remove(player, card, 1);
		for (var raw : WonsoData.moleculeTools()) {
			JsonObject o = raw.getAsJsonObject();
			if (!moleculeId.equals(o.get("id").getAsString())) {
				continue;
			}
			String effect = o.get("effect").getAsString();
			double value = o.get("value").getAsDouble();
			switch (effect) {
				case "heal_percent" -> mine.hp = Math.min(mine.maxHp, mine.hp + (int) Math.round(mine.maxHp * value));
				case "defense_stage" -> mine.defStage += (int) value;
				case "attack_stage" -> mine.atkStage += (int) value;
				default -> {
				}
			}
			return true;
		}
		mine.hp = Math.min(mine.maxHp, mine.hp + mine.maxHp / 5);
		return true;
	}

	private static void endChecks(ServerPlayer player, Session session) {
		Fighter foe = session.activeFoe();
		if (foe != null && foe.hp <= 0) {
			session.log.add(foe.symbol + " 기절!");
			int next = nextAlive(session.foe, session.fActive);
			if (next >= 0) {
				session.fActive = next;
			} else if (!session.nextWave()) {
				win(player, session);
				return;
			} else {
				session.log.add("다음 웨이브 " + (session.waveIndex + 1));
			}
		}
		Fighter mine = session.activePlayer();
		if (mine != null && mine.hp <= 0) {
			session.log.add(mine.symbol + " 기절 — 교체 필요.");
			int next = nextAlive(session.player, session.pActive);
			if (next >= 0) {
				session.pActive = next;
			} else {
				session.done = true;
				session.result = "lose";
				session.log.add("파티 전멸. 패배.");
			}
		}
	}

	private static void win(ServerPlayer player, Session session) {
		session.done = true;
		session.result = "win";
		session.log.add("신전 클리어. 원소 카드 보상.");
		for (Fighter f : session.defeated) {
			InventoryUtil.give(player, WonsoItems.elementCard(f.symbol), 1);
			DiscoveryManager.discoverElement(player, f.symbol);
		}
		ElementEnergyManager.addEnergy(player, 15);
		DiscoveryManager.grant(player, "round_clear");
	}

	private static int nextAlive(List<Fighter> list, int current) {
		for (int i = 0; i < list.size(); i++) {
			int idx = (current + 1 + i) % list.size();
			if (list.get(idx).hp > 0) {
				return idx;
			}
		}
		return -1;
	}

	private static boolean sameGroupAlly(Session session, Fighter atk, boolean playerSide) {
		List<Fighter> team = playerSide ? session.player : session.foe;
		for (Fighter f : team) {
			if (f != atk && f.hp > 0 && f.category.equals(atk.category)) {
				return true;
			}
		}
		return false;
	}

	private static double typeMult(Fighter atk, Fighter def) {
		String a = atk.category;
		String d = def.category;
		if ("noble_gas".equals(d)) {
			return 0.5;
		}
		if ("alkali_metal".equals(a) && "halogen".equals(d)) {
			return 2.0;
		}
		if ("alkali_metal".equals(a) && "transition_metal".equals(d)) {
			return 0.5;
		}
		if ("halogen".equals(a) && ("alkali_metal".equals(d) || "alkaline_earth_metal".equals(d))) {
			return 2.0;
		}
		if ("halogen".equals(a) && (NOBLE.contains(def.symbol) || "transition_metal".equals(d) && NOBLE.contains(def.symbol))) {
			return 0.5;
		}
		if ("halogen".equals(a) && NOBLE.contains(def.symbol)) {
			return 0.5;
		}
		if ("transition_metal".equals(a) && "alkali_metal".equals(d)) {
			return 2.0;
		}
		if ("transition_metal".equals(a) && "halogen".equals(d)) {
			return 0.5;
		}
		if (NOBLE.contains(atk.symbol) && "halogen".equals(d)) {
			return 2.0;
		}
		if (NOBLE.contains(atk.symbol) && "alkali_metal".equals(d)) {
			return 0.5;
		}
		if ("reactive_nonmetal".equals(a) && d.contains("metal") && !"metalloid".equals(d)) {
			return 2.0;
		}
		if ("reactive_nonmetal".equals(a) && "halogen".equals(d)) {
			return 0.5;
		}
		return 1.0;
	}

	private static Skill skillOf(Fighter f, int slot) {
		String cat = f.category;
		return switch (slot) {
			case 2 -> new Skill(name2(cat), 8, 1.35, true, false, false, false);
			case 3 -> new Skill(name3(cat), 12, 0.85, true, false, true, false);
			case 4 -> new Skill("족 시너지", 15, 1.1, true, false, false, true);
			default -> new Skill(name1(cat), 0, 1.0, true, false, false, false);
		};
	}

	private static String name1(String cat) {
		return switch (cat) {
			case "halogen" -> "염소 가스";
			case "alkali_metal" -> "이온화 타격";
			case "noble_gas" -> "비활성 막";
			case "transition_metal" -> "금속 타격";
			default -> "기본 공격";
		};
	}

	private static String name2(String cat) {
		return switch (cat) {
			case "halogen" -> "산화 공격";
			case "alkali_metal" -> "격렬 반응";
			default -> "강화 타격";
		};
	}

	private static String name3(String cat) {
		return switch (cat) {
			case "halogen" -> "독성 잔류";
			case "alkali_metal" -> "부식";
			default -> "잔류 효과";
		};
	}

	private static int randZ(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private static int parseInt(String s, int fallback) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static void sync(ServerPlayer player, Session session) {
		ServerPlayNetworking.send(player, new WonsoPackets.BattleStateS2C(session.toJson().toString()));
	}

	private record Skill(String name, int cost, double power, boolean stab, boolean pierce, boolean poison, boolean synergy, boolean heal) {
		Skill(String name, int cost, double power, boolean stab, boolean pierce, boolean poison, boolean synergy) {
			this(name, cost, power, stab, pierce, poison, synergy, false);
		}
	}

	static final class Fighter {
		final int z;
		final String symbol;
		final String nameKo;
		final String category;
		int hp;
		final int maxHp;
		final int attack;
		final int defense;
		final int speed;
		int atkStage;
		int defStage;
		int poisonTurns;

		private Fighter(int z, String symbol, String nameKo, String category, int hp, int attack, int defense, int speed) {
			this.z = z;
			this.symbol = symbol;
			this.nameKo = nameKo;
			this.category = category;
			this.hp = hp;
			this.maxHp = hp;
			this.attack = attack;
			this.defense = defense;
			this.speed = speed;
		}

		static Fighter from(int z) {
			ElementRecord el = WonsoData.byZ(z);
			if (el == null) {
				return new Fighter(z, "?", "?", "unknown", 30, 6, 6, 6);
			}
			return new Fighter(
					z,
					el.symbol(),
					el.nameKo(),
					el.category(),
					WonsoData.battleInt(el, "hp", 36),
					WonsoData.battleInt(el, "attack", 7),
					WonsoData.battleInt(el, "defense", 6),
					WonsoData.battleInt(el, "speed", 8)
			);
		}

		JsonObject toJson() {
			JsonObject o = new JsonObject();
			o.addProperty("z", z);
			o.addProperty("symbol", symbol);
			o.addProperty("nameKo", nameKo);
			o.addProperty("category", category);
			o.addProperty("hp", hp);
			o.addProperty("maxHp", maxHp);
			o.addProperty("attack", attack);
			o.addProperty("defense", defense);
			o.addProperty("speed", speed);
			JsonArray skills = new JsonArray();
			for (int slot = 1; slot <= 4; slot++) {
				Skill skill = skillOf(this, slot);
				JsonObject so = new JsonObject();
				so.addProperty("slot", slot);
				so.addProperty("name", skill.name);
				so.addProperty("cost", skill.cost);
				skills.add(so);
			}
			o.add("skills", skills);
			return o;
		}
	}

	static final class Session {
		final List<Fighter> player = new ArrayList<>();
		final List<Fighter> foe = new ArrayList<>();
		final List<List<Integer>> waves = new ArrayList<>();
		final List<Fighter> defeated = new ArrayList<>();
		final List<String> log = new ArrayList<>();
		int pActive;
		int fActive;
		int waveIndex = -1;
		boolean done;
		String result = "";

		Fighter activePlayer() {
			return pActive >= 0 && pActive < player.size() ? player.get(pActive) : null;
		}

		Fighter activeFoe() {
			return fActive >= 0 && fActive < foe.size() ? foe.get(fActive) : null;
		}

		int indexOf(List<Fighter> list, int z) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).z == z) {
					return i;
				}
			}
			return -1;
		}

		void loadWave() {
			waveIndex = 0;
			foe.clear();
			for (int z : waves.get(0)) {
				foe.add(Fighter.from(z));
			}
			fActive = 0;
		}

		boolean nextWave() {
			for (Fighter f : foe) {
				if (f.hp <= 0) {
					defeated.add(f);
				}
			}
			if (waveIndex + 1 >= waves.size()) {
				return false;
			}
			waveIndex += 1;
			foe.clear();
			for (int z : waves.get(waveIndex)) {
				foe.add(Fighter.from(z));
			}
			fActive = 0;
			return true;
		}

		JsonObject toJson() {
			JsonObject o = new JsonObject();
			o.addProperty("done", done);
			o.addProperty("result", result);
			o.addProperty("wave", waveIndex + 1);
			o.addProperty("waves", waves.size());
			o.add("mine", activePlayer() == null ? new JsonObject() : activePlayer().toJson());
			o.add("foe", activeFoe() == null ? new JsonObject() : activeFoe().toJson());
			JsonArray party = new JsonArray();
			for (Fighter f : player) {
				party.add(f.toJson());
			}
			o.add("party", party);
			JsonArray wild = new JsonArray();
			for (Fighter f : foe) {
				wild.add(f.toJson());
			}
			o.add("wild", wild);
			JsonArray lines = new JsonArray();
			int from = Math.max(0, log.size() - 8);
			for (int i = from; i < log.size(); i++) {
				lines.add(log.get(i));
			}
			o.add("log", lines);
			return o;
		}
	}
}
