package brainclassroom.onboarding;

import java.util.concurrent.atomic.AtomicLong;

public final class StarterAssessmentCheck {
    private static int checks;
    private static final int[] CORRECT = {0, 2, 1, 0, 2, 1, 1, 0, 2, 1, 2, 0, 2, 0, 1, 0, 2, 1};

    private StarterAssessmentCheck() {
    }

    public static void main(String[] args) {
        ProfileService profiles = new ProfileService();
        reject(() -> profiles.create("   ", 8, "선택 안 함", "곰"), "공백 이름 거부");
        reject(() -> profiles.create("가".repeat(21), 8, "여자", "곰"), "이름 길이 검증");
        reject(() -> profiles.create("가", 14, "여자", "곰"), "나이 범위 검증");
        reject(() -> profiles.create("가", 8, "모름", "곰"), "알 수 없는 성별 거부");
        reject(() -> profiles.create("가", 8, "여자", "용"), "알 수 없는 아바타 거부");
        StudentProfile profile = profiles.create("  연습친구  ", 8, "선택 안 함", "토끼");
        check(profile.name().equals("연습친구") && !profile.id().isBlank(), "이름 정리와 서비스 ID 생성");
        AtomicLong time = new AtomicLong();
        StarterAssessmentService service = new StarterAssessmentService(time::get);
        reject(service::result, "미완료 결과 조회 차단");
        reject(() -> service.submit("math-1", -1), "미선택 입력 거부");
        reject(() -> service.submit("math-1", 3), "보기 범위 검증");
        for (int index = 0; index < CORRECT.length; index++) {
            var question = service.current();
            check(question.areaNumber() == index / 3 + 1 && question.questionNumber() == index % 3 + 1,
                    "분야·문항 순서 " + index);
            if (index >= 9 && index <= 11) {
                check(question.phase() == StarterAssessmentService.Phase.MEMORIZE
                        && question.choices().isEmpty() && !question.stimulus().isEmpty(), "기억 제시 단계: 선택지 숨김");
                reject(() -> service.submit(question.id(), 0), "제시 중 응답 차단");
                time.addAndGet(2_999_000_000L);
                check(service.current().phase() == StarterAssessmentService.Phase.MEMORIZE, "3초 이전 제시 유지");
                time.addAndGet(1_000_000L);
                check(service.current().phase() == StarterAssessmentService.Phase.ANSWER
                        && service.current().stimulus().isEmpty(), "3초 경계에서 자극 숨김·입력 전환");
            }
            String id = question.id();
            time.addAndGet(500_000_000L);
            service.submit(id, CORRECT[index]);
            if (!service.isComplete()) {
                reject(() -> service.submit(id, 0), "이전 문제 중복 요청 차단");
            }
        }
        AssessmentResult perfect = service.result();
        check(perfect.areas().stream().allMatch(area -> area.correctCount() == 3 && area.accuracyPercent() == 100),
                "18문항 실제 채점·분야별 100% 집계");
        check(service.responses().stream().allMatch(response -> response.elapsedMillis() == 500),
                "응답시간에서 기억 제시 3초 제외·조회로 시간 초기화 안 함");
        check(service.result().equals(perfect), "결과 반복 조회 불변");
        reject(() -> service.submit("flexibility-3", 0), "완료 후 제출 차단");
        try {
            perfect.areas().clear();
            throw new AssertionError("결과 목록 변경 가능");
        } catch (UnsupportedOperationException expected) {
            checks++;
        }
        AtomicLong wrongTime = new AtomicLong();
        StarterAssessmentService wrong = new StarterAssessmentService(wrongTime::get);
        for (int correct : CORRECT) {
            String id = wrong.current().id();
            wrongTime.addAndGet(3_000_000_000L);
            wrong.submit(id, (correct + 1) % 3);
        }
        check(wrong.result().areas().stream().allMatch(area -> area.accuracyPercent() == 0), "전부 오답이면 실제 0% 표시");
        check(!wrong.result().strengthText().contains("수학능력 과제를 잘"), "오답 결과를 강점으로 위장하지 않음");
        System.out.println(checks + " service checks passed");
    }

    private static void reject(Runnable task, String label) {
        try {
            task.run();
            throw new AssertionError(label);
        } catch (IllegalArgumentException | IllegalStateException expected) {
            check(true, label);
        }
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
        checks++;
    }
}
