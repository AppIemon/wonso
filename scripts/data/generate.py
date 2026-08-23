#!/usr/bin/env python3
"""Generate wonso shared-data JSON from documented schemas.

Implements ARCHITECTURE_PLAN §6, CONTENT_WRITING_PLAN P0, ELEMENTWAR_REFERENCE §5,
VANILLA_REFINING_SPEC §10, REAL_WORLD_EXTRACTION abundance tiers.
"""

from __future__ import annotations

import json
import math
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "shared-data"

# 대한화학회 한글명 (H~Og)
KO = [
    "",
    "수소", "헬륨", "리튬", "베릴륨", "붕소", "탄소", "질소", "산소", "플루오린", "네온",
    "나트륨", "마그네슘", "알루미늄", "규소", "인", "황", "염소", "아르곤", "칼륨", "칼슘",
    "스칸듐", "타이타늄", "바나듐", "크로뮴", "망가니즈", "철", "코발트", "니켈", "구리", "아연",
    "갈륨", "저마늄", "비소", "셀레늄", "브로민", "크립톤", "루비듐", "스트론튬", "이트륨", "지르코늄",
    "나이오븀", "몰리브데넘", "테크네튬", "루테늄", "로듐", "팔라듐", "은", "카드뮴", "인듐", "주석",
    "안티모니", "텔루륨", "아이오딘", "제논", "세슘", "바륨", "란타넘", "세륨", "프라세오디뮴", "네오디뮴",
    "프로메튬", "사마륨", "유로퓸", "가돌리늄", "터븀", "디스프로슘", "홀뮴", "어븀", "툴륨", "이터븀",
    "루테튬", "하프늄", "탄탈럼", "텅스텐", "레늄", "오스뮴", "이리듐", "백금", "금", "수은",
    "탈륨", "납", "비스무트", "폴로늄", "아스타틴", "라돈", "프랑슘", "라듐", "악티늄", "토륨",
    "프로탁티늄", "우라늄", "넵투늄", "플루토늄", "아메리슘", "퀴륨", "버클륨", "캘리포늄", "아인슈타이늄", "페르뮴",
    "멘델레븀", "노벨륨", "로렌슘", "러더포듐", "더브늄", "시보귬", "보륨", "하슘", "마이트너륨", "다름슈타튬",
    "뢴트게늄", "코페르니슘", "니호늄", "플레로븀", "모스코븀", "리버모륨", "테네신", "오가네손",
]

EN = [
    "",
    "Hydrogen", "Helium", "Lithium", "Beryllium", "Boron", "Carbon", "Nitrogen", "Oxygen", "Fluorine", "Neon",
    "Sodium", "Magnesium", "Aluminium", "Silicon", "Phosphorus", "Sulfur", "Chlorine", "Argon", "Potassium", "Calcium",
    "Scandium", "Titanium", "Vanadium", "Chromium", "Manganese", "Iron", "Cobalt", "Nickel", "Copper", "Zinc",
    "Gallium", "Germanium", "Arsenic", "Selenium", "Bromine", "Krypton", "Rubidium", "Strontium", "Yttrium", "Zirconium",
    "Niobium", "Molybdenum", "Technetium", "Ruthenium", "Rhodium", "Palladium", "Silver", "Cadmium", "Indium", "Tin",
    "Antimony", "Tellurium", "Iodine", "Xenon", "Caesium", "Barium", "Lanthanum", "Cerium", "Praseodymium", "Neodymium",
    "Promethium", "Samarium", "Europium", "Gadolinium", "Terbium", "Dysprosium", "Holmium", "Erbium", "Thulium", "Ytterbium",
    "Lutetium", "Hafnium", "Tantalum", "Tungsten", "Rhenium", "Osmium", "Iridium", "Platinum", "Gold", "Mercury",
    "Thallium", "Lead", "Bismuth", "Polonium", "Astatine", "Radon", "Francium", "Radium", "Actinium", "Thorium",
    "Protactinium", "Uranium", "Neptunium", "Plutonium", "Americium", "Curium", "Berkelium", "Californium", "Einsteinium", "Fermium",
    "Mendelevium", "Nobelium", "Lawrencium", "Rutherfordium", "Dubnium", "Seaborgium", "Bohrium", "Hassium", "Meitnerium", "Darmstadtium",
    "Roentgenium", "Copernicium", "Nihonium", "Flerovium", "Moscovium", "Livermorium", "Tennessine", "Oganesson",
]

SYM = [
    "",
    "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne",
    "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca",
    "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn",
    "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr",
    "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn",
    "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd",
    "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb",
    "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg",
    "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th",
    "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm",
    "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt", "Ds",
    "Rg", "Cn", "Nh", "Fl", "Mc", "Lv", "Ts", "Og",
]

# IUPAC standard atomic weights (2021/2024). None = no stable isotope / synthetic.
MASS: list[float | None] = [None] + [
    1.008, 4.0026, 6.94, 9.0122, 10.81, 12.011, 14.007, 15.999, 18.998, 20.180,
    22.990, 24.305, 26.982, 28.085, 30.974, 32.06, 35.45, 39.948, 39.098, 40.078,
    44.956, 47.867, 50.942, 51.996, 54.938, 55.845, 58.933, 58.693, 63.546, 65.38,
    69.723, 72.630, 74.922, 78.971, 79.904, 83.798, 85.468, 87.62, 88.906, 91.224,
    92.906, 95.95, None, 101.07, 102.91, 106.42, 107.87, 112.41, 114.82, 118.71,
    121.76, 127.60, 126.90, 131.29, 132.91, 137.33, 138.91, 140.12, 140.91, 144.24,
    None, 150.36, 151.96, 157.25, 158.93, 162.50, 164.93, 167.26, 168.93, 173.05,
    174.97, 178.49, 180.95, 183.84, 186.21, 190.23, 192.22, 195.08, 196.97, 200.59,
    204.38, 207.2, 208.98, None, None, None, None, None, None, 232.04,
    231.04, 238.03, None, None, None, None, None, None, None, None,
    None, None, None, None, None, None, None, None, None, None,
    None, None, None, None, None, None, None, None,
]

YEAR = [None] + [
    "고대", "1868", "1817", "1798", "1808", "고대", "1772", "1774", "1886", "1898",
    "1807", "1755", "1825", "1824", "1669", "고대", "1774", "1894", "1807", "고대",
    "1879", "1791", "1801", "1797", "1774", "고대", "1735", "1751", "고대", "고대",
    "1875", "1886", "고대", "1817", "1826", "1898", "1861", "1790", "1794", "1789",
    "1801", "1781", "1937", "1844", "1803", "1803", "고대", "1817", "1863", "고대",
    "고대", "1782", "1811", "1898", "1860", "1808", "1839", "1803", "1885", "1885",
    "1945", "1879", "1901", "1880", "1843", "1886", "1878", "1842", "1879", "1878",
    "1907", "1923", "1802", "1783", "1925", "1803", "1803", "1735", "고대", "고대",
    "1861", "고대", "1753", "1898", "1940", "1900", "1939", "1898", "1899", "1828",
    "1913", "1789", "1940", "1940", "1944", "1944", "1949", "1950", "1952", "1952",
    "1955", "1958", "1961", "1969", "1967", "1974", "1976", "1984", "1982", "1994",
    "1994", "1996", "2003", "1998", "2003", "2000", "2010", "2002",
]

# Electronegativity (Pauling). None = unknown / not applicable.
ENEG: list[float | None] = [None] + [
    2.20, None, 0.98, 1.57, 2.04, 2.55, 3.04, 3.44, 3.98, None,
    0.93, 1.31, 1.61, 1.90, 2.19, 2.58, 3.16, None, 0.82, 1.00,
    1.36, 1.54, 1.63, 1.66, 1.55, 1.83, 1.88, 1.91, 1.90, 1.65,
    1.81, 2.01, 2.18, 2.55, 2.96, 3.00, 0.82, 0.95, 1.22, 1.33,
    1.60, 2.16, 1.90, 2.20, 2.28, 2.20, 1.93, 1.69, 1.78, 1.96,
    2.05, 2.10, 2.66, 2.60, 0.79, 0.89, 1.10, 1.12, 1.13, 1.14,
    1.13, 1.17, 1.20, 1.20, 1.10, 1.22, 1.23, 1.24, 1.25, 1.10,
    1.27, 1.30, 1.50, 2.36, 1.90, 2.20, 2.20, 2.28, 2.54, 2.00,
    1.62, 1.87, 2.02, 2.00, 2.20, 2.20, 0.70, 0.90, 1.10, 1.30,
    1.50, 1.38, 1.36, 1.28, 1.13, 1.28, 1.30, 1.30, 1.30, 1.30,
    1.30, 1.30, 1.30, None, None, None, None, None, None, None,
    None, None, None, None, None, None, None, None,
]

