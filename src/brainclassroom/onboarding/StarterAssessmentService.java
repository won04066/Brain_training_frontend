package brainclassroom.onboarding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * 독립된 시작 테스트의 로컬 MVP 서비스. 정식 백엔드의 측정·시간·추천 서비스를 대체하지 않는다.
 * 6개 영역 × 3문항, 무제한 응답. 기억력 제시만 3초이며 성별·나이로 점수를 보정하지 않는다.
 */
public final class StarterAssessmentService {
    public enum Phase { MEMORIZE, ANSWER }

    /** 정답·해설이 없는 화면 전용 데이터. */
    public record QuestionView(String id, String areaId, String areaTitle, int areaNumber,
            int questionNumber, String prompt, List<String> choices, Phase phase, String stimulus) {
        public QuestionView {
            choices = List.copyOf(choices);
        }
    }

    public record Response(String questionId, String areaId, boolean correct, long elapsedMillis) {
    }

    private record Task(String id, String areaId, String title, String prompt,
            List<String> choices, int correctIndex, String stimulus) {
        private Task {
            choices = List.copyOf(choices);
        }
    }

    private final List<Task> tasks = tasks();
    private final List<Response> responses = new ArrayList<>();
    private final LongSupplier nanoTime;
    private int index;
    private long shownAtNanos;

    public StarterAssessmentService() {
        this(System::nanoTime);
    }

    public StarterAssessmentService(LongSupplier nanoTime) {
        this.nanoTime = java.util.Objects.requireNonNull(nanoTime);
        shownAtNanos = nanoTime.getAsLong();
    }

    public synchronized boolean isComplete() {
        return index == tasks.size();
    }

    public synchronized QuestionView current() {
        if (isComplete()) {
            throw new IllegalStateException("시작 테스트가 완료되었습니다.");
        }
        Task task = tasks.get(index);
        boolean showing = !task.stimulus().isEmpty() && elapsedMillis() < 3000;
        return new QuestionView(task.id(), task.areaId(), task.title(), index / 3 + 1, index % 3 + 1,
                task.prompt(), showing ? List.of() : task.choices(), showing ? Phase.MEMORIZE : Phase.ANSWER,
                showing ? task.stimulus() : "");
    }

    public synchronized void submit(String questionId, int choiceIndex) {
        QuestionView view = current();
        if (!view.id().equals(questionId)) {
            throw new IllegalArgumentException("이미 처리했거나 현재 문제가 아닌 요청입니다.");
        }
        if (view.phase() != Phase.ANSWER) {
            throw new IllegalStateException("먼저 제시된 순서를 기억해 주세요.");
        }
        Task task = tasks.get(index);
        if (choiceIndex < 0 || choiceIndex >= task.choices().size()) {
            throw new IllegalArgumentException("보기를 선택해 주세요.");
        }
        long responseMillis = Math.max(0, elapsedMillis() - (task.stimulus().isEmpty() ? 0 : 3000));
        responses.add(new Response(task.id(), task.areaId(), task.correctIndex() == choiceIndex, responseMillis));
        index++;
        shownAtNanos = nanoTime.getAsLong();
    }

    public synchronized List<Response> responses() {
        return List.copyOf(responses);
    }

    public synchronized AssessmentResult result() {
        if (!isComplete()) {
            throw new IllegalStateException("6개 분야를 모두 마친 뒤 결과를 볼 수 있습니다.");
        }
        List<AssessmentResult.AreaResult> areas = new ArrayList<>();
        for (int area = 0; area < 6; area++) {
            Task first = tasks.get(area * 3);
            int correct = 0;
            for (int question = area * 3; question < area * 3 + 3; question++) {
                if (responses.get(question).correct()) {
                    correct++;
                }
            }
            areas.add(new AssessmentResult.AreaResult(first.areaId(), first.title(), correct, 3));
        }
        return new AssessmentResult(areas);
    }

    private long elapsedMillis() {
        return Math.max(0, (nanoTime.getAsLong() - shownAtNanos) / 1_000_000);
    }

    private static Task task(String id, String area, String title, String prompt,
            int correct, String stimulus, String... choices) {
        return new Task(id, area, title, prompt, List.of(choices), correct, stimulus);
    }

