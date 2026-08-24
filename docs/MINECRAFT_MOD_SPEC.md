# wonso — Minecraft 26.2 Fabric 모드 기획서

> **타깃**: Minecraft Java Edition **26.2** (Fabric)
> **플레이 스타일**: **야생 서바이벌** 확장 (바닐라 파괴 최소)
> **레퍼런스**: https://elementwar.xyz (원소 대전)
> **연계**: wonso 원소·원자·분자 3모드 교육 UI

---

## 1. 한 줄 정의

**wonso** Fabric 모드는 Minecraft 야생에 **118원소 화학 시스템**을 추가한다. 채굴·탐험으로 원소를 모으고, 합성실에서 분자를 만들고, 주기율표 GUI로 원소·원자·분자를 학습한다. **PvP는 포켓몬식** — 플레이어는 직접 싸우지 않고 **원소령**을 지휘한다. ([ELEMENT_BATTLE_SPEC.md](./ELEMENT_BATTLE_SPEC.md))

---

## 2. 야생 서바이벌 설계 원칙

| 원칙 | 설명 |
|------|------|
| **바닐라 우선** | 나무→돌→철 진행은 그대로. 화학은 **선택적 심화 루트** |
| **발견 주도** | 원소·분子는 도감에 미발견 → 발견 시 보상 |
| **점진 해금** | Z 1~20 (초반) → 21~56 (중반) → 57~118 (후반·엔드) |
| **교육+게임** | 3모드 GUI는 학습, 전투·합성은 게임플레이 |
| **멀티 친화** | 싱글·서버 모두 동작, **원소 대전 PvP** (플레이어 직접 싸움 ❌) |

### 2.1 야생 진행 곡선

```text
Day 1~3   바닐라 기본 + Journal + **맷돌로 모래 분쇄** 첫 슬러지
Day 3~7   **정제기** 조합 + Fe/C/Si 거친 루트 (지지리)
Day 7~14  분자 도감, 에너지, 슬래그·폐기물 관리
Day 14~30 Na/Cl 브라인, 네더 루트, 핵융합 입문
Day 30+   희귀 원소, 별, 엔드, 원소 대전 PvP
```

---

## 3. 핵심 시스템 (7개)

### 3.1 원소 카드 (Element Cards)

**elementwar.xyz**: 손패의 원소 카드, 공격력·체력, 전장 배치

**모드 구현**:

| 항목 | 설계 |
|------|------|
| 아이템 | `wonso:element_card_{symbol}` × 118 |
| 획득 | **현실 반영 추출** ([REAL_WORLD_EXTRACTION.md](./REAL_WORLD_EXTRACTION.md)) + 거친 정제 |
| 속성 | attack, health, rarity, cost, category(족) |
| 용도 | 합성 재료, **원소령 소환(전투)**, 족 시너지, 도감 |

**야생 획득 (거친 정제 루트)** — 카드는 **한 방에 안 나옴**:

| 원소 | 바닐라 시작 | 지지리 요약 |
|------|-------------|-------------|
| Si | **모래** 48~64 | 분쇄→슬러지→정제→순화 |
| O | **물** + 레드스톤 | 전기분해 느림, 양동이 소모 |
| C | **목탄/석탄** 32 | 재·슬러지→정제 |
| Fe | **철 광석** 16 (분말 루트) | 주괴 넣기는 비효율 |
| Cu | **구리 광석** 16 | |
| Au | **금 광석** 24 | 희귀·비효율 |
| Na | **켈프+모래+물** | 브라인 루트, Cl과 분리 RNG |
| Cl | **켈프+물** | Na와 따로 순화 |
| U / Og | 주기율광석·핵융합 | 일상 루트 ❌ |

### 3.2 화학 합성실 (Synthesis Lab)

**elementwar.xyz**: 원소 드래그 → 배합 → 분자

**모드 구현**:

| 항목 | 설계 |
|------|------|
| 블록 | `Synthesis Lab` — 작업대 상위, 화로 다음 티어 |
| GUI | elementwar 합성실 UI 클론 (드래그 슬롯 + 배합 버튼) |
| 입력 | 원소 카드 N장 (또는 정제된 원소 아이템) |
| 출력 | 분子 카드 / 분子 아이템 |
| 실패 | 잘못된 조합 → "반응 실패" + 소량 에너지 |

