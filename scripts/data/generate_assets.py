#!/usr/bin/env python3
"""Generate pixel item/block textures and ko_kr/en_us lang from shared-data."""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "wonso-mod/src/main/resources/assets/wonso"
ITEMS = ASSETS / "textures/item"
BLOCKS = ASSETS / "textures/block"
MODELS_ITEM = ASSETS / "models/item"
MODELS_BLOCK = ASSETS / "models/block"
BLOCKSTATES = ASSETS / "blockstates"
LANG = ASSETS / "lang"

CAT_RGB = {
    "alkali_metal": (184, 92, 56),
    "alkaline_earth_metal": (201, 162, 39),
    "transition_metal": (61, 126, 166),
    "post_transition_metal": (92, 107, 115),
    "metalloid": (42, 157, 143),
    "reactive_nonmetal": (64, 145, 108),
    "halogen": (155, 93, 229),
    "noble_gas": (72, 202, 228),
    "lanthanide": (224, 122, 95),
    "actinide": (155, 34, 38),
    "unknown": (80, 80, 80),
}


def px(path: Path, rgb: tuple[int, int, int], mark: str | None = None) -> None:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle((1, 1, 14, 14), fill=rgb + (255,), outline=(20, 20, 20, 255))
    if mark == "ore":
        d.rectangle((4, 4, 11, 11), fill=(230, 230, 200, 255))
    elif mark == "machine":
        d.rectangle((3, 6, 12, 12), fill=(40, 40, 48, 255))
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)


def item_model(name: str) -> None:
    MODELS_ITEM.mkdir(parents=True, exist_ok=True)
    (MODELS_ITEM / f"{name}.json").write_text(json.dumps({
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"wonso:item/{name}"},
    }, indent=2) + "\n")


def block_model(name: str) -> None:
    MODELS_BLOCK.mkdir(parents=True, exist_ok=True)
    BLOCKSTATES.mkdir(parents=True, exist_ok=True)
    (MODELS_BLOCK / f"{name}.json").write_text(json.dumps({
        "parent": "minecraft:block/cube_all",
        "textures": {"all": f"wonso:block/{name}"},
    }, indent=2) + "\n")
    (BLOCKSTATES / f"{name}.json").write_text(json.dumps({
        "variants": {"": {"model": f"wonso:block/{name}"}}
    }, indent=2) + "\n")
    (MODELS_ITEM / f"{name}.json").write_text(json.dumps({
        "parent": f"wonso:block/{name}"
    }, indent=2) + "\n")


