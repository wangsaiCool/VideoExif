# extractVideoExifByFFmpeg
## 目的
利用Java调用FFmpeg,提取视频的exif信息

## 用到的其他工具
1,第三方库FFmpeg FFprobe;    
2,FFmpeg安装及使用  

    2.1,下载地址    
        http://ffmpeg.org/releases/    

    2.2,安装：    
        $ sudo apt-get install yasm    

        $ ./configure --enable-shared --prefix=/monchickey/ffmpeg

        $ make   #漫长的等待

        $ sudo make install 



    2.3,安装成功：      

        异常及解决方法：  
        $ /usr/local/ffmpeg/bin/ffmpeg    
            提示：ffmpeg:error while loading shared libraries: libavdevice.so.58: cannot open shared object file: No such file or directory...

            $ sudo find / -name libavdevice.so.58    
            /home/wangsai/soft/ffmpeg-4.0/libavdevice/libavdevice.so.58
            find: ‘/run/user/1000/gvfs’: Permission denied
            /usr/local/ffmpeg/lib/libavdevice.so.58

            $ sudo vim /etc/ld.so.conf  #把下面的内容添加进去.    
            /usr/local/ffmpeg/lib/libavdevice.so.58
            /home/wangsai/soft/ffmpeg-4.0/libavdevice/libavdevice.so.58
            /usr/local/ffmpeg/lib

            $ sudo ldconfig 
        
        正常：
        $ /usr/local/ffmpeg/bin/ffmpeg
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
TODO 