**티어**:

1. `Basic Lab` — Tier A 분子 (~50)
2. `Advanced Lab` — Tier B (~150)
3. `Fusion Chamber` — 핵융합, 초중원소

### 3.3 분자 도감 (Molecule Encyclopedia)

**elementwar.xyz**: 발견 진행률, 5종 정렬, 미발견/발견

**모드 구현**:

| 항목 | 설계 |
|------|------|
| 아이템 | `Chemistry Journal` — 첫 튜토리얼 보상 |
| GUI | elementwar 도감 UI (정렬: 난이도/이름/희귀도/공격력/체력) |
| 기록 | 플레이어 NBT `discovered_molecules[]` |
| Advancement | 분자별 + 카테고리별 업적 |
| 3모드 | 도감 항목 → 원소/원자/분子 탭 전환 |

### 3.4 분자 에너지 (Element Energy)

**elementwar.xyz**: H₂O=3, CO₂=2, CH₄=6, C₆H₁₂O₆=12

**모드 구현**:

| 항목 | 설계 |
|------|------|
| 저장 | 플레이어 `element_energy` (float, HUD 표시) |
| 획득 | 분子 카드 분해, 에너지 변환기 블록 |
| 소모 | 에너지 스킬 4종, 별 구매, 핵융합 |
| HUD | ⚡ 아이콘 + 숫자 (사이트 상단 UI 대응) |

**에너지 스킬** (elementwar 4종):

| 스킬 | 비용 | 효과 |
|------|------|------|
| 전체 치료 | 20 | HP 회복 |
| 공격력 증가 | 15 | 30초 공격 버프 |
| 에너지 증가 | 10 | 에너지 +5 |
| 카드 뽑기 | 25 | 랜덤 원소 카드 1장 |

### 3.5 주기율표 3모드 GUI (wonso 핵심)

elementwar에는 암시만 있고, wonso 모드의 **교육 차별점**:

| 탭 | 내용 | 인터랙션 |
|----|------|----------|
| **원소** | 118칸 PT, 카테고리 색 | 클릭 → 카드 정보, 획득 경로 |
| **원자** | Bohr 쉘 모델 (2D) | p⁺/n⁰/e⁻ 표시 |
| **분자** | 관련 분子 목록 | 합성법, 에너지값, 도감 링크 |

**열기**: Journal 우클릭 → PT 버튼, 또는 키바인딩 `P`

### 3.6 별 시스템 (Stellar System) — v1.1

**elementwar.xyz**: 원시별 구매(에너지50), 블랙홀/중성자별, 디버프, 성장

**모드 구현**:

| 항목 | 설계 |
|------|------|
| 아이템 | `Stellar Core`, `Neutron Star Fragment`, `Black Hole Essence` |
| 획득 | 심층 야생, 네더 요새, 엔드 |
| 효과 | 원소 뽑기 확장(최대 26번), 분子 보너스, 디버프 |
| GUI | elementwar 별 관리 Screen |

### 3.7 원소 대전 (Element Battle) — PvP/PvE

> 상세: [ELEMENT_BATTLE_SPEC.md](./ELEMENT_BATTLE_SPEC.md)

**포켓몬식 규칙**: 플레이어 직접 전투 ❌ → **원소 파티(6)** 로 **원소령** 지휘.

| 항목 | 설계 |
|------|------|
| 파티 | 최대 6원소, 야생에서 모은 카드로 편성 |
| 전투 | 턴제 1v1, 족 상성, 스킬 4슬롯, 분子 도구 |
| PvP | 아레나 차원 + 온라인 매칭 (v1.2) |
| PvE | AI·야생 조우·라운드 웨이브 |
| 승리 | 상대 파티 전멸 (기지 HP 모드는 옵션) |

### 3.8 라운드 모드 / 기지 전투 — v1.1

**elementwar.xyz**: 적 기지 HP 2K, 라운드 클리어 → 랭킹

**모드 구현**:

| 항목 | 설계 |
|------|------|
| 구조물 | `Element Shrine` — 플레이어가 설치, 웨이브 방어 |
| 몹 | `Elemental` 몹 — 족별 특성 (할로겄=독, 알칼리=폭발) |
| 라운드 | 웨이브 N 클리어 → 보상 + 랭킹 기록 |
| 전투 | 원소 대전 시스템 사용 (플레이어 직접 X) |

