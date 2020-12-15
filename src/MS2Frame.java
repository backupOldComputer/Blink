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
    public static void sToStep(final BufferedImage m, BufferedImage s){
	for(int i=0; i<s.getWidth(); ++i){
		for(int j=0; j<s.getHeight(); ++j){
			if(s.getRGB(i,j) != Color.WHITE.getRGB()){
				s.setRGB(i,j,0);
				continue;
			}//选区外为0，选区内为step值
			int source = m.getRGB(i,j);
			int red = (source&0xff0000)/0x10000;
			int stepRed = (256 - red)/HALF_HDP;
			if(DEBUG)System.err.print(stepRed+" ");
			s.setRGB(i, j, 0x10000*stepRed );
		}
	}
    }
    public static void main(String[] args) throws Exception {
	for(int pictureIndex=0;pictureIndex<args.length;++pictureIndex){
		String sPath = args[pictureIndex];
		String mPath = s2m(args[pictureIndex]);
		BufferedImage m = ImageIO.read(new FileInputStream(mPath));
		BufferedImage s = ImageIO.read(new FileInputStream(sPath));
		sToStep(m,s);
		if(DEBUG)ImageIO.write(s, FRAME_FORMAT, new FileOutputStream("step.png"));
		pictureToFrames(m, s);
	}
    }
    public static void pictureToFrames(BufferedImage m, final BufferedImage s) throws Exception {
	if(m.getWidth()!=s.getWidth() || m.getHeight()!=s.getHeight())
		throw new AssertionError();
	//检查宽与高后用frameIndex=0启动递归写入
	recurWrite(m, s, 0);
    }
    /** 写入第frameIndex帧到第(2*HALF_HDP-frameIndex)帧 */
    public static void recurWrite(BufferedImage m, final BufferedImage s, final int frameIndex) throws Exception {
	if(frameIndex < 0 || frameIndex > HALF_HDP)
		throw new AssertionError();
	//写入第frameIndex帧
	ImageIO.write(m, FRAME_FORMAT, SOUT);
	boolean moreBrighter = true;
	nextFrame(m,s,moreBrighter);
	//递归写入中间帧
	if(frameIndex == HALF_HDP) return; //frameIndex自增到了中间就不再展开新递归
	recurWrite(m,s,1+frameIndex);	//疑问：通过拷贝m并去掉后半程nextFrame的应该不会提高性能
	//写入第(2*HALF_HDP-frameIndex)帧
	ImageIO.write(m, FRAME_FORMAT, SOUT);
	moreBrighter = false;
	nextFrame(m,s,moreBrighter);
    }
    public static void nextFrame(BufferedImage m, final BufferedImage s, final boolean moreBrighter){
	for(int i=0; i<s.getWidth(); ++i){
		for(int j=0; j<s.getHeight(); ++j){
			int step = s.getRGB(i,j) % 0x1000000;
			m.setRGB(i, j, m.getRGB(i,j) + (moreBrighter ? 1 : -1) * step );
/*
			Color source = new Color(m.getRGB(i,j), true);
			Color dest = moreBrighter ? source.brighter() : source.darker();
			m.setRGB(i,j,dest.getRGB());
*/
		}
	}
    }
}
