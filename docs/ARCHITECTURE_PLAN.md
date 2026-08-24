# wonso — 클라이언트/서버 아키텍처 계획

> 코드 구현 전 기술 설계 문서. 스택 선택·모듈 분리·API·데이터 배포 전략을 정의한다.

---

## 1. 아키텍처 원칙

1. **데이터 주도** — 118원소·분자·퀴즈는 JSON/DB로 분리, UI는 렌더만
2. **오프라인 우선** — 클라이언트 번들에 Tier A 데이터 포함
3. **모드 공유 커널** — ElementContext를 3모드가 공유
4. **점진적 서버 도입** — α~γ는 클라이언트만, v1.0부터 API
5. **AppIemon 패턴 정합** — SvelteKit 또는 Nuxt 풀스택 (기존 cfrule/umm 패턴)

---

## 2. 권장 스택 (결정안)

| 레이어 | 선택 | 근거 |
|--------|------|------|
| **프레임워크** | SvelteKit 2 + Svelte 5 | AppIemon cfrule 패턴, API routes 내장 |
| **언어** | TypeScript | 데이터 스키마·118원소 타입 안전 |
| **스타일** | Tailwind CSS | 반응형 주기율표 그리드 |
| **DB** | MongoDB | umm/cfrule 기존 운영 경험 |
| **배포** | Vercel (웹) + Capacitor (Android) | cfrule 동일 |
| **캐시** | IndexedDB (클라이언트) | 오프라인 분자 Tier B/C |

**대안**: AppsInToss 타깃 시 React + Vite (periodic-table-app 패턴). Toss 미니앱이 1차 타깃이면 React 전환 검토.

---

## 3. 시스템 구성도

```text
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT (Browser / App)               │
├─────────────────────────────────────────────────────────────┤
│  App Shell                                                  │
│    ├─ ModeRouter (element | atom | molecule)                │
│    ├─ ElementContext (selected Z, history)                  │
│    └─ QuizEngine (local)                                    │
│                                                             │
│  Modes                                                      │
│    ├─ ElementMode → PeriodicTable, ElementDetail          │
│    ├─ AtomMode    → AtomViewer, IsotopePanel               │
│    └─ MoleculeMode → FormulaParser, MoleculeLibrary       │
│                                                             │
│  Data Layer (client)                                        │
│    ├─ elements.bundle.json (118, always loaded)           │
│    ├─ atoms.bundle.json (118, always loaded)              │
│    ├─ molecules.tierA.json (bundled)                      │
│    └─ molecules.tierB+C (IndexedDB / lazy fetch)          │
│                                                             │
│  Services                                                   │
│    ├─ SyncService (progress ↔ server)                     │
│    └─ OfflineQueue (quiz results when offline)              │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS / REST (+ optional WS)
┌──────────────────────────┴──────────────────────────────────┐
│                        SERVER (SvelteKit API)                 │
├─────────────────────────────────────────────────────────────┤
│  /api/auth/*          회원·세션                             │
│  /api/elements/*      원소 메타 (버전·checksum)             │
│  /api/molecules/*     분자 Tier B/C 페이징                  │
│  /api/quiz/*          퀴즈 세트·결과·리더보드               │
│  /api/progress/*      학습 진도·XP                          │
│  /api/admin/*         콘텐츠 CMS (교사)                     │
│                                                             │
│  MongoDB Collections                                        │
│    users, progress, quizResults, quizSets, contentVersions  │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. 클라이언트 모듈 구조 (계획)

```text
src/
├── app.html
├── routes/
│   ├── +layout.svelte          # 모드 탭, ElementContext
│   ├── +page.svelte            # 홈 / 모드 선택
│   ├── element/
│   │   ├── +page.svelte        # 주기율표
│   │   └── [z]/+page.svelte    # 원소 상세
│   ├── atom/
│   │   └── [z]/+page.svelte    # 원자 구조
│   ├── molecule/
│   │   ├── +page.svelte        # 분자 라이브러리
│   │   └── [id]/+page.svelte   # 분자 상세
│   └── quiz/
│       └── [mode]/+page.svelte # 모드별 퀴즈
│
├── lib/
│   ├── modes/
│   │   ├── element/            # PeriodicTable, ElementCard
│   │   ├── atom/               # BohrViewer, ShellDiagram
│   │   └── molecule/           # FormulaParser, AtomBreakdown
│   │
│   ├── data/
│   │   ├── schemas/            # Zod: Element, Atom, Molecule
│   │   ├── bundles/            # 정적 JSON (빌드 시 임베드)
│   │   └── loaders/            # lazy load, IndexedDB
│   │
│   ├── quiz/
│   │   ├── generators/         # 모드별 문제 생성
│   │   ├── validators/         # 답 검증
│   │   └── types.ts
│   │
│   ├── stores/
│   │   ├── elementContext.ts   # 선택 원소 Z
│   │   ├── progress.ts         # 로컬 XP
│   │   └── settings.ts         # 시각화 레벨 L1/L2/L3
│   │
│   └── server/                 # SvelteKit server-only
│       ├── db.ts
│       ├── models/
│       └── services/
│
└── static/
    └── data/                   # 빌드된 JSON (gzip)
