package com.mecca.service;

import com.mecca.model.ActivityPoint;
import com.mecca.model.KTUCategory;
import com.mecca.model.PointsSummary;

import java.util.ArrayList;
import java.util.List;

/** STUB. Owner: M7. Keep method names/signatures EXACTLY. All methods are static. */
public class ActivityPointsService {
    public static void awardPoints(int userId, int eventId) { }   // TODO M7

    /** Fake data until M7 pushes the real implementation. */
    public static PointsSummary getSummary(int userId) {
        PointsSummary s = new PointsSummary();
        s.setAdmissionType("REGULAR");
        s.setEarned(new int[]{24, 10, 0});
        s.setRequired(new int[]{40, 40, 40});
        s.setTotal(34);
        s.setTotalRequired(120);
        s.setCreditsComplete(false);
        return s;
    }

    public static List<ActivityPoint> getHistory(int userId) { return new ArrayList<>(); }           // TODO M7
    public static boolean submitClaim(int userId, String ktuCode, int level, String note) { return false; } // TODO M7
    public static List<ActivityPoint> getPendingClaims() { return new ArrayList<>(); }               // TODO M7
    public static void approveClaim(int apId) { }                                                    // TODO M7
    public static void rejectClaim(int apId) { }                                                     // TODO M7 (extra, needed by M8)

    /** Fake categories until M7 pushes. */
    public static List<KTUCategory> getCategories() {
        List<KTUCategory> list = new ArrayList<>();
        KTUCategory c = new KTUCategory();
        c.setCode("1.1"); c.setGroupNo(1); c.setName("Sports/Games/Arts participation"); c.setMaxPoints(40);
        c.setPointsByLevel(new int[]{1, 5, 10, 20, 40});
        list.add(c);
        return list;
    }
}
