# wonso

수소(H) ~ 오가네손(Og) 118원소를 **원소·원자·분자** 3모드로 학습하는 **Minecraft 26.2 Fabric 야생 모드**.

레퍼런스: [elementwar.xyz](https://elementwar.xyz) — 카드·합성·에너지·도감 + **포켓몬식 원소 대전** (플레이어는 싸우지 않고 원소령에게 명령만)

## 구현 상태

문서(`docs/`) 스펙을 코드로 옮긴 구현입니다.

| 모듈 | 내용 |
|------|------|
| `shared-data/` | 118원소·118원자·Tier A+B 분자·퀴즈·합성·4단 정제·핵융합·별·에너지 4스킬 |
| `wonso-mod/` | Fabric 26.2 — 카드, Journal/PT 3모드, 합성·에너지·정제 블록 GUI, 월드젠 광석, 신전 PvE 대전, 파티 6 |
| `web/` | SvelteKit 3모드 UI (주기율표 / Bohr / 분자 / 퀴즈) |
| `scripts/data/` | JSON 생성·완전성 검증·Hive 텍스처 |

## 인게임 (명령어 없음)

일반 플레이는 **아이템·블록 GUI** 만 사용합니다. `/wonso …` 는 **OP(권한 2) 전용** 디버그입니다.

- **화학 도감** 우클릭 또는 키 `P` — 주기율표 3모드, 하단 탭으로 도감/합성/에너지/파티/대전/정제/융합/안내
- **원소 카드** 우클릭 — 해당 원소가 선택된 주기율표
- **분자 카드** 우클릭 — 분자 도감 / 웅크리기 사용 = 에너지
- **가이드북** 우클릭 — 조작 안내
- 분쇄기·반응통·정제기·전기분해기 우클릭 — 4단 정제
- 핵융합 챔버 — H+H→He, 3He→C, U+C→Og / 별 점화
- 원소 신전 — 턴제 PvE (파티 6, 스킬 4, 분자 도구, 족 상성)
- 연구대 — 파티 편성
- 에너지 스킬: Z/X/C/V

## 바로 해보기 (JAR)

바닐라에는 안 들어갑니다. **Minecraft 26.2 + Fabric** 이 필요합니다.

1. [Fabric Loader](https://fabricmc.net/use/installer/) 0.19.3+ 로 26.2 프로필 설치
2. [Fabric API](https://modrinth.com/mod/fabric-api) `0.158.0+26.2` 를 `mods/` 에 넣기
3. `wonso-0.1.2-alpha.jar` 를 같은 `mods/` 에 넣기
   - **[Releases](https://github.com/AppIemon/wonso/releases)** 에서 받기
   - 로컬: `wonso-mod/build/libs/wonso-0.1.2-alpha.jar` (`./gradlew build`)
   - PR: Actions → `mod-build` → Artifacts → `wonso-jar`
4. 런처 Java를 **25** 로 맞추기
5. 새 월드 입장 → 화학 도감이 지급됨. `P` 주기율표, 신전·정제기 우클릭

## 텍스처 (Hive AI)

모든 아이템/블록/GUI 그림은 **Hive AI V3 Flux Schnell** (`Authorization: Bearer`) 로 생성합니다.

```bash
export HIVE_API_KEY=...
python3 scripts/data/generate_hive_assets.py
python3 scripts/data/generate_assets.py   # items/*.json · 모델 · 언어. 기존 PNG는 덮어쓰지 않음
```

`generate_assets.py --force-pixels` 는 Hive 없이 단색 플레이스홀더만 다시 그립니다.

## 모드 빌드 (Java 25, Gradle 9.5.1, Loom 1.17)

```bash
cd wonso-mod
./gradlew build
```

## 웹

```bash
python3 scripts/data/generate.py
mkdir -p web/static/data && cp -R shared-data/. web/static/data/
cd web && npm install && npm run dev
```

## 데이터 검증

```bash
python3 scripts/data/validate.py
```
