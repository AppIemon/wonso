# wonso — 콘텐츠 작성 계획

> 118원소(H~Og) × 3모드 데이터·퀴즈·해설 문안 작성 로드맵
> **작성자 역할 분리**: 데이터 엔지니어(구조) / 화학 검수자(정확성) / UX 라이터(해설·퀴즈)

---

## 1. 콘텐츠 총량 개요

| 콘텐츠 유형 | 수량 | 우선순위 | 작성 방식 |
|-------------|------|----------|-----------|
| 원소 레코드 | 118 | P0 | 반자동(CSV→JSON) + 수동 검수 |
| 원자 모델 레코드 | 118 | P0 | 원소 데이터 파생 + 동위원소 수동 |
| 분자 Tier A | 50 | P0 | 수동 작성 |
| 분자 Tier B | 150 | P1 | 반자동 + 검수 |
| 분자 Tier C | 300+ | P2 | 크라우드 + 검수 |
| 원소 퀴즈 문항 | 80 | P0 | 템플릿 생성 + 수동 |
| 원자 퀴즈 문항 | 50 | P1 | 템플릿 + 수동 |
| 분자 퀴즈 문항 | 50 | P1 | 템플릿 + 수동 |
| 통합 퀴즈 (3지선다) | 30 | P1 | 수동 |
| 원소 상세 해설 (1~3문장) | 118 | P1 | UX 라이터 |
| 분자 해설 | 50→500 | P0→P2 | 단계적 |

**총 초기(P0) 작성량**: 118 + 118 + 50 + 80 ≈ **346 레코드** + 해설 문안

---

## 2. 작성 원칙

### 2.1 언어·표기

- **한글 원소명**: 대한화학회 표기 준수 (수소, 오가네손)
- **영문**: IUPAC (Hydrogen, Oganesson)
- **기호**: 대소문자 정확 (Na, Cl, Og)
- **분자식 표시**: `formula`(ASCII) + `formulaDisplay`(유니코드 아래첨자)
- **난이도 문안**: 중학생 눈높이, 전문 용어는 첫 등장 시 괄호 설명

### 2.2 정확성

- 원자량: IUPAC 2024 기준, 불확정은 `massUncertainty` 필드
- 전자 배치: 축약형 우선 `[He] 2s² 2p²`
- 동위원소: 안정/불안정 명시, 방사성은 `abundance: null`
- Og(Z=118): 합성 원소, 분자 없음 → `hasKnownMolecules: false` + 해설

### 2.3 교육 정합 (중등)

- 성취기준 연계 태그: `achievement: ["9화01-01", ...]`
- 핵심 구분 강조: 원소(종류) / 원자(한 개) / 분자(결합체)
- 계수·아래첨자: 색상·문안으로 반복 강조

---

## 3. 페이즈별 작성 계획

### Phase 0 — 기반 (1주차 개념)

| 작업 | 담당 | 산출물 |
|------|------|--------|
| 스키마 확정 | 기획+개발 | Zod 스키마 문서 |
| 소스 CSV 수집 | 데이터 | `sources/pubchem.csv`, `sources/iupac.json` |
| 118원소 자동 생성 스크립트 | 개발 | `build-elements.ts` |
| 한글명 매핑 테이블 | 데이터 | `sources/korean-names.json` |
| 검증 테스트 | 개발 | 118 완전성 CI |

**완료 기준**: `elements.json` 118개, Zod pass, 한글명 100% 매핑

---

### Phase 1 — 원소 모드 콘텐츠 (2~3주차)

#### 1-A. 원소 기본 데이터 (118)

**작성 순서** (학습 난이도·교육과정 순):

| 배치 | Z 범위 | 원소 수 | 비고 |
|------|--------|---------|------|
| Batch 1 | 1~20 | 20 | H~Ca, 중1 도입 |
| Batch 2 | 21~56 | 36 | 전이금속 포함 |
| Batch 3 | 57~88 | 32 | 란타넘족 |
| Batch 4 | 89~118 | 29 | 악티늄족, 초중원소 |

