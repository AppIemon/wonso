# 데이터 파이프라인

1. `python3 scripts/data/generate.py` — 118원소·118원자·Tier A 50분자·퀴즈·정제 JSON
2. `python3 scripts/data/validate.py` — 118 완전성 + formula 파싱
3. `python3 scripts/data/generate_assets.py` — Fabric 텍스처/언어
4. `python3 scripts/data/write_advancements.py` — Advancement 12단계
