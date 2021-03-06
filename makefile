redo: clean video/videoOnly.mp4
	echo redo

voice: video/videoOnly.mp4
	ffmpeg -i video/videoOnly.mp4 -stream_loop -1 -i audio/扯.mp3 -t `cat config/t.NoLine` -y "video/`date`.mp4"

video/videoOnly.mp4: src/MS2Frame.class 
	java src.MS2Frame picture/S/* | ffmpeg -r `cat config/r.NoLine` -i pipe:0 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y video/videoOnly.mp4

test: src/MS2Frame.class
	echo 3 > config/t.NoLine
	java src.MS2Frame picture/S/* | ffmpeg -r `cat config/r.NoLine` -i pipe:0 -stream_loop -1 -i audio/扯.mp3 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y -ss 0 -t `cat config/t.NoLine` "video/`date`.mp4"

factor: src/MS2Frame.class
	echo 600 > config/t.NoLine
	java src.MS2Frame picture/S/* | ffmpeg -r `cat config/r.NoLine` -i pipe:0 -stream_loop -1 -i audio/扯.mp3 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y -ss 0 -t `cat config/t.NoLine` "video/`date`.mp4"

src/MS2Frame.class: src/MS2Frame.java
	javac src/MS2Frame.java


form: src/Form.class
	java src.Form
src/Form.class: src/Form.java src/BlockCipher.java src/FileOperate.java src/Form.java src/Tool.java 
	javac src/Form.java

select_w_div_h: src/SelectWDivH.class
	java src.SelectWDivH ../pWarehouse/M/*/*.JPG
src/SelectWDivH.class: src/SelectWDivH.java
	javac src/SelectWDivH.java


clean:
	rm src/*.class
	shred -v -u --iterations=1 video/*
	shred -v -u --iterations=1 picture/*/*/*.*
	shred -v -u --iterations=1 picture/*/*.*
	shred -v -u --iterations=1 picture/*.*
shred_f_out_debug:
	shred -v -u F_OUT_DEBUG/*.*
