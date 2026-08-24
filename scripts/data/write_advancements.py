#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[2] / "wonso-mod/src/main/resources/data/wonso/advancement"
ROOT.mkdir(parents=True, exist_ok=True)

chain = [
    ("root", None, "wonso:chemistry_journal", "화학 도감", "Journal 획득"),
    ("first_element", "wonso:root", "wonso:element_card_h", "첫 원소", "첫 원소 카드"),
    ("open_periodic_table", "wonso:first_element", "wonso:chemistry_journal", "주기율표", "주기율표 열기"),
    ("atom_view", "wonso:open_periodic_table", "wonso:element_card_c", "원자 모드", "원자 탭 확인"),
    ("first_synthesis", "wonso:atom_view", "wonso:molecule_card_water", "첫 합성", "첫 분자 합성"),
    ("molecule_dex", "wonso:first_synthesis", "wonso:chemistry_journal", "도감", "도감 1개 발견"),
    ("energy_convert", "wonso:molecule_dex", "wonso:energy_converter", "에너지 변환", "분자→에너지"),
    ("energy_skill", "wonso:energy_convert", "wonso:energy_converter", "에너지 스킬", "첫 에너지 스킬"),
    ("group_synergy", "wonso:energy_skill", "wonso:element_card_cl", "족 시너지", "같은 족 3장"),
    ("fusion_intro", "wonso:group_synergy", "wonso:fusion_chamber", "핵융합 입문", "핵융합"),
    ("star_system", "wonso:fusion_intro", "wonso:stellar_core", "별", "별 획득"),
    ("round_clear", "wonso:star_system", "wonso:element_shrine", "라운드 1", "라운드 1 클리어"),
    ("first_crush", "wonso:root", "wonso:crusher", "첫 분쇄", "맷돌로 모래 분쇄"),
    ("first_sludge", "wonso:first_crush", "wonso:waste_sludge", "첫 슬러지", "슬러지 획득"),
    ("first_impure", "wonso:first_sludge", "wonso:impure_si", "불순 잔여물", "불순 잔여물"),
    ("first_card_from_sand", "wonso:first_impure", "wonso:element_card_si", "모래에서 규소", "Si 카드"),
    ("slag_hoarder", "wonso:first_crush", "wonso:slag", "슬래그 수집가", "슬래그 64"),
    ("refinery_master", "wonso:first_impure", "wonso:element_refinery", "정제 장인", "정제 100회"),
]

for name, parent, icon, title, desc in chain:
    data = {
        "display": {
            "icon": {"id": icon},
            "title": {"text": title},
            "description": {"text": desc},
            "frame": "task",
            "show_toast": True,
            "announce_chat": True,
        },
        "criteria": {
            "wonso": {"trigger": "minecraft:impossible"}
        }
    }
    if parent:
        data["parent"] = parent
    else:
        data["display"]["background"] = "minecraft:gui/advancements/backgrounds/stone"
        data["criteria"] = {
            "wonso": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": "wonso:chemistry_journal"}]},
            }
        }
    (ROOT / f"{name}.json").write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("advancements", len(chain))
