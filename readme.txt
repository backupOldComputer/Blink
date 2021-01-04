作业流程
1.	java src.SelectWDivH <files>
    if(图像宽高比==指定相机主流宽高比)生成图像副本.choosen
2.	人工编辑图像副本.choosen。使得选区内为白色，选区外为黑色、透明色或其他有利于高压缩率的颜色
    原图放入M，选区放入S
3.	java src.MS2Frame <paths>
    计算闪烁帧并写到标准输出
4.	make 
    重定向到ffmpeg生成视频
