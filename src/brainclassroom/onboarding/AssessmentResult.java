package brainclassroom.onboarding;

import java.util.List;

/** 영역별 정답률이다. 표준화 점수·인지능력 환산 점수가 아니다. */
public record AssessmentResult(List<AreaResult> areas) {
    private static final List<String> AREA_IDS = List.of("math", "logic", "spatial", "memory", "attention", "flexibility");

    public AssessmentResult {
        areas = List.copyOf(areas);
        if (areas.size() != 6 || !areas.stream().map(AreaResult::areaId).toList().equals(AREA_IDS)) {
            throw new IllegalArgumentException("6개 분야 결과가 순서대로 필요합니다.");
        }
    }

    public record AreaResult(String areaId, String title, int correctCount, int questionCount) {
        public AreaResult {
            if (areaId == null || areaId.isBlank() || title == null || title.isBlank()
                    || questionCount <= 0 || correctCount < 0 || correctCount > questionCount) {
                throw new IllegalArgumentException("분야별 결과 범위를 확인해 주세요.");
            }
        }

        public double accuracyPercent() {
            return 100.0 * correctCount / questionCount;
        }

        public boolean confidentThisTime() {
            return correctCount * 3 >= questionCount * 2;
        }
    }

    public String strengthTitles() {
        String names = areas.stream().filter(AreaResult::confidentThisTime).map(AreaResult::title)
                .collect(java.util.stream.Collectors.joining(", "));
        return names.isEmpty() ? "연습하며 함께 찾아봐요" : names;
    }

    public String practiceTitles() {
        String names = areas.stream().filter(area -> !area.confidentThisTime()).map(AreaResult::title)
                .collect(java.util.stream.Collectors.joining(", "));
        return names.isEmpty() ? "좋아하는 분야부터 도전해요" : names;
    }

    public String strengthText() {
        String names = areas.stream().filter(AreaResult::confidentThisTime).map(AreaResult::title)
                .collect(java.util.stream.Collectors.joining(", "));
        return names.isEmpty() ? "끝까지 도전한 끈기가 멋져요. 연습하며 잘하는 분야를 찾아봐요."
                : names + " 과제를 잘 풀었어요. 이번 풀이에서 보여 준 강점이에요.";
    }

    public String practiceText() {
        String names = areas.stream().filter(area -> !area.confidentThisTime()).map(AreaResult::title)
                .collect(java.util.stream.Collectors.joining(", "));
        return names.isEmpty() ? "모든 분야에서 차근차근 잘 풀었어요. 좋아하는 학습부터 시작해 봐요."
                : names + " 과제는 한 번 더 연습해 봐요. 천천히 다시 풀면 익숙해질 수 있어요.";
    }
}
