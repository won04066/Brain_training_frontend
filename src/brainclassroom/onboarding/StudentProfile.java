package brainclassroom.onboarding;

import java.util.List;

/** 시작 화면의 임시 프로필. 아직 파일에 저장하지 않는다. */
public record StudentProfile(String id, String name, int age, String gender, String avatar) {
    public static final List<String> GENDERS = List.of("선택 안 함", "여자", "남자");
    public static final List<String> AVATARS = List.of("곰", "토끼", "고양이", "여우");

    public StudentProfile {
        if (id == null || id.isBlank() || name == null || name.isBlank() || name.strip().length() > 20) {
            throw new IllegalArgumentException("이름은 1~20자로 입력해 주세요.");
        }
        name = name.strip();
        if (age < 6 || age > 13) {
            throw new IllegalArgumentException("나이는 6~13세로 선택해 주세요.");
        }
        if (!GENDERS.contains(gender) || !AVATARS.contains(avatar)) {
            throw new IllegalArgumentException("성별과 아바타 선택을 확인해 주세요.");
        }
    }
}
