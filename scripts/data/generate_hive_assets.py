#!/usr/bin/env python3
"""Generate ALL wonso textures via Hive AI V3 (Authorization: Bearer).

Requires env HIVE_API_KEY. Does not print the key.

  export HIVE_API_KEY=...
  python3 scripts/data/generate_hive_assets.py

Then: python3 scripts/data/generate_assets.py
  (writes 26.2 items/*.json + models + lang; will not overwrite Hive PNGs)

Docs: https://docs.thehive.ai/docs/image-generation-models
POST https://api.thehive.ai/api/v3/black-forest-labs/flux-schnell
"""

from __future__ import annotations

import json
import os
import sys
import time
import urllib.error
import urllib.request
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "wonso-mod/src/main/resources/assets/wonso"
CACHE = ROOT / "content/hive-cache"
ENDPOINT = "https://api.thehive.ai/api/v3/black-forest-labs/flux-schnell"

STYLE = (
    "isometric minecraft item icon, 16-bit pixel art, crisp pixels, dark navy background, "
    "no text, no letters, no watermark, no UI chrome, game asset, chemistry laboratory aesthetic, "
    "gold and teal accents"
)

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

CAT_PROMPT = {
    "alkali_metal": "orange flame alkali metal trading card, sodium fire",
    "alkaline_earth_metal": "golden alkaline earth metal trading card, magnesium spark",
    "transition_metal": "blue steel transition metal trading card, iron ingot",
    "post_transition_metal": "pewter post-transition metal trading card",
    "metalloid": "teal silicon metalloid trading card, crystal wafer",
    "reactive_nonmetal": "green carbon nonmetal trading card, diamond and charcoal",
    "halogen": "violet halogen gas trading card, chlorine flask",
    "noble_gas": "cyan glowing noble gas trading card, neon tube",
    "lanthanide": "coral lanthanide trading card, rare earth",
    "actinide": "crimson actinide trading card, radioactive glow",
    "unknown": "grey unknown element trading card",
}

MOL_PROMPT = {
    "elemental": "blue molecule trading card, diatomic gas flask",
    "inorganic": "teal inorganic molecule trading card, mineral crystal",
    "ionic": "white salt ionic compound trading card, cubic crystals",
    "organic_simple": "amber organic molecule trading card, hexagon rings",
}

ITEMS = {
    "chemistry_journal": "leather chemistry journal item, gold atom emblem",
    "guide_book": "worn guidebook item, teal bookmark",
    "refining_catalyst": "red catalyst crystal item",
    "stellar_core": "golden star core gem item",
    "neutron_star_fragment": "icy blue neutron star shard item",
    "black_hole_essence": "tiny black hole orb item, purple rim",
    "silica_grit": "pile of pale silica grit item",
    "sand_dust": "small pile of yellow sand dust item",
    "stone_powder": "grey stone powder pile item",
    "metal_trace": "tiny silver metal filings item",
    "quartz_dust": "sparkling white quartz dust item",
    "iron_ore_powder": "rusty iron ore powder item",
    "rock_slag": "dark rock slag chunk item",
    "nether_quartz_dust": "pink nether quartz dust item",
    "murky_silicon_slurry": "jar of murky green silicon slurry",
    "waste_sludge": "jar of murky industrial sludge",
    "iron_slurry": "jar of rusty iron slurry",
    "sulfur_trace": "yellow sulfur powder item",
    "carbon_residue": "black carbon residue lump item",
    "ash": "grey ash pile item",
    "electrolyzed_hydrogen": "glowing hydrogen vial item",
    "oxygen_bubble": "blue oxygen bubble vial item",
    "salt_brine": "clear salt brine bottle item",
    "slag": "industrial slag chunk item",
    "toxic_waste": "green toxic waste barrel item",
    "wrong_element_chip": "purple broken element chip item",
    "calcium_phosphate": "white calcium phosphate powder item",
    "silicon_dioxide": "pale silicon dioxide crystal item",
    "sodium_chloride": "white salt crystal item",
    "pure_si": "pure silicon wafer shard item",
    "pure_fe": "pure iron nugget item",
    "pure_c": "pure carbon diamond chip item",
    "impure_template": "dirty brown impure metal residue chunk item",
}

