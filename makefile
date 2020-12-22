all: help test

help:
	ls ../pWarehouse/S/*/*.bmp | head
	ls ../pWarehouse/S/*/*.png | head
	#echo 用 make select_w_div_h 配合PhotoShop的魔棒工具制作 S
	#echo 用 make form 操作窗体准备好 M 和 S
	#echo 用 make sample 生成样例

test: src/MS2Frame.class
	java src.MS2Frame sample/S/test/*.png | '/usr/local/ffmpeg/bin/ffmpeg' -r 6 -i pipe:0 -y -b:v 128K out.mp4

sample: src/MS2Frame.class
	java src.MS2Frame sample/S/SampleInput/*.png | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -y -b:v 128K out.mp4

c_video: src/MS2Frame.class 
	java src.MS2Frame ../pWarehouse/S/*/*.bmp | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -y out.mp4
	
r_video: src/MS2Frame.class 
	java src.MS2Frame ../pWarehouse/S/*/*.png | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -y out.mp4
	
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
	shred -v video/*.mp4
shred_f_out_debug:
	shred -v -u F_OUT_DEBUG/*.*
shred_mWarehouse:
	shred -v ../mWarehouse/*/*/*
