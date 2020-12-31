
sample: src/MS2Frame.class 
	java src.MS2Frame picture/SampleInput/S | ffmpeg -r 8 -i pipe:0 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y video/out.mp4
	
voice: src/MS2Frame.class
	java src.MS2Frame picture/SampleInput/*.choosen | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -acodec aac -i audio/short.mp3 -y video/out.mp4

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
#shred_video:
#	shred -v --iterations=1 video/*/*.mp4
shred_f_out_debug:
	shred -v -u F_OUT_DEBUG/*.*
shred_factory:
	shred -v -u picture/factory/*/*
VSS: png_video
	mv --backup=numbered video/out.mp4 video/VSS.mp4
	shred -v -u picture/factory/*/*
	shutdown