BLOCKS = {
    "synthesis_lab": "minecraft chemistry synthesis lab block, glass flasks, copper pipes",
    "element_refinery": "minecraft brick refinery machine block, furnaces and pipes",
    "energy_converter": "minecraft energy converter block, glowing amber core",
    "periodite_ore": "minecraft stone ore with iridescent periodic crystals",
    "deep_periodite": "minecraft deepslate ore with dark crystals",
    "nether_salt": "minecraft netherrack salt crystal cluster",
    "end_crystal_ore": "minecraft end stone with purple crystal ore",
    "fusion_chamber": "minecraft fusion reactor block, magenta plasma window",
    "element_shrine": "minecraft stone shrine block, glowing elemental runes",
    "research_table": "minecraft wooden research table with books and flasks",
    "crusher": "minecraft stone crusher mill block, iron gears",
    "reaction_vat": "minecraft green glass reaction vat block",
    "evaporation_pan": "minecraft shallow copper evaporation pan block",
    "electrolyzer": "minecraft copper electrolyzer block with electrodes",
    "distiller": "minecraft glass distiller column block",
    "reduction_furnace": "minecraft blast furnace reduction machine, orange glow",
    "waste_bin": "minecraft industrial waste barrel block",
    "slag_bin": "minecraft slag dumpster block, dark slag",
}

GUI = {
    "periodic": "wide dark periodic table UI panel, glowing element cells, chemistry HUD, no readable text",
    "battle": "pokemon-style trainer battle UI, two elemental spirits facing each other, dark arena",
    "refine": "industrial chemistry refining machine UI, pipes, slurry tanks, brass gauges",
    "fusion": "stellar fusion chamber UI, plasma core, stars, dark space",
    "party": "six-slot elemental party roster UI, hexagonal badges",
    "energy": "energy converter UI, lightning vial, amber glow",
    "synthesis": "alchemy synthesis lab UI, flasks and formula slots",
    "guide": "dark guidebook UI panel, gold corners, chemistry notes",
}


def api_key() -> str:
    key = os.environ.get("HIVE_API_KEY") or os.environ.get("HIVE_TOKEN") or ""
    if not key:
        sys.stderr.write("HIVE_API_KEY is not set. Add the Hive V3 Bearer secret and rerun.\n")
        sys.exit(2)
    return key


# Flux Schnell on Hive V3 only accepts a few size pairs; 1024x1024 is allowed.
API_SIZE = 1024


def hive_generate(prompt: str, seed: int, retries: int = 5) -> Image.Image:
    body = json.dumps({
        "input": {
            "prompt": f"{STYLE}. {prompt}",
            "image_size": {"width": API_SIZE, "height": API_SIZE},
            "num_inference_steps": 4,
            "num_images": 1,
            "seed": seed,
            "output_format": "png",
        }
    }).encode("utf-8")
    last = None
    for attempt in range(retries):
        req = urllib.request.Request(
            ENDPOINT,
            data=body,
            headers={
                "authorization": f"Bearer {api_key()}",
                "Content-Type": "application/json",
                "accept": "application/json",
            },
            method="POST",
        )
        try:
            with urllib.request.urlopen(req, timeout=180) as resp:
                payload = json.loads(resp.read().decode("utf-8"))
            outputs = payload.get("output") or payload.get("outputs") or []
            if not outputs:
                raise RuntimeError(f"Hive empty output: {str(payload)[:400]}")
            url = outputs[0].get("url") if isinstance(outputs[0], dict) else outputs[0]
            if not url:
                raise RuntimeError(f"Hive missing url: {str(payload)[:400]}")
            with urllib.request.urlopen(url, timeout=90) as img_resp:
                return Image.open(BytesIO(img_resp.read())).convert("RGBA")
        except urllib.error.HTTPError as e:
            detail = e.read()[:240]
            last = RuntimeError(f"Hive HTTP {e.code}: {detail!r}")
            if e.code in (400, 401, 403, 404):
                raise SystemExit(str(last)) from e
            wait = min(32, 2 ** attempt)
            sys.stderr.write(f"Hive HTTP {e.code}, retry in {wait}s\n")
            time.sleep(wait)
    raise SystemExit(f"Hive failed: {last}") from last


def pixelize(img: Image.Image, size: int) -> Image.Image:
    img = img.resize((size, size), Image.Resampling.LANCZOS)
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.12)
    return img.filter(ImageFilter.SHARPEN)


def save_png(path: Path, img: Image.Image, size: int) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    pixelize(img, size).save(path)