**각 원소 체크리스트**:

- [ ] Z, symbol, nameKo, nameEn
- [ ] atomicMass (+ uncertainty if needed)
- [ ] period, group, category
- [ ] stateAtRoomTemp
- [ ] electronConfiguration
- [ ] displayRow, displayColumn
- [ ] electronegativity, density (nullable)
- [ ] yearDiscovered
- [ ] aliasesKo (검색용)
- [ ] descriptionKo (1~3문장, Batch 1부터)

#### 1-B. 원소 퀴즈 (80문항)

| 유형 | 문항 수 | 생성 방법 |
|------|---------|-----------|
| 기호→한글명 | 30 | 템플릿: `{symbol}의 한글명?` |
| 한글명→기호 | 20 | 템플릿 |
| 족/주기 | 15 | 수동 (할로겐, 알칼리금속 등) |
| 상온 상태 | 10 | 수동 (액체 Hg, Br) |
| 카테고리 | 5 | 수동 |

**문항 템플릿 예시**:

```yaml
- type: symbol_to_name
  promptKo: "기호 'Na'의 한글 원소명은?"
  correctAnswer: "나트륨"
  explanationKo: "Na는 Sodium의 기호이며, 한글로는 나트륨입니다."
  relatedZ: 11
  difficulty: 1
```

**완료 기준**: 원소 모드 α 릴리스 가능, 퀴즈 50문항 이상

---

### Phase 2 — 원자 모드 콘텐츠 (4~5주차)

#### 2-A. 원자 모델 데이터 (118)

**파생 규칙** (자동화 가능):

- `defaultProtons` = Z
- `defaultElectrons` = Z (중성)
- `shellDistribution` = 전자 배치 규칙 테이블에서 계산
- `valenceElectrons` = 주기율표 위치에서 계산

**수동 작성 필요**:

- `isotopes[]` — 각 원소 최소 1개, 주요 원소 2~3개
- 불안정 동위원소: `abundance: null`, 반감기 텍스트 (선택)

**우선 수동 작성 원소 (교육 필수 20)**:

H, C, N, O, Na, Cl, Fe, Cu, Ag, Au, He, Ne, Ar, K, Ca, S, P, Mg, Al, Si

#### 2-B. 원자 해설 문안

| 주제 | 문안 수 | 예시 |
|------|---------|------|
| 양성자=원자번호 | 1 (공통) | 마스터 템플릿 |
| 전자=양성자 (중성) | 1 (공통) | |
| 동위원소 설명 | 20 | 탄소 ¹²C/¹⁴C |
| 원자가 전자 | 15 | Na 1개, Cl 7개 |

#### 2-C. 원자 퀴즈 (50문항)

| 유형 | 수 |
|------|-----|
| 전자 수 맞추기 | 15 |
| 양성자 수 맞추기 | 10 |
| 동위원소 비교 | 10 |
| 전자 배치 쓰기 | 10 |
| 원자가 전자 | 5 |

**완료 기준**: Bohr L1 뷰어 + 퀴즈 30문항

---

### Phase 3 — 분자 모드 콘텐츠 (6~8주차)

#### 3-A. Tier A 분자 50 (필수)

**카테고리별 목표**:

| 카테고리 | 수 | 예시 |
|----------|-----|------|
| 단원질 | 8 | H₂, O₂, N₂, Cl₂, F₂, Br₂, I₂, O₃ |
| 무기 간단 | 15 | H₂O, CO₂, NH₃, HCl, NaCl, SO₂, NO₂, H₂S, CO, SiO₂, P₄, S₈, H₂SO₄, HNO₃, H₃PO₄, CaCO₃ |
| 단순 유기 | 12 | CH₄, C₂H₆, C₂H₄, C₂H₂, C₃H₈, CH₃OH, C₂H₅OH, CH₃COOH, C₆H₁₂O₆, C₂H₄O₂, CH₄O, C₃H₆O |
| 이온·염 | 10 | Na⁺, Cl⁻, Ca²⁺, Fe³⁺, SO₄²⁻, NO₃⁻, OH⁻, NH₄⁺, CO₃²⁻, PO₄³⁻ |
| 특수/교육 | 5 | H₂O₂, NaOH, Ca(OH)₂, MgO, Al₂O₃ |

