package src;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    private static final boolean F_OUT_DEBUG = false;
    private static final String F_OUT_PATH = "F_OUT_DEBUG";
    private static int F_OUT_SUFFIX = 0;
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

    public static final boolean DEBUG = true;
    public static final boolean HSB_MODE = true;
    public static final int COLOR_INDEX = HSB_MODE ? 1 : 0;
    public static final int REPEAT = ( F_OUT_DEBUG ? 1 : ( DEUBG ? 2 : 3 ) );
    public static final int HALF_HDP = DEBUG ? 6 : 12;
    public static final int MAX_RED = 255-144;	//red大于MAX_RED会触发darker()
    public static final String FRAME_FORMAT = "PNG";	//需考虑ffmpeg能否解码
    public static final String FILE_SEP = "/";
    
    public static String pathS2M(String path) {
	String result = path.substring(0, path.lastIndexOf('.') );//去掉后缀
	String prefix = "pWarehouse"+FILE_SEP;
	// .replaceFirst(prefix+"S", prefix+"M");
	return result;
    }
    public static void main(String[] sPaths) {
	if(sPaths == null || sPaths.length == 0){
		System.err.println("需要参数：指示选区的掩码图片的路径集");
		return;
	}
	if(sPaths.length == 1 && sPaths[0].indexOf('*') != -1){
		System.err.println(sPaths[0] + "为空，程序退出");
		return;
	}
	if( ! HSB_MODE && MAX_RED <= 0 ) throw new AssertionError();
	for(String sp : sPaths){
		File mf = new File(pathS2M(sp));
		File sf = new File(sp);
		if( ( ! mf.exists() ) || mf.isDirectory() || ( ! sf.isFile()) ) 
			throw new AssertionError();
		try{
			BufferedImage s = ImageIO.read(sf);
			BufferedImage m = ImageIO.read(mf);
			ms2frames(m, s);
		}catch(FileNotFoundException e){
			throw new AssertionError();
		}
	}
    }
    public static void ms2frames(BufferedImage m, BufferedImage s) {
	int r = s.getWidth();
	int c = s.getHeight();
	if( r > m.getWidth() || c > m.getHeight() ) throw new AssertionError();
	int rb=-1,re=0,cb=(c-1),ce=0; // 二维for循环计算 选区 窗口 矩阵 坐标
	for(int i=0; i<r; ++i){
	    for(int j=0; j<c; ++j){
		if( isChooes( s,i,j ) ){
		    if(rb == -1) rb = i; // 首个选区像素可确定rb
		    re = i; // 最后一个选区像素可确定re
		    cb = (j<cb)?j:cb;
		    ce = (j>ce)?j:ce; 
	   	}
	    }
	}
	double[][] step = new double[re+1-rb][ce+1-cb]; // 3重循环算step
	for(int i=0; i < step.length ; ++i){
	    int x = i+rb;
	    for(int j=0; j < step[0].length ; ++j){
	    	int y = j+cb;
	    	if( ! isChooes( s,x,y ) ){
	    		step[i][j] = 0;//加色模式：选区外为0，选区内为step值
	    		continue;
	    	}
	    	Color co = new Color(m.getRGB(x,y),true);
	    	if(HSB_MODE){
	    		float[] hsbvals = new float[3];
	    		hsbvals = Color.RGBtoHSB(co.getRed(), co.getGreen()
					, co.getBlue(), hsbvals); 
			double diff = 1.0 - hsbvals[COLOR_INDEX];
	    		step[i][j] = ((diff)/HALF_HDP); 
	    	}else{
	    		co = handleTooBright(co);
	    		step[i][j] = (double)(0xff - co.getRed()) / HALF_HDP; 
	    	}
	    	m.setRGB(x, y, co.getRGB());
	    }
	}

	BufferedImage[] frames = new BufferedImage[HALF_HDP*2];
	frames[0] = m;	//首帧使用（可能被darker()处理过的）m 
/* 三重循环算出半程帧并引用于后半程 ( 以 HALF_HDP==6 , step==30时为例 ) 
 * 帧组下标： 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11（0为原图）
 * 分量深度：30,40,50,60,70,80,max,80,70,60,50,40（100为原图深度）
 */
	for(int k=1; k <= HALF_HDP ;k++){ 
		BufferedImage mClone = imClone(frames[k-1]);
		nextFrame(mClone, step, rb, cb, 1);
		frames[k] = mClone;
		frames[frames.length-k] = frames[k]; //往返闪烁
	}
	writeFrames(frames); //REPEAT次一重循环写入帧
    }
    /** 此方法会修改m */
    public static void nextFrame(BufferedImage m, final double[][] step, int rb, int cb, final int addOrSub){
	WritableRaster wrm = m.getRaster();
	int h = step[0].length;
	for(int i=0; i < step.length ; ++i){
		if( step[i].length != h )	//断言不是锯齿数组
			throw new AssertionError();
		int x = i+rb;
		for(int j=0; j < h ; ++j){
			int y = j+cb;
			int[] iArray = new int[3];
			wrm.getPixel(x,y,iArray);
			if(HSB_MODE){
				float[] hsbvals = new float[3];
				hsbvals = Color.RGBtoHSB(iArray[0],iArray[1],
						iArray[2],hsbvals); 
				hsbvals[COLOR_INDEX] += addOrSub*(step[i][j]);
				if(hsbvals[COLOR_INDEX] > 1.0)
					hsbvals[COLOR_INDEX] = 1.0f;
				m.setRGB(x,y, Color.HSBtoRGB(hsbvals[0],
							hsbvals[1],hsbvals[2]));
			}else{
				iArray[COLOR_INDEX] += (int)addOrSub*step[i][j];
				wrm.setPixel( x,y , iArray );
			}
		}
	}
    }
    public static void writeFrames(BufferedImage[] frames){
	try{
		for(int t=0;t<REPEAT;++t){
			for(int f=0;f<frames.length;++f)
				ImageIO.write(frames[f], FRAME_FORMAT, sOut());
		}
	}catch(IOException ex){
		throw new AssertionError();
	}
    }
    public static BufferedImage imClone(BufferedImage from) {
	BufferedImage to = new BufferedImage(from.getWidth(), from.getHeight(), from.getType());
        to.setData(from.getData());
	return to;
    }
    /** 当前版本只有 s 中的纯白像素才是选区 */
    public static boolean isChooes(BufferedImage s, int x, int y) {
	return s.getRGB(x,y) == Color.WHITE.getRGB();
    }
    public static Color handleTooBright(Color mc){
	while( mc.getRed() > MAX_RED )
		mc = mc.darker();
	return mc;
    }
}
