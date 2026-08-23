# wonso — 현실 반영 원소 획득 (Real-World Extraction)

> **최우선 원칙**: 게임용 지름길보다 **실제 지구·산업에서 원소를 얻는 방식**을 먼저 설계하고, 마크 바닐라 아이템으로 **번역**한다.
> **연계**: [VANILLA_REFINING_SPEC.md](./VANILLA_REFINING_SPEC.md) — 같은 루트의 인게임 구현(지지리·슬러지)

---

## 1. 현실 반영 5원칙

| # | 원칙 | 게임 번역 |
|---|------|-----------|
| 1 | **원소가 아니라 화합물부터** | 모래 = Si, 아니라 **SiO₂(이산화규소)** |
| 2 | **공정 이름이 실제와 같음** | 전기분해, 환원, 정광, 증발, 침출, 분해 |
| 3 | **지구 풍부도 ≈ 획득 난이도** | O, Si, Al, Fe 쉬움 · Au, U, Og 극희귀 |
| 4 | **에너지·시간 = 현실 비용** | Na·Al = 전기분해 대량 · Au = 미량 추출 |
| 5 | **산업 부산물·공정 결합** | 슬래그·폐액·CO₂ — 현실의 노폐물 |

```text
현실:  모래(SiO₂) + 탄소 + 2000°C → Si + CO₂
게임:  모래 + 목탄 + 고로/정제기(오래) → 불순 Si + 슬래그 + (게임적) CO₂ 가스

현실:  식염수 증발 → NaCl → 전기분해 → Na + Cl₂
게임:  켈프+물 증발통 → 소금 결정 → 전기분해기 + 레드스톤(전력) → Na, Cl 각각
```

---

## 2. 게임 공정 ↔ 실제 산업 공정

| 게임 블록/단계 | 실제 공정 | 현실 예시 |
|----------------|-----------|-----------|
| `Crusher` (분쇄) | 분쇄·분쇄 | 광석 입도 감소 |
| `Reaction Vat` (반응통) | 침출·용해·산화 | H₂SO₄ 침출, 브라인 |
| `Evaporation Pan` (증발천) | 증발·결정 | 해수→식염, 리튬 브라인 |
| `Smelter` / 화로 상위 | **정광** | Fe₂O₃ + CO → Fe |
| `Reduction Furnace` (환원로) | **고온 환원** | SiO₂ + C → Si |
| `Electrolyzer` (전기분해기) | **전기분해** | NaCl→Na, Al₂O₃→Al, H₂O→H+O |
| `Distiller` (증류기) | **증류·분별** | 공기 분리, Br₂, I₂ |
| `Element Refinery` | 정제·순화 | 재처리, 족 분리 |
| `Fusion Chamber` | 핵반응 (Og 등) | 합성 원소만 |

**레드스톤** = 전력(⚡) 대용 · **석탄/목탄** = 환원제 + 열원 · **물** = 용매·전해질

---

## 3. 화합물 우선 모델 (Compound-First)

플레이어가 다루는 중간물은 **실제 화학식 이름**:

| 게임 아이템 ID | 화학식 | 바닐라 유래 |
|----------------|--------|-------------|
| `silicon_dioxide` | SiO₂ | 모래, 석영 |
| `iron_oxide_ore` | Fe₂O₃ / Fe₃O₄ | 철 광석 |
| `copper_carbonate_ore` | CuCO₃·Cu(OH)₂ | 구리 광석 (청동산화) |
| `sodium_chloride` | NaCl | 증발천·켈프 브라인 |
| `calcium_carbonate` | CaCO₃ | 해얼돌, 뼈 |
| `calcium_phosphate` | Ca₃(PO₄)₂ | 뼈 분쇄 |
| `sulfur_compound` | S (다양) | 네더 황화물, blaze |
| `aluminum_oxide_mix` | Al₂O₃ + impurities | 점토·보크사이트 루트 |
| `carbon_allotrope` | C | 석탄, 다이아, 흑요석 |
| `atmospheric_nitrogen` | N₂ (78%) | 공기 집集 |
| `dissolved_oxygen` | O₂ / H₂O | 물 전기분해, 공기 |

