package src;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final OutputStream sout = System.out;
    public static final String FRAME_FORMAT = "PNG"; //我的ffmpeg不支持tiff
    public static final int HALF_HDP = 8;
    public static String s2m(String path){
	return path.substring(0, path.length() - 4 );
	//return path.replaceFirst("pWarehouse/S", "pWarehouse/M");
    }
    public static void main(String[] args) throws Exception {
	for(int pictureIndex=0;pictureIndex<args.length;++pictureIndex){
		String sPath = args[pictureIndex];
		String mPath = s2m(args[pictureIndex]);
		BufferedImage[] frames = new BufferedImage[2*HALF_HDP];
		frames[0] = ImageIO.read(new FileInputStream(mPath));
		BufferedImage s = ImageIO.read(new FileInputStream(sPath));
		if(frames[0].getWidth()!=s.getWidth() || frames[0].getHeight()!=s.getHeight())
			throw new AssertionError();
	        for(int frameIndex=1; frameIndex<=HALF_HDP; ++frameIndex){
			frames[frameIndex] = ImageIO.read(new FileInputStream(mPath));
			computeFrame(frames[frameIndex], s, frameIndex);
			//TODO:及时把输出流传给ffmpeg
			frames[2*HALF_HDP-frameIndex] = frames[frameIndex];
		}
		for(int k=0;k<frames.length;++k)
			ImageIO.write(frames[k], FRAME_FORMAT, System.out);
		}
    }
    /** 写入第frameIndex帧到第(2*HALF_HDP-frameIndex)帧
    */
    public static void recurWrite(BufferedImage m, final BufferedImage s, int frameIndex) throws Exception {
	if(frameIndex < 0 || frameIndex >= HALF_HDP)
		throw new AssertionError();
	//写入第frameIndex帧
	ImageIO.write(m, FRAME_FORMAT, sout);
	boolean moreBrighter = true;
	nextFrame(m,s,moreBrighter);
	//写入中间帧
	++frameIndex;
	if(frameIndex == HALF_HDP) return; //frameIndex自增到了中间就不再展开新递归
	recurWrite(m,s,frameIndex);
	//写入第(2*HALF_HDP-frameIndex)帧
	ImageIO.write(m, FRAME_FORMAT, sout);
	moreBrighter = false;
	nextFrame(m,s,moreBrighter);
    }
    public static BufferedImage cloneBufferedImage(final BufferedImage from){
	return null;//TODO
    }
    public static void nextFrame(BufferedImage m, final BufferedImage s, final boolean moreBrighter){
	for(int i=0; i<s.getWidth(); ++i){
		for(int j=0; j<s.getHeight(); ++j){
			if(s.getRGB(i,j) != Color.WHITE.getRGB())
				continue;
			Color source = new Color(m.getRGB(i,j), true);
			Color dest = moreBrighter ? source.brighter() : source.darker();
			m.setRGB(i,j,dest.getRGB());
		}
	}
    }
    public static void computeFrame(BufferedImage dest, final BufferedImage s, final int frameIndex){
	for(int i=0; i<s.getWidth(); ++i){
		for(int j=0; j<s.getHeight(); ++j){
			if(s.getRGB(i,j) != Color.WHITE.getRGB())
				continue;
			int source = dest.getRGB(i,j);
			dest.setRGB(i,j,source+0x80000*frameIndex);//new Color(source, true).brighter().getRGB());
		}
	}
    }
}
