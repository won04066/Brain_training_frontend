# 원본 규칙과 프론트 적용

## 출처

원본: [won04066/Brain_training](https://github.com/won04066/Brain_training)

- 확인한 main: `855f077f397b4b8ca6a2f13e0ced354a2f99bc91`
- 규칙·설계 문서: `docs/backend-plan`, 커밋 `34587d469b490373b5bde3ae90a42b2a7b201628`
- [공통 개발규칙](https://github.com/won04066/Brain_training/blob/34587d469b490373b5bde3ae90a42b2a7b201628/docs/백엔드_공통_개발규칙.md)
- [전체 개발계획](https://github.com/won04066/Brain_training/blob/34587d469b490373b5bde3ae90a42b2a7b201628/docs/백엔드_전체_개발계획.md)
- [1단계 설계안](https://github.com/won04066/Brain_training/blob/34587d469b490373b5bde3ae90a42b2a7b201628/docs/두뇌교실_백엔드_1단계_설계안.md)
- [게임 기획](https://github.com/won04066/Brain_training/blob/34587d469b490373b5bde3ae90a42b2a7b201628/docs/GAME_DESIGN.md)

위 문서는 main 미병합 기준안이다. 프론트 구현의 준수 기준으로 사용하되 팀 전체 승인이나 서비스 구현 완료로 해석하지 않는다.
사용자는 프론트 플랫폼으로 Java Swing을 선택했다. 기존 환경 JDK 21을 사용하며 새로운 의존성을 도입하지 않았다.

## 적용

| 원본 규칙 | 시작 프로젝트 적용 |
|---|---|
| GUI → Service → Repository | 화면 전용 패키지. GUI의 파일 접근·채점 구현 없음 |
| 측정·훈련·지식 분리 | 별도 탭, 인지 영역 선택은 측정·훈련에만 표시 |
| EDT 갱신 | EDT에서 시작·생성·선택·렌더링 검증 |
| 정답·해설 비노출 | 화면에 정답 데이터 없음 |
| 미구현 성공값 금지 | 게임 시작 버튼 비활성화, 서비스 미연결 명시 |
| 미산출을 0점으로 표현 금지 | 측정 기록 부재로 표시 |
| 공통 로직 중복 금지 | 백엔드 enum·타이머·판정·보상 구현을 복제하지 않음 |
| Git 작업 브랜치·리뷰 | feat/frontend/swing-starter, 후속 Draft PR·팀 검토 필요 |

## 다음 계약 검토

1. 첫 측정·훈련 한 쌍과 발표·제출 범위.
2. 백엔드 공통 타입의 배포·클래스패스와 서비스 조립 방식. 별도 저장소라는 이유로 동일 소스를 복사하지 않는다.
3. 정답 없는 화면 데이터, 게임별 입력, 제시/숨김/입력 단계, 오류와 취소 상태.
4. 백그라운드 서비스 호출, 중복 입력 방지, 완료 저장 실패·재시도.
5. 밀리초 단위 시간과 초기 제안값 채택, 난이도·점수·추천·보상 정책.
6. 인지 기록과 별도인 지식 분야·난이도·학습·복습 범위.

기존 main의 TEST/LEARNING/REVIEW 회차와 설계안의 MEASUREMENT/TRAINING을 동일 계약으로 취급하지 않는다.
45초·보너스 5초·60초 상한·15문항·120초 상한은 제안값이다. 현재 UI는 이를 실행 정책으로 구현하지 않는다.
