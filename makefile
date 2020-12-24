all: help test

help:
	ls ../pWarehouse/S/*/*.bmp | head
	ls ../pWarehouse/S/*/*.png | head
	#echo 用 make select_w_div_h 配合PhotoShop的魔棒工具制作 S
	#echo 用 make form 操作窗体准备好 M 和 S
	#echo 用 make sample 生成样例

test: src/MS2Frame.class
	java src.MS2Frame sample/S/test/*.png | '/usr/local/ffmpeg/bin/ffmpeg' -r 10 -i pipe:0 -y -b:v 128K out.mp4

sample: src/MS2Frame.class
	java src.MS2Frame sample/S/SampleInput/*.png | '/usr/local/ffmpeg/bin/ffmpeg' -r 10 -i pipe:0 -y -b:v 128K out.mp4

choosen_video: src/MS2Frame.class 
	java src.MS2Frame picture/*/*.choosen | '/usr/local/ffmpeg/bin/ffmpeg' -r 10 -i pipe:0  -vcodec libx264 -pix_fmt yuv420p -acodec aac  -y out.mp4
	
bmp_video: src/MS2Frame.class 
	java src.MS2Frame picture/*/*.bmp     | '/usr/local/ffmpeg/bin/ffmpeg' -r 10 -i pipe:0  -vcodec libx264 -pix_fmt yuv420p -acodec aac  -y out.mp4
	
png_video: src/MS2Frame.class 
	java src.MS2Frame picture/*/*.png     | '/usr/local/ffmpeg/bin/ffmpeg' -r 10 -i pipe:0  -vcodec libx264 -pix_fmt yuv420p -acodec aac  -y out.mp4
	
src/MS2Frame.class: src/MS2Frame.java
	javac src/MS2Frame.java


form: src/Form.class
	java src.Form
src/Form.class: src/Form.java src/BlockCipher.java src/FileOperate.java src/Form.java src/Tool.java 
	javac src/Form.java

#../pWarehouse/M/*/*.JPG
select_w_div_h: src/SelectWDivH.class
	java src.SelectWDivH sample/S/test/*.JPG
src/SelectWDivH.class: src/SelectWDivH.java
	javac src/SelectWDivH.java


clean:
	rm src/*.class
shred_video:
	shred -v --iterations=1 video/*.mp4
shred_mWarehouse:
	shred -v --iterations=1 ../mWarehouse/*/*/*
shred_f_out_debug:
	shred -v -u F_OUT_DEBUG/*.*
shred_picture:
	shred -v -u picture/*/*
VSS: png_video
	mv --backup=numbered out.mp4 video/VSS.mp4
#	shred -v -u picture/*/*
	shutdown
