package src;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final boolean DEBUG = false;
    public static final int HALF_HDP = DEBUG ? 4 : 12;
    public static final String FRAME_FORMAT = "PNG"; //我的ffmpeg不支持tiff
    public static final OutputStream SOUT = System.out;	//new FileOutputStream();	//
    public static String pathTranslate(String path) {
	String result = path.substring(0, path.length() - 4 );
	// path.replaceFirst("pWarehouse/S", "pWarehouse/M");
	if(new File(result).isFile()) 
		return result;
	else
		throw new AssertionError();
    }
    public static int[][] sToStep(final BufferedImage m, final BufferedImage s){
	int[][] step = new int[s.getWidth()][s.getHeight()];
	for(int i=0; i<s.getWidth(); ++i){
		for(int j=0; j<s.getHeight(); ++j){
			if(s.getRGB(i,j) != java.awt.Color.WHITE.getRGB()){
				step[i][j] = 0;
				continue;
			}//选区外为0，选区内为step值
			int source = m.getRGB(i,j);
			int red = (source&0xff0000)/0x10000;
			int stepRed = (255 - red)/HALF_HDP;	//256做被减数有时会导致颜色抖动
//			if(DEBUG)System.err.print(stepRed+"#");
			step[i][j] = 0x10000*stepRed;
		}
	}
	return step;
    }
    public static void main(String[] sPaths) throws Exception {
	for(int k=0;k< sPaths.length ;++k){
		BufferedImage s = ImageIO.read(new FileInputStream(sPaths[k]));
		BufferedImage m = ImageIO.read(new FileInputStream(pathTranslate(sPaths[k])));
		if( s.getWidth() > m.getWidth() || s.getHeight() > m.getHeight() )
			throw new AssertionError();
		int[][] step = sToStep(m,s);
//		if(DEBUG)ImageIO.write(s, FRAME_FORMAT, new FileOutputStream("step.png"));
		pictureToFrames(m, step);
	}
    }
    public static void pictureToFrames(BufferedImage m, final int[][] step) throws Exception {
	recurWrite(m, step, 0); //用frameIndex=0启动递归写入
    }
    /** 写入第frameIndex帧到第(2*HALF_HDP-frameIndex-1)帧 */
    public static void recurWrite(BufferedImage m, final int[][] step, final int frameIndex) throws Exception {
	if(frameIndex == HALF_HDP) return; //frameIndex自增到了中间就不再展开新递归
	if(frameIndex < 0 || frameIndex > HALF_HDP)
		throw new AssertionError();
	//写入第frameIndex帧
	ImageIO.write(m, FRAME_FORMAT, SOUT);
	nextFrame(m, step, 1);
	//递归写入中间帧
	recurWrite(m, step, 1+frameIndex);	//疑问：通过拷贝m并去掉后半程nextFrame的应该不会提高性能
	//写入第(2*HALF_HDP-frameIndex-1)帧
	ImageIO.write(m, FRAME_FORMAT, SOUT);
	if(frameIndex != 0) nextFrame(m, step, -1);
    }
    public static void nextFrame(BufferedImage m, final int[][] step, final int addOrSub){
	for(int i=0; i<step.length; ++i)
		for(int j=0; j<step[0].length; ++j)
			m.setRGB( i, j, m.getRGB(i,j) + addOrSub * step[i][j] );
    }
}