# Density g/cm3 (approx, STP / common allotrope). None unknown.
DENSITY: list[float | None] = [None] + [
    0.0000899, 0.0001785, 0.534, 1.85, 2.34, 2.267, 0.001251, 0.001429, 0.001696, 0.000900,
    0.968, 1.738, 2.70, 2.329, 1.82, 2.067, 0.003214, 0.001784, 0.862, 1.55,
    2.985, 4.506, 6.11, 7.15, 7.21, 7.874, 8.90, 8.908, 8.96, 7.14,
    5.91, 5.323, 5.727, 4.81, 3.1028, 0.003749, 1.532, 2.64, 4.472, 6.52,
    8.57, 10.28, 11.5, 12.45, 12.41, 12.023, 10.49, 8.65, 7.31, 7.287,
    6.685, 6.232, 4.933, 0.005887, 1.93, 3.51, 6.145, 6.77, 6.773, 7.007,
    7.26, 7.52, 5.243, 7.90, 8.23, 8.54, 8.79, 9.066, 9.32, 6.90,
    9.84, 13.31, 16.65, 19.25, 21.02, 22.59, 22.56, 21.45, 19.32, 13.534,
    11.85, 11.34, 9.78, 9.196, 6.35, 0.00973, 1.87, 5.5, 10.07, 11.72,
    15.37, 19.05, 20.45, 19.82, 13.69, 13.51, 14.78, 15.1, 8.84, None,
    None, None, None, None, None, None, None, None, None, None,
    None, None, None, None, None, None, None, None,
]

# Most common / representative mass number (for defaultNeutrons)
MASS_NUMBER = [0] + [
    1, 4, 7, 9, 11, 12, 14, 16, 19, 20,
    23, 24, 27, 28, 31, 32, 35, 40, 39, 40,
    45, 48, 51, 52, 55, 56, 59, 58, 63, 64,
    69, 74, 75, 80, 79, 84, 85, 88, 89, 90,
    93, 98, 98, 102, 103, 106, 107, 114, 115, 120,
    121, 130, 127, 132, 133, 138, 139, 140, 141, 142,
    145, 152, 153, 158, 159, 164, 165, 166, 169, 174,
    175, 180, 181, 184, 187, 192, 193, 195, 197, 202,
    205, 208, 209, 209, 210, 222, 223, 226, 227, 232,
    231, 238, 237, 244, 243, 247, 247, 251, 252, 257,
    258, 259, 266, 267, 268, 269, 270, 269, 278, 281,
    282, 285, 286, 289, 290, 293, 294, 294,
]

# Condensed electron configuration (교육용 축약형)
ECONF = [None] + [
    "1s¹", "1s²", "[He] 2s¹", "[He] 2s²", "[He] 2s² 2p¹", "[He] 2s² 2p²", "[He] 2s² 2p³", "[He] 2s² 2p⁴", "[He] 2s² 2p⁵", "[He] 2s² 2p⁶",
    "[Ne] 3s¹", "[Ne] 3s²", "[Ne] 3s² 3p¹", "[Ne] 3s² 3p²", "[Ne] 3s² 3p³", "[Ne] 3s² 3p⁴", "[Ne] 3s² 3p⁵", "[Ne] 3s² 3p⁶", "[Ar] 4s¹", "[Ar] 4s²",
    "[Ar] 3d¹ 4s²", "[Ar] 3d² 4s²", "[Ar] 3d³ 4s²", "[Ar] 3d⁵ 4s¹", "[Ar] 3d⁵ 4s²", "[Ar] 3d⁶ 4s²", "[Ar] 3d⁷ 4s²", "[Ar] 3d⁸ 4s²", "[Ar] 3d¹⁰ 4s¹", "[Ar] 3d¹⁰ 4s²",
    "[Ar] 3d¹⁰ 4s² 4p¹", "[Ar] 3d¹⁰ 4s² 4p²", "[Ar] 3d¹⁰ 4s² 4p³", "[Ar] 3d¹⁰ 4s² 4p⁴", "[Ar] 3d¹⁰ 4s² 4p⁵", "[Ar] 3d¹⁰ 4s² 4p⁶", "[Kr] 5s¹", "[Kr] 5s²", "[Kr] 4d¹ 5s²", "[Kr] 4d² 5s²",
    "[Kr] 4d⁴ 5s¹", "[Kr] 4d⁵ 5s¹", "[Kr] 4d⁵ 5s²", "[Kr] 4d⁷ 5s¹", "[Kr] 4d⁸ 5s¹", "[Kr] 4d¹⁰", "[Kr] 4d¹⁰ 5s¹", "[Kr] 4d¹⁰ 5s²", "[Kr] 4d¹⁰ 5s² 5p¹", "[Kr] 4d¹⁰ 5s² 5p²",
    "[Kr] 4d¹⁰ 5s² 5p³", "[Kr] 4d¹⁰ 5s² 5p⁴", "[Kr] 4d¹⁰ 5s² 5p⁵", "[Kr] 4d¹⁰ 5s² 5p⁶", "[Xe] 6s¹", "[Xe] 6s²", "[Xe] 5d¹ 6s²", "[Xe] 4f¹ 5d¹ 6s²", "[Xe] 4f³ 6s²", "[Xe] 4f⁴ 6s²",
    "[Xe] 4f⁵ 6s²", "[Xe] 4f⁶ 6s²", "[Xe] 4f⁷ 6s²", "[Xe] 4f⁷ 5d¹ 6s²", "[Xe] 4f⁹ 6s²", "[Xe] 4f¹⁰ 6s²", "[Xe] 4f¹¹ 6s²", "[Xe] 4f¹² 6s²", "[Xe] 4f¹³ 6s²", "[Xe] 4f¹⁴ 6s²",
    "[Xe] 4f¹⁴ 5d¹ 6s²", "[Xe] 4f¹⁴ 5d² 6s²", "[Xe] 4f¹⁴ 5d³ 6s²", "[Xe] 4f¹⁴ 5d⁴ 6s²", "[Xe] 4f¹⁴ 5d⁵ 6s²", "[Xe] 4f¹⁴ 5d⁶ 6s²", "[Xe] 4f¹⁴ 5d⁷ 6s²", "[Xe] 4f¹⁴ 5d⁹ 6s¹", "[Xe] 4f¹⁴ 5d¹⁰ 6s¹", "[Xe] 4f¹⁴ 5d¹⁰ 6s²",
    "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p¹", "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p²", "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p³", "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p⁴", "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p⁵", "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p⁶", "[Rn] 7s¹", "[Rn] 7s²", "[Rn] 6d¹ 7s²", "[Rn] 6d² 7s²",
    "[Rn] 5f² 6d¹ 7s²", "[Rn] 5f³ 6d¹ 7s²", "[Rn] 5f⁴ 6d¹ 7s²", "[Rn] 5f⁶ 7s²", "[Rn] 5f⁷ 7s²", "[Rn] 5f⁷ 6d¹ 7s²", "[Rn] 5f⁹ 7s²", "[Rn] 5f¹⁰ 7s²", "[Rn] 5f¹¹ 7s²", "[Rn] 5f¹² 7s²",
    "[Rn] 5f¹³ 7s²", "[Rn] 5f¹⁴ 7s²", "[Rn] 5f¹⁴ 7s² 7p¹", "[Rn] 5f¹⁴ 6d² 7s²", "[Rn] 5f¹⁴ 6d³ 7s²", "[Rn] 5f¹⁴ 6d⁴ 7s²", "[Rn] 5f¹⁴ 6d⁵ 7s²", "[Rn] 5f¹⁴ 6d⁶ 7s²", "[Rn] 5f¹⁴ 6d⁷ 7s²", "[Rn] 5f¹⁴ 6d⁸ 7s²",
    "[Rn] 5f¹⁴ 6d⁹ 7s²", "[Rn] 5f¹⁴ 6d¹⁰ 7s²", "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p¹", "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p²", "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p³", "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p⁴", "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p⁵", "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p⁶",
]

ALIASES: dict[int, list[str]] = {
    1: ["하이드로젠"], 6: ["카본"], 8: ["옥시전"], 9: ["불소", "플로린"],
    11: ["소듐"], 13: ["알류미늄"], 14: ["실리콘"], 16: ["설퍼"],
    17: ["클로린"], 19: ["포타슘"], 22: ["티타늄"], 24: ["크롬"],
    25: ["망간"], 26: ["아이언"], 29: ["코퍼"], 32: ["게르마늄"],
    35: ["브롬"], 47: ["실버"], 53: ["요오드", "요드"], 74: ["울프람"],
    79: ["골드"], 80: ["머큐리"], 82: ["납금속"], 92: ["우라니움"],
    115: ["머늄"], 118: ["오가네슨"],
}

