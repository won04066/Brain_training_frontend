package brainclassroom.frontend;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class FrontendApp {
    private FrontendApp() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("GUI 실행에는 데스크톱 디스플레이가 필요합니다. 화면 검증: bash scripts/check.sh");
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("두뇌 성장 교실");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new HomePanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