**원소 카드**는 항상 **마지막 단계** — `순수 Fe`, `순수 Na` 등.

---

## 4. 지구 풍부도 티어 (획득 난이도)

| 티어 | 지각 질량 % (대표) | 원소 예 | 게임 공정 수 | 바닐라 소모 |
|------|-------------------|---------|--------------|-------------|
| **T0** | 46% O | O | 1~2 (물/공기) | 중 |
| **T1** | 28% Si, 8% Al, 5% Fe | Si, Al, Fe, Ca, Mg, Na, K | 2~4 | 대 |
| **T2** | 1~0.1% | Cu, Zn, Pb, Sn, Ni | 3~5 | 대 |
| **T3** | ppm~ppb | Au, Ag, U, W, Pt | 5~8 + 희귀 광맥 | 극대 |
| **T4** | 합성만 | Pu, Og, Mc… | 핵융합·특수 | 사실상 불가 근접 |

풍부도 데이터는 `shared-data/crust_abundance.json`으로 CI 검증.

---

## 5. 원소족별 현실 루트 (마크 번역)

### 5.1 비활성 기체 (He, Ne, Ar, Kr, Xe) — 0족/18족

| 원소 | 현실 | 마크 루트 |
|------|------|-----------|
| **He** | 천연가스 | (한계) 영혼 모래+가스 루트 희귀 trace — "현실도 헬륨은 특수" |
| **Ne, Ar** | **공기 분별 증류** | `Air Collector`(고지·오픈) → `liquid_air` → `Distiller` → Ar / Ne trace |
| **Kr, Xe** | 공기 분별 (희귀) | 동일, 확률 극저 |

### 5.2 알칼리·알칼리토금속 (1·2족)

| 원소 | 현실 | 마크 루트 |
|------|------|-----------|
| **Li** | 염수·광물 | 사막 브라인 증발 → trace → 전기분해 |
| **Na** | **식염 전기분해** | 켈프+물→`Evaporation Pan`→NaCl→`Electrolyzer` |
| **K** | KCl 광물 / 식염 부산물 | 네더 브라인 trace + 전기분해 |
| **Ca** | **석회석 CaCO₃** | 해얼돌·조약돌→`calcium_carbonate`→煅烧分解→Ca |
| **Mg** | 돌말·해수 | 점토+물 침출→Mg trace→전기분해 |

### 5.3 전이금속·일반 금속 (3~12족, 주요)

| 원소 | 현실 광물/공정 | 마크 |
|------|----------------|------|
| **Fe** | **철광석 정광** Fe₂O₃+CO→Fe | 철 광석 분쇄→`iron_oxide`→환원로+석탄 |
| **Cu** | 구리 광석 정광·전해정련 | 구리 광석→산화구리→환원→(순)전기분해 |
| **Zn** | ZnS 광석 | 스페린러 루트 trace (네더?) |
| **Al** | **보크사이트→Al₂O₃→전기분해** | 점토+석영→`alumina`→전기분해기(⚡많이) |
| **Sn, Pb** | 산화 광석 정광 | 자갈 trace 광맥 |
| **Cr, Mn, Ni** | 산화·황화 광석 | 심층 슬레이트 trace |
| **Ag, Au** | **플라서·미량 광석** | 모래·자갈 **판** + 금/은 광석 극저확률 |
| **W, Mo** | 고온 광물 | 심층 광맥 |
| **U** | 우라닌 광석 | 심층 주기율광석 trace |

### 5.4 비금속 (C, N, P, S, Se…)

| 원소 | 현실 | 마크 |
|------|------|------|
| **C** | 유기물·석탄·흑연 | 목탄 **건조분해**·다이아(동질异形体) |
| **N** | **공기 78% N₂** | Air Collector→Distiller→N₂ 액화 분리 |
| **P** | **뼈·인회석** | 뼈→`calcium_phosphate`→산 처리→P |
| **S** | 황화광·화산 | 네더 황·blaze powder→정광 |
| **Si** | **SiO₂ 환원** | 모래→SiO₂→환원로+탄소(고온) |
| **Se, Br, I** | 산화물·브라인 | 해양 브라인 증발·증류 (Br, I) |

