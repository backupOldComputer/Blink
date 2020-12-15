package src;
import java.io.*;
import java.awt.Color;
import java.awt.image.*;
import javax.imageio.*;

public class MS2Frame{
    public static final String FRAME_FORMAT = "PNG"; //我的ffmpeg不支持tiff
    public static final int HALF_HDP = 8;
    public static void main(String[] args) throws Exception {
	for(int i=0;i<args.length;++i){
		BufferedImage s = ImageIO.read(new FileInputStream(args[i]));
		BufferedImage m = ImageIO.read(new FileInputStream(s2m(args[i])));
		ms2Frame(m,s);
	}
    }
    public static String s2m(String path){
	return path.substring(0, path.length() - 4 );
	//return path.replaceFirst("pWarehouse/S", "pWarehouse/M");
    }
    public static void ms2Frame(BufferedImage m, BufferedImage s) throws Exception {
	if( m.getWidth() != s.getWidth() || m.getHeight() != s.getHeight())
		throw new AssertionError();
	ImageIO.write(m, FRAME_FORMAT, System.out);	//每张图生成(HALF_HDP*2+1)帧
	
        for(int frameIndex=1; frameIndex<=HALF_HDP; ++frameIndex){
		for(int i=0; i<s.getWidth(); ++i){
			for(int j=0; j<s.getHeight(); ++j){
				if(s.getRGB(i,j) != Color.WHITE.getRGB())
					continue;
				int source = m.getRGB(i,j);
				m.setRGB(i,j,new Color(source, true).brighter().getRGB());
			}
		}
		ImageIO.write(m, FRAME_FORMAT, System.out);
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
    }
}
