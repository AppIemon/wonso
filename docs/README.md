# wonso 기획 문서 인덱스

| 문서 | 내용 |
|------|------|
| [PRODUCT_SPEC.md](./PRODUCT_SPEC.md) | 제품 기획서 — 3모드 정의, UX, KPI, 로드맵 |
| [MINECRAFT_MOD_SPEC.md](./MINECRAFT_MOD_SPEC.md) | **Minecraft 26.2 Fabric 모드** — 야생 서바이벌, 블록·아이템, GUI |
| [ELEMENTWAR_REFERENCE.md](./ELEMENTWAR_REFERENCE.md) | **elementwar.xyz** 레퍼런스 분석 & 모드 이식 맵 |
| [REAL_WORLD_EXTRACTION.md](./REAL_WORLD_EXTRACTION.md) | **현실 반영** — 화합물·산업 공정·118 루트 (최우선) |
| [VANILLA_REFINING_SPEC.md](./VANILLA_REFINING_SPEC.md) | 바닐라 거친 정제 — 현실 루트의 인게임 구현 |
| [ELEMENT_BATTLE_SPEC.md](./ELEMENT_BATTLE_SPEC.md) | **원소 대전** — 포켓몬식 PvP/PvE (플레이어 직접 전투 ❌) |
| [ARCHITECTURE_PLAN.md](./ARCHITECTURE_PLAN.md) | 공유 데이터·API·스키마 (모드+웹) |
| [CONTENT_WRITING_PLAN.md](./CONTENT_WRITING_PLAN.md) | 118원소·분자·퀴즈 콘텐츠 작성 로드맵 |

## 프로젝트 한 줄

**wonso** — 수소(H)~오가네손(Og) 118원소를 **원소·원자·분자** 3모드로 학습하는 **Minecraft 26.2 Fabric 야생 모드**. 야생에서 원소를 모아 **포켓몬식 원소 대전 PvP** (플레이어 직접 싸움 ❌).

## 레퍼런스

- https://elementwar.xyz — 원소 대전 (화학 원소 전략 카드 게임)

## 현재 상태

- 코드: 미구현 (기획 단계)
- 다음 단계: Fabric 모듈 셋업 → shared-data 스키마 → 원소 Batch 1 (Z 1~20)

## 문서 관계

```text
elementwar.xyz (레퍼런스)
        │
        ▼
REAL_WORLD_EXTRACTION.md ──→ VANILLA_REFINING_SPEC.md
        │                           │
        │                           ├── MINECRAFT_MOD_SPEC.md
        │                           ├── ELEMENT_BATTLE_SPEC.md (PvP)
        ▼                           ▼
shared-data/extraction/        Fabric 26.2 모드
        │
        ▼
PRODUCT_SPEC.md + CONTENT_WRITING_PLAN.md
```
