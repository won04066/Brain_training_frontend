package brainclassroom.frontend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** 외부 이미지 없이 직접 그린 동물 아바타. */
public final class AvatarBadge extends JPanel {
    private static final long serialVersionUID = 1L;
    private final String animal;

    public AvatarBadge(String animal) {
        if (!brainclassroom.onboarding.StudentProfile.AVATARS.contains(animal)) {
            throw new IllegalArgumentException("알 수 없는 아바타입니다.");
        }
        this.animal = animal;
        setOpaque(false);
        setPreferredSize(new Dimension(120, 120));
        getAccessibleContext().setAccessibleName(animal + " 아바타");
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scale = Math.min(getWidth(), getHeight()) / 120.0;
            g.translate((getWidth() - 120 * scale) / 2, (getHeight() - 120 * scale) / 2);
            g.scale(scale, scale);
            Color fur = switch (animal) {
                case "토끼" -> new Color(227, 206, 225);
                case "고양이" -> new Color(237, 198, 123);
                case "여우" -> new Color(226, 146, 91);
                default -> new Color(174, 142, 109);
            };
            g.setColor(new Color(230, 239, 221));
            g.fillOval(0, 0, 120, 120);
            g.setColor(fur);
            if (animal.equals("토끼")) {
                g.fillRoundRect(28, 8, 22, 62, 22, 22);
                g.fillRoundRect(70, 8, 22, 62, 22, 22);
            } else if (animal.equals("곰")) {
                g.fillOval(18, 22, 30, 32);
                g.fillOval(72, 22, 30, 32);
            } else {
                g.fillPolygon(new int[] {22, 24, 55}, new int[] {62, 15, 48}, 3);
                g.fillPolygon(new int[] {65, 96, 98}, new int[] {48, 15, 62}, 3);
            }
            g.fillOval(19, 34, 82, 73);
            g.setColor(new Color(255, 243, 222));
            g.fillOval(37, 66, 46, 32);
            g.setColor(Ui.INK);
            g.fillOval(39, 60, 7, 9);
            g.fillOval(75, 60, 7, 9);
            g.fillOval(56, 77, 9, 6);
            g.setStroke(new BasicStroke(2));
            g.drawArc(49, 79, 23, 12, 190, 160);
        } finally {
            g.dispose();
        }
    }
}
