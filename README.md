# wonso

수소(H) ~ 오가네손(Og) 118원소를 **원소·원자·분자** 3모드로 학습하는 **Minecraft 26.2 Fabric 야생 모드**.

레퍼런스: [elementwar.xyz](https://elementwar.xyz) — 카드·합성·에너지·도감 + **포켓몬식 원소 대전 PvP**

## 구현 상태

문서(`docs/`) 스펙을 코드로 옮긴 첫 구현입니다.

| 모듈 | 내용 |
|------|------|
| `shared-data/` | 118원소·118원자·Tier A 50분자·퀴즈·합성·정제·에너지 4스킬 |
| `wonso-mod/` | Fabric 26.2 모드 — 카드 118, Journal/PT 3모드 GUI, 합성·에너지·정제, Advancement 12 |
| `web/` | SvelteKit 3모드 UI (주기율표 / Bohr / 분자 / 퀴즈) |
| `scripts/data/` | JSON 생성·118 완전성 검증 |

## 기획 문서

- [docs/README.md](./docs/README.md) — 문서 인덱스
- [제품 기획서](./docs/PRODUCT_SPEC.md)
- [Minecraft Fabric 모드 기획](./docs/MINECRAFT_MOD_SPEC.md)
- [elementwar.xyz 레퍼런스](./docs/ELEMENTWAR_REFERENCE.md)
- [현실 반영 원소 획득](./docs/REAL_WORLD_EXTRACTION.md)
- [바닐라 거친 정제](./docs/VANILLA_REFINING_SPEC.md)
- [원소 대전 (PvP/PvE)](./docs/ELEMENT_BATTLE_SPEC.md)
- [아키텍처 계획](./docs/ARCHITECTURE_PLAN.md)
- [콘텐츠 작성 계획](./docs/CONTENT_WRITING_PLAN.md)

## 모드 (Java 25, Gradle 9.5.1, Loom 1.17)

```bash
cd wonso-mod
./gradlew build
```

인게임:

- 화학 도감 우클릭 또는 키 `P` — 주기율표 3모드
- `/wonso card H` · `/wonso energy 50` · `/wonso synth water` · `/wonso skill heal_all`
- 분자 카드 웅크리기 사용 — 에너지 변환
- 에너지 스킬: Z/X/C/V

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