**각 분자 체크리스트**:

- [ ] id (slug: `water`, `carbon-dioxide`)
- [ ] formula, formulaDisplay
- [ ] nameKo, nameEn
- [ ] elements[] (파싱 검증)
- [ ] relatedElements[] (Z 목록)
- [ ] category, difficulty
- [ ] descriptionKo (계수/아래첨자 교육 포인트 포함)
- [ ] structureType (선택)

**descriptionKo 작성 가이드**:

> "물(H₂O)은 **수소 원자 2개**와 **산소 원자 1개**가 결합한 **분자**입니다. H₂O에서 숫자 2는 아래첨자로, 산소 한 분자 안에 수소가 2개 있다는 뜻입니다."

#### 3-B. 원소↔분자 역참조 (118)

각 원소 Z에 `relatedMoleculeIds[]` 연결:

- H → [h2, water, hcl, ammonia, methane, ...]
- Og → [] + `noteKo: "오가네손은 초중원소로, 안정한 분자가 알려지지 않았습니다."`

#### 3-C. 분자 퀴즈 (50문항)

| 유형 | 수 |
|------|-----|
| 원자 개수 파싱 | 20 |
| 원소/원자/분자 구분 | 15 |
| 분자식→이름 | 10 |
| 이름→분자식 | 5 |

**구분 퀴즈 예시**:

```yaml
promptKo: "다음 중 '분자'인 것은?"
choices: ["O (산소 원자)", "O₂ (산소 분자)", "O (산소 원소)"]
correctAnswer: "O₂ (산소 분자)"
explanationKo: "O는 산소 원소/원자를 나타낼 때 쓰고, O₂는 산소 분자 1개를 나타냅니다."
difficulty: 1
```

#### 3-D. Tier B (150) — Phase 3 후반~Phase 4

- 원소별 추가 분자 1~3개
- 유기 화합물 확장
- 산·염기·염 확장

**완료 기준**: Tier A 50 + 역참조 95% + 퀴즈 30문항

---

### Phase 4 — 통합·확장 (9~10주차)

| 작업 | 산출물 |
|------|--------|
| 통합 퀴즈 30 | 3모드 혼합 |
| 원소 descriptionKo 118 완성 | 전 원소 해설 |
| Tier B 150 | molecules JSON |
| Og 등 초중원소 특수 해설 | 불확실성 각주 |
| 영문 i18n 키 | nameEn, descriptionEn |
| 교사용 퀴즈 세트 10 | JSON export |

---

## 4. 작성 워크플로 (Git 기반)

```text
content/
├── sources/                 # 원본 (수정 금지 스냅샷)
│   ├── pubchem-periodic.csv
│   ├── korean-names.json
│   └── manual-overrides.yaml
├── drafts/                  # 작성 중 YAML (human-friendly)
│   ├── elements/
│   │   ├── batch-01-01-20.yaml
│   │   └── ...
│   ├── atoms/
│   ├── molecules/
│   │   ├── tier-a.yaml
│   │   └── tier-b.yaml
│   └── quiz/
│       ├── element.yaml
│       ├── atom.yaml
│       └── molecule.yaml
├── generated/               # 빌드 출력 (CI only)
│   ├── elements.json
│   ├── atoms.json
│   └── molecules.json
└── README.md                # 작성 가이드
```

**PR 규칙**:

- Batch 단위 PR (예: `content/elements-batch-1`)
- CI: Zod validate + 118 completeness + formula parse test
- 화학 검수자 1명 approve 필수

---

## 5. 역할·RACI

