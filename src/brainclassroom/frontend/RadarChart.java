package brainclassroom.frontend;

import brainclassroom.onboarding.AssessmentResult;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Optional;
import javax.swing.JPanel;

/** 6개 축 모두 동일하게 0~100%의 시작 테스트 정답률을 표시한다. */
@SuppressWarnings("serial")
public final class RadarChart extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Optional<AssessmentResult> result;

    public RadarChart(Optional<AssessmentResult> result) {
        this.result = java.util.Objects.requireNonNull(result);
        setOpaque(false);
        setPreferredSize(new Dimension(400, 330));
        setName("assessment-radar");
        String description = result.map(value -> value.areas().stream()
                .map(area -> area.title() + " " + Math.round(area.accuracyPercent()) + "%")
                .collect(java.util.stream.Collectors.joining(", "))).orElse("아직 테스트 기록이 없습니다.");
        getAccessibleContext().setAccessibleName("6개 분야 시작 테스트 정답률 그래프");
        getAccessibleContext().setAccessibleDescription(description);
        setToolTipText(description);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double cx = getWidth() / 2.0;
            double plotHeight = getHeight() - 32.0;
            double cy = plotHeight / 2.0;
            double radius = Math.max(10, Math.min(getWidth() / 2.0 - 83, plotHeight / 2.0 - 38));
            for (int ring = 1; ring <= 4; ring++) {
                g.setColor(new Color(199, 215, 202));
                g.draw(polygon(cx, cy, radius * ring / 4.0, null));
            }
            for (int axis = 0; axis < 6; axis++) {
                double angle = angle(axis);
                g.drawLine((int) cx, (int) cy, (int) (cx + Math.cos(angle) * radius), (int) (cy + Math.sin(angle) * radius));
            }
            if (result.isPresent()) {
                double[] values = result.get().areas().stream().mapToDouble(area -> area.accuracyPercent() / 100).toArray();
                Path2D shape = polygon(cx, cy, radius, values);
                g.setColor(new Color(58, 145, 111, 65));
                g.fill(shape);
                g.setStroke(new BasicStroke(2.5f));
                g.setColor(Ui.GREEN);
                g.draw(shape);
                for (int axis = 0; axis < 6; axis++) {
                    int x = (int) (cx + Math.cos(angle(axis)) * radius * values[axis]);
                    int y = (int) (cy + Math.sin(angle(axis)) * radius * values[axis]);
                    g.fillOval(x - 4, y - 4, 8, 8);
                }
            }
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
            for (int axis = 0; axis < 6; axis++) {
                String title = LearningArea.catalog().get(axis).title();
                String value = result.isPresent() ? " " + Math.round(result.get().areas().get(axis).accuracyPercent()) + "%" : "";
                String text = title + value;
                int x = (int) (cx + Math.cos(angle(axis)) * (radius + 30));
                int y = (int) (cy + Math.sin(angle(axis)) * (radius + 30));
                int width = g.getFontMetrics().stringWidth(text);
                x = Math.max(2, Math.min(getWidth() - width - 2, x - width / 2));
                g.setColor(Ui.INK);
                g.drawString(text, x, y + 5);
            }
            g.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
            g.setColor(Ui.INK);
            String caption = result.isPresent() ? "중심 0% · 바깥 100% | 분야별 정답률" : "테스트 후 그래프가 나타나요";
            g.drawString(caption, Math.max(4, (getWidth() - g.getFontMetrics().stringWidth(caption)) / 2), getHeight() - 6);
        } finally {
            g.dispose();
        }
    }

    private static double angle(int axis) {
        return -Math.PI / 2 + axis * Math.PI / 3;
    }

    private static Path2D polygon(double cx, double cy, double radius, double[] values) {
        Path2D path = new Path2D.Double();
        for (int axis = 0; axis < 6; axis++) {
            double value = values == null ? 1 : values[axis];
            double x = cx + Math.cos(angle(axis)) * radius * value;
            double y = cy + Math.sin(angle(axis)) * radius * value;
            if (axis == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }
}
