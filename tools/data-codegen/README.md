# data-codegen

`scripts/data/generate.py`가 shared-data JSON을 생성한다.
모드 리소스는 `scripts/data/generate_assets.py`가 textures/lang/models를 만든다.
Java 상수는 런타임에 `WonsoData`가 JSON을 로드하므로 별도 상수 생성은 필수가 아니다.