def main() -> None:
    elements = json.loads((ROOT / "shared-data/elements.json").read_text())
    molecules = json.loads((ROOT / "shared-data/molecules.json").read_text())
    ko = {
        "itemGroup.wonso": "wonso",
        "gui.wonso.periodic_table": "주기율표",
        "gui.wonso.molecule_dex": "분자 도감",
        "gui.wonso.synthesis": "화학 합성실",
        "gui.wonso.energy": "원소 에너지",
        "gui.wonso.battle": "원소 대전",
        "key.categories.wonso": "wonso",
        "key.wonso.periodic": "주기율표 열기",
        "key.wonso.skill_heal": "에너지 스킬: 치료",
        "key.wonso.skill_atk": "에너지 스킬: 공격",
        "key.wonso.skill_energy": "에너지 스킬: 에너지+",
        "key.wonso.skill_draw": "에너지 스킬: 카드 뽑기",
        "message.wonso.reaction_fail": "반응 실패",
        "message.wonso.reaction_ok": "합성 성공: %s",
        "message.wonso.crushed": "분쇄 완료",
        "item.wonso.chemistry_journal": "화학 도감",
        "item.wonso.guide_book": "가이드북",
        "item.wonso.refining_catalyst": "정제 촉매",
        "item.wonso.stellar_core": "항성 핵",
        "item.wonso.neutron_star_fragment": "중성자별 파편",
        "item.wonso.black_hole_essence": "블랙홀 정수",
    }
    en = {
        "itemGroup.wonso": "wonso",
        "gui.wonso.periodic_table": "Periodic Table",
        "gui.wonso.molecule_dex": "Molecule Dex",
        "gui.wonso.synthesis": "Synthesis Lab",
        "gui.wonso.energy": "Element Energy",
        "gui.wonso.battle": "Element Battle",
        "key.categories.wonso": "wonso",
        "key.wonso.periodic": "Open periodic table",
        "item.wonso.chemistry_journal": "Chemistry Journal",
        "item.wonso.guide_book": "Guide Book",
        "item.wonso.refining_catalyst": "Refining Catalyst",
        "message.wonso.reaction_fail": "Reaction failed",
        "message.wonso.reaction_ok": "Synthesized %s",
        "message.wonso.crushed": "Crushed",
    }

    for el in elements:
        name = f"element_card_{el['symbol'].lower()}"
        px(ITEMS / f"{name}.png", CAT_RGB.get(el["category"], (80, 80, 80)))
        item_model(name)
        ko[f"item.wonso.{name}"] = f"{el['nameKo']} 원소 카드"
        en[f"item.wonso.{name}"] = f"{el['nameEn']} Element Card"
        iname = f"impure_{el['symbol'].lower()}"
        px(ITEMS / f"{iname}.png", (90, 80, 70))
        item_model(iname)
        ko[f"item.wonso.{iname}"] = f"불순 {el['nameKo']} 잔여물"
        en[f"item.wonso.{iname}"] = f"Impure {el['nameEn']}"

    for m in molecules:
        name = f"molecule_card_{m['id'].replace('-', '_')}"
        px(ITEMS / f"{name}.png", (90, 140, 190))
        item_model(name)
        ko[f"item.wonso.{name}"] = f"{m['nameKo']} 분자 카드"
        en[f"item.wonso.{name}"] = f"{m['nameEn']} Molecule Card"

    extras = {
        "chemistry_journal": ((40, 70, 120), None),
        "guide_book": ((80, 50, 30), None),
        "refining_catalyst": ((180, 40, 40), None),
        "stellar_core": ((240, 200, 80), None),
        "neutron_star_fragment": ((200, 200, 255), None),
        "black_hole_essence": ((20, 0, 40), None),
        "silica_grit": ((200, 190, 150), None),
        "sand_dust": ((210, 200, 140), None),
        "stone_powder": ((140, 140, 140), None),
        "metal_trace": ((160, 160, 170), None),
        "quartz_dust": ((230, 230, 230), None),
        "iron_ore_powder": ((140, 90, 70), None),
        "rock_slag": ((70, 70, 70), None),
        "nether_quartz_dust": ((230, 210, 200), None),
        "murky_silicon_slurry": ((120, 130, 90), None),
        "waste_sludge": ((60, 70, 40), None),
        "iron_slurry": ((120, 60, 40), None),
        "sulfur_trace": ((210, 200, 40), None),
        "carbon_residue": ((30, 30, 30), None),
        "ash": ((90, 90, 90), None),
        "electrolyzed_hydrogen": ((220, 240, 255), None),
        "oxygen_bubble": ((180, 220, 255), None),
        "salt_brine": ((180, 210, 210), None),
        "slag": ((80, 70, 60), None),
        "toxic_waste": ((80, 140, 40), None),
        "wrong_element_chip": ((160, 80, 160), None),
        "calcium_phosphate": ((230, 230, 210), None),
        "silicon_dioxide": ((220, 210, 180), None),
        "sodium_chloride": ((240, 240, 240), None),
        "pure_si": ((180, 180, 190), None),
        "pure_fe": ((160, 160, 170), None),
        "pure_c": ((20, 20, 20), None),
    }
    extra_ko = {
        "silica_grit": "규사 입자", "sand_dust": "모래 먼지", "stone_powder": "돌가루",
        "metal_trace": "미량 금속", "quartz_dust": "석영 가루", "iron_ore_powder": "철광 분말",
        "rock_slag": "암석 슬래그", "nether_quartz_dust": "네더 석영 가루",
        "murky_silicon_slurry": "탁한 규소 슬러리", "waste_sludge": "폐 슬러지",
        "iron_slurry": "철 슬러리", "sulfur_trace": "황 흔적", "carbon_residue": "탄소 잔여물",
        "ash": "재", "electrolyzed_hydrogen": "전해 수소", "oxygen_bubble": "산소 기포",
        "salt_brine": "염수", "slag": "슬래그", "toxic_waste": "독 폐기물",
        "wrong_element_chip": "오원소 칩", "calcium_phosphate": "인산칼슘",
        "silicon_dioxide": "이산화규소", "sodium_chloride": "염화나트륨",
        "pure_si": "순수 규소", "pure_fe": "순수 철", "pure_c": "순수 탄소",
    }
    for name, (rgb, mark) in extras.items():
        px(ITEMS / f"{name}.png", rgb, mark)
        item_model(name)
        if name not in ("chemistry_journal", "guide_book", "refining_catalyst", "stellar_core",
                        "neutron_star_fragment", "black_hole_essence"):
            ko[f"item.wonso.{name}"] = extra_ko.get(name, name)
            en[f"item.wonso.{name}"] = name

    blocks = {
        "synthesis_lab": ((50, 80, 110), "machine", "화학 합성실", "Synthesis Lab"),
        "element_refinery": ((90, 70, 50), "machine", "원소 정제기", "Element Refinery"),
        "energy_converter": ((80, 90, 40), "machine", "에너지 변환기", "Energy Converter"),
        "periodite_ore": ((70, 80, 90), "ore", "주기율광석", "Periodite Ore"),
        "deep_periodite": ((40, 50, 60), "ore", "심층 주기율광석", "Deep Periodite"),
        "nether_salt": ((180, 140, 120), "ore", "네더 소금", "Nether Salt"),
        "end_crystal_ore": ((180, 160, 220), "ore", "엔드 결정 광석", "End Crystal Ore"),
        "fusion_chamber": ((120, 40, 80), "machine", "핵융합 챔버", "Fusion Chamber"),
        "element_shrine": ((90, 60, 120), "machine", "원소 신전", "Element Shrine"),
        "research_table": ((80, 60, 40), "machine", "연구대", "Research Table"),
        "crusher": ((90, 90, 90), "machine", "분쇄기", "Crusher"),
        "reaction_vat": ((50, 90, 70), "machine", "반응통", "Reaction Vat"),
        "evaporation_pan": ((160, 140, 90), "machine", "증발천", "Evaporation Pan"),
        "electrolyzer": ((60, 90, 140), "machine", "전기분해기", "Electrolyzer"),
        "distiller": ((70, 100, 120), "machine", "증류기", "Distiller"),
        "reduction_furnace": ((140, 70, 40), "machine", "환원로", "Reduction Furnace"),
        "waste_bin": ((40, 50, 40), "machine", "폐기통", "Waste Bin"),
        "slag_bin": ((50, 45, 40), "machine", "슬래그통", "Slag Bin"),
    }
    for name, (rgb, mark, k, e) in blocks.items():
        px(BLOCKS / f"{name}.png", rgb, mark)
        block_model(name)
        ko[f"block.wonso.{name}"] = k
        en[f"block.wonso.{name}"] = e

    # icon
    icon = Image.new("RGBA", (128, 128), (11, 19, 32, 255))
    d = ImageDraw.Draw(icon)
    d.rectangle((16, 16, 111, 111), outline=(232, 200, 106, 255), width=6)
    d.rectangle((40, 40, 88, 88), fill=(64, 145, 108, 255))
    (ASSETS / "icon.png").parent.mkdir(parents=True, exist_ok=True)
    icon.save(ASSETS / "icon.png")

    LANG.mkdir(parents=True, exist_ok=True)
    (LANG / "ko_kr.json").write_text(json.dumps(ko, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (LANG / "en_us.json").write_text(json.dumps(en, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("assets generated")


if __name__ == "__main__":
    main()