---

## 4. 블록·아이템 목록 (v1)

### 4.1 블록

| ID | 이름 | 티어 |
|----|------|------|
| `synthesis_lab` | 화학 합성실 | 기본 |
| `element_refinery` | 원소 정제기 | 기본 |
| `energy_converter` | 에너지 변환기 | 기본 |
| `periodite_ore` | 주기율광석 | 월드 생성 |
| `fusion_chamber` | 핵융합 챔버 | v1.1 |
| `element_shrine` | 원소 신전 | v1.1 |
| `research_table` | 연구대 | v1.1 |

### 4.2 아이템 (카테고리)

| 카테고리 | 수량 | 비고 |
|----------|------|------|
| Element Cards | 118 | 핵심 |
| Molecule Cards | 50→500 | Tier A/B/C |
| Chemistry Journal | 1 | 도감+PT |
| Guide Book | 1 | 튜토리얼 |
| Refining Catalyst | 1 | 정제 보조 |
| Stellar items | ~10 | v1.1 |

---

## 5. 월드 생성 & 바이옴

### 5.1 새 광물

| 광물 | Y레벨 | 바이옴 | 원소 |
|------|-------|--------|------|
| Periodite Ore | -64~16 | 전체 (희귀) | 랜덤 Z 1~26 |
| Deep Periodite | -64~-32 | 심층 | Z 27~56 |
| Nether Salt | 네더 | 전체 | Na, Cl |
| End Crystal Ore | 엔드 | 엔드 | 희귀 원소 |

### 5.2 바이옴 보정

| 바이옴 | 추가 원소 |
|--------|-----------|
| 사막 | Na, Mg |
| 바다/갯벌 | Cl, Br |
| 정글 | 다양 (유기) |
| 심층 | Fe, Cu, U |
| 네더 | S, Fe |
| 엔드 | Xe, Og(합성만) |

---

## 6. Fabric 26.2 기술 스펙

### 6.1 빌드 환경

| 컴포넌트 | 버전 |
|----------|------|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.156.0+26.2 |
| Loom | 1.17 (`net.fabricmc.fabric-loom`) |
| Gradle | 9.5.1 |
| Java | 21+ |

> 26.2는 **비난독화** — `fabric-loom` (not `fabric-loom-remap`)

### 6.2 모듈 구조 (계획)

```text
wonso-mod/
├── build.gradle
├── gradle.properties
├── src/main/
│   ├── java/com/appiemon/wonso/
│   │   ├── WonsoMod.java              # Entrypoint
│   │   ├── registry/
│   │   │   ├── WonsoBlocks.java
│   │   │   ├── WonsoItems.java
│   │   │   ├── WonsoEntities.java
│   │   │   └── WonsoScreenHandlers.java
│   │   ├── block/
│   │   │   ├── SynthesisLabBlock.java
│   │   │   └── ElementRefineryBlock.java
│   │   ├── item/
│   │   │   ├── ElementCardItem.java
│   │   │   └── ChemistryJournalItem.java
│   │   ├── screen/
│   │   │   ├── PeriodicTableScreen.java    # 3모드 GUI
│   │   │   ├── SynthesisLabScreen.java
│   │   │   ├── MoleculeDexScreen.java
│   │   │   └── EnergyPanelScreen.java
│   │   ├── client/
│   │   │   ├── WonsoHudOverlay.java        # ⚡ 에너지 HUD
│   │   │   └── AtomModelRenderer.java      # Bohr 2D
│   │   ├── gameplay/
│   │   │   ├── ElementEnergyManager.java
│   │   │   ├── SynthesisRecipeManager.java
│   │   │   ├── DiscoveryManager.java       # 도감
│   │   │   └── GroupSynergyHandler.java
│   │   ├── world/
│   │   │   ├── PerioditeOreFeature.java
│   │   │   └── BiomeElementSpawns.java
│   │   └── network/
│   │       └── WonsoPackets.java
│   └── resources/
│       ├── fabric.mod.json
│       ├── assets/wonso/
│       │   ├── lang/ko_kr.json
│       │   ├── textures/          # 카드·블록·GUI
│       │   ├── models/
│       │   └── textures/gui/      # elementwar 스타일 GUI
│       └── data/wonso/
│           ├── recipe/            # 합성 레시피
│           ├── advancement/         # 12단계 튜토리얼
│           └── loot_table/
├── shared-data/                   # wonso repo 공유 JSON
│   ├── elements.json
│   ├── molecules.json
│   └── synthesis.json
└── tools/
    └── data-codegen/              # JSON → Java 상수 생성
```

