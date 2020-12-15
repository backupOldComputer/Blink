package src;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final String FRAME_FORMAT = "PNG"; //我的ffmpeg不支持tiff
    public static final int HALF_HDP = 8;
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
			for(int i=0; i<s.getWidth(); ++i){
				for(int j=0; j<s.getHeight(); ++j){
					if(s.getRGB(i,j) != Color.WHITE.getRGB())
						continue;
					int source = frames[frameIndex].getRGB(i,j);
					frames[frameIndex].setRGB(i,j,source+0x80000*frameIndex);//new Color(source, true).brighter().getRGB());
				}
			}
			frames[2*HALF_HDP-frameIndex] = frames[frameIndex];
		}
		for(int k=0;k<frames.length;++k)
			ImageIO.write(frames[k], FRAME_FORMAT, System.out);
		}
    }
    public static String s2m(String path){
	return path.substring(0, path.length() - 4 );
	//return path.replaceFirst("pWarehouse/S", "pWarehouse/M");
    }
/*
    public static void ms2Frame(BufferedImage m, BufferedImage s) throws Exception {
	if( m.getWidth() != s.getWidth() || m.getHeight() != s.getHeight())
		throw new AssertionError();
	BufferedImage[] frames = new BufferedImage[2*HALF_HDP];//每张图生成(HALF_HDP*2)帧
//	frames[0] = m.clone();
        for(int frameIndex=1; frameIndex<=HALF_HDP; ++frameIndex){
		frames[frameIndex] = ImageIO.read(new FileInputStream(mPath));
		for(int i=0; i<s.getWidth(); ++i){
			for(int j=0; j<s.getHeight(); ++j){
				if(s.getRGB(i,j) != Color.WHITE.getRGB())
					continue;
				int source = m.getRGB(i,j);
				m.setRGB(source+0x20000);//i,j,new Color(source, true).brighter().getRGB());
			}
		}
		frames[2*HALF_HDP-frameIndex] = frames[i];
	//	ImageIO.write(m, FRAME_FORMAT, System.out);
	}
        for(int frameIndex=1; frameIndex<=HALF_HDP; ++frameIndex){
		for(int i=0; i<m.getWidth(); ++i){
			for(int j=0; j<m.getHeight(); ++j){
				if(s.getRGB(i,j) != Color.WHITE.getRGB())
					continue;
				int source = m.getRGB(i,j);
				m.setRGB(i,j,new Color(source, true).darker().getRGB());
			}
		}
		ImageIO.write(m, FRAME_FORMAT, System.out);
	}
	
	for(int k=0;k<frames.length;++k)
		ImageIO.write(m, FRAME_FORMAT, System.out);
    }*/
}
