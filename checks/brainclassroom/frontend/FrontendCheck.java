package brainclassroom.frontend;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public final class FrontendCheck {
    private FrontendCheck() {
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            HomePanel home = new HomePanel();
            check(LearningArea.catalog().stream().map(LearningArea::id).distinct().count() == 6,
                    "6개 분야 고정 식별자 중복 없음");
            check(LearningArea.catalog().stream().allMatch(area ->
                    descendants(home).stream().anyMatch(c -> ("area-" + area.id()).equals(c.getName()))),
                    "6개 분야 카드 표시");
            for (LearningArea area : LearningArea.catalog()) {
                button(home, "area-" + area.id()).doClick();
                check(hasLabel(home, area.trainingGame()), area.title() + " 훈련 상세 연결");
                check("선택됨".equals(button(home, "area-" + area.id())
                        .getAccessibleContext().getAccessibleDescription()), area.title() + " 선택 상태");
            }
            check(!button(home, "start-game").isEnabled(), "미연결 게임 실행 차단");
            button(home, "mode-measurement").doClick();
            for (LearningArea area : LearningArea.catalog()) {
                button(home, "area-" + area.id()).doClick();
                check(hasLabel(home, area.measurementGame()), area.title() + " 측정 상세 연결");
            }
            button(home, "mode-training").doClick();
            check(hasLabel(home, "장난감 분류 공장"), "모드 전환 시 선택 분야 유지");
            button(home, "area-math").doClick();
            check(hasLabel(home, "아직 완료한 스테이지가 없어요."), "기록 부재를 0점과 구분");
            render(home, Path.of("build", "home-screen.png"));
        });
        System.out.println("23 UI checks passed; screenshot: build/home-screen.png");
    }

    private static AbstractButton button(Container parent, String name) {
        return descendants(parent).stream().filter(c -> name.equals(c.getName()))
                .filter(AbstractButton.class::isInstance).map(AbstractButton.class::cast)
                .findFirst().orElseThrow();
    }

    private static boolean hasLabel(Container parent, String text) {
        return descendants(parent).stream().filter(JLabel.class::isInstance).map(JLabel.class::cast)
                .anyMatch(label -> label.getText().equals(text));
    }

    private static void render(HomePanel home, Path path) {
        home.setSize(home.getPreferredSize());
        layout(home);
        BufferedImage image = new BufferedImage(home.getWidth(), home.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            home.printAll(graphics);
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "png", path.toFile());
        } catch (java.io.IOException error) {
            throw new IllegalStateException("화면 이미지 저장 실패", error);
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

    private static List<Component> descendants(Container parent) {
        List<Component> result = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            result.add(child);
            if (child instanceof Container container) {
                result.addAll(descendants(container));
            }
        }
        return result;
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
        System.out.println("PASS: " + label);
    }
}
