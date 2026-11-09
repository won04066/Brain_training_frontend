package brainclassroom.frontend;

import java.awt.Color;
import java.util.List;

/** 기획안의 화면용 카탈로그. 서버 공통 타입이나 게임 판정 모델을 대신하지 않는다. */
public record LearningArea(String id, String title, String subtitle, String symbol,
        Color color, String trainingGame, String trainingDescription,
        String measurementGame, String measurementDescription) {
    public LearningArea {
        if (id == null || id.isBlank() || title == null || title.isBlank()
                || subtitle == null || subtitle.isBlank() || symbol == null || symbol.isBlank()
                || color == null || trainingGame == null || trainingGame.isBlank()
                || trainingDescription == null || trainingDescription.isBlank()
                || measurementGame == null || measurementGame.isBlank()
                || measurementDescription == null || measurementDescription.isBlank()) {
            throw new IllegalArgumentException("영역 표시 정보가 필요합니다.");
        }
    }

    public static List<LearningArea> catalog() {
        return List.of(
                new LearningArea("math", "수학능력", "숫자와 친해지는 시간", "1+2", new Color(210, 98, 47),
                        "숫자 블록 망치", "필요 없는 숫자 블록을 골라 없애고, 남은 숫자의 합을 목표 숫자로 만들어 봐요.",
                        "숫자 조합 테스트", "두 개의 숫자 패널을 골라 목표 숫자와 같은 값을 만들어 봐요."),
                new LearningArea("logic", "논리추론능력", "차근차근 풀어 보는 규칙", "A>B", new Color(111, 88, 183),
                        "규칙 로봇", "이동과 회전 명령을 차례로 배치해 로봇을 목적지까지 안내해 봐요.",
                        "저울 추리", "저울의 기울기를 살펴보고 물체들의 무게 관계를 추리해 봐요."),
                new LearningArea("spatial", "공간지각능력", "모양을 돌리고 이어 봐요", "◇", new Color(47, 128, 171),
                        "기차 선로 건설", "선로 조각을 돌리고 연결해서 출발역과 도착역을 이어 봐요.",
                        "회전 도형 판별", "도형을 머릿속으로 돌려 보고 기준과 같은 모양을 찾아봐요."),
                new LearningArea("memory", "기억력", "보고, 기억하고, 떠올려요", "3·8·1", new Color(181, 86, 128),
                        "거꾸로 기억하기", "차례대로 나타나는 숫자나 그림을 기억한 뒤 거꾸로 입력해 봐요.",
                        "순간 기억 테스트", "잠깐 나타났다가 사라지는 숫자나 그림을 같은 순서로 떠올려 봐요."),
                new LearningArea("attention", "주의집중력", "꼼꼼하게 찾는 작은 발견", "◎", new Color(46, 137, 113),
                        "숨은 보물 찾기", "여러 물건 사이에서 주어진 색과 모양 조건에 맞는 보물을 찾아봐요.",
                        "목표 그림 찾기", "비슷한 그림들 사이에서 목표와 똑같은 그림을 찾아봐요."),
                new LearningArea("flexibility", "사고전환능력", "새로운 규칙에도 유연하게", "⇄", new Color(164, 121, 34),
                        "장난감 분류 공장", "지금의 규칙을 확인하고, 색이나 모양에 맞춰 장난감을 분류해 봐요.",
                        "바뀌는 규칙", "색에서 모양으로! 달라지는 규칙에 맞춰 그림을 분류해 봐요."));
    }
}
