package src;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final boolean DEBUG = true;
    public static final int REPEAT = DEBUG ? 2 : 3;
    public static final int HALF_HDP = DEBUG ? 6 : 12;
    public static final int MAX_SUB_RED = 144;//m的选区像素red分量大于MAX_SUB_RED会触发darker()
    public static final String FRAME_FORMAT = "PNG"; //帧图片格式，需考虑ffmpeg能否解码，以及java能否编码
    public static final String FILE_SEP = "/";
    
    private static boolean F_OUT_DEBUG = false;
    private static int F_OUT_SUFFIX = 0;
    private static String F_OUT_PATH = "F_OUT_DEBUG";
    private static OutputStream sOut() { //TODO:外置类，cmake
	if( ! F_OUT_DEBUG) return System.out;
	++F_OUT_SUFFIX; 
	try{
		new File(F_OUT_PATH).mkdir();
		String name = F_OUT_SUFFIX+"."+FRAME_FORMAT;
		return new FileOutputStream(F_OUT_PATH + FILE_SEP + name);
	}catch(FileNotFoundException ex){
		throw new AssertionError();
	}
    }
    public static String pathS2M(String path) {
	String result = path.substring(0, path.length() - 4 );
	String prefix = "pWarehouse"+FILE_SEP;
	// path.replaceFirst(prefix+"S", prefix+"M");
	if(new File(result).isFile()) 
		return result;
	else
		throw new AssertionError();
    }
    public static void main(String[] sPaths) throws IOException {
	if(sPaths == null || sPaths.length == 0){
		System.err.println("🌃🌃🌃以指示选区的掩码图片的路经集作为参数执行此程序，程序会自动去掉后缀以匹配原图片🌃🌃🌃");
		return;
	}
	if(sPaths.length == 1 && sPaths[0].indexOf('*') != -1){
		System.err.println(sPaths[0] + "为空，程序退出");
		return;
	}
	if(MAX_SUB_RED > 0xff) throw new AssertionError();
	for(String sp : sPaths){
		BufferedImage s = ImageIO.read(new File(sp));
		BufferedImage m = ImageIO.read(new File(pathS2M(sp)));
		pictureToFrames(m, s);
	}
    }
    public static boolean isChooes(int rgb) {
	return rgb == Color.WHITE.getRGB();
    }
    public static int rgba2Red(int rgba) {
	return ( rgba/0x10000 ) & 0xff;
    }
    public static BufferedImage imClone(BufferedImage from) {
	BufferedImage to = new BufferedImage(from.getWidth(), from.getHeight(), from.getType());
        to.setData(from.getData());
	return to;
    }
    public static void pictureToFrames(BufferedImage m, BufferedImage s) throws IOException {
	int r = s.getWidth();
	int c = s.getHeight();
	if( r > m.getWidth() || c > m.getHeight() ) throw new AssertionError();
	int rb=-1,re=0,cb=(c-1),ce=0;	//二维for循环计算 选区 窗口 矩阵 坐标
	for(int i=0; i<r; ++i){
	    for(int j=0; j<c; ++j){
		if( isChooes( s.getRGB(i,j) ) ){
		    if(rb == -1) rb = i; // 首个选区像素可确定rb
		    re = i; // 最后一个选区像素可确定re
		    cb = (j<cb)?j:cb;
		    ce = (j>ce)?j:ce; 
	   	}
	    }
	}
	int[][] step = new int[re+1-rb][ce+1-cb]; //三重循环处理选区红亮
	for(int i=0; i < step.length ; ++i){
		int x = i+rb;
		for(int j=0; j < step[0].length ; ++j){
			int y = j+cb;
			if( ! isChooes( s.getRGB(x,y) ) ){
				step[i][j] = 0;//选区外为0，选区内为step值
				continue;
			}
			step[i][j] = (0xff - handleTooBright(m,x,y)) /HALF_HDP; 
		}
	}
	//循环写入
	BufferedImage[] frames = new BufferedImage[HALF_HDP*2];
	frames[0] = m;	//首帧使用（可能被 sToStep 修改过的）m 
/* 三重循环算出半程帧并引用于后半程 ( 以 HALF_HDP==6 , step==30时为例 ) 
 * frames下标： 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11（0为原图）
 * 红分量深度：30,40,50,60,70,80,90,80,70,60,50,40（100为原图深度）
 */
	for(int k=1; k <= HALF_HDP ;k++){ 
		BufferedImage mClone = imClone(frames[k-1]);
		WritableRaster wrm = mClone.getRaster();
		wrm = nextFrame(wrm, step, rb, cb, 1);
		frames[k] = mClone;
		frames[frames.length-k] = frames[k]; //往返闪烁
	}
	writeFrames(frames); //REPEAT次一重循环写入帧
    }
    public static int handleTooBright(BufferedImage m, int x, int y){
	WritableRaster wrm = m.getRaster();
	int[] iArray = new int[3];
	do{
		wrm.getPixel(x,y,iArray);
		if(iArray[0] <= 0xff - MAX_SUB_RED) break;
		Color source = new Color(m.getRGB(x,y), true);
		m.setRGB(x,y,source.darker().getRGB());
		//		iArray[0] -= 10;
		//		wrm.setPixel(x,y,iArray);	//过于白亮的像素会变成蓝绿色
	}while(true);
//	if(DEBUG)System.err.print(stepRed+"#");
	return iArray[0];
    }
    public static void writeFrames(BufferedImage[] frames){
	for(int t=0;t<REPEAT;++t){
		for(int f=0;f<frames.length;++f)
			try{
				ImageIO.write(frames[f], FRAME_FORMAT, sOut());
			}catch(IOException ex){
				throw new AssertionError();
			}
		if(F_OUT_DEBUG) {
			System.err.println("文件DEBUG状态下只需写盘一轮"); 
			break;
		}
	}
    }
    /** 此方法会修改m */
    public static WritableRaster nextFrame(WritableRaster wrm, final int[][] step, int rb, int cb, final int addOrSub){
	int h = step[0].length;
	for(int i=0; i < step.length ; ++i){
		if( step[i].length != h )	//断言不是锯齿数组
			throw new AssertionError();
		int x = i+rb;
		for(int j=0; j < h ; ++j){
			int y = j+cb;
		    if(step[i][j]!=0){
			int[] iArray = new int[4];
			wrm.getPixel(x,y,iArray);
			iArray[0] += addOrSub*step[i][j];
			wrm.setPixel( x,y , iArray );
		    }
		}
	}
	return wrm;
    }
}
