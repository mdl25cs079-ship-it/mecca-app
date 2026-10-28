package com.mecca.util;

public final class Constants {
    private Constants() {}

    // Roles
    public static final String STUDENT = "STUDENT";
    public static final String ORGANIZER = "ORGANIZER";
    public static final String ADMIN = "ADMIN";

    // Event status
    public static final String EVENT_ACTIVE = "ACTIVE";
    public static final String EVENT_CANCELLED = "CANCELLED";
    public static final String EVENT_COMPLETED = "COMPLETED";

    // Registration status
    public static final String REG_CONFIRMED = "CONFIRMED";
    public static final String REG_CANCELLED = "CANCELLED";

    // Points status
    public static final String POINTS_PENDING = "PENDING";
    public static final String POINTS_APPROVED = "APPROVED";

    // Club status
    public static final String CLUB_PENDING = "PENDING";
    public static final String CLUB_APPROVED = "APPROVED";

    // Levels: 1=College 2=Zonal 3=State 4=National 5=International
    public static final int LEVEL_COLLEGE = 1;
    public static final int LEVEL_ZONAL = 2;
    public static final int LEVEL_STATE = 3;
    public static final int LEVEL_NATIONAL = 4;
    public static final int LEVEL_INTERNATIONAL = 5;
    public static final String[] LEVEL_NAMES = {"", "College", "Zonal", "State", "National", "International"};

    // KTU groups 1,2,3 shown as A,B,C
    public static final String[] GROUP_NAMES = {"", "A", "B", "C"};
    public static final int GROUP_CAP = 40;

    // Admission types
    public static final String ADM_REGULAR = "REGULAR";
    public static final String ADM_LATERAL = "LATERAL";
    public static final String ADM_PWD = "PWD";

    /** Required points per group for an admission type: 40 / 30 / 20. */
    public static int requiredPerGroup(String admissionType) {
        if (ADM_LATERAL.equals(admissionType)) return 30;
        if (ADM_PWD.equals(admissionType)) return 20;
        return 40;
    }

    /** Required total points for an admission type: 120 / 90 / 60. */
    public static int requiredTotal(String admissionType) {
        return requiredPerGroup(admissionType) * 3;
    }

    // Mail modes
    public static final String MAIL_LIVE = "LIVE";
    public static final String MAIL_MOCK = "MOCK";
}
