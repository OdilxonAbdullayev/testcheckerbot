package uz.db.enums;

public enum QuizType {
    MILLIY_SERTIFIKAT("MILLIY_SERTIFIKAT"),
    ATTESTATSIYA("ATTESTATSIYA");

    private String displayName;

    QuizType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