### 5.5 할로겐 (F, Cl, Br, I)

| 원소 | 현실 | 마크 |
|------|------|------|
| **F** | 형석 CaF₂ | 심층 trace + 산 처리 (고난이도) |
| **Cl** | **식염 전기분해** | NaCl 전해 → Cl₂ → Cl 원소 |
| **Br, I** | 브라인 증류 | 켈프 브라인 `Distiller` |

### 5.6 란타넘족·악티늄족 (57~88, 89+)

| 그룹 | 현실 | 마크 |
|------|------|------|
| La~Lu | 희토류 복합 광물, **족 분리 극난** | `주기율광석`→`mixed_rare_earth`→다단계 침출·수십 단계 (의도적 지옥) |
| Ac, Th, U | 방사성 광물 | 심층·워든 연계 trace |
| 합성 (Pu~) | 핵반응 | Fusion Chamber only |

---

## 6. 바닐라 블록 ↔ 실제 광물학 매핑

| 마크 바닐라 | 실제로는 | 주요 원소 |
|-------------|----------|-----------|
| **모래** | 석영 입자 (SiO₂) | Si, O |
| **석영 블록** | 대형 석영 결정 | Si, O |
| **조약돌/돌** | 화강암·편마암 (복합) | Si, Al, Ca, Fe, K… trace |
| **자갈** | 하상·풍화 산물 | Si + 광물 pebble |
| **점토** | 알루미늄 규산염 | Al, Si, O |
| **해얼돌** | 탄산칼슘 CaCO₃ | Ca, C, O |
| **철 광석** | 적철광·자철광 | Fe (+ S trace) |
| **구리 광석** | 카르코파이트 등 | Cu, Fe, S |
| **금 광석** | 미량 금 포함 광석 | Au (ppm) |
| **석탄/목탄** | 탄소 퇴적물 | C, H, S trace |
| **레드스톤** | (게임) **전력** | 촉매·전해 에너지 |
| **네더 석영** | SiO₂ (다른 결정형) | Si, O |
| **영혼 모래** | 화산유리·철氧化物 혼합 (게임 허용) | Si, Fe, trace |
| **영혼 흙** | 유기·광물 혼합 | C, Fe, trace |
| **뼈** | 인산칼슘 | Ca, P, O |
| **켈프** | 해조 (K, I trace) | I, K, C + 브라인 힌트 |
| **다이아** | 탄소 동질异形体 | C |
| **에메랄드** | 녹주석 Be₃Al₂Si₆O₁₈ | Be, Al, Si, O |
| **청금석** | 황화물·알루미늄 실리케이트 | S, Al trace |
| **블레이즈 powder** | 황 화합물 연상 | S |
| **마그마 크림** | Ca, S 화합물 연상 | Ca, S |
| **물** | H₂O | H, O |
| **공기** | N₂, O₂, Ar… | N, O, Ar |

---

## 7. 대표 루트 상세 (현실→게임 플로우)

### 7.1 규소 Si — 모래 환원 (산업과 동일 개념)

```text
현실: SiO₂ + 2C → Si + 2CO↑  (2000°C, 산업용 메탈 실리콘)

게임:
  모래 32 → Crusher → silicon_dioxide_powder×8
  + 목탄 16 (환원제)
  → Reduction Furnace (석탄 연료×8, 400틱)
  → metallurgical_silicon×2 + CO_offgas + slag×4
  → Refinery 순화
  → Si 원소 카드×1

Journal: "모래는 규소 자체가 아니라 이산화규소. 탄소로 산소를 빼야 한다."
```

### 7.2 철 Fe — 정광 (blast furnace)

