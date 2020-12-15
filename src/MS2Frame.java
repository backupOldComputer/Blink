package src;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final boolean DEBUG = true;
    public static final OutputStream SOUT = System.out;	//new FileOutputStream();	//
    public static final String FRAME_FORMAT = "PNG"; //我的ffmpeg不支持tiff
    public static final int HALF_HDP = 4;
    public static String s2m(String path) {
	return path.substring(0, path.length() - 4 );
	//return path.replaceFirst("pWarehouse/S", "pWarehouse/M");
    }
    public static int[][] sToStep(final BufferedImage m, final BufferedImage s){
	int[][] step = new int[s.getWidth()][s.getHeight()];
	for(int i=0; i<s.getWidth(); ++i){
		for(int j=0; j<s.getHeight(); ++j){
			if(s.getRGB(i,j) != Color.WHITE.getRGB()){
				step[i][j] = 0;
				continue;
			}//选区外为0，选区内为step值
			int source = m.getRGB(i,j);
			int red = (source&0xff0000)/0x10000;
			int stepRed = (255 - red)/HALF_HDP;	//256做被减数会导致颜色抖动
			if(DEBUG)System.err.print(stepRed+" ");
			step[i][j] = 0x10000*stepRed;
		}
	}
	return step;
    }
    public static void main(String[] args) throws Exception {
	for(int pictureIndex=0;pictureIndex<args.length;++pictureIndex){
		String sPath = args[pictureIndex];
		String mPath = s2m(args[pictureIndex]);
		BufferedImage m = ImageIO.read(new FileInputStream(mPath));
		BufferedImage s = ImageIO.read(new FileInputStream(sPath));
		int[][] step = sToStep(m,s);
		if(DEBUG)ImageIO.write(s, FRAME_FORMAT, new FileOutputStream("step.png"));
		pictureToFrames(m, step);
	}
    }
    public static void pictureToFrames(BufferedImage m, final int[][] step) throws Exception {
//	if(m.getWidth()!=s.getWidth() || m.getHeight()!=s.getHeight())throw new AssertionError();
	//检查宽与高后用frameIndex=0启动递归写入
	recurWrite(m, step, 0);
    }
    /** 写入第frameIndex帧到第(2*HALF_HDP-frameIndex-1)帧 */
    public static void recurWrite(BufferedImage m, final int[][] step, int frameIndex) throws Exception {
	if(frameIndex == HALF_HDP) return; //frameIndex自增到了中间就不再展开新递归
	if(frameIndex < 0 || frameIndex > HALF_HDP)
		throw new AssertionError();
	//写入第frameIndex帧
	ImageIO.write(m, FRAME_FORMAT, SOUT);
	boolean moreBrighter = true;
	nextFrame(m,step,moreBrighter);
	//递归写入中间帧
	recurWrite(m,step,1+frameIndex);	//疑问：通过拷贝m并去掉后半程nextFrame的应该不会提高性能
	//写入第(2*HALF_HDP-frameIndex)帧
	ImageIO.write(m, FRAME_FORMAT, SOUT);
	moreBrighter = false;
	nextFrame(m,step,moreBrighter);
    }
    public static void nextFrame(BufferedImage m, final int[][] step, final boolean moreBrighter){
	for(int i=0; i<step.length; ++i){
		for(int j=0; j<step[0].length; ++j){
			m.setRGB(i, j, m.getRGB(i,j) + (moreBrighter ? 1 : -1) * step[i][j] );
		}
	}
    }
}