# Crust abundance mass % (approx). None = synthetic / trace unknown.
ABUNDANCE: dict[int, float | None] = {
    1: 0.14, 2: 0.0000008, 3: 0.002, 4: 0.00028, 5: 0.001, 6: 0.02, 7: 0.002, 8: 46.1,
    9: 0.054, 10: 0.0000005, 11: 2.3, 12: 2.9, 13: 8.23, 14: 28.2, 15: 0.1, 16: 0.042,
    17: 0.017, 18: 0.00035, 19: 1.5, 20: 5.0, 22: 0.56, 24: 0.014, 25: 0.11, 26: 5.6,
    28: 0.0084, 29: 0.0068, 30: 0.0078, 47: 0.000008, 79: 0.0000004, 82: 0.001, 92: 0.00018,
}

# Extra isotopes for education-critical elements
EXTRA_ISOTOPES: dict[int, list[dict]] = {
    1: [
        {"massNumber": 1, "neutrons": 0, "abundance": 99.985, "name": "¹H (경수소)"},
        {"massNumber": 2, "neutrons": 1, "abundance": 0.015, "name": "²H (중수소)"},
        {"massNumber": 3, "neutrons": 2, "abundance": None, "name": "³H (삼중수소)"},
    ],
    6: [
        {"massNumber": 12, "neutrons": 6, "abundance": 98.93, "name": "¹²C"},
        {"massNumber": 13, "neutrons": 7, "abundance": 1.07, "name": "¹³C"},
        {"massNumber": 14, "neutrons": 8, "abundance": None, "name": "¹⁴C"},
    ],
    8: [
        {"massNumber": 16, "neutrons": 8, "abundance": 99.76, "name": "¹⁶O"},
        {"massNumber": 18, "neutrons": 10, "abundance": 0.20, "name": "¹⁸O"},
    ],
    17: [
        {"massNumber": 35, "neutrons": 18, "abundance": 75.76, "name": "³⁵Cl"},
        {"massNumber": 37, "neutrons": 20, "abundance": 24.24, "name": "³⁷Cl"},
    ],
    26: [
        {"massNumber": 56, "neutrons": 30, "abundance": 91.75, "name": "⁵⁶Fe"},
        {"massNumber": 54, "neutrons": 28, "abundance": 5.85, "name": "⁵⁴Fe"},
    ],
    92: [
        {"massNumber": 238, "neutrons": 146, "abundance": 99.27, "name": "²³⁸U"},
        {"massNumber": 235, "neutrons": 143, "abundance": 0.72, "name": "²³⁵U"},
    ],
}


def period_of(z: int) -> int:
    if z <= 2:
        return 1
    if z <= 10:
        return 2
    if z <= 18:
        return 3
    if z <= 36:
        return 4
    if z <= 54:
        return 5
    if z <= 86:
        return 6
    return 7


def group_of(z: int) -> int | None:
    if 57 <= z <= 71 or 89 <= z <= 103:
        return None
    table = {
        1: 1, 2: 18,
        3: 1, 4: 2, 5: 13, 6: 14, 7: 15, 8: 16, 9: 17, 10: 18,
        11: 1, 12: 2, 13: 13, 14: 14, 15: 15, 16: 16, 17: 17, 18: 18,
    }
    if z in table:
        return table[z]
    if 19 <= z <= 36:
        return z - 18
    if 37 <= z <= 54:
        return z - 36
    if 55 <= z <= 56:
        return z - 54
    if 72 <= z <= 86:
        return z - 68
    if 87 <= z <= 88:
        return z - 86
    if 104 <= z <= 118:
        return z - 100
    return None


def category_of(z: int) -> str:
    if z in (1,):
        return "reactive_nonmetal"
    if z in (3, 11, 19, 37, 55, 87):
        return "alkali_metal"
    if z in (4, 12, 20, 38, 56, 88):
        return "alkaline_earth_metal"
    if z in (5, 14, 32, 33, 51, 52, 84):
        return "metalloid"
    if z in (6, 7, 8, 15, 16, 34):
        return "reactive_nonmetal"
    if z in (9, 17, 35, 53, 85, 117):
        return "halogen"
    if z in (2, 10, 18, 36, 54, 86, 118):
        return "noble_gas"
    if 21 <= z <= 30 or 39 <= z <= 48 or 72 <= z <= 80 or 104 <= z <= 112:
        return "transition_metal"
    if z in (13, 31, 49, 50, 81, 82, 83, 113, 114, 115, 116):
        return "post_transition_metal"
    if 57 <= z <= 71:
        return "lanthanide"
    if 89 <= z <= 103:
        return "actinide"
    return "unknown"


def state_of(z: int) -> str:
    if z in (2, 7, 8, 9, 10, 17, 18, 36, 54, 86):
        return "gas"
    if z in (35, 80):
        return "liquid"
    if z >= 99:
        return "unknown"
    return "solid"


def display_pos(z: int) -> tuple[int, int]:
    """Standard 18-col table + lanthanide/actinide rows 8–9."""
    if 57 <= z <= 71:
        return 8, z - 54  # La col 3 … Lu col 17
    if 89 <= z <= 103:
        return 9, z - 86
    g = group_of(z)
    p = period_of(z)
    if g is None:
        return p, 3
    return p, g


def valence_electrons(z: int, group: int | None) -> int:
    if group is None:
        return 3 if 57 <= z <= 71 or 89 <= z <= 103 else 0
    if group == 18:
        return 0 if z == 2 else 8
    if 3 <= group <= 12:
        # transition: rough s+d valence
        return min(group, 8)
    if group <= 2:
        return group
    return group - 10


def shell_distribution(z: int) -> list[int]:
    """n-shell electron counts (Bohr-style), using actual configuration totals."""
    max_e = [2, 8, 18, 32, 32, 18, 8]
    remaining = z
    shells: list[int] = []
    for cap in max_e:
        if remaining <= 0:
            break
        take = min(cap, remaining)
        # Keep educational 2-8-8 for early periods when possible
        shells.append(take)
        remaining -= take
    # Fix common educational cases (K=2,8,8,1 not 2,8,9)
    # Recalculate with Madelung-ish n-level grouping from configuration is heavy;
    # use a known table for Z<=20 then n-level fill.
    simple = {
        1: [1], 2: [2], 3: [2, 1], 4: [2, 2], 5: [2, 3], 6: [2, 4], 7: [2, 5], 8: [2, 6],
        9: [2, 7], 10: [2, 8], 11: [2, 8, 1], 12: [2, 8, 2], 13: [2, 8, 3], 14: [2, 8, 4],
        15: [2, 8, 5], 16: [2, 8, 6], 17: [2, 8, 7], 18: [2, 8, 8], 19: [2, 8, 8, 1],
        20: [2, 8, 8, 2], 26: [2, 8, 14, 2], 29: [2, 8, 18, 1], 36: [2, 8, 18, 8],
    }
    return simple.get(z, shells)


def abundance_tier(z: int) -> str:
    if z in (8,):
        return "T0"
    if z in (1, 11, 12, 13, 14, 16, 19, 20, 26):
        return "T1"
    if z in (6, 7, 9, 15, 17, 22, 24, 25, 28, 29, 30):
        return "T2"
    if z <= 83 and z not in (43, 61):
        return "T3"
    return "T4"


def rarity_of(z: int) -> str:
    t = abundance_tier(z)
    return {"T0": "common", "T1": "common", "T2": "uncommon", "T3": "rare", "T4": "legendary"}[t]


def card_stats(z: int) -> dict:
    cat = category_of(z)
    atk = 4
    hp = 6
    if cat == "alkali_metal":
        atk, hp = 8, 4
    elif cat == "halogen":
        atk, hp = 9, 5
    elif cat == "noble_gas":
        atk, hp = 2, 12
    elif cat == "transition_metal":
        atk, hp = 6, 8
    elif cat == "reactive_nonmetal":
        atk, hp = 5, 6
    elif cat in ("lanthanide", "actinide"):
        atk, hp = 7, 7
    atk += z // 30
    hp += z // 25
    cost = 1 + z // 20 + ({"common": 0, "uncommon": 1, "rare": 2, "legendary": 4}[rarity_of(z)])
    return {
        "attack": atk,
        "health": hp,
        "rarity": rarity_of(z),
        "cost": cost,
        "category": cat,
    }


def battle_stats(z: int) -> dict:
    card = card_stats(z)
    return {
        "hp": card["health"] * 6,
        "attack": card["attack"] + 2,
        "defense": 4 + (z % 7),
        "speed": 6 + ((118 - z) % 11),
        "rarity": card["rarity"],
    }


