package brainclassroom.frontend;

import java.awt.BorderLayout;
import brainclassroom.onboarding.StudentProfile;
import brainclassroom.onboarding.AssessmentResult;
import java.util.Optional;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.SwingUtilities;

/** 화면 선택만 관리한다. 회차·채점·시간·저장은 백엔드 서비스가 담당한다. */
@SuppressWarnings("serial") // 화면 인스턴스는 직렬화·영구 저장하지 않는다.
public final class HomePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color BACKGROUND = new Color(247, 248, 244);
    private static final Color INK = new Color(38, 54, 48);
    private static final Color GREEN = new Color(41, 103, 78);
    private final Map<String, JButton> areaButtons = new LinkedHashMap<>();
    private final JPanel detailPanel = new JPanel();
    private final JLabel sectionTitle = label("오늘은 어떤 힘을 키워 볼까요?", 23, true);
    private final JLabel sectionDescription = label("좋아하는 분야를 골라 천천히 연습해 보세요.", 14, false);
    private final JButton startButton = new JButton("게임 준비 중");
    private LearningArea selectedArea = LearningArea.catalog().getFirst();
    private boolean measurement;
    private final Optional<StudentProfile> profile;
    private final Optional<AssessmentResult> assessmentResult;

    public HomePanel() {
        this(Optional.empty(), Optional.empty());
    }

    public HomePanel(StudentProfile profile, AssessmentResult result) {
        this(Optional.of(profile), Optional.of(result));
    }

    private HomePanel(Optional<StudentProfile> profile, Optional<AssessmentResult> result) {
        this.profile = profile;
        this.assessmentResult = result;
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("화면은 EDT에서 생성해야 합니다.");
        }
        setLayout(new BorderLayout(24, 24));
        setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));
        setPreferredSize(new Dimension(1220, 940));
        setBackground(BACKGROUND);
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        JLabel footer = label("조금씩, 꾸준히 자라는 생각의 힘   ·   나만의 속도로 도전해요", 13, false);
        add(footer, BorderLayout.SOUTH);
        updateSelection();
    }

    private JPanel createHeader() {
        JPanel header = panel(new BorderLayout());
        header.setPreferredSize(new Dimension(1000, 64));
        JPanel brand = panel(new GridLayout(2, 1, 0, 5));
        brand.add(label("두뇌 성장 교실", 25, true));
        brand.add(label("매일 만나는 작은 생각 놀이터", 13, false));
        header.add(brand, BorderLayout.WEST);
        JPanel navigation = panel(new GridLayout(1, 2, 8, 0));
        ButtonGroup group = new ButtonGroup();
        JToggleButton training = new JToggleButton("학습하기", true);
        training.setName("mode-training");
        JToggleButton assessment = new JToggleButton("측정하기");
        assessment.setName("mode-measurement");
        for (JToggleButton button : new JToggleButton[] {training, assessment}) {
            button.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
            button.setPreferredSize(new Dimension(125, 44));
            group.add(button);
            navigation.add(button);
        }
        training.addActionListener(event -> changeMode(false));
        assessment.addActionListener(event -> changeMode(true));
        header.add(navigation, BorderLayout.EAST);
        return header;
    }

    private JPanel createBody() {
        JPanel body = panel(new BorderLayout(24, 24));
        JPanel hero = new JPanel(new BorderLayout(20, 12));
        hero.setBackground(new Color(224, 236, 216));
        hero.setPreferredSize(new Dimension(1000, 280));
        hero.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        JPanel heroCopy = new JPanel();
        heroCopy.setLayout(new BoxLayout(heroCopy, BoxLayout.Y_AXIS));
        heroCopy.setOpaque(false);
        heroCopy.add(label("생각이 자라는 하루", 13, true));
        heroCopy.add(Box.createVerticalStrut(12));
        heroCopy.add(label(profile.map(value -> value.name() + "님의 생각 놀이터").orElse("놀면서 배우고, 한 뼘 더 자라요"), 25, true));
        heroCopy.add(Box.createVerticalStrut(10));
        heroCopy.add(label("6가지 생각의 힘을 재미있는 게임으로 만나 보세요.", 15, false));
        heroCopy.add(Box.createVerticalStrut(12));
        assessmentResult.ifPresent(result -> {
            heroCopy.add(Ui.paragraph("이번 풀이의 강점: " + result.strengthTitles(), 300));
            heroCopy.add(Box.createVerticalStrut(8));
            heroCopy.add(Ui.paragraph("함께 연습할 분야: " + result.practiceTitles(), 300));
        });
        hero.add(heroCopy, BorderLayout.CENTER);
        profile.ifPresent(value -> {
            AvatarBadge avatar = new AvatarBadge(value.avatar());
            avatar.setPreferredSize(new Dimension(90, 90));
            hero.add(avatar, BorderLayout.WEST);
        });
        RadarChart radar = new RadarChart(assessmentResult);
        radar.setPreferredSize(new Dimension(370, 210));
        hero.add(radar, BorderLayout.EAST);
        body.add(hero, BorderLayout.NORTH);

        JPanel learning = panel(new BorderLayout(20, 16));
        JPanel heading = panel(new GridLayout(2, 1, 0, 6));
        heading.setPreferredSize(new Dimension(700, 62));
        heading.add(sectionTitle);
        heading.add(sectionDescription);
        learning.add(heading, BorderLayout.NORTH);
        JPanel grid = panel(new GridLayout(3, 2, 14, 14));
        for (LearningArea area : LearningArea.catalog()) {
            JButton card = new JButton();
            card.setName("area-" + area.id());
            card.setLayout(new BorderLayout(8, 10));
            card.setBackground(Color.WHITE);
            card.setFocusPainted(true);
            card.setHorizontalAlignment(JButton.LEFT);
            card.getAccessibleContext().setAccessibleName(area.title() + " 선택");
            JLabel icon = label(area.symbol(), 24, true);
            icon.setForeground(area.color());
            card.add(icon, BorderLayout.WEST);
            JPanel words = new JPanel(new GridLayout(2, 1, 0, 5));
            words.setOpaque(false);
            words.add(label(area.title(), 19, true));
            words.add(label(area.subtitle(), 12, false));
            card.add(words, BorderLayout.CENTER);
            card.addActionListener(event -> {
                selectedArea = area;
                updateSelection();
            });
            areaButtons.put(area.id(), card);
            grid.add(card);
        }
        learning.add(grid, BorderLayout.CENTER);
        body.add(learning, BorderLayout.CENTER);

        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        JScrollPane detailScroll = new JScrollPane(detailPanel);
        detailScroll.setBorder(BorderFactory.createEmptyBorder());
        detailScroll.setPreferredSize(new Dimension(300, 400));
        detailScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        learning.add(detailScroll, BorderLayout.EAST);
        return body;
    }

    private void changeMode(boolean isMeasurement) {
        measurement = isMeasurement;
        sectionTitle.setText(measurement ? "나의 생각하는 힘을 알아볼까요?" : "오늘은 어떤 힘을 키워 볼까요?");
        sectionDescription.setText(measurement ? "각 분야의 측정 게임과 진행 방법을 살펴보세요." : "좋아하는 분야를 골라 천천히 연습해 보세요.");
        updateSelection();
    }

    private void updateSelection() {
        for (LearningArea area : LearningArea.catalog()) {
            JButton button = areaButtons.get(area.id());
            boolean selected = selectedArea.id().equals(area.id());
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? GREEN : new Color(225, 229, 223), selected ? 2 : 1),
                    BorderFactory.createEmptyBorder(16, 16, 16, 16)));
            button.setBackground(selected ? new Color(235, 244, 232) : Color.WHITE);
            button.getAccessibleContext().setAccessibleDescription(selected ? "선택됨" : "선택되지 않음");
        }
        detailPanel.removeAll();
        addDetail(label(measurement ? "분야별 측정" : "오늘의 학습", 13, true), 16);
        JLabel symbol = label(selectedArea.symbol(), 36, true);
        symbol.setForeground(selectedArea.color());
        addDetail(symbol, 14);
        addDetail(label(selectedArea.title(), 23, true), 8);
        addDetail(label(measurement ? selectedArea.measurementGame() : selectedArea.trainingGame(), 18, true), 16);
        JLabel description = label("<html><div style='width:175px'>"
                + (measurement ? selectedArea.measurementDescription() : selectedArea.trainingDescription())
                + "</div></html>", 15, false);
        addDetail(description, 22);
        addDetail(label(measurement ? "나의 수행 기록을 알아봐요" : "도전하고, 다시 생각해 봐요", 14, true), 8);
        String recordText = measurement ? assessmentResult.map(result -> result.areas().stream()
                .filter(area -> area.areaId().equals(selectedArea.id())).findFirst().orElseThrow())
                .map(area -> "시작 테스트: " + area.correctCount() + "/" + area.questionCount() + "문제 정답")
                .orElse("아직 측정 기록이 없어요.") : "아직 완료한 스테이지가 없어요.";
        addDetail(label(recordText, 13, false), 22);
        startButton.setEnabled(false);
        startButton.setName("start-game");
        startButton.setAlignmentX(LEFT_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        startButton.setToolTipText("게임 플레이는 이후 업데이트에서 연결됩니다.");
        detailPanel.add(startButton);
        detailPanel.add(Box.createVerticalStrut(8));
        addDetail(label("게임은 곧 만날 수 있어요!", 12, false), 12);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void addDetail(JLabel component, int bottomGap) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        detailPanel.add(component);
        detailPanel.add(Box.createVerticalStrut(bottomGap));
    }

    private static JPanel panel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.DIALOG, bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(INK);
        return label;
    }
}
