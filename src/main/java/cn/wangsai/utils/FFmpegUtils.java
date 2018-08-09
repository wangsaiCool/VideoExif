package cn.wangsai.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by WangSai on 8/8/18
 */
public class FFmpegUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegUtils.class);

    private static final String INFO_FMT = "/home/work/soft/ffmpeg/bin/ffprobe -i %s  -select_streams v:0 -show_entries  stream=width,height,duration:stream_tags=rotate:format_tags -v quiet -of json";
    private static final long PROCESS_TIMEOUT = 5000; // 5sec

    public static JSONObject getVideoInfo(String video)
            throws IOException, InterruptedException, RuntimeException {
        String infoCmd = String.format(INFO_FMT, video);
        long start = System.currentTimeMillis();
        Process process = Runtime.getRuntime().exec(infoCmd);
        process.waitFor(PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
        if (process.isAlive()) {
            process.destroyForcibly();
            throw new RuntimeException(String.format("get video info timeout:[%d]", PROCESS_TIMEOUT));
        }

        if (process.exitValue() != 0) {
            String verbose = String.format("get video info error: %s", IOUtils.toString(process.getErrorStream()));
            throw new RuntimeException(verbose);
        }
        LOGGER.info("It takes [%d seconds] to process video:%s", (System.currentTimeMillis() - start) * 1.0 / 1000, video);
        JSONObject json = new JSONObject(IOUtils.toString(process.getInputStream()));
        return json;
    }
}
