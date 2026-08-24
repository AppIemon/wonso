# 데이터 파이프라인

1. `python3 scripts/data/generate.py` — 118원소·118원자·Tier A 50분자·퀴즈·정제 JSON
2. `python3 scripts/data/validate.py` — 118 완전성 + formula 파싱
3. `python3 scripts/data/generate_hive_assets.py` — Hive AI Bearer로 아이템/블록/GUI PNG (`HIVE_API_KEY`)
4. `python3 scripts/data/generate_assets.py` — Fabric `items/*.json`·모델·언어 (기존 PNG 유지)
5. `python3 scripts/data/write_advancements.py` — Advancement 12단계
