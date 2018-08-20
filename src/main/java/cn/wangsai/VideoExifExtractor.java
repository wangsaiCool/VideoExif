package cn.wangsai;

import cn.wangsai.utils.FFmpegUtils;
import cn.wangsai.utils.Settings;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.DirectColorModel;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.SchemaOutputResolver;

public class VideoExifExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoExifExtractor.class);

    private static final String KEY_FORMAT = "format";
    private static final String KEY_CREATION_TIME = "creation_time";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LOCATION_APPLE_MOV = "com.apple.quicktime.location";
    private static final String KEY_TAGS = "tags";
    private static final String ABNORMAL_DATE = "1970-01-01T23:59:59";

    private static final String DATE_TIME = "dateTime";
    private static final String GPS_LONGITUDE = "GPSLongitude";
    private static final String GPS_LONGITUDE_REF = "GPSLongitudeRef";
    private static final String GPS_LATITUDE = "GPSLatitude";
    private static final String GPS_LATITUDE_REF = "GPSLatitudeRef";
    private static final String GPS_ALTITUDE = "GPSAltitude";
    private static final String GPS_ALTITUDE_REF = "GPSAltitudeRef";

    private static final String PATTERN = "([\\+-]?\\d+\\.\\d+)([\\+-]{1}\\d+\\.\\d+)";
    private static final Pattern REGX = Pattern.compile(PATTERN);

    public static void main(String[] args) throws org.apache.commons.cli.ParseException {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        //help
        options.addOption("help", false, "Print the usage info.");
        //指定文件路径
        options.addOption("dir", true, "Specify the file Directory.\nFor example,\n" +
                "/home/Downloads/");
        options.addOption("file", false, "Specify the file name.\nFor example,\n" +
                "Titanic.map4");

        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption("help")) {
            Collection<Option> opts = options.getOptions();
            System.out.println("===================================");
            for (Option option : opts) {
                System.out.println(option.getOpt() + "-" + option.getDescription());
            }
            System.out.println("===================================");
        }

        if (commandLine.hasOption("dir")){
            String path = commandLine.getOptionValue("dir");
            Settings.setDir(path);
        }

        if (commandLine.hasOption("file")){
            String fileName = commandLine.getOptionValue("file");
            Settings.setName(fileName);
        }

        //未指定具体文件
        if (Settings.isSetDir() && !Settings.isSetName()) {
            try {

                List<File> files = getFilesAbsolutePath(Settings.getDir());
                for (int i = 0; i < files.size(); i++) {
                    System.out.println(files.get(i).getName());
                    System.out.println(generateExifJson(files.get(i).getAbsolutePath()));
                    System.out.println("================================================");
                }
            } catch (IOException e) {
                LOGGER.error("Get file error.", e);
            }
        }
        //指定具体文件
        else if (Settings.isSetDir() && Settings.isSetName()){
            String dir = Settings.getDir();
            if (!dir.endsWith("/")) {
                dir += "/";
            }
            System.out.println(Settings.getName() + ":");
            System.out.println(generateExifJson(dir + Settings.getName()));
            System.out.println("\n");
        }


    }

    /**
     * 根据fileStream提取exif信息
     *
     * @return JSONObject
     * {"dateTime": "2013:02:26 16:45:55",
     * "GPSLongitude":"40/1 13/1 199200/10000",
     * "GPSLongitudeRef":"E",
     * "GPSLatitude":"160/1 12/1 475200/10000",
     * "GPSLatitudeRef":"N"
     * }
     */
    private static JSONObject generateExifJson(String videoTempPath) {

        try {
            //利用FFmpeg提取时间和地理位置信息
            JSONObject jsonObject = FFmpegUtils.getVideoInfo(videoTempPath);
            //提取FFmpeg结果中的时间和经纬度信息,并进行格式化
            JSONObject retJson = extractContent(jsonObject);
            return retJson;
        } catch (IOException e) {
            LOGGER.error("Read file stream catch IOExcepiton", e);
        } catch (InterruptedException e) {
            LOGGER.error("Process in getVideoInfo() catch InterruptedException", e);
        } catch (JSONException e) {
            LOGGER.error("Process in getVideoInfo() catch JSONException", e);
        }
        return new JSONObject();
    }

    /**
     * 提取FFmpeg返回值中的时间和经纬度,处理location关键字不一样的问题.
     * 提取出{"format": {"tags": {"creation_time": "2013-02-26T08:45:55.000000Z","location":"+40.2222+160.2132"}}}中的时间值和经纬度值
     *
     * @param jsonObject
     * @return JSONObject
     * {"dateTime": "2013:02:26 16:45:55",
     * "GPSLongitude":"40/1 13/1 199200/10000",
     * "GPSLongitudeRef":"E",
     * "GPSLatitude":"160/1 12/1 475200/10000",
     * "GPSLatitudeRef":"N",
     * }
     */
    private static JSONObject extractContent(JSONObject jsonObject) throws JSONException {
        HashMap<String, String> result = new HashMap<>();
        if (jsonObject == null || !jsonObject.has(KEY_FORMAT)) {
            return null;
        }
        JSONObject jsonFormat = jsonObject.getJSONObject(KEY_FORMAT);
        if (!jsonFormat.has(KEY_TAGS)) {
            return null;
        }
        JSONObject jsonTags = jsonFormat.getJSONObject(KEY_TAGS);
        Iterator iterator = jsonTags.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (key.equalsIgnoreCase(KEY_CREATION_TIME)) {
                result.putAll(formatDateTime(jsonTags.getString(key)));
            }
            //APPLE的MOV视频文件的经纬度坐标的Key为 "com.apple.quicktime.location.ISO6709",而mp4关键字为location
            //mp4有两个经纬度Key:location,location-eng
            if (key.equalsIgnoreCase(KEY_LOCATION) || key.contains(KEY_LOCATION_APPLE_MOV)) {
                result.putAll(formatGPSLocation(jsonTags.getString(key)));
            }
        }
        return new JSONObject(result);
    }

    /**
     * 格式化UTC时间.
     *
     * @param dateTimeStr "2013-02-26T08:45:55.000000Z"
     * @return HashMap {"dateTime":"2013:02:26 16:45:55"}
     */
    private static HashMap<String, String> formatDateTime(String dateTimeStr) {
        HashMap<String, String> result = new HashMap<>();
        if (StringUtils.isBlank(dateTimeStr)) {
            return result;
        }
        //早于1970-01-01T23:59:59 按照异常时间处理
        try {
            if (dateTimeStr.substring(0, ABNORMAL_DATE.length()).compareToIgnoreCase(ABNORMAL_DATE) < 0) {
                return result;
            }
        } catch (StringIndexOutOfBoundsException e) {
            return result;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000000Z'");
        SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        //正常日期字符串处理
        try {
            Date date = simpleDateFormat.parse(dateTimeStr);
            String formatDateTime = format.format(date);
            result.put(DATE_TIME, formatDateTime);
            return result;
        } catch (ParseException e) {
            return result;
        }
    }

    /**
     * 处理经纬度 (+/-纬度+/-经度+/-海拔)"+40.0443+116.3215+048.104/"
     *
     * @param gpsLocation
     * @return HashMap
     * {"dateTime": "2013:02:26 16:45:55",
     * "GPSLatitude":"40/1 13/1 199200/10000",
     * "GPSLatitudeRef":"N",
     * "GPSLongitude":"160/1 12/1 475200/10000",
     * "GPSLongitudeRef":"E"
     * }
     */
    private static HashMap<String, String> formatGPSLocation(String gpsLocation) {
        HashMap<String, String> result = new HashMap<>();
        if (StringUtils.isBlank(gpsLocation)) {
            return result;
        }
        //正则匹配日期
        Matcher matcher = REGX.matcher(gpsLocation);
        if (!matcher.find()) {
            LOGGER.warn(String.format("Failed to parse GPS:%s", gpsLocation));
            return result;
        }
        try {
            //纬度
            double latitude = Double.parseDouble(matcher.group(1));
            if (Math.abs(latitude) > 90.0) {
                throw new IllegalArgumentException("Illegal latitude");
            }
            if (latitude > 0.0) {
                result.put(GPS_LATITUDE_REF, "N");
            } else {
                result.put(GPS_LATITUDE_REF, "S");
            }
            String dms = transferDDToDegreeMinSec(latitude);
            result.put(GPS_LATITUDE, dms);
            //经度
            double longitude = Double.parseDouble(matcher.group(2));
            if (Math.abs(latitude) > 180.0) {
                throw new IllegalArgumentException("Illegal Longitude");
            }
            if (longitude > 0.0) {
                result.put(GPS_LONGITUDE_REF, "E");
            } else {
                result.put(GPS_LONGITUDE_REF, "W");
            }
            dms = transferDDToDegreeMinSec(longitude);
            result.put(GPS_LONGITUDE, dms);
            return result;
        } catch (NumberFormatException e) {
            LOGGER.warn(String.format("Failed to parseDouble {%s} to Double.", gpsLocation));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(String.format("Failed to format GPSLocation {%s} to Degree'Min'Sec.", gpsLocation));
        }
        result.clear();
        return result;
    }

    private static String transferDDToDegreeMinSec(double degreeDegree) throws IllegalArgumentException {
        //+16.5555
        double numDD = Math.abs(degreeDegree);
        if (numDD > 180.0) {
            throw new IllegalArgumentException("GPS value is illegal.");
        }
        StringBuilder retStrB = new StringBuilder();
        //16
        int degree = (int) Math.floor(numDD);
        retStrB.append(degree + "/1 ");
        //(0.5555*60)=33.3300
        numDD = (numDD - degree) * 60;
        int min = (int) Math.floor(numDD);
        retStrB.append(min + "/1 ");
        //0.33 * 60 = 19.8
        numDD = (numDD - min) * 60;
        int sec = (int) (numDD * 10000);
        retStrB.append(sec + "/10000");
        return retStrB.toString();
    }

    private static List<File> getFilesAbsolutePath(String dir) throws IOException {
        List<File> namesList = new ArrayList<>();
        List<File> notFoundNames = new ArrayList<>();
        File dir1 = new File(dir);
        if (!dir1.exists()) {
            LOGGER.error(String.format("%s not Found.", dir));
            throw new IOException(String.format("%s not Found.", dir));
        }

        namesList = getFileName(dir1, namesList, notFoundNames);
        for (int i = 0; i < notFoundNames.size(); i++) {
            if (i == 0) {
                System.out.println("Files invalid:");
            }
            System.out.println(notFoundNames.get(i));
        }
        System.out.println();
        return namesList;
    }

    private static List<File> getFileName(File name, List<File> namesList, List<File> notFoundNames) {
        if (!name.exists()) {
            notFoundNames.add(name);
        }
        if (name.isDirectory()) {
            File[] files = name.listFiles();
            for (int i = 0; i < files.length; i++) {
                getFileName(files[i], namesList, notFoundNames);
            }
        }
        if (name.isFile()) {
            namesList.add(name);
        }
        return namesList;
    }
}
