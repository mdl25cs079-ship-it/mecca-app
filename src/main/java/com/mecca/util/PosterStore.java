// src/main/java/com/mecca/util/PosterStore.java
package com.mecca.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Saves and finds event posters as files: posters/event_<id>.<ext> */
public class PosterStore {

    private static final File DIR = new File("posters");
    private static final String[] EXTS = {"png", "jpg", "jpeg", "gif"};

    /** Returns the poster file for an event, or null if it has none. */
    public static File getPoster(int eventId) {
        for (String ext : EXTS) {
            File f = new File(DIR, "event_" + eventId + "." + ext);
            if (f.exists()) return f;
        }
        return null;
    }

    /** Copies the chosen image into the posters folder, replacing any old poster. */
    public static void savePoster(int eventId, File source) throws IOException {
        DIR.mkdirs();
        String name = source.getName().toLowerCase();
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot + 1) : "png";
        deletePoster(eventId);
        Files.copy(source.toPath(), new File(DIR, "event_" + eventId + "." + ext).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public static void deletePoster(int eventId) {
        for (String ext : EXTS) {
            File f = new File(DIR, "event_" + eventId + "." + ext);
            if (f.exists()) f.delete();
        }
    }
}