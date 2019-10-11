# extractVideoExifByFFmpeg
## 目的
    利用Java调用FFmpeg,提取视频的exif信息

## 依赖第三工具
    1, FFmpeg
    
## FFmpeg安装及使用  

    1,下载地址    
        http://ffmpeg.org/releases/    

    2,安装：    
        $ sudo apt-get install yasm    

        $ ./configure --enable-shared --prefix=/home/work/soft/ffmpeg/

        $ make   #漫长的等待

        $ sudo make install 

    3,安装成功：      

        异常及解决方法：  
        $ /home/work/soft/ffmpeg/bin/ffmpeg    
            提示：ffmpeg:error while loading shared libraries: libavdevice.so.58: cannot open shared object file: No such file or directory...

            $ sudo find / -name libavdevice.so.58   #会出现类似以下内容    
            /home/wangsai/soft/ffmpeg-4.0/libavdevice/libavdevice.so.58
            find: ‘/run/user/1000/gvfs’: Permission denied
            /usr/local/ffmpeg/lib/libavdevice.so.58

            $ sudo vim /etc/ld.so.conf  #把下面的内容添加进去.    
            /usr/local/ffmpeg/lib/libavdevice.so.58
            /home/wangsai/soft/ffmpeg-4.0/libavdevice/libavdevice.so.58
            /usr/local/ffmpeg/lib

            $ sudo ldconfig 
        
        正常：
        $ /home/work/soft/ffmpeg/bin/ffmpeg   #会出现以下类似内容
            ffmpeg version 4.0 Copyright (c) 2000-2018 the FFmpeg developers
            built with gcc 5.4.0 (Ubuntu 5.4.0-6ubuntu1~16.04.9) 20160609
            configuration: --enable-shared --prefix=/usr/local/ffmpeg
            libavutil 56. 14.100 / 56. 14.100
            libavcodec 58. 18.100 / 58. 18.100
            libavformat 58. 12.100 / 58. 12.100
            libavdevice 58. 3.100 / 58. 3.100
            libavfilter 7. 16.100 / 7. 16.100
            libswscale 5. 1.100 / 5. 1.100
            libswresample 3. 1.100 / 3. 1.100
            Hyper fast Audio and Video encoder
            usage: ffmpeg [options] [[infile options] -i infile]... {[outfile options] outfile}...


## 支持的视频格式
    .mp4, .mov, .rmvb, .flv, .3gp, .f4v, others...

## 使用说明

### 如何使用命令行提取EXIF信息   
  
    1 命令行提取Demo
    .your path/bin/ffprobe -i 文件名 需要提取的参数 
    
    1.1 Demo1，提取视频长宽高、时长、旋转信息
    .your path/bin/ffprobe -i filename  -select_streams v:0 -show_entries       stream=width,height,duration:stream_tags=rotate:format_tags -v quiet -of json

    1.2 Demo2，提取等视频长宽高、帧率、时长、旋转信息、编码方式
    .your path/bin/ffprobe -i 文件名  
        -select_streams v:0 
        -show_entries stream=codec_name,codec_long_name,width,height,color_range,color_space,color_transfer,color_primaries,
               chroma_location,r_frame_rate,avg_frame_rate,start_time,duration_ts,duration,bit_rate,bits_per_raw_sample:stream_tags=rotate:format=format_name,format_long_name,probe_score:format_tags
         -v quiet -of json

    2 视频EXIF字段信息说明: https://wcs.chinanetcenter.com/document/API/Appendix/avinfo-description

    3 更多命令行参数，参考: http://ffmpeg.org/documentation.html

    4 视频旋转信息说明，参考: http://note.youdao.com/noteshare?id=72e9afa826efa0228cf374779cbc9313


### 如何嵌入到Java代码使用
    参考: ../cn.wangsai.VideoExifExtractor.main