def description_ko(z: int) -> str:
    cat_ko = {
        "alkali_metal": "알칼리금속",
        "alkaline_earth_metal": "알칼리토금속",
        "transition_metal": "전이금속",
        "post_transition_metal": "전이후금속",
        "metalloid": "준금속",
        "reactive_nonmetal": "반응성 비금속",
        "halogen": "할로겐",
        "noble_gas": "비활성 기체",
        "lanthanide": "란타넘족",
        "actinide": "악티늄족",
        "unknown": "미분류",
    }
    extra = ""
    if z == 1:
        extra = " 원소(종류)·원자(한 입자)·분자(H₂, H₂O)를 구분하는 모범 사례입니다."
    elif z == 118:
        extra = " 초중원소로 반감기가 매우 짧고, 안정한 분자는 알려지지 않았습니다."
    elif z in (35, 80):
        extra = " 상온에서 액체인 드문 원소입니다."
    elif z >= 95:
        extra = " 실험실에서만 합성되며 데이터는 최신 연구 기준입니다."
    g = group_of(z)
    gtxt = f"{g}족" if g else "란타넘/악티늄 계열"
    return (
        f"{KO[z]}({SYM[z]}, Z={z})는 주기율표 {period_of(z)}주기 {gtxt}의 "
        f"{cat_ko[category_of(z)]}입니다.{extra}"
    )


def extraction_route(z: int) -> dict:
    """REAL_WORLD_EXTRACTION-aligned routes, translated to vanilla."""
    routes = {
        1: {"process": "전기분해", "vanilla": ["water_bucket", "redstone"], "compound": "H2O", "noteKo": "물은 수소 원소가 아니라 H₂O. 전기분해로 H와 O를 나눈다."},
        8: {"process": "전기분해", "vanilla": ["water_bucket", "redstone"], "compound": "H2O", "noteKo": "산소는 물 전기분해 또는 공기 분별로 얻는다."},
        6: {"process": "건조분해", "vanilla": ["charcoal", "coal"], "compound": "C", "noteKo": "나무와 석탄은 탄소의 저장고."},
        14: {"process": "고온 환원", "vanilla": ["sand", "charcoal"], "compound": "SiO2", "noteKo": "모래는 규소가 아니라 이산화규소. 탄소로 산소를 빼야 한다."},
        26: {"process": "정광", "vanilla": ["raw_iron", "coal"], "compound": "Fe2O3", "noteKo": "철 광석을 그냥 구워먹기엔 아깝다. 분말 루트가 효율적이다."},
        29: {"process": "정광", "vanilla": ["raw_copper", "coal"], "compound": "CuCO3", "noteKo": "구리 광석 → 산화구리 → 환원."},
        11: {"process": "전기분해", "vanilla": ["kelp", "sand", "water_bucket"], "compound": "NaCl", "noteKo": "바다와 모래 사이 어딘가에 나트륨이… 식염 전기분해."},
        17: {"process": "전기분해", "vanilla": ["kelp", "water_bucket"], "compound": "NaCl", "noteKo": "염소는 나트륨과 같은 브라인에서 따로 순화된다."},
        13: {"process": "전기분해", "vanilla": ["clay_ball", "redstone"], "compound": "Al2O3", "noteKo": "현실도 알루미늄은 전기 지옥. 점토→알루미나→전해."},
        20: {"process": "열분해", "vanilla": ["calcite", "bone"], "compound": "CaCO3", "noteKo": "해얼돌·뼈는 탄산칼슘/인산칼슘."},
        16: {"process": "정광", "vanilla": ["blaze_powder", "nether_quartz"], "compound": "S", "noteKo": "황은 네더 황화물·블레이즈에서."},
        7: {"process": "증류", "vanilla": ["glass_bottle"], "compound": "N2", "noteKo": "공기의 78%는 질소. 분별 증류."},
        15: {"process": "산 처리", "vanilla": ["bone"], "compound": "Ca3(PO4)2", "noteKo": "뼈를 분쇄하면 인회석 루트."},
        79: {"process": "미량 추출", "vanilla": ["raw_gold"], "compound": "Au", "noteKo": "금 광석은 정제하기도 전에 귀하다."},
    }
    if z in routes:
        base = routes[z]
    elif z >= 104:
        base = {"process": "핵융합", "vanilla": ["periodite_ore"], "compound": None, "noteKo": "합성 원소. Fusion Chamber만 가능."}
    elif 57 <= z <= 71 or 89 <= z <= 103:
        base = {"process": "족 분리", "vanilla": ["periodite_ore"], "compound": "mixed_rare_earth", "noteKo": "희토류/악티늄족은 다단계 침출이 필요하다."}
    elif category_of(z) == "noble_gas":
        base = {"process": "증류", "vanilla": ["glass_bottle"], "compound": "liquid_air", "noteKo": "비활성 기체는 공기 분별 증류 루트."}
    elif category_of(z) == "halogen":
        base = {"process": "증류", "vanilla": ["kelp", "water_bucket"], "compound": "brine", "noteKo": "할로겐은 브라인·형석 루트."}
    else:
        base = {"process": "정광", "vanilla": ["cobblestone", "periodite_ore"], "compound": None, "noteKo": f"{KO[z]}는 지각 미량 또는 주기율광석 보조 루트."}
    return {
        "z": z,
        "tier": abundance_tier(z),
        "crustMassPercent": ABUNDANCE.get(z),
        **base,
    }


def build_elements() -> list[dict]:
    out = []
    for z in range(1, 119):
        row, col = display_pos(z)
        g = group_of(z)
        mass = MASS[z]
        rec = {
            "z": z,
            "symbol": SYM[z],
            "nameKo": KO[z],
            "nameEn": EN[z],
            "atomicMass": mass,
            "massUncertainty": None if mass is not None else "synthetic",
            "period": period_of(z),
            "group": g,
            "category": category_of(z),
            "stateAtRoomTemp": state_of(z),
            "electronConfiguration": ECONF[z],
            "electronegativity": ENEG[z],
            "density": DENSITY[z],
            "yearDiscovered": YEAR[z],
            "displayRow": row,
            "displayColumn": col,
            "aliasesKo": ALIASES.get(z, []),
            "hasKnownMolecules": z < 104 and z != 118,
            "descriptionKo": description_ko(z),
            "card": card_stats(z),
            "battle": battle_stats(z),
            "extraction": extraction_route(z),
        }
        out.append(rec)
    return out


def build_atoms() -> list[dict]:
    out = []
    for z in range(1, 119):
        mn = MASS_NUMBER[z]
        neutrons = mn - z
        isotopes = EXTRA_ISOTOPES.get(z, [{
            "massNumber": mn,
            "neutrons": neutrons,
            "abundance": None if MASS[z] is None else 100.0,
            "name": f"{mn}{SYM[z]}",
        }])
        out.append({
            "z": z,
            "defaultProtons": z,
            "defaultNeutrons": neutrons,
            "defaultElectrons": z,
            "shellDistribution": shell_distribution(z),
            "valenceElectrons": valence_electrons(z, group_of(z)),
            "isotopes": isotopes,
            "ions": [],
        })
    return out


SUB = str.maketrans("0123456789+-", "₀₁₂₃₄₅₆₇₈₉₊₋")


def to_display(formula: str) -> str:
    out = []
    i = 0
    while i < len(formula):
        ch = formula[i]
        if ch.isdigit() and i > 0:
            out.append(ch.translate(SUB))
        elif ch in "+-" and i > 0:
            out.append(ch.translate(SUB))
        else:
            out.append(ch)
        i += 1
    return "".join(out)


def parse_formula(formula: str) -> list[dict]:
    """Parse simple formulas including one-level parentheses and charge suffix."""
    body = re.sub(r"[+\-]\d*$", "", formula)
    body = re.sub(r"[+\-]$", "", body)
    tokens: list[tuple[str, int]] = []

    def add(sym: str, n: int) -> None:
        tokens.append((sym, n))

    i = 0
    while i < len(body):
        if body[i] == "(":
            j = body.index(")", i)
            inner = parse_formula(body[i + 1 : j])
            k = j + 1
            num = ""
            while k < len(body) and body[k].isdigit():
                num += body[k]
                k += 1
            mul = int(num) if num else 1
            for part in inner:
                add(part["symbol"], part["count"] * mul)
            i = k
            continue
        m = re.match(r"([A-Z][a-z]?)(\d*)", body[i:])
        if not m:
            raise ValueError(f"Cannot parse {formula} at {body[i:]}")
        add(m.group(1), int(m.group(2) or 1))
        i += m.end()

    merged: dict[str, int] = {}
    for s, n in tokens:
        merged[s] = merged.get(s, 0) + n
    return [{"symbol": s, "count": n} for s, n in merged.items()]