```

---

## 5. 서버 API 설계 (v1.0)

### 5.1 인증

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/auth/register` | 이메일/소셜 |
| POST | `/api/auth/login` | JWT 세션 |
| GET | `/api/auth/me` | 현재 사용자 |

### 5.2 콘텐츠 (버전 관리)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/content/manifest` | 데이터 버전·checksum 목록 |
| GET | `/api/content/elements` | 118원소 (변경 시만 fetch) |
| GET | `/api/content/atoms` | 118원자 구성 |
| GET | `/api/molecules` | `?tier=B&page=1&z=6` 페이징 |

**Manifest 예시**:

```json
{
  "version": "2026.08.1",
  "bundles": {
    "elements": { "checksum": "abc...", "size": 45000 },
    "atoms": { "checksum": "def...", "size": 38000 },
    "molecules_tier_a": { "checksum": "ghi...", "size": 12000 }
  }
}
```

### 5.3 진도·퀴즈

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/progress` | 사용자 전체 진도 |
| POST | `/api/progress/event` | 탐색·퀴즈 이벤트 |
| GET | `/api/quiz/set/:mode` | 퀴즈 세트 (난이도) |
| POST | `/api/quiz/submit` | 결과 제출 |
| GET | `/api/quiz/leaderboard` | 주간 리더보드 |

### 5.4 교사 (v1.2)

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/admin/quiz-set` | 커스텀 퀴즈 생성 |
| GET | `/api/admin/class/:id/progress` | 학급 진도 |

---

## 6. 데이터 스키마 (TypeScript 개념)

### 6.1 Element (118 records)

```typescript
interface Element {
  z: number;                    // 1-118
  symbol: string;
  nameKo: string;
  nameEn: string;
  atomicMass: number | null;
  massUncertainty?: string;
  period: number;
  group: number | null;
  category: ElementCategory;
  stateAtRoomTemp: 'solid' | 'liquid' | 'gas' | 'unknown';
  electronConfiguration: string | null;
  electronegativity: number | null;
  density: number | null;
  yearDiscovered: string | null;
  displayRow: number;
  displayColumn: number;
  aliasesKo?: string[];
  hasKnownMolecules: boolean;   // Og 등 false
}
```

### 6.2 Atom (118 records)

```typescript
interface AtomModel {
  z: number;
  defaultNeutrons: number;
  shellDistribution: number[];  // [2,8,8,1] for K
  valenceElectrons: number;
  isotopes: Isotope[];
  ions?: IonForm[];
}

interface Isotope {
  massNumber: number;
  neutrons: number;
  abundance: number | null;   // null = unstable/synthetic
  name?: string;
}
```

### 6.3 Molecule (500+ records, tiered)

```typescript
interface Molecule {
  id: string;
  formula: string;
  formulaDisplay: string;
  nameKo: string;
  nameEn: string;
  elements: { symbol: string; count: number }[];
  relatedElements: number[];  // Z numbers
  category: MoleculeCategory;
  structureType?: string;
  descriptionKo: string;
  tier: 'A' | 'B' | 'C';
  difficulty: 1 | 2 | 3;
}
```

### 6.4 Quiz

```typescript
interface QuizQuestion {
  id: string;
  mode: 'element' | 'atom' | 'molecule' | 'integrated';
  type: string;
  difficulty: 1 | 2 | 3;
  promptKo: string;
  choices?: string[];
  correctAnswer: string | number;
  explanationKo: string;
  relatedZ?: number;
  relatedMoleculeId?: string;
}
```

---