| 역할 | 책임 |
|------|------|
| **기획 (PM)** | 스키마, 페이즈, KPI |
| **데이터 작성자** | YAML drafts 작성 |
| **화학 검수자** | 정확성, 교육과정 |
| **UX 라이터** | 해설·퀴즈 문안 톤 |
| **개발** | build 스크립트, CI, 임베드 |
| **디자인** | 주기율표 색상·카테고리 legend |

---

## 6. 품질 게이트 (Definition of Done)

### 원소 레코드 DoD

- [ ] Zod 스키마 pass
- [ ] 한글명 대한화학회 일치
- [ ] displayRow/Column 주기율표 렌더 테스트 pass
- [ ] descriptionKo 맞춤법 검수
- [ ] relatedMoleculeIds 연결 또는 null 사유 문서화

### 분자 레코드 DoD

- [ ] formula 파싱 결과 = elements[]
- [ ] formulaDisplay 유니코드 정확
- [ ] descriptionKo에 계수/아래첨자 교육 포인트 1개 이상
- [ ] difficulty 태그

### 퀴즈 문항 DoD

- [ ] explanationKo 2문장 이상
- [ ] 오답이 그럴듯함 (distractor 품질)
- [ ] relatedZ 또는 relatedMoleculeId 연결
- [ ] 난이도 1~3 태그

---

## 7. 특수 케이스 처리 가이드

| 케이스 | 처리 |
|--------|------|
| **수소 (Z=1)** | 원소/원자/분자 3모드 모범 사례. H, H₂, H₂O 연결 |
| **오가네손 (Z=118)** | 합성, 반감기 짧음, 분자 없음 명시 |
| **머늄 (Mc, Z=115)** | 2016 명명, 한글명 확인 |
| **상온 액체** | Hg(브롬 Br도 액체) — 퀴즈 포인트 |
| **7족 할로겐** | F, Cl, Br, I, At — 족 퀴즈 |
| **0족 (18족)** | He, Ne, Ar 등 — 비활성 기체 |
| **동위원소 불안정** | C-14, U-238 — 방사성 각주 |
| **다원자 분자** | P₄, S₈ — "분자"이지만 원자 수 >2 |
| **이온 표기** | Na⁺, SO₄²⁻ — 분자 모드 v1.1 |

---

## 8. 주간 작성 목표 (권장 속도)

| 주차 | 목표 산출물 | 누적 |
|------|-------------|------|
| W1 | 스키마 + 원소 Batch 1 (20) | 20 원소 |
| W2 | 원소 Batch 2~3 (68) | 88 원소 |
| W3 | 원소 Batch 4 + 퀴즈 40 | 118 원소, 40 퀴즈 |
| W4 | 원자 118 (자동+수동 20) | 118 원자 |
| W5 | 원자 퀴즈 30 + 해설 | |
| W6 | Tier A 분자 30 | 30 분자 |
| W7 | Tier A 분자 20 + 퀴즈 30 | 50 분자 |
| W8 | 역참조 118 + 통합 퀴즈 20 | |
| W9 | descriptionKo 118 완성 | |
| W10 | Tier B 150 + 검수 라운드 | |

---

## 9. 도구·자동화 계획

| 도구 | 용도 |
|------|------|
| `build-elements.ts` | CSV→YAML→JSON |
| `build-atoms.ts` | 전자 배치 규칙 계산 |
| `parse-formula.ts` | H₂SO₄ → elements 검증 |
| `generate-quiz.ts` | 템플릿 기반 문항 bulk 생성 |
| Zod + Vitest | CI 데이터 검증 |
| 맞춤법 (부산대) | descriptionKo 검수 |
| Notion/Linear | 작성 진도 트래킹 |

---

## 10. 오픈 질문 (작성 전 확정)

1. **Og 한글명**: "오가네손" 확정? (대한화학회)
2. **Tier A 50 목록**: 위 표 최종 확정 필요
3. **이온·염**: 분자 모드 v1 포함 vs v1.1 연기
4. **유기화학 범위**: 단순 유기만 vs 고등 범위
5. **방정식 콘텐츠**: 작성 범위·시점
