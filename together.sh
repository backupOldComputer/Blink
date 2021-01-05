p="/home/ch/图片/Blink/picture/S/TA (54)"
m=`ls "$p" | wc -l`
java src.MS2Frame "$p" | ffmpeg -r `cat config/rNoLine` -i pipe:0 -stream_loop -1 -i audio/扯.mp3 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y -ss 0 -t $[m*3] "video/`date`.mp4"
