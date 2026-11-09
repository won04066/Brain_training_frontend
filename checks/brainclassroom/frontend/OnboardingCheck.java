package brainclassroom.frontend;

import brainclassroom.onboarding.StarterAssessmentService;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class OnboardingCheck {
    private static int checks;

    private OnboardingCheck() {
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicLong time = new AtomicLong();
            OnboardingPanel app = new OnboardingPanel(() -> new StarterAssessmentService(time::get));
            check(app.getName().equals("page-profile"), "첫 화면은 프로필");
            render(app, "onboarding-profile");
            button(app, "profile-next").doClick();
            check(app.getName().equals("page-profile"), "빈 이름으로 진행 차단");
            ((JTextField) named(app, "profile-name")).setText("연습친구");
            ((JSpinner) named(app, "profile-age")).setValue(9);
            button(app, "profile-next").doClick();
            check(app.getName().equals("page-avatar"), "프로필 다음은 아바타");
            button(app, "avatar-토끼").doClick();
            check(button(app, "avatar-토끼").isSelected(), "토끼 아바타 선택");
            render(app, "onboarding-avatar");
            button(app, "avatar-back").doClick();
            check(((JTextField) named(app, "profile-name")).getText().equals("연습친구"), "뒤로 가도 이름 유지");
            button(app, "profile-next").doClick();
            button(app, "avatar-next").doClick();
            check(app.getName().equals("page-intro"), "시작 전 6분야 안내");
            button(app, "assessment-begin").doClick();
            check(!button(app, "answer-submit").isEnabled(), "선택 전 제출 차단");
            render(app, "onboarding-test");
            int[] answers = {0, 2, 0, 0, 2, 1, 0, 1, 0, 1, 0, 0, 2, 1, 0, 0, 2, 1};
            for (int index = 0; index < answers.length; index++) {
                if (index >= 9 && index <= 11) {
                    check(active(app).stream().anyMatch(c -> "memory-stimulus".equals(c.getName())), "기억 자극 표시");
                    if (index == 9) {
                        render(app, "onboarding-memory");
                    }
                    time.addAndGet(3_000_000_000L);
                    app.refreshPhase();
                    check(active(app).stream().noneMatch(c -> "memory-stimulus".equals(c.getName())), "기억 자극 제거");
                }
                button(app, "answer-" + answers[index]).doClick();
                AbstractButton oldSubmit = button(app, "answer-submit");
                oldSubmit.doClick();
                oldSubmit.doClick();
                if (index < 17) {
                    check(app.getName().equals("page-assessment"), "완료 전 메인 진입 불가");
                }
            }
            check(app.getName().equals("page-result"), "18문항 후 결과 화면");
            RadarChart resultRadar = (RadarChart) named(app, "assessment-radar");
            String resultValues = resultRadar.getAccessibleContext().getAccessibleDescription();
            check(resultValues.contains("수학능력 67%") && resultValues.contains("논리추론능력 100%")
                    && resultValues.contains("공간지각능력 0%") && resultValues.contains("주의집중력 33%"), "실제 선택에 따른 그래프 값");
            check(active(app).stream().filter(JLabel.class::isInstance).map(JLabel.class::cast)
                    .anyMatch(label -> label.getText().contains("강점")), "강점 설명 표시");
            render(app, "onboarding-result");
            button(app, "result-home").doClick();
            check(app.getName().equals("page-home"), "결과 확인 후 메인 진입");
            RadarChart homeRadar = (RadarChart) named(app, "assessment-radar");
            check(resultValues.equals(homeRadar.getAccessibleContext().getAccessibleDescription()), "결과·메인 동일 그래프");
            check(active(app).stream().filter(JLabel.class::isInstance).map(JLabel.class::cast)
                    .anyMatch(label -> label.getText().equals("연습친구님의 생각 놀이터")), "메인에 프로필 반영");
            for (LearningArea area : LearningArea.catalog()) {
                button(app, "area-" + area.id()).doClick();
                check(active(app).stream().filter(JLabel.class::isInstance).map(JLabel.class::cast)
                        .anyMatch(label -> label.getText().equals(area.trainingGame())), "메인에서 분야별 학습 선택");
            }
            button(app, "area-math").doClick();
            render(app, "onboarding-home");
        });
        System.out.println(checks + " onboarding UI checks passed");
    }

    private static AbstractButton button(Container parent, String name) {
        return (AbstractButton) named(parent, name);
    }

    private static Component named(Container parent, String name) {
        return active(parent).stream().filter(component -> name.equals(component.getName())).findFirst().orElseThrow();
    }

    private static List<Component> active(Container parent) {
        List<Component> result = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (child.isVisible()) {
                result.add(child);
                if (child instanceof Container container) {
                    result.addAll(active(container));
                }
            }
        }
        return result;
    }

    private static void render(OnboardingPanel app, String filename) {
        app.setSize(app.getPreferredSize());
        layout(app);
        BufferedImage image = new BufferedImage(app.getWidth(), app.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            app.printAll(graphics);
            Path path = Path.of("build", filename + ".png");
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "png", path.toFile());
        } catch (java.io.IOException error) {
            throw new IllegalStateException(error);
        } finally {
            graphics.dispose();
        }
    }

    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) {
            if (child instanceof Container container) {
                layout(container);
            }
        }
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
        checks++;
    }
}