TIER_A = [
    # 단원질
    ("h2", "H2", "수소", "Hydrogen", "elemental", "linear", 1, 2,
     "수소 분자(H₂)는 수소 원자 2개가 결합한 분자입니다. H₂의 2는 아래첨자로, 한 분자 안에 수소 원자가 2개라는 뜻입니다."),
    ("o2", "O2", "산소", "Oxygen", "elemental", "linear", 1, 2,
     "산소 분자(O₂)는 산소 원자 2개의 결합체입니다. 원소 기호 O와 분자식 O₂를 혼동하지 마세요."),
    ("n2", "N2", "질소", "Nitrogen", "elemental", "linear", 1, 2,
     "질소 분자(N₂)는 공기 부피의 약 78%를 차지하는 분자입니다."),
    ("cl2", "Cl2", "염소", "Chlorine", "elemental", "linear", 1, 3,
     "염소 분자(Cl₂)는 할로겐 이원자 분자입니다."),
    ("f2", "F2", "플루오린", "Fluorine", "elemental", "linear", 2, 3,
     "플루오린 분자(F₂)는 반응성이 매우 큰 할로겐 분자입니다."),
    ("br2", "Br2", "브로민", "Bromine", "elemental", "linear", 2, 3,
     "브로민(Br₂)은 상온에서 액체인 이원자 분자입니다."),
    ("i2", "I2", "아이오딘", "Iodine", "elemental", "linear", 2, 3,
     "아이오딘(I₂)은 할로겐 이원자 분자입니다."),
    ("o3", "O3", "오존", "Ozone", "elemental", "bent", 2, 4,
     "오존(O₃)은 산소 원자 3개가 결합한 분자로, O₂와 다른 물질입니다."),
    # 무기
    ("water", "H2O", "물", "Water", "inorganic", "bent", 1, 3,
     "물(H₂O)은 수소 원자 2개와 산소 원자 1개가 결합한 분자입니다. H₂O에서 숫자 2는 아래첨자로, 산소 한 분자 안에 수소가 2개 있다는 뜻입니다."),
    ("carbon-dioxide", "CO2", "이산화탄소", "Carbon dioxide", "inorganic", "linear", 1, 2,
     "이산화탄소(CO₂)는 탄소 1개와 산소 2개의 분자입니다. 아래첨자 2는 산소 원자 수입니다."),
    ("ammonia", "NH3", "암모니아", "Ammonia", "inorganic", "trigonal_pyramidal", 1, 3,
     "암모니아(NH₃)는 질소 1개와 수소 3개가 결합한 분자입니다."),
    ("hydrogen-chloride", "HCl", "염화 수소", "Hydrogen chloride", "inorganic", "linear", 1, 3,
     "염화 수소(HCl)는 수소와 염소가 1:1로 결합한 분자입니다."),
    ("sodium-chloride", "NaCl", "염화 나트륨", "Sodium chloride", "ionic", "ionic", 1, 4,
     "염화 나트륨(NaCl)은 이온 결합 화합물입니다. 화학식의 숫자는 원자 개수 비입니다."),
    ("sulfur-dioxide", "SO2", "이산화 황", "Sulfur dioxide", "inorganic", "bent", 2, 3,
     "이산화 황(SO₂)은 황 1개와 산소 2개의 분자입니다."),
    ("nitrogen-dioxide", "NO2", "이산화 질소", "Nitrogen dioxide", "inorganic", "bent", 2, 3,
     "이산화 질소(NO₂)에서 아래첨자 2는 산소 원자 수입니다."),
    ("hydrogen-sulfide", "H2S", "황화 수소", "Hydrogen sulfide", "inorganic", "bent", 2, 3,
     "황화 수소(H₂S)는 수소 2개와 황 1개의 분자입니다."),
    ("carbon-monoxide", "CO", "일산화 탄소", "Carbon monoxide", "inorganic", "linear", 2, 2,
     "일산화 탄소(CO)는 탄소와 산소가 1:1인 분자입니다. CO₂와 구분하세요."),
    ("silicon-dioxide", "SiO2", "이산화 규소", "Silicon dioxide", "inorganic", "network", 1, 2,
     "이산화 규소(SiO₂)는 모래의 주성분입니다. 규소 원소(Si)와 다릅니다."),
    ("white-phosphorus", "P4", "흰 인", "White phosphorus", "elemental", "tetrahedral", 2, 4,
     "흰 인(P₄)은 인 원자 4개가 모인 분자입니다. 원자 수가 2보다 커도 분자입니다."),
    ("octasulfur", "S8", "황 분자", "Octasulfur", "elemental", "ring", 2, 4,
     "황(S₈)은 원자 8개가 고리를 이룬 분자입니다."),
    ("sulfuric-acid", "H2SO4", "황산", "Sulfuric acid", "inorganic", "tetrahedral", 2, 7,
     "황산(H₂SO₄)은 수소 2, 황 1, 산소 4가 모인 분자입니다. 아래첨자를 각각 세어 보세요."),
    ("nitric-acid", "HNO3", "질산", "Nitric acid", "inorganic", "trigonal_planar", 2, 5,
     "질산(HNO₃)은 수소·질소·산소로 이루어진 산 분자입니다."),
    ("phosphoric-acid", "H3PO4", "인산", "Phosphoric acid", "inorganic", "tetrahedral", 2, 8,
     "인산(H₃PO₄)에서 아래첨자 3과 4는 각각 수소·산소 원자 수입니다."),
    ("calcium-carbonate", "CaCO3", "탄산 칼슘", "Calcium carbonate", "ionic", "ionic", 2, 5,
     "탄산 칼슘(CaCO₃)은 해얼돌·석회석의 주성분입니다."),
    # 유기
    ("methane", "CH4", "메테인", "Methane", "organic_simple", "tetrahedral", 1, 6,
     "메테인(CH₄)은 탄소 1개와 수소 4개의 가장 간단한 유기 분자입니다."),
    ("ethane", "C2H6", "에테인", "Ethane", "organic_simple", "tetrahedral", 2, 4,
     "에테인(C₂H₆)에서 아래첨자 2와 6은 탄소·수소 원자 수입니다."),
    ("ethene", "C2H4", "에텐", "Ethene", "organic_simple", "planar", 2, 4,
     "에텐(C₂H₄)은 이중 결합을 가진 간단한 유기 분자입니다."),
    ("ethyne", "C2H2", "에타인", "Ethyne", "organic_simple", "linear", 2, 4,
     "에타인(C₂H₂)은 탄소 2개와 수소 2개의 선형 분자입니다."),
    ("propane", "C3H8", "프로페인", "Propane", "organic_simple", "tetrahedral", 2, 4,
     "프로페인(C₃H₈)은 탄소 3개 사슬의 탄화수소 분자입니다."),
    ("methanol", "CH4O", "메탄올", "Methanol", "organic_simple", "tetrahedral", 2, 5,
     "메탄올(CH₄O, CH₃OH)은 탄소·수소·산소가 모인 알코올 분자입니다."),
    ("ethanol", "C2H6O", "에탄올", "Ethanol", "organic_simple", "tetrahedral", 1, 6,
     "에탄올(C₂H₆O)은 탄소 2, 수소 6, 산소 1의 분자입니다. 계수와 아래첨자를 구분하세요."),
    ("acetic-acid", "C2H4O2", "아세트산", "Acetic acid", "organic_simple", "planar", 2, 6,
     "아세트산(C₂H₄O₂, CH₃COOH)은 탄소 2, 수소 4, 산소 2의 분자입니다."),
    ("glucose", "C6H12O6", "포도당", "Glucose", "organic_simple", "ring", 2, 12,
     "포도당(C₆H₁₂O₆)은 탄소 6, 수소 12, 산소 6이 결합한 분자입니다. 큰 아래첨자도 원자 개수입니다."),
    ("acetone", "C3H6O", "아세톤", "Acetone", "organic_simple", "planar", 2, 5,
     "아세톤(C₃H₆O)은 탄소 3, 수소 6, 산소 1의 케톤 분자입니다."),
    # 특수
    ("hydrogen-peroxide", "H2O2", "과산화 수소", "Hydrogen peroxide", "inorganic", "bent", 2, 4,
     "과산화 수소(H₂O₂)는 물(H₂O)과 원자 수가 다릅니다. 산소가 2개입니다."),
    ("sodium-hydroxide", "NaOH", "수산화 나트륨", "Sodium hydroxide", "ionic", "ionic", 2, 4,
     "수산화 나트륨(NaOH)은 이온성 염기입니다. 화학식의 각 기호가 원자·이온을 나타냅니다."),
    ("calcium-hydroxide", "Ca(OH)2", "수산화 칼슘", "Calcium hydroxide", "ionic", "ionic", 2, 5,
     "수산화 칼슘(Ca(OH)₂)에서 괄호 뒤 2는 OH 전체가 2개라는 계수성 아래첨자입니다."),
    ("magnesium-oxide", "MgO", "산화 마그네슘", "Magnesium oxide", "ionic", "ionic", 2, 3,
     "산화 마그네슘(MgO)은 마그네슘과 산소가 1:1인 이온 화합물입니다."),
    ("aluminium-oxide", "Al2O3", "산화 알루미늄", "Aluminium oxide", "ionic", "network", 2, 3,
     "산화 알루미늄(Al₂O₃)은 보크사이트·점토 루트의 핵심 화합물입니다."),
    ("sodium-ion", "Na+", "나트륨 이온", "Sodium ion", "ionic", "ion", 2, 1,
     "Na⁺는 나트륨 원자가 전자 1개를 잃은 이온입니다. 원소·원자·이온을 구분하세요."),
    ("chloride-ion", "Cl-", "염화 이온", "Chloride ion", "ionic", "ion", 2, 1,
     "Cl⁻는 염소 원자가 전자 1개를 얻은 이온입니다."),
    ("calcium-ion", "Ca2+", "칼슘 이온", "Calcium ion", "ionic", "ion", 2, 1,
     "Ca²⁺는 칼슘 원자가 전자 2개를 잃은 이온입니다."),
    ("iron-iii-ion", "Fe3+", "철(III) 이온", "Iron(III) ion", "ionic", "ion", 3, 1,
     "Fe³⁺는 철 원자가 전자 3개를 잃은 이온입니다."),
    ("sulfate", "SO42-", "황산 이온", "Sulfate", "ionic", "tetrahedral", 2, 4,
     "황산 이온(SO₄²⁻)은 황 1개와 산소 4개가 모인 다원자 이온입니다."),
    ("nitrate", "NO3-", "질산 이온", "Nitrate", "ionic", "trigonal_planar", 2, 4,
     "질산 이온(NO₃⁻)에서 아래첨자 3은 산소 원자 수입니다."),
    ("hydroxide", "OH-", "수산화 이온", "Hydroxide", "ionic", "linear", 1, 2,
     "수산화 이온(OH⁻)은 산소와 수소가 결합한 이온입니다."),
    ("ammonium", "NH4+", "암모늄 이온", "Ammonium", "ionic", "tetrahedral", 2, 4,
     "암모늄(NH₄⁺)은 질소 1개와 수소 4개의 다원자 이온입니다."),
    ("carbonate", "CO32-", "탄산 이온", "Carbonate", "ionic", "trigonal_planar", 2, 4,
     "탄산 이온(CO₃²⁻)은 탄소 1개와 산소 3개의 다원자 이온입니다."),
    ("phosphate", "PO43-", "인산 이온", "Phosphate", "ionic", "tetrahedral", 2, 5,
     "인산 이온(PO₄³⁻)에서 아래첨자 4는 산소 원자 수입니다."),
    ("ozone-note", "O3", "오존(교육)", "Ozone (edu)", "inorganic", "bent", 1, 4,
     "같은 O 원소라도 O(원자), O₂(분자), O₃(분자)는 다릅니다."),
]