```text
현실: Fe₂O₃ + 3CO → 2Fe + 3CO₂

게임:
  철 광석 8 → iron_oxide_powder
  + 석탄 4 (CO 원천)
  → Smelter/환원로 200틱
  → pig_iron×2 + slag
  → (선택) 재정련 → wrought_iron
  → Fe 카드

비효율: 철 주괴 직접 투입 = 이미 정광됨 → Fe trace만 (교육 메시지)
```

### 7.3 나트륨 Na + 염소 Cl — 식염 전기분해 (허 공정)

```text
현실: 2NaCl(융액/수용액) --전기--> 2Na + Cl₂

게임:
  켈프 16 + 물 4 → Evaporation Pan + 열
  → sodium_chloride×4 + brine_waste
  → Electrolyzer + 레드스톤 16 + 물 2
  → chlorine_gas (포집) → Cl 카드
  → sodium_metal (위험!) → Na 카드 (물 접촉 폭발 연계)

Journal: "식염은 나트륨과 염소의 화합물. 전기로 억지로 뜯어낸다."
```

### 7.4 알루미늄 Al — 보크사이트 전기분해

```text
현실: 점토/보크사이트 → Al₂O₃ → Bayer → Hall-Héroult 전기분해 (에너지 극대)

게임:
  점토 32 + 석영 8 → Reaction Vat
  → crude_alumina×4 + red_mud_waste (현실 적색 슬러지)
  → Electrolyzer + 레드스톤 32 + 석탄 8
  → Al 카드×1 + slag

의도: Al은 Na보다 ⚡ 더 듬 — 현실 반영
```

### 7.5 질소 N — 공기 분리

```text
현실: 액화 공기 분별 → N₂ 78%, O₂ 21%, Ar 1%

게임:
  Air Collector (Y>100 or 오픈 스카이 64틱)
  → compressed_air
  → Distiller (느림, 600틱)
  → liquid_nitrogen×1 + liquid_oxygen×1 + argon_trace

N₂는 분자 카드로도, N 원소는 추가 전기분해 or 특수 단계
```

### 7.6 금 Au — 플라서·미량 광석

```text
현실: 1톤 광석 → 1~5g Au (ppm)

게임:
  자갈 64 + 물 양동이 (판 채굴 루트)
  → gold_panning_residue×1 (10% 확률)
  + 금 광석 8 (별도)
  → cyanide_leach (게임: Reaction Vat + 독성)
  → impure_Au×1 (확률 15%)
  → 순화 → Au 카드

의도: 금은 "지지리"의 정석
```

### 7.7 인 P — 뼈

```text
현실: 뼈 → 인산 → 백린 (역사적 Will-o'-the-wisp)

게임:
  뼈 16 → Crusher → calcium_phosphate
  → Reaction Vat + 황 trace (게임 화학)
  → phosphoric_acid → elemental_P (위험, 어두운 곳 발광)
  → P 카드
```

---

## 8. 초중원소·합성 (Og, Ts, …)

| 원소 | 현실 | 게임만 허용 |
|------|------|-------------|
| Og | 2002 합성, 반감기 ms | Fusion Chamber + 희귀 촉매 + ⚡100+ |
| U, Pu… | 핵연료 주기 | 심층 광물 trace + Fusion |
| Fm~ | 합성만 | Fusion only |

**야생 바닐라로는 불가** — 현실과 동일.

---

## 9. 데이터 스키마 (작성 필수)

```typescript
interface ExtractionRoute {
  elementZ: number;
  symbol: string;
  tier: 0 | 1 | 2 | 3 | 4;
  crustAbundancePpm: number | null;
  realWorldProcess: string;      // "Hall-Héroult process"
  realWorldSource: string;       // "bauxite / clay"
  vanillaInputs: { item: string; count: number }[];
  gameSteps: GameProcessStep[];  // ordered pipeline
  byproducts: { item: string; count: number }[];
  journalRealismKo: string;      // 교육 한줄
  energyCostRedstone: number;    // 전력 대용
  realismNotes?: string;         // 검수용 각주
}

interface GameProcessStep {
  machine: 'crusher' | 'evaporation_pan' | 'reduction_furnace' |
           'electrolyzer' | 'distiller' | 'smelter' | 'reaction_vat' | 'refinery';
  inputs: { item: string; count: number }[];
  outputs: { item: string; count: number; chance?: number }[];
  durationTicks: number;
  realismLabelKo: string;        // "전기분해", "고온 환원"
}
```

