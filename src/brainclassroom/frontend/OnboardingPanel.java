package brainclassroom.frontend;

import brainclassroom.onboarding.AssessmentResult;
import brainclassroom.onboarding.ProfileService;
import brainclassroom.onboarding.StarterAssessmentService;
import brainclassroom.onboarding.StudentProfile;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Optional;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** 첫 실행의 화면 이동과 입력 수집. 프로필 생성·출제·판정은 별도 서비스에 위임한다. */
@SuppressWarnings("serial")
public final class OnboardingPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final CardLayout pages = new CardLayout();
    private final ProfileService profiles = new ProfileService();
    private final Supplier<StarterAssessmentService> assessmentFactory;
    private final JTextField nameInput = new JTextField(20);
    private final JSpinner ageInput = new JSpinner(new SpinnerNumberModel(8, 6, 13, 1));
    private final JComboBox<String> genderInput = new JComboBox<>(StudentProfile.GENDERS.toArray(String[]::new));
    private final JLabel profileError = Ui.label(" ", 14, false);
    private final JPanel questionHost = Ui.panel(new BorderLayout());
    private final Timer phaseTimer;
    private String avatar = StudentProfile.AVATARS.getFirst();
    private StudentProfile profile;
    private StarterAssessmentService assessment;
    private String visiblePage = "profile";
    private StarterAssessmentService.Phase visiblePhase;
    private int selectedChoice = -1;

    public OnboardingPanel() {
        this(StarterAssessmentService::new);
    }

    /** 서비스 팩토리를 주입하여 시간 대기 없이 같은 화면 흐름을 검증한다. */
    public OnboardingPanel(Supplier<StarterAssessmentService> assessmentFactory) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("화면은 EDT에서 생성해야 합니다.");
        }
        this.assessmentFactory = java.util.Objects.requireNonNull(assessmentFactory);
        setLayout(pages);
        setPreferredSize(new Dimension(1220, 940));
        setBackground(Ui.BACKGROUND);
        phaseTimer = new Timer(100, event -> refreshPhase());
        add(profilePage(), "profile");
        add(avatarPage(), "avatar");
        add(introPage(), "intro");
        add(questionHost, "assessment");
        showPage("profile");
    }

    private JPanel profilePage() {
        JPanel page = page("처음 만나서 반가워요!", "나를 소개하고, 나만의 두뇌 성장 교실을 시작해요.");
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        Ui.padding(form, 32);
        form.setPreferredSize(new Dimension(540, 420));
        nameInput.setName("profile-name");
        nameInput.getAccessibleContext().setAccessibleName("이름 또는 별명");
        nameInput.setFont(new Font(Font.DIALOG, Font.PLAIN, 21));
        ageInput.setName("profile-age");
        genderInput.setName("profile-gender");
        addField(form, "이름 또는 별명 (1~20자)", nameInput);
        addField(form, "나이 (만 나이, 6~13세)", ageInput);
        addField(form, "성별 (선택하지 않아도 괜찮아요)", genderInput);
        profileError.setName("profile-error");
        profileError.setForeground(new Color(164, 55, 45));
        form.add(profileError);
        form.add(Box.createVerticalStrut(14));
        form.add(Ui.paragraph("프로필과 테스트 결과는 이번 실행 동안만 유지돼요. 성별은 문제와 결과에 영향을 주지 않아요.", 320));
        page.add(center(form), BorderLayout.CENTER);
        page.add(actions(Ui.button("profile-next", "다음 · 아바타 고르기 →", this::validateProfile)), BorderLayout.SOUTH);
        return page;
    }

    private void validateProfile() {
        try {
            ageInput.commitEdit();
            profiles.validate(nameInput.getText(), (Integer) ageInput.getValue(), (String) genderInput.getSelectedItem(), avatar);
            profileError.setText(" ");
            showPage("avatar");
        } catch (IllegalArgumentException | java.text.ParseException error) {
            profileError.setText("이름은 1~20자, 나이는 6~13세로 입력해 주세요.");
        }
    }

    private JPanel avatarPage() {
        JPanel page = page("나의 아바타를 골라 봐요", "함께 생각하고 성장할 친구를 선택해 주세요.");
        JPanel avatars = Ui.panel(new GridLayout(1, 4, 18, 0));
        ButtonGroup group = new ButtonGroup();
        for (String animal : StudentProfile.AVATARS) {
            JToggleButton choice = new JToggleButton(animal, animal.equals(avatar));
            choice.setName("avatar-" + animal);
            choice.setLayout(new BorderLayout(10, 16));
            choice.setPreferredSize(new Dimension(200, 220));
            choice.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
            choice.add(new AvatarBadge(animal), BorderLayout.CENTER);
            choice.add(Ui.label(animal, 23, true), BorderLayout.SOUTH);
            choice.getAccessibleContext().setAccessibleName(animal + " 아바타 선택");
            choice.addActionListener(event -> avatar = animal);
            group.add(choice);
            avatars.add(choice);
        }
        page.add(center(avatars), BorderLayout.CENTER);
        page.add(actions(Ui.button("avatar-back", "← 내 소개 수정", () -> showPage("profile")),
                Ui.button("avatar-next", "내 친구로 선택 →", () -> {
                    profile = profiles.create(nameInput.getText(), (Integer) ageInput.getValue(),
                            (String) genderInput.getSelectedItem(), avatar);
                    showPage("intro");
                })), BorderLayout.SOUTH);
        return page;
    }

    private JPanel introPage() {
        JPanel page = page("6가지 생각의 힘을 만나 볼까요?", "분야마다 3문제, 모두 18문제를 풀어요. 시간에 쫓기지 않고 천천히 생각해도 돼요.");
        JPanel list = Ui.panel(new GridLayout(3, 2, 18, 18));
        for (LearningArea area : LearningArea.catalog()) {
            JPanel item = new JPanel(new BorderLayout(10, 12));
            Ui.padding(item, 22);
            item.setBackground(Color.WHITE);
            item.add(Ui.label(area.symbol() + "  " + area.title(), 21, true), BorderLayout.NORTH);
            item.add(Ui.label(area.measurementGame(), 15, false), BorderLayout.CENTER);
            list.add(item);
        }
        page.add(list, BorderLayout.CENTER);
        JPanel bottom = Ui.panel(new BorderLayout());
        bottom.add(Ui.label("시작용 간이 테스트예요. 정식 측정 게임·표준화된 인지 검사가 아니에요.", 14, false), BorderLayout.NORTH);
        bottom.add(actions(Ui.button("intro-back", "← 아바타 다시 고르기", () -> showPage("avatar")),
                Ui.button("assessment-begin", "첫 번째 테스트 시작 →", () -> {
                    assessment = assessmentFactory.get();
                    showPage("assessment");
                    showQuestion();
                })), BorderLayout.SOUTH);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private void showQuestion() {
        phaseTimer.stop();
        questionHost.removeAll();
        StarterAssessmentService.QuestionView view = assessment.current();
        visiblePhase = view.phase();
        selectedChoice = -1;
        JPanel page = page(view.areaNumber() + " / 6  ·  " + view.areaTitle(),
                view.questionNumber() + " / 3 문제  ·  " + (view.phase() == StarterAssessmentService.Phase.MEMORIZE
                        ? "3초 동안 순서를 기억해 주세요." : "보기를 고르고 답안을 제출해 주세요."));
        JPanel task = new JPanel(new BorderLayout(16, 26));
        task.setBackground(Color.WHITE);
        Ui.padding(task, 34);
        task.add(Ui.paragraph(view.prompt(), 650), BorderLayout.NORTH);
        if (view.phase() == StarterAssessmentService.Phase.MEMORIZE) {
            JLabel stimulus = Ui.label(view.stimulus(), 50, true);
            stimulus.setName("memory-stimulus");
            stimulus.setHorizontalAlignment(JLabel.CENTER);
            task.add(stimulus, BorderLayout.CENTER);
        } else {
            JPanel choices = Ui.panel(new GridLayout(3, 1, 14, 14));
            ButtonGroup group = new ButtonGroup();
            javax.swing.JButton submit = Ui.button("answer-submit", "답안 제출 →", () -> submit(view.id()));
            submit.setEnabled(false);
            for (int option = 0; option < view.choices().size(); option++) {
                final int index = option;
                JToggleButton button = new JToggleButton(view.choices().get(option));
                button.setFont(new Font(Font.DIALOG, Font.BOLD, 25));
                button.setName("answer-" + option);
                button.addActionListener(event -> {
                    selectedChoice = index;
                    submit.setEnabled(true);
                });
                group.add(button);
                choices.add(button);
            }
            task.add(choices, BorderLayout.CENTER);
            task.add(actions(submit), BorderLayout.SOUTH);
        }
        page.add(task, BorderLayout.CENTER);
        int answered = assessment.responses().size();
        JProgressBar progress = new JProgressBar(0, 18);
        progress.setValue(answered);
        progress.setStringPainted(true);
        progress.setString(answered + " / 18 문제 완료");
        page.add(progress, BorderLayout.SOUTH);
        questionHost.add(page, BorderLayout.CENTER);
        questionHost.revalidate();
        questionHost.repaint();
        if (view.phase() == StarterAssessmentService.Phase.MEMORIZE && isDisplayable()) {
            phaseTimer.start();
        }
    }

    private void submit(String questionId) {
        if (selectedChoice < 0) {
            return;
        }
        // 한 번 처리된 화면의 이벤트가 늦게 도착해도 새 문제에 적용하지 않는다.
        if (!assessment.isComplete() && assessment.current().id().equals(questionId)) {
            assessment.submit(questionId, selectedChoice);
            if (assessment.isComplete()) {
                phaseTimer.stop();
                showResults(assessment.result());
            } else {
                showQuestion();
            }
        }
    }

    /** 디스플레이 주기의 갱신. 조회는 서비스의 제시 시각을 바꾸지 않는다. */
    public void refreshPhase() {
        if (visiblePage.equals("assessment") && assessment != null && !assessment.isComplete()
                && assessment.current().phase() != visiblePhase) {
            showQuestion();
        }
    }

    private void showResults(AssessmentResult result) {
        JPanel page = page(profile.name() + "님의 첫 도전, 잘 마쳤어요!", "6개 분야의 시작 테스트 정답률을 확인해 보세요.");
        JPanel body = Ui.panel(new BorderLayout(28, 20));
        RadarChart chart = new RadarChart(Optional.of(result));
        chart.setPreferredSize(new Dimension(520, 450));
        body.add(chart, BorderLayout.CENTER);
        JPanel feedback = new JPanel();
        feedback.setLayout(new BoxLayout(feedback, BoxLayout.Y_AXIS));
        feedback.setBackground(Color.WHITE);
        Ui.padding(feedback, 28);
        feedback.add(new AvatarBadge(profile.avatar()));
        feedback.add(Ui.label("이번 풀이에서 보인 강점", 22, true));
        feedback.add(Box.createVerticalStrut(14));
        feedback.add(Ui.paragraph(result.strengthText(), 230));
        feedback.add(Box.createVerticalStrut(26));
        feedback.add(Ui.label("함께 연습해 볼 부분", 22, true));
        feedback.add(Box.createVerticalStrut(14));
        feedback.add(Ui.paragraph(result.practiceText(), 230));
        feedback.add(Box.createVerticalStrut(22));
        feedback.add(Ui.paragraph("분야별 3문항의 정답률이에요. 2문제 이상 맞힌 분야를 격려하고, 나머지는 연습해 봐요. 능력의 우열이나 IQ를 뜻하지 않아요.", 230));
        JScrollPane scroll = new JScrollPane(feedback);
        scroll.setPreferredSize(new Dimension(430, 500));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        body.add(scroll, BorderLayout.EAST);
        page.add(body, BorderLayout.CENTER);
        page.add(actions(Ui.button("result-home", "나의 교실로 들어가기 →", () -> {
            add(new HomePanel(profile, result), "home");
            showPage("home");
        })), BorderLayout.SOUTH);
        add(page, "result");
        showPage("result");
    }

    private void showPage(String name) {
        visiblePage = name;
        setName("page-" + name);
        pages.show(this, name);
        revalidate();
        repaint();
    }

    private static JPanel page(String title, String description) {
        JPanel page = new JPanel(new BorderLayout(24, 28));
        page.setBackground(Ui.BACKGROUND);
        Ui.padding(page, 38);
        JPanel heading = Ui.panel(new GridLayout(3, 1, 0, 12));
        heading.add(Ui.label("두뇌 성장 교실", 17, true));
        heading.add(Ui.label(title, 30, true));
        heading.add(Ui.label(description, 16, false));
        page.add(heading, BorderLayout.NORTH);
        return page;
    }

    private static JPanel center(JPanel content) {
        JPanel container = Ui.panel(new java.awt.GridBagLayout());
        container.add(content);
        return container;
    }

    private static JPanel actions(javax.swing.JButton... buttons) {
        JPanel actions = Ui.panel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 18));
        for (javax.swing.JButton button : buttons) {
            actions.add(button);
        }
        return actions;
    }

    private static void addField(JPanel form, String title, javax.swing.JComponent input) {
        JLabel label = Ui.label(title, 16, true);
        label.setLabelFor(input);
        form.add(label);
        form.add(Box.createVerticalStrut(8));
        label.setAlignmentX(LEFT_ALIGNMENT);
        input.setAlignmentX(LEFT_ALIGNMENT);
        input.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        input.setPreferredSize(new Dimension(460, 42));
        form.add(input);
        form.add(Box.createVerticalStrut(22));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (visiblePage.equals("assessment") && visiblePhase == StarterAssessmentService.Phase.MEMORIZE) {
            phaseTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        phaseTimer.stop();
        super.removeNotify();
    }
}
