all: test
	ls pWarehouse/S/*/*.bmp | head
	ls pWarehouse/S/*/*.png | head
	#echo 用 make wdivh 配合魔棒工具制作 S
	#echo 用 make frameclean 清除现有帧
	#echo 用 make form 及必要的手动操作准备好 M 和 S
	#echo 用 make r_video 或 make c_video 生成视频
test: src/MS2Frame.class
	java src.MS2Frame pWarehouse/S/*/*.bmp | '/usr/local/ffmpeg/bin/ffmpeg' -r 6 -i pipe:0 -y -b:v 400K out.mp4

c_video: src/MS2Frame.class 
	java src.MS2Frame pWarehouse/S/*/*.bmp | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -y -b:v 100K out.mp4
	
r_video: src/MS2Frame.class 
	java src.MS2Frame pWarehouse/S/*/*.png | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -y -b:v 100K out.mp4
	
src/MS2Frame.class: src/MS2Frame.java
	javac src/MS2Frame.java
src/MS2Frame.java:
	#echo 执行依赖src/MS2Frame.java


wdivh: src/WDivH.class
	java src.WDivH pWarehouse/M/*/*.jpg pWarehouse/M/*/*.JPG
src/WDivH.class: src/WDivH.java
	javac src/WDivH.java
src/WDivH.java:
	#echo 执行依赖src/WDivH.java


form: src/Form.class
	java src.Form
src/Form.class:
	javac src/Form.java
src/Form.java:
	#echo 执行依赖src/Form.java


shred_mWarehouse:
	shred -v mWarehouse/*/*/*
shred_video:
	shred -v video/*.mp4
	
frameclean: 
	#echo 正在删除图片帧
	rm -r frames/*
#realclean:
#	#echo 正在删除所有make生成的文件
#	rm src/*.class