# Fix duplicate O3 — replace last with hydrogen gas already have; use methane hydrate-like or Na2CO3
TIER_A[-1] = (
    "sodium-carbonate", "Na2CO3", "탄산 나트륨", "Sodium carbonate", "ionic", "ionic", 2, 6,
    "탄산 나트륨(Na₂CO₃)에서 아래첨자 2는 나트륨 원자 2개를 뜻합니다. 앞의 계수가 아닙니다.",
)


def z_of_symbol(symbol: str) -> int:
    return SYM.index(symbol)


def related_z(elements: list[dict]) -> list[int]:
    return sorted({z_of_symbol(p["symbol"]) for p in elements})


def build_molecules() -> list[dict]:
    out = []
    seen = set()
    for mid, formula, ko, en, cat, struct, diff, energy, desc in TIER_A:
        if mid in seen:
            continue
        seen.add(mid)
        parts = parse_formula(formula)
        out.append({
            "id": mid,
            "formula": formula,
            "formulaDisplay": to_display(formula),
            "nameKo": ko,
            "nameEn": en,
            "elements": parts,
            "relatedElements": related_z(parts),
            "category": cat,
            "structureType": struct,
            "descriptionKo": desc,
            "tier": "A",
            "difficulty": diff,
            "energy": energy,
            "synthesis": [p["symbol"] for p in parts],
        })
    return out


def attach_molecule_refs(elements: list[dict], molecules: list[dict]) -> None:
    for el in elements:
        ids = [m["id"] for m in molecules if el["z"] in m["relatedElements"]]
        el["relatedMoleculeIds"] = ids
        if not ids and not el["hasKnownMolecules"]:
            el["noteKo"] = f"{el['nameKo']}은 초중원소로, 안정한 분자가 알려지지 않았습니다."


def build_synthesis(molecules: list[dict]) -> list[dict]:
    recipes = []
    for m in molecules:
        if m["category"] == "ionic" and m["structureType"] == "ion":
            continue
        recipes.append({
            "id": f"synth-{m['id']}",
            "output": m["id"],
            "inputs": [{"symbol": p["symbol"], "count": p["count"]} for p in m["elements"]],
            "labTier": 1 if m["difficulty"] == 1 else 2,
            "energyCost": 0,
            "energyGainOnFail": 1,
        })
    return recipes


def build_energy_abilities() -> list[dict]:
    return [
        {"id": "heal_all", "nameKo": "전체 치료", "cost": 20, "effect": "heal_player", "value": 8},
        {"id": "attack_up", "nameKo": "공격력 증가", "cost": 15, "effect": "strength", "seconds": 30},
        {"id": "energy_up", "nameKo": "에너지 증가", "cost": 10, "effect": "energy", "value": 5},
        {"id": "draw_card", "nameKo": "카드 뽑기", "cost": 25, "effect": "random_element_card", "value": 1},
    ]


def build_stars() -> list[dict]:
    return [
        {"id": "protostar", "nameKo": "원시별", "costEnergy": 50, "drawBonus": 2, "debuff": None},
        {"id": "neutron_star", "nameKo": "중성자별", "costEnergy": 80, "drawBonus": 4, "debuff": "slowness"},
        {"id": "black_hole", "nameKo": "블랙홀", "costEnergy": 120, "drawBonus": 8, "debuff": "wither_trace"},
    ]


def build_fusion() -> list[dict]:
    return [
        {"id": "fusion-he", "inputs": ["H", "H"], "output": "He", "energyCost": 30, "tier": 1},
        {"id": "fusion-c", "inputs": ["He", "He", "He"], "output": "C", "energyCost": 60, "tier": 1},
        {"id": "fusion-og", "inputs": ["U", "C"], "output": "Og", "energyCost": 200, "tier": 3},
    ]


