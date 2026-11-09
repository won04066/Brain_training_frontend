package brainclassroom.onboarding;

import java.util.UUID;

/** 현재 실행의 프로필 생성만 담당한다. 성별은 출제·판정에 사용하지 않는다. */
public final class ProfileService {
    public void validate(String name, int age, String gender, String avatar) {
        new StudentProfile("validation-only", name, age, gender, avatar);
    }

    public StudentProfile create(String name, int age, String gender, String avatar) {
        return new StudentProfile(UUID.randomUUID().toString(), name, age, gender, avatar);
    }
}