    private static List<Task> tasks() {
        return List.of(
                task("math-1", "math", "수학능력", "두 숫자를 더해서 12를 만들어요. 어떤 짝일까요?", 0, "", "5와 7", "8과 2", "5와 2"),
                task("math-2", "math", "수학능력", "두 숫자를 더해서 15를 만들어요. 어떤 짝일까요?", 2, "", "6과 7", "9와 4", "8과 7"),
                task("math-3", "math", "수학능력", "두 숫자를 더해서 18을 만들어요. 어떤 짝일까요?", 1, "", "6과 9", "7과 11", "8과 8"),
                task("logic-1", "logic", "논리추론능력", "곰은 토끼보다 무거워요. 토끼는 새보다 무거워요. 가장 무거운 동물은?", 0, "", "곰", "토끼", "새"),
                task("logic-2", "logic", "논리추론능력", "빨강 상자는 파랑보다 가벼워요. 초록 상자는 빨강보다 가벼워요. 가장 가벼운 상자는?", 2, "", "빨강", "파랑", "초록"),
                task("logic-3", "logic", "논리추론능력", "민지는 준호보다 먼저, 준호는 수아보다 먼저 도착했어요. 두 번째로 도착한 사람은?", 1, "", "민지", "준호", "수아"),
                task("spatial-1", "spatial", "공간지각능력", "위쪽 삼각형 ▲을 오른쪽으로 90도 돌리면 어떤 모양일까요?", 1, "", "◀", "▶", "▼"),
                task("spatial-2", "spatial", "공간지각능력", "오른쪽 삼각형 ▶을 반 바퀴(180도) 돌리면?", 0, "", "◀", "▲", "▼"),
                task("spatial-3", "spatial", "공간지각능력", "아래쪽 삼각형 ▼을 오른쪽으로 90도 돌리면?", 2, "", "▲", "▶", "◀"),
                task("memory-1", "memory", "기억력", "방금 본 숫자를 같은 순서로 골라 주세요.", 1, "3 → 8 → 1", "3 → 1 → 8", "3 → 8 → 1", "8 → 3 → 1"),
                task("memory-2", "memory", "기억력", "방금 본 숫자를 같은 순서로 골라 주세요.", 2, "2 → 5 → 9 → 4", "2 → 9 → 5 → 4", "4 → 9 → 5 → 2", "2 → 5 → 9 → 4"),
                task("memory-3", "memory", "기억력", "방금 본 숫자를 같은 순서로 골라 주세요.", 0, "7 → 1 → 6 → 3 → 8", "7 → 1 → 6 → 3 → 8", "7 → 6 → 1 → 3 → 8", "8 → 3 → 6 → 1 → 7"),
                task("attention-1", "attention", "주의집중력", "목표는 ‘빨간색 별’이에요. 색과 모양이 모두 같은 것을 찾아요.", 2, "", "파란색 별 ★", "빨간색 원 ●", "빨간색 별 ★"),
                task("attention-2", "attention", "주의집중력", "목표는 ‘파란색 네모’예요. 색과 모양이 모두 같은 것을 찾아요.", 0, "", "파란색 네모 ■", "파란색 원 ●", "초록색 네모 ■"),
                task("attention-3", "attention", "주의집중력", "목표는 ‘초록색 세모’예요. 색과 모양이 모두 같은 것을 찾아요.", 1, "", "노란색 세모 ▲", "초록색 세모 ▲", "초록색 별 ★"),
                task("flexibility-1", "flexibility", "사고전환능력", "현재 규칙: 색깔! ‘빨간색 별’을 색깔 상자로 분류해요.", 0, "", "빨강 상자", "파랑 상자", "초록 상자"),
                task("flexibility-2", "flexibility", "사고전환능력", "규칙이 바뀌었어요: 모양! ‘빨간색 원’을 모양 상자로 분류해요.", 2, "", "별 상자", "네모 상자", "원 상자"),
                task("flexibility-3", "flexibility", "사고전환능력", "다시 바뀐 규칙: 색깔! ‘파란색 원’을 색깔 상자로 분류해요.", 1, "", "빨강 상자", "파랑 상자", "초록 상자"));
    }
}
