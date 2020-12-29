
	java src.MS2Frame '/home/ch/图片/workspace/pWarehouse/S/TA (1)' | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y video/1.mp4
	java src.MS2Frame '/home/ch/图片/workspace/pWarehouse/S/TA ()' | '/usr/local/ffmpeg/bin/ffmpeg' -r 8 -i pipe:0 -vcodec libx264 -pix_fmt yuv420p -acodec aac -y video/.mp4
