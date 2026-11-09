# Brain_training_frontend

두뇌 성장 교실의 **Java Swing 데스크톱 프론트엔드** 시작 프로젝트.
추가 라이브러리 없이 JDK 21로 빌드한다. 웹 서버·Node.js는 필요하지 않다.

## 시작

JDK 21의 `bin`이 PATH에 있어야 한다. GUI 실행에는 데스크톱 디스플레이가 필요하다.

```sh
bash scripts/build.sh
bash scripts/check.sh
bash scripts/run.sh
```

`build/`는 생성 결과이며 Git에서 제외된다. 스크립트는 어느 디렉터리에서도 실행할 수 있다.

## 현재 범위

- 인지 측정·인지 훈련·지식학습을 구분한 Swing 시작 화면.
- 인지 6영역 선택과 측정/훈련 전환. 게임 시작은 백엔드 미연결로 비활성화.
- EDT에서 생성·화면 전환을 검증하는 headless 체크.
- 실제 게임, 채점, 저장, 사용자 등록, 시간·보상 정책, 백엔드 어댑터는 미구현.

백엔드 main의 PracticeService는 기존 문제 회차 코드다. 인지 측정 서비스로 대신 연결하지 않는다.
프론트에서 공통 백엔드 enum과 서비스를 복제하지 않고, 계약이 확정되면 어댑터를 연결한다.

## 구조

- `src/brainclassroom/frontend/`: 실행 진입점·UI
- `checks/brainclassroom/frontend/`: 외부 테스트 라이브러리 없는 화면 검증
- `docs/UPSTREAM_RULES.md`: 원본 규칙 출처·적용·미확정 계약
- `AGENTS.md`: 후속 개발 작업 규칙

다음 단계는 첫 측정·훈련 게임 한 쌍과 화면 데이터·입력·오류·상태 전환을 백엔드 담당자와 맞추는 것이다.
