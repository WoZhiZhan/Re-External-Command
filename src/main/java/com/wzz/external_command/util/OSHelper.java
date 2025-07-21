package com.wzz.external_command.util;

import java.io.File;

public class OSHelper {
    public static String getOSName() {
        return System.getProperty("os.name", "").toLowerCase();
    }

    public static String getVendor() {
        return System.getProperty("java.vendor", "").toLowerCase();
    }

    public static boolean isWindows() {
        return getOSName().contains("win");
    }

    public static boolean isLinux() {
        return getOSName().contains("nux") || getOSName().contains("nix");
    }

    public static boolean isMac() {
        return getOSName().contains("mac");
    }

    public static boolean isAndroid() {
        return getVendor().contains("android");
    }

    public static boolean isAdmin() {
        return isWindows() && new File("C:\\Windows\\System32\\drivers\\etc\\hosts").canWrite();
    }
}