`shared-data/extraction/` — **118개 전수** (Og 등 합성 루트 명시)

---

## 10. 118원소 루트 요약표 (작성 타깃)

| Z | 기호 | 티어 | 현실 주요 공정 | 마크 주 입력 |
|---|------|------|----------------|--------------|
| 1 | H | T0 | 물 전기분해 | 물+Electrolyzer |
| 2 | He | T4 | 천연가스 | 희귀 trace |
| 6 | C | T1 | 유기물·석탄 | 목탄, 다이아 |
| 7 | N | T0 | 공기 분리 | Air Collector |
| 8 | O | T0 | 물/공기 | 물, Distiller |
| 11 | Na | T1 | NaCl 전기분해 | 켈프 브라인 |
| 12 | Mg | T1 | 돌말/해수 | 점토 침출 |
| 13 | Al | T1 | 보크사이트 전해 | 점토+석영 |
| 14 | Si | T1 | SiO₂ 환원 | **모래** |
| 15 | P | T1 | 인회석·뼈 | 뼈 |
| 16 | S | T1 | 황화광 | 네더, blaze |
| 17 | Cl | T1 | NaCl 전해 | 식염 |
| 26 | Fe | T1 | 철광 정광 | **철 광석** |
| 29 | Cu | T2 | 구리광 정광 | **구리 광석** |
| 47 | Ag | T3 | 광석·판 | 자갈 판 |
| 79 | Au | T3 | 미량 광석 | 금 광석, 판 |
| 92 | U | T3 | 우라닌 | 심층 광맥 |
| 118 | Og | T4 | 합성 | Fusion only |

**전체 118**: `extraction/elements_001-118.json` — Phase 4 콘텐츠 (화학 검수 필수)

---

## 11. 교육·Journal 연동

각 원소 카드 획득 시:

1. **현실 공정 이름** 팝업 (예: "허-에루 전기분해")
2. **3모드 원자 탭** — "이 Na는 식염에서 전기로 뜯어낸 1개의 원자"
3. **분자 탭** — "다시 NaCl로 합칠 수 있음"

`journalRealismKo` 필드가 모든 루트에 필수.

---

## 12. VANILLA_REFINING과 관계

```text
REAL_WORLD_EXTRACTION.md     ← "무엇을, 어떤 화학으로" (진실)
        ↓
VANILLA_REFINING_SPEC.md     ← "마크에서 지지리게" (게임화)
        ↓
shared-data/extraction/*.json
```

- **현실**: SiO₂ + C → Si (환원)
- **게임**: 같은 반응 + 슬래그 4개 + 400틱 + 65% 확률

화학 **방향**은 현실 고정, **수량·RNG**는 게임 밸런스.

---

## 13. 검수·작성 워크플로

| 단계 | 담당 | 산출물 |
|------|------|--------|
| 1 | 화학 검수자 | 118 루트 `realWorldProcess` 초안 |
| 2 | 데이터 | `extraction/*.json` |
| 3 | CI | 화학식 파싱, 입력 아이템 바닐라 여부 |
| 4 | UX | `journalRealismKo` 문안 |
| 5 | 밸런스 | `energyCost`, 확률, 소모량 |

**DoD**: 모든 Z에 현실 공정명 + 바닐라 입력 ≥1 + 화학 검수 approve.

---

## 14. 릴리스

| 버전 | 현실 루트 수 |
|------|-------------|
| 0.2 β | 10 (H, C, O, Fe, Si, Cu, S, Na, Cl, N) |
| 1.0 | Tier 0~1 (약 30) |
| 1.1 | Tier 2 (약 25) |
| 1.2 | Tier 3 희귀 (약 20) |
| 2.0 | 118 전수 + 희토류 지옥 |
