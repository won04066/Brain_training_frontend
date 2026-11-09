package brainclassroom.frontend;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public final class FrontendCheck {
    private FrontendCheck() {
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            HomePanel home = new HomePanel();
            List<Component> components = descendants(home);
            JTabbedPane tabs = components.stream().filter(JTabbedPane.class::isInstance)
                    .map(JTabbedPane.class::cast).findFirst().orElseThrow();
            check(tabs.getTabCount() == 3, "측정·훈련·지식학습 분리");
            List<JComboBox<?>> selectors = new ArrayList<>();
            for (Component component : components) {
                if (component instanceof JComboBox<?> selector) {
                    selectors.add(selector);
                }
            }
            check(selectors.size() == 2 && selectors.stream().allMatch(s -> s.getItemCount() == 6),
                    "측정·훈련 각각 인지 6영역");
            selectors.get(0).setSelectedIndex(3);
            check(descendants(home).stream().filter(JLabel.class::isInstance).map(JLabel.class::cast)
                    .anyMatch(label -> label.getText().contains("측정 · 기억력")), "영역 선택 피드백");
            tabs.setSelectedIndex(1);
            selectors.get(1).setSelectedIndex(0);
            check(descendants(home).stream().filter(JLabel.class::isInstance).map(JLabel.class::cast)
                    .anyMatch(label -> label.getText().contains("훈련 · 수학능력")), "훈련 화면 전환");
            check(components.stream().filter(JButton.class::isInstance).map(JButton.class::cast)
                    .anyMatch(button -> button.getText().equals("백엔드 연결 준비 중") && !button.isEnabled()),
                    "미연결 게임 시작 차단");
            home.setSize(home.getPreferredSize());
            home.doLayout();
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(860, 560,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D graphics = image.createGraphics();
            try {
                home.printAll(graphics);
            } finally {
                graphics.dispose();
            }
            check(home.getWidth() == 860, "headless Swing 화면 렌더링");
        });
        System.out.println("6 UI checks passed");
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
