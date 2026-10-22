package com.mecca.util;

import com.mecca.service.NotificationService;

public class TestNotify {
    public static void main(String[] args) throws Exception {
        NotificationService.sendConfirmation(4, 1);
        Thread.sleep(8000);
        System.exit(0);
    }
}