def cached(kind: str, name: str, prompt: str, seed: int, size: int) -> Image.Image:
    CACHE.mkdir(parents=True, exist_ok=True)
    cache_path = CACHE / f"{kind}_{name}.png"
    if cache_path.exists():
        return Image.open(cache_path).convert("RGBA")
    print(f"hive {kind}/{name}", flush=True)
    img = hive_generate(prompt, seed)
    img.save(cache_path)
    time.sleep(0.35)
    return img


def tint(base: Image.Image, rgb: tuple[int, int, int], amount: float) -> Image.Image:
    overlay = Image.new("RGBA", base.size, rgb + (int(255 * amount),))
    return Image.alpha_composite(base.convert("RGBA"), overlay)


def stamp_card(base: Image.Image, rgb: tuple[int, int, int], mark: int) -> Image.Image:
    img = pixelize(base, 16)
    img = tint(img, rgb, 0.28)
    d = ImageDraw.Draw(img)
    d.rectangle((1, 1, 14, 14), outline=(232, 200, 106, 255))
    # unique spark from mark so 118 cards are not identical
    d.point((2 + (mark % 12), 2 + ((mark * 3) % 12)), fill=(255, 255, 255, 255))
    d.point((13 - (mark % 12), 13 - ((mark * 5) % 12)), fill=rgb + (255,))
    return img


def jobs() -> list[tuple[str, str, int, str, int]]:
    out: list[tuple[str, str, int, str, int]] = []
    seed = 100
    for name, prompt in GUI.items():
        out.append(("gui", name, seed, prompt, 256))
        seed += 1
    out.append(("icon", "mod", seed, "wonso chemistry mod icon, gold atom on dark navy square, pixel art", 128))
    seed += 1
    for name, prompt in BLOCKS.items():
        out.append(("block", name, seed, prompt, 16))
        seed += 1
    for name, prompt in ITEMS.items():
        out.append(("item", name, seed, prompt, 16))
        seed += 1
    for cat, prompt in CAT_PROMPT.items():
        out.append(("tpl", f"element_{cat}", seed, f"trading card {prompt}", 16))
        seed += 1
    for cat, prompt in MOL_PROMPT.items():
        out.append(("tpl", f"molecule_{cat}", seed, prompt, 16))
        seed += 1
    return out


def main() -> None:
    api_key()
    generated: dict[tuple[str, str], Image.Image] = {}
    for kind, name, seed, prompt, size in jobs():
        img = cached(kind, name, prompt, seed, size)
        generated[(kind, name)] = img
        if kind == "gui":
            save_png(ASSETS / "textures" / "gui" / f"{name}.png", img, size)
        elif kind == "block":
            save_png(ASSETS / "textures" / "block" / f"{name}.png", img, 16)
        elif kind == "icon":
            save_png(ASSETS / "icon.png", img, 128)
        elif kind == "item" and name != "impure_template":
            save_png(ASSETS / "textures" / "item" / f"{name}.png", img, 16)

    elements = json.loads((ROOT / "shared-data/elements.json").read_text())
    impure_tpl = generated.get(("item", "impure_template"))
    for el in elements:
        cat = el.get("category", "unknown")
        tpl = generated.get(("tpl", f"element_{cat}")) or generated.get(("tpl", "element_unknown"))
        if tpl is None:
            continue
        rgb = CAT_RGB.get(cat, (80, 80, 80))
        card = stamp_card(tpl, rgb, el["z"])
        out = ASSETS / "textures" / "item" / f"element_card_{el['symbol'].lower()}.png"
        out.parent.mkdir(parents=True, exist_ok=True)
        card.save(out)
        if impure_tpl is not None:
            dirty = stamp_card(impure_tpl, (90, 80, 70), el["z"] + 50)
            dirty.save(ASSETS / "textures" / "item" / f"impure_{el['symbol'].lower()}.png")

    molecules = json.loads((ROOT / "shared-data/molecules.json").read_text())
    for i, m in enumerate(molecules):
        cat = m.get("category", "elemental")
        tpl = generated.get(("tpl", f"molecule_{cat}")) or generated.get(("tpl", "molecule_elemental"))
        if tpl is None:
            continue
        rgb = {
            "elemental": (90, 140, 190),
            "inorganic": (70, 160, 150),
            "ionic": (230, 230, 230),
            "organic_simple": (210, 160, 70),
        }.get(cat, (90, 140, 190))
        card = stamp_card(tpl, rgb, i + 1)
        path = ASSETS / "textures" / "item" / f"molecule_card_{m['id'].replace('-', '_')}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        card.save(path)

    print("hive assets written")


if __name__ == "__main__":
    main()