def build_quiz(elements: list[dict], molecules: list[dict]) -> dict:
    qs = []
    n = 0

    def add(**kwargs):
        nonlocal n
        n += 1
        qs.append({"id": f"q{n:03d}", **kwargs})

    for z in [1, 6, 8, 11, 17, 26, 29, 79, 10, 20, 7, 16, 15, 12, 13, 14, 9, 2, 18, 80, 35, 47, 92, 118, 19]:
        el = elements[z - 1]
        add(mode="element", type="symbol_to_name", difficulty=1 if z <= 20 else 2,
            promptKo=f"기호 '{el['symbol']}'의 한글 원소명은?",
            choices=None, correctAnswer=el["nameKo"],
            explanationKo=f"{el['symbol']}는 {el['nameEn']}의 기호이며, 한글로는 {el['nameKo']}입니다. 대한화학회 표기를 따릅니다.",
            relatedZ=z)
        add(mode="element", type="name_to_symbol", difficulty=1 if z <= 20 else 2,
            promptKo=f"'{el['nameKo']}'의 원소 기호는?",
            choices=None, correctAnswer=el["symbol"],
            explanationKo=f"{el['nameKo']}의 IUPAC 기호는 {el['symbol']}입니다.",
            relatedZ=z)

    add(mode="element", type="group", difficulty=1, promptKo="할로겐은 몇 족인가?",
        choices=["1족", "2족", "17족", "18족"], correctAnswer="17족",
        explanationKo="할로겐(F, Cl, Br, I, At, Ts)은 주기율표 17족입니다.", relatedZ=17)
    add(mode="element", type="state", difficulty=1, promptKo="상온에서 액체인 원소는?",
        choices=["수은과 브로민", "수은만", "브로민만", "갈륨만"], correctAnswer="수은과 브로민",
        explanationKo="상온 액체 원소의 대표는 수은(Hg)과 브로민(Br)입니다.", relatedZ=80)
    add(mode="element", type="category", difficulty=1, promptKo="18족 원소의 공통 성질은?",
        choices=["할로겐", "알칼리금속", "비활성 기체", "전이금속"], correctAnswer="비활성 기체",
        explanationKo="18족은 원자가 전자가 채워져 반응성이 매우 낮습니다.", relatedZ=10)

    for z, ev in [(6, 6), (11, 1), (17, 7), (8, 6), (20, 2)]:
        el = elements[z - 1]
        add(mode="atom", type="electron_count", difficulty=1,
            promptKo=f"{el['nameKo']} 원자의 전자 수는? (중성 원자)",
            correctAnswer=z,
            explanationKo=f"중성 원자에서 전자 수 = 양성자 수 = 원자번호 Z={z}입니다.",
            relatedZ=z)
        add(mode="atom", type="proton_count", difficulty=1,
            promptKo=f"{el['nameKo']} 원자의 양성자 수는?",
            correctAnswer=z,
            explanationKo=f"양성자 수는 원자번호와 같습니다. {el['symbol']}의 Z는 {z}입니다.",
            relatedZ=z)

    add(mode="atom", type="isotope", difficulty=2,
        promptKo="¹²C와 ¹⁴C의 차이는?",
        choices=["양성자 수", "중성자 수", "원소 종류", "전자 배치 족"],
        correctAnswer="중성자 수",
        explanationKo="같은 원소(탄소, Z=6)이지만 중성자 수가 6과 8로 다릅니다. 이를 동위원소라고 합니다.",
        relatedZ=6)
    add(mode="atom", type="shell", difficulty=1,
        promptKo="전자 배치 2-8-1은 무슨 원소인가?",
        choices=["네온", "나트륨", "마그네슘", "플루오린"],
        correctAnswer="나트륨",
        explanationKo="2+8+1=11이므로 원자번호 11, 나트륨(Na)입니다. 원자가 전자는 1개입니다.",
        relatedZ=11)
    add(mode="atom", type="valence", difficulty=1,
        promptKo="염소(Cl)의 원자가 전자 수는?",
        correctAnswer=7,
        explanationKo="염소는 17족으로 원자가 전자 7개(2-8-7)입니다.",
        relatedZ=17)

    for mid in ["water", "carbon-dioxide", "methane", "glucose", "ammonia", "oxygen", "o2", "sodium-chloride"]:
        mol = next((m for m in molecules if m["id"] == mid or m["id"] == mid.replace("oxygen", "o2")), None)
        if not mol:
            continue
        total = sum(p["count"] for p in mol["elements"])
        add(mode="molecule", type="parse_count", difficulty=mol["difficulty"],
            promptKo=f"{mol['formulaDisplay']}에서 원자 총수는?",
            correctAnswer=total,
            explanationKo=f"{mol['nameKo']}({mol['formulaDisplay']})는 " +
                          ", ".join(f"{p['symbol']}×{p['count']}" for p in mol["elements"]) +
                          f"이므로 원자 총수는 {total}입니다. 아래첨자는 원자 개수입니다.",
            relatedMoleculeId=mol["id"])

    add(mode="molecule", type="classify", difficulty=1,
        promptKo="다음 중 '분자'인 것은?",
        choices=["O (산소 원자)", "O₂ (산소 분자)", "O (산소 원소)"],
        correctAnswer="O₂ (산소 분자)",
        explanationKo="O는 산소 원소/원자를 나타낼 때 쓰고, O₂는 산소 분자 1개를 나타냅니다. 원소·원자·분자를 구분하세요.",
        relatedMoleculeId="o2")
    add(mode="molecule", type="classify", difficulty=1,
        promptKo="2H₂O에서 산소 원자는 몇 개인가?",
        choices=["1", "2", "4", "6"],
        correctAnswer="2",
        explanationKo="계수 2는 물 분자 2개를 뜻하고, 분자당 산소는 1개이므로 산소 원자는 2개입니다. 수소 원자는 4개입니다.",
        relatedMoleculeId="water")
    add(mode="molecule", type="name_to_formula", difficulty=1,
        promptKo="물의 분자식은?",
        choices=["HO", "H2O", "H2O2", "OH"],
        correctAnswer="H2O",
        explanationKo="물은 수소 2개+산소 1개의 분자이므로 H₂O입니다.",
        relatedMoleculeId="water")
    add(mode="integrated", type="three_way", difficulty=1,
        promptKo="다음 중 '원자'만 가리키는 것은?",
        choices=["NaCl", "O₂", "¹²C", "공기"],
        correctAnswer="¹²C",
        explanationKo="¹²C는 탄소 원자(특정 동위원소)입니다. O₂는 분자, NaCl은 화합물입니다.",
        relatedZ=6)

    by_mode = {"element": [], "atom": [], "molecule": [], "integrated": []}
    for q in qs:
        by_mode[q["mode"]].append(q)
    return {"all": qs, **by_mode}


