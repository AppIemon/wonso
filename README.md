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
| `scripts/data/` | JSON 생성·완전성 검증 |

## 인게임

- 화학 도감 우클릭 또는 키 `P` — 주기율표 3모드, 파티/대전/정제/융합
- 분쇄기·반응통·정제기·전기분해기 우클릭 — 4단 정제
- 핵융합 챔버 — H+H→He, 3He→C, U+C→Og / 별 점화
- 원소 신전 — 턴제 PvE (파티 6, 스킬 4, 분자 도구, 족 상성)
- 연구대 — 파티 편성
- 에너지 스킬: Z/X/C/V
- `/wonso card H` · `energy 50` · `synth water` · `refine crush` · `fusion fusion-he` · `star protostar` · `battle` · `party add 17`

월드젠: 오버월드 periodite (−64~16) / deep periodite (−64~−32), 네더 소금, 엔드 결정 광석.

## 바로 해보기 (JAR)

바닐라에는 안 들어갑니다. **Minecraft 26.2 + Fabric** 이 필요합니다.

1. [Fabric Loader](https://fabricmc.net/use/installer/) 0.19.3+ 로 26.2 프로필 설치
2. [Fabric API](https://modrinth.com/mod/fabric-api) `0.158.0+26.2` 를 `mods/` 에 넣기
3. `wonso-0.1.1-alpha.jar` 를 같은 `mods/` 에 넣기
   - **[Releases](https://github.com/AppIemon/wonso/releases)** 에서 받기
   - 로컬: `wonso-mod/build/libs/wonso-0.1.1-alpha.jar` (`./gradlew build`)
   - PR: Actions → `mod-build` → Artifacts → `wonso-jar`
4. 런처 Java를 **25** 로 맞추기
5. 새 월드 입장 → 화학 도감이 지급됨. `P` 주기율표, 신전·정제기 우클릭

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
