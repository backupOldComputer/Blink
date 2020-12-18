all: test
	ls pWarehouse/S/*/*.bmp | head
	ls pWarehouse/S/*/*.png | head
	#echo 用 make wdivh 配合魔棒工具制作 S
	#echo 用 make frameclean 清除现有帧
	#echo 用 make form 及必要的手动操作准备好 M 和 S
	#echo 用 make r_video 或 make c_video 生成视频
test: src/MS2Frame.class
	java src.MS2Frame pWarehouse/S/*/*.bmp | '/usr/local/ffmpeg/bin/ffmpeg' -r 6 -i pipe:0 -i audio/*.mp3 -y -b:v 128K out.mp4

c_video: src/MS2Frame.class 
	java src.MS2Frame pWarehouse/S/*/*.bmp | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -y -b:v 400K out.mp4
	
r_video: src/MS2Frame.class 
	java src.MS2Frame pWarehouse/S/*/*.png | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -y -b:v 400K out.mp4
	
src/MS2Frame.class: src/MS2Frame.java
	javac src/MS2Frame.java


select_w_div_h: src/SelectWDivH.class
	java src.SelectWDivH pWarehouse/M/*/*.JPG
src/SelectWDivH.class: src/SelectWDivH.java
	javac src/SelectWDivH.java


form: src/Form.class
	java src.Form
src/Form.class:
	javac src/Form.java


shred_mWarehouse:
	shred -v mWarehouse/*/*/*
shred_video:
	shred -v video/*.mp4
clean:
	rm src/*.class
