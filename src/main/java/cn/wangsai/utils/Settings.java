package cn.wangsai.utils;

/**
 * Created by WangSai on 8/9/18
 */
public class Settings {

    private static String dir = "";
    private static String name = "";
    private static boolean setDir = false;
    private static boolean setName = false;

    public static boolean isSetDir() {
        return setDir;
    }

    public static void setIsSetDir(boolean setDir) {
        Settings.setDir = setDir;
    }

    public static boolean isSetName() {
        return setName;
    }

    public static void setIsSetName(boolean setName) {
        Settings.setName = setName;
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
