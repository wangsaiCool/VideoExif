package cn.wangsai.utils;

/**
 * Created by WangSai on 8/9/18
 */
public class Settings {

    private static String dir = "";
    private static String name = "";
    private static boolean isSetName = false;
    private static boolean isSetDir = false;

    public static boolean isSetName() {
        return isSetName;
    }

    public static void setIsSetName(boolean isSetName) {
        Settings.isSetName = isSetName;
    }

    public static boolean isSetDir() {
        return isSetDir;
    }

    public static void setIsSetDir(boolean isSetDir) {
        Settings.isSetDir = isSetDir;
    }

    public static String getDir() {
        return dir;
    }

    public static void setDir(String dir) {
        setIsSetDir(true);
        Settings.dir = dir;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        setIsSetName(true);
        Settings.name = name;
    }
}