def build_refining() -> dict:
    crush = [
        {"id": "crush-sand", "input": {"item": "minecraft:sand", "count": 16}, "outputs": [{"id": "silica_grit", "count": 4}, {"id": "sand_dust", "count": 8}], "ticks": 80},
        {"id": "crush-cobble", "input": {"item": "minecraft:cobblestone", "count": 16}, "outputs": [{"id": "stone_powder", "count": 6}, {"id": "metal_trace", "count": 1, "chance": 0.4}], "ticks": 80},
        {"id": "crush-quartz", "input": {"item": "minecraft:quartz", "count": 8}, "outputs": [{"id": "quartz_dust", "count": 4}, {"id": "silica_grit", "count": 2}], "ticks": 80},
        {"id": "crush-iron", "input": {"item": "minecraft:raw_iron", "count": 8}, "outputs": [{"id": "iron_ore_powder", "count": 4}, {"id": "rock_slag", "count": 2}], "ticks": 100},
        {"id": "crush-nether-quartz", "input": {"item": "minecraft:nether_quartz", "count": 8}, "outputs": [{"id": "nether_quartz_dust", "count": 4}], "ticks": 80},
        {"id": "crush-bone", "input": {"item": "minecraft:bone", "count": 8}, "outputs": [{"id": "calcium_phosphate", "count": 2}], "ticks": 80},
    ]
    dissolve = [
        {"id": "slurry-si", "inputs": [{"id": "silica_grit", "count": 4}, {"item": "minecraft:water_bucket", "count": 1}, {"item": "minecraft:coal", "count": 2}], "outputs": [{"id": "murky_silicon_slurry", "count": 2}, {"id": "waste_sludge", "count": 1}], "ticks": 200},
        {"id": "slurry-fe", "inputs": [{"id": "iron_ore_powder", "count": 4}, {"item": "minecraft:water_bucket", "count": 1}, {"item": "minecraft:coal", "count": 4}], "outputs": [{"id": "iron_slurry", "count": 2}, {"id": "sulfur_trace", "count": 1, "chance": 0.35}], "ticks": 220},
        {"id": "charcoal-residue", "inputs": [{"item": "minecraft:charcoal", "count": 16}], "outputs": [{"id": "carbon_residue", "count": 4}, {"id": "ash", "count": 8}], "ticks": 160},
        {"id": "electro-water", "inputs": [{"item": "minecraft:water_bucket", "count": 4}, {"item": "minecraft:redstone", "count": 8}], "outputs": [{"id": "electrolyzed_hydrogen", "count": 1}, {"id": "oxygen_bubble", "count": 1}], "ticks": 400},
        {"id": "brine", "inputs": [{"item": "minecraft:kelp", "count": 32}, {"item": "minecraft:sand", "count": 16}, {"item": "minecraft:water_bucket", "count": 4}], "outputs": [{"id": "salt_brine", "count": 2}, {"id": "waste_sludge", "count": 1}], "ticks": 300},
    ]
    crude = [
        {"id": "refine-si", "tier": 1, "success": 0.65, "inputs": [{"id": "murky_silicon_slurry", "count": 2}], "output": {"id": "impure_Si", "count": 1}, "byproducts": [{"id": "slag", "count": 1}], "ticks": 400},
        {"id": "refine-fe", "tier": 1, "success": 0.65, "inputs": [{"id": "iron_slurry", "count": 2}], "output": {"id": "impure_Fe", "count": 1}, "byproducts": [{"id": "slag", "count": 1}], "ticks": 360},
        {"id": "refine-c", "tier": 1, "success": 0.65, "inputs": [{"id": "carbon_residue", "count": 4}], "output": {"id": "impure_C", "count": 1}, "byproducts": [{"id": "ash", "count": 2}], "ticks": 300},
        {"id": "refine-o", "tier": 1, "success": 0.65, "inputs": [{"id": "oxygen_bubble", "count": 2}], "output": {"id": "impure_O", "count": 1}, "byproducts": [], "ticks": 280},
        {"id": "refine-h", "tier": 1, "success": 0.65, "inputs": [{"id": "electrolyzed_hydrogen", "count": 2}], "output": {"id": "impure_H", "count": 1}, "byproducts": [], "ticks": 280},
        {"id": "refine-nacl", "tier": 2, "success": 0.45, "inputs": [{"id": "salt_brine", "count": 2}], "output": {"id": "impure_Cl", "count": 1, "alt": {"id": "impure_Na", "weight": 0.4}}, "byproducts": [{"id": "toxic_waste", "count": 1}], "ticks": 500, "exclusive": True},
        {"id": "refine-cu", "tier": 2, "success": 0.45, "inputs": [{"item": "minecraft:raw_copper", "count": 16}], "output": {"id": "impure_Cu", "count": 1}, "byproducts": [{"id": "slag", "count": 2}], "ticks": 420},
        {"id": "ingot-fe-penalty", "tier": 1, "success": 1.0, "inputs": [{"item": "minecraft:iron_ingot", "count": 4}], "output": {"id": "impure_Fe", "count": 1}, "byproducts": [], "ticks": 200, "noteKo": "광석을 쓰세요"},
    ]
    purify = [
        {"id": "purify-si-2nd", "inputs": [{"id": "impure_Si", "count": 3}, {"id": "refining_catalyst", "count": 1}], "output": {"id": "pure_Si", "count": 1}, "success": 0.5, "ticks": 300},
        {"id": "purify-si-card", "inputs": [{"id": "impure_Si", "count": 2}], "energy": 5, "output": {"card": "Si", "count": 1}, "success": 1.0, "ticks": 200},
        {"id": "purify-fe-card", "inputs": [{"id": "impure_Fe", "count": 2}], "energy": 5, "output": {"card": "Fe", "count": 1}, "success": 1.0, "ticks": 200},
        {"id": "purify-c-card", "inputs": [{"id": "impure_C", "count": 2}], "energy": 5, "output": {"card": "C", "count": 1}, "success": 1.0, "ticks": 200},
        {"id": "purify-o-card", "inputs": [{"id": "impure_O", "count": 2}], "energy": 5, "output": {"card": "O", "count": 1}, "success": 1.0, "ticks": 200},
        {"id": "purify-h-card", "inputs": [{"id": "impure_H", "count": 2}], "energy": 5, "output": {"card": "H", "count": 1}, "success": 1.0, "ticks": 200},
        {"id": "purify-na-card", "inputs": [{"id": "impure_Na", "count": 2}], "energy": 8, "output": {"card": "Na", "count": 1}, "success": 1.0, "ticks": 240},
        {"id": "purify-cl-card", "inputs": [{"id": "impure_Cl", "count": 2}], "energy": 8, "output": {"card": "Cl", "count": 1}, "success": 1.0, "ticks": 240},
        {"id": "chips-to-random", "inputs": [{"id": "wrong_element_chip", "count": 10}], "output": {"card": "RANDOM_Z1_20", "count": 1}, "success": 1.0, "ticks": 100},
    ]
    yield_table = [
        {"element": "Si", "vanilla": "sand", "consume": "48-64 + water + coal 16"},
        {"element": "O", "vanilla": "water", "consume": "buckets 8+ + redstone"},
        {"element": "C", "vanilla": "charcoal/coal", "consume": "32"},
        {"element": "Fe", "vanilla": "raw_iron", "consume": "16-24 powder route"},
        {"element": "Cu", "vanilla": "raw_copper", "consume": "16"},
        {"element": "Au", "vanilla": "raw_gold", "consume": "24"},
        {"element": "Na", "vanilla": "kelp+sand+water", "consume": "brine route"},
        {"element": "Cl", "vanilla": "kelp+water", "consume": "separate from Na"},
    ]
    return {
        "crush_recipes": crush,
        "dissolve_recipes": dissolve,
        "crude_refine": crude,
        "purify_recipes": purify,
        "yield_table": yield_table,
    }


def build_crust() -> list[dict]:
    rows = []
    for z in range(1, 119):
        rows.append({
            "z": z,
            "symbol": SYM[z],
            "crustMassPercent": ABUNDANCE.get(z),
            "tier": abundance_tier(z),
        })
    return rows


def dump(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def checksum(path: Path) -> str:
    import hashlib
    return hashlib.sha256(path.read_bytes()).hexdigest()[:16]


def main() -> None:
    elements = build_elements()
    atoms = build_atoms()
    molecules = build_molecules()
    attach_molecule_refs(elements, molecules)
    synthesis = build_synthesis(molecules)
    quiz = build_quiz(elements, molecules)
    refining = build_refining()

    dump(OUT / "elements.json", elements)
    dump(OUT / "atoms.json", atoms)
    dump(OUT / "molecules.json", molecules)
    dump(OUT / "molecules_tier_a.json", molecules)
    dump(OUT / "synthesis.json", synthesis)
    dump(OUT / "fusion.json", build_fusion())
    dump(OUT / "stars.json", build_stars())
    dump(OUT / "energy-abilities.json", build_energy_abilities())
    dump(OUT / "crust_abundance.json", build_crust())
    dump(OUT / "extraction" / "routes.json", [e["extraction"] | {"z": e["z"], "symbol": e["symbol"]} for e in elements])
    dump(OUT / "vanilla_refining" / "crush_recipes.json", refining["crush_recipes"])
    dump(OUT / "vanilla_refining" / "dissolve_recipes.json", refining["dissolve_recipes"])
    dump(OUT / "vanilla_refining" / "crude_refine.json", refining["crude_refine"])
    dump(OUT / "vanilla_refining" / "purify_recipes.json", refining["purify_recipes"])
    dump(OUT / "vanilla_refining" / "yield_table.json", refining["yield_table"])
    dump(OUT / "quiz" / "all.json", quiz["all"])
    dump(OUT / "quiz" / "element.json", quiz["element"])
    dump(OUT / "quiz" / "atom.json", quiz["atom"])
    dump(OUT / "quiz" / "molecule.json", quiz["molecule"])
    dump(OUT / "quiz" / "integrated.json", quiz["integrated"])

    ore_map = [
        {"ore": "periodite_ore", "y": [-64, 16], "zRange": [1, 26], "biome": "overworld"},
        {"ore": "deep_periodite", "y": [-64, -32], "zRange": [27, 56], "biome": "deepslate"},
        {"ore": "nether_salt", "y": [0, 128], "zRange": [11, 17], "biome": "nether", "symbols": ["Na", "Cl"]},
        {"ore": "end_crystal_ore", "y": [0, 80], "zRange": [54, 118], "biome": "end"},
    ]
    dump(OUT / "ore_to_element.json", ore_map)

    group_synergy = [
        {"category": "halogen", "minCards": 3, "bonus": {"attack": 2}, "nameKo": "할로겐 시너지"},
        {"category": "alkali_metal", "minCards": 3, "bonus": {"attack": 2}, "nameKo": "알칼리 시너지"},
        {"category": "noble_gas", "minCards": 3, "bonus": {"defense": 3}, "nameKo": "비활성 시너지"},
        {"category": "transition_metal", "minCards": 3, "bonus": {"health": 2}, "nameKo": "전이금속 시너지"},
    ]
    dump(OUT / "group_synergy.json", group_synergy)

    molecule_tools = [
        {"id": "water", "effect": "heal_percent", "value": 0.30},
        {"id": "sodium-chloride", "effect": "defense_stage", "value": 1},
        {"id": "hydrogen-peroxide", "effect": "attack_stage", "value": 1},
        {"id": "glucose", "effect": "heal_percent", "value": 0.50, "turns": 2},
    ]
    dump(OUT / "molecule_tools.json", molecule_tools)

    files = [
        "elements.json", "atoms.json", "molecules_tier_a.json",
        "synthesis.json", "energy-abilities.json",
    ]
    manifest = {
        "version": "2026.08.1",
        "bundles": {
            name.replace(".json", ""): {
                "checksum": checksum(OUT / name),
                "size": (OUT / name).stat().st_size,
            }
            for name in files
        },
    }
    dump(OUT / "manifest.json", manifest)
    print(f"Wrote shared-data: {len(elements)} elements, {len(atoms)} atoms, {len(molecules)} molecules, {len(quiz['all'])} quizzes")


if __name__ == "__main__":
    main()
