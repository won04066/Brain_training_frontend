package brainclassroom.frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/** 화면 선택만 담당한다. 게임 상태·채점·파일 저장은 서비스의 책임이다. */
public final class HomePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final List<String> AREA_LABELS = List.of(
            "수학능력", "논리추론능력", "공간지각능력", "기억력", "주의집중력", "사고전환능력");
    private final JTabbedPane activities = new JTabbedPane();
    private final JLabel selectionLabel = new JLabel("인지 영역을 선택해 주세요.");
    private final JButton startButton = new JButton("백엔드 연결 준비 중");

    public HomePanel() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("화면은 EDT에서 생성해야 합니다.");
        }
        setLayout(new BorderLayout(16, 20));
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        setPreferredSize(new Dimension(860, 560));
        setBackground(new Color(245, 248, 252));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 10));
        JLabel title = new JLabel("두뇌 성장 교실");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        heading.add(title);
        heading.add(new JLabel("생각하는 힘을 기르는 나만의 교실"));
        add(heading, BorderLayout.NORTH);

        activities.addTab("인지 측정", createCognitivePanel("측정"));
        activities.addTab("인지 훈련", createCognitivePanel("훈련"));
        JPanel knowledge = new JPanel(new BorderLayout());
        knowledge.add(new JLabel("지식학습 · 분야와 학습/복습 계약을 확인한 후 연결합니다."), BorderLayout.NORTH);
        activities.addTab("지식학습", knowledge);
        activities.addChangeListener(event -> selectionLabel.setText("활동을 선택했습니다. 백엔드 연결을 기다려 주세요."));
        add(activities, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(3, 1, 0, 8));
        footer.add(selectionLabel);
        footer.add(new JLabel("아직 측정 기록이 없습니다. 게임 결과는 IQ 검사 점수가 아닙니다."));
        startButton.setEnabled(false);
        footer.add(startButton);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createCognitivePanel(String activityLabel) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
        panel.add(new JLabel(activityLabel + "할 인지 영역을 골라 주세요."), BorderLayout.NORTH);
        JComboBox<String> areas = new JComboBox<>(AREA_LABELS.toArray(String[]::new));
        areas.setSelectedIndex(-1);
        areas.addActionListener(event -> selectionLabel.setText(
                activityLabel + " · " + areas.getSelectedItem() + " · 게임 서비스 미연결"));
        JPanel selector = new JPanel(new BorderLayout());
        selector.add(areas, BorderLayout.NORTH);
        panel.add(selector, BorderLayout.CENTER);
        return panel;
    }
}