## 7. 클라이언트 vs 서버 책임 분리

| 기능 | 클라이언트 | 서버 |
|------|------------|------|
| 주기율표 렌더 | ✅ | — |
| Bohr 모델 애니메이션 | ✅ | — |
| 분자식 파싱·하이라이트 | ✅ | — |
| 로컬 퀴즈 (랜덤 생성) | ✅ | — |
| 계정·인증 | 토큰 저장 | ✅ 발급·검증 |
| 진도 영구 저장 | IndexedDB 캐시 | ✅ MongoDB |
| 리더보드 | 표시만 | ✅ 집계 |
| 콘텐츠 업데이트 | manifest 비교 후 fetch | ✅ CDN/DB |
| 교사 퀴즈 세트 | 표시 | ✅ CRUD |
| 분석·리포트 | — | ✅ (v1.2) |

---

## 8. 데이터 파이프라인 (작성 → 배포)

```text
[작성]
  scripts/data/
    sources/          # PubChem CSV, NIST, 수동 YAML
    build-elements.ts # 118 Element JSON 생성
    build-atoms.ts    # AtomModel JSON 생성
    build-molecules.ts# Tier 분류
    validate.ts       # Zod 스키마 검증, 118 완전성 체크

[검증]
  tests/data/
    elements.test.ts  # Z 1-118 연속, symbol 유일
    atoms.test.ts     # protons === z
    molecules.test.ts # formula 파싱 일치

[배포]
  static/data/*.json.gz  → 클라이언트 번들
  MongoDB import         → 서버 API (Tier B/C)
  manifest.json          → 버전 관리
```

**118 완전성 검증 규칙**:

- `elements.length === 118`
- `∀ z ∈ [1,118], ∃ element where element.z === z`
- `symbols` 유일, `displayRow/Column` 충돌 없음
- Og(Z=118): `hasKnownMolecules === false` 허용

---

## 9. 성능·번들 전략

| 데이터 | 예상 크기 | 전략 |
|--------|-----------|------|
| elements.json | ~45 KB | 메인 번들 |
| atoms.json | ~35 KB | 메인 번들 |
| molecules_tier_a.json | ~15 KB | 메인 번들 |
| molecules_tier_b.json | ~80 KB | lazy + IndexedDB |
| molecules_tier_c.json | ~200 KB | API 페이징 |
| 이미지/아이콘 | ~500 KB | SVG 스프라이트 |

**총 초기 번들 목표**: < 150 KB (gzip 데이터) + UI 코드

---

## 10. 보안·프라이버리

- 퀴즈 정답은 클라이언트에 포함 가능 (교육 앱, 치팅 허용 범위)
- 리더보드 제출은 서버 검증 (타임스탬프·중복 방지)
- 미성년자 계정: 부모 연결, 최소 데이터 수집
- Og 등 합성 원소: "실험적 데이터" 면책 문구

---

## 11. 구현 단계 (기술 작업 순서)

| 순서 | 작업 | 산출물 |
|------|------|--------|
| 1 | 스키마 + validate 스크립트 | Zod types, CI test |
| 2 | elements 118 작성·임베드 | elements.json |
| 3 | SvelteKit 셸 + ElementMode UI | 주기율표 α |
| 4 | atoms 118 작성 | atoms.json |
| 5 | AtomMode Bohr L1 | 원자 모드 β |
| 6 | molecules Tier A 50 | molecules JSON |
| 7 | FormulaParser + MoleculeMode | 분자 모드 γ |
| 8 | QuizEngine (로컬) | 3모드 퀴즈 |
| 9 | MongoDB + auth API | 서버 v1.0 |
| 10 | SyncService + manifest | 콘텐츠 업데이트 |
| 11 | Capacitor Android | 앱 패키징 |

---

## 12. 미결정 사항 (결정 필요)

| # | 질문 | 옵션 | 권장 |
|---|------|------|------|
| 1 | 1차 배포 플랫폼 | 웹 / AppsInToss / Android | 웹 → Android |
| 2 | 시각화 L3 오비탈 | 포함 / v2 연기 | v2 연기 |
| 3 | 방정식 모드 | v1.2 / v2 | v1.2 |
| 4 | 소셜 로그인 | Google / Kakao / 이메일 | Kakao + 이메일 (한국) |
| 5 | "마크 모드" 별도 기능? | 확인 필요 | — |