### 6.3 26.2 주의사항

| 변경 | 대응 |
|------|------|
| Blaze3D (OpenGL 제거) | GUI는 Blaze3D API, 커스텀 렌더 최소화 |
| Block/Item ID 분리 | Registry 분리 준수 |
| Fluid Interaction API | 액체 원소 (v2, Br₂ 등) |
| Tag remove API | 바닐라 레시피 태그 수정 시 사용 |

### 6.4 클라이언트 vs 서버 (모드)

| 기능 | 클라이언트 | 서버 |
|------|------------|------|
| GUI 전체 | ✅ 렌더 | — |
| 합성 검증 | 표시 | ✅ 레시피 검증 |
| 에너지 변경 | HUD | ✅ NBT 저장 |
| 도감 발견 | 표시 | ✅ 기록 |
| 웨이브 스폰 | — | ✅ |
| 랭킹 제출 | 요청 | ✅ API 프록시 (v1.2) |

---

## 7. Advancement 튜토리얼 (12단계)

elementwar.xyz 12단계 튜토리얼 매핑:

| # | Advancement | 내용 |
|---|-------------|------|
| 1 | `wonso/root` | Journal 획득 |
| 2 | `wonso/first_element` | 첫 원소 카드 |
| 3 | `wonso/open_periodic_table` | 주기율표 열기 |
| 4 | `wonso/atom_view` | 원자 모드 탭 확인 |
| 5 | `wonso/first_synthesis` | 첫 분자 합성 |
| 6 | `wonso/molecule_dex` | 도감 1개 발견 |
| 7 | `wonso/energy_convert` | 분子→에너지 |
| 8 | `wonso/energy_skill` | 첫 에너지 스킬 |
| 9 | `wonso/group_synergy` | 같은 족 3장 조합 |
| 10 | `wonso/fusion_intro` | 핵융합 입문 |
| 11 | `wonso/star_system` | 별 획득 |
| 12 | `wonso/round_clear` | 라운드 1 클리어 |

---

## 8. 릴리스 로드맵 (모드)

| 버전 | 범위 | elementwar 대응 |
|------|------|-----------------|
| **0.1 α** | 원소 카드 20 + Journal + PT GUI | 카드, 도감 기초 |
| **0.2 β** | 합성실 + Tier A 50분子 + 에너지 | 합성실, 에너지 |
| **0.3 γ** | 원소 118 + 원자 모드 GUI | 3모드 완성 |
| **1.0** | 월드 생성 + 정제 + Advancement | 야생 풀 루프 |
| **1.1** | 별·핵융합·웨이브·라운드 | 별, 라운드, 핵융합 |
| **1.2** | **원소 대전 PvP** + 아레나 + 매칭 + 랭킹 | 온라인 매칭, 랭킹 |
| **2.0** | 연구소 + Tier B/C 분子 | 연구소, 확장 |

---

## 9. 에셋 & UI 방향

| 항목 | 방향 |
|------|------|
| GUI | elementwar.xyz 다크 테마 + 화학 아이콘 클론 |
| 카드 아트 | 원소별 고유 색 (족 카테고리) |
| 주기율표 | 사이트 PT 레이아웃 + MC 픽셀 아트 |
| 사운드 | 합성 성공/실패, 에너지 충전 |
| Guide Book | 튜토리얼 12단계 텍스트 |

---

## 10. 의존 모드 & 호환

| 모드 | 관계 |
|------|------|
| Fabric API | 필수 |
| JEI/EMI | 권장 (레시피 표시) |
| Mod Menu | 권장 (설정) |
| Sodium | 호환 목표 |
| 바닐라 | 기본 호환, 게임 규칙 변경 최소 |

---

## 11. KPI

| 지표 | 1.0 목표 |
|------|----------|
| 원소 카드 118 | 100% |
| Tier A 분子 | 50 |
| Advancement 12 | 100% |
| 야생 Day 7 리텐션 | 측정 후 설정 |
| elementwar 랭킹 연동 | v1.2 |
