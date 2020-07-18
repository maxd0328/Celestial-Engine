package celestial.data;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import celestial.error.CelestialGenericException;
import celestial.vecmath.Vector4f;

public final class ImageBuffer implements java.io.Serializable {
	
	private static final long serialVersionUID = -1086428600932046495L;
	
	private final int width;
	private final int height;
	
	private final int[] pixels;
	protected boolean locked = false;
	
	public ImageBuffer(int width, int height) {
		this.width = width;
		this.height = height;
		this.pixels = new int[width * height];
		
		for(int y = 0 ; y < height ; y++)
			for(int x = 0 ; x < width ; x++)
				pixels[y * width + x] = ((255 & 0xFF) << 24) |
                						((0 & 0xFF) << 16) |
                						((0 & 0xFF) << 8)  |
                						((0 & 0xFF) << 0);
	}
	
	public ImageBuffer(BufferedImage img) {
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.pixels = new int[width * height];
		
		for(int y = 0 ; y < height ; y++)
			for(int x = 0 ; x < width ; x++)
				pixels[y * width + x] = img.getRGB(x, y);
	}
	
	public ByteBuffer toByteBuffer() {
		ByteBuffer pixelBuffer = BufferUtils.createByteBuffer(width * height * Integer.BYTES);
		
		for(int y = 0 ; y < height ; y++) {
			for(int x = 0 ; x < width ; x++) {
				int color = getARGB(x, y);
				pixelBuffer.put((byte) ((color >> 16) & 0xFF)); //R
				pixelBuffer.put((byte) ((color >> 8) & 0xFF));  //G
				pixelBuffer.put((byte) (color & 0xFF));         //B
				pixelBuffer.put((byte) ((color >> 24) & 0xFF)); //A
			}
		}
		
		pixelBuffer.flip();
		return pixelBuffer;
	}
	
	public ByteBuffer toDirectByteBuffer() {
		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(width * height * Integer.BYTES);
		
		for(int y = 0 ; y < height ; y++) {
			for(int x = 0 ; x < width ; x++) {
				int color = getARGB(x, y);
				pixelBuffer.put((byte) ((color >> 16) & 0xFF)); //R
				pixelBuffer.put((byte) ((color >> 8) & 0xFF));  //G
				pixelBuffer.put((byte) (color & 0xFF));         //B
				pixelBuffer.put((byte) ((color >> 24) & 0xFF)); //A
			}
		}
		
		pixelBuffer.flip();
		return pixelBuffer;
	}
	
	public void setColor4i(int x, int y, int r, int g, int b, int a) {
		if(locked) throw new CelestialGenericException("Buffer has been locked by host");
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			throw new CelestialGenericException("Index out of bounds");
		pixels[y * width + x] = ((a & 0xFF) << 24) |
								((r & 0xFF) << 16) |
								((g & 0xFF) << 8)  |
								((b & 0xFF) << 0);
	}
	
	public void setColor4f(int x, int y, float r, float g, float b, float a) {
		if(locked) throw new CelestialGenericException("Buffer has been locked by host");
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			throw new CelestialGenericException("Index out of bounds");
		pixels[y * width + x] = ((((int)(a*255)) & 0xFF) << 24) |
								((((int)(r*255)) & 0xFF) << 16) |
								((((int)(g*255)) & 0xFF) << 8)  |
								((((int)(b*255)) & 0xFF) << 0);
	}
	
	public void addColor4i(int x, int y, int r, int g, int b, int a) {
		if(locked) throw new CelestialGenericException("Buffer has been locked by host");
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			throw new CelestialGenericException("Index out of bounds");
		pixels[y * width + x] = ((getARGB(x, y) >> 24) & 0xFF) + ((a & 0xFF) << 24) |
								((getARGB(x, y) >> 16) & 0xFF) + ((r & 0xFF) << 16) |
								((getARGB(x, y) >>  8) & 0xFF) + ((g & 0xFF) << 8)  |
								((getARGB(x, y) >>  0) & 0xFF) + ((b & 0xFF) << 0);
	}
	
	public void addColor4f(int x, int y, float r, float g, float b, float a) {
		if(locked) throw new CelestialGenericException("Buffer has been locked by host");
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			throw new CelestialGenericException("Index out of bounds");
		pixels[y * width + x] = ((getARGB(x, y) >> 24) & 0xFF) + ((((int)(a*255)) & 0xFF) << 24) |
								((getARGB(x, y) >> 16) & 0xFF) + ((((int)(r*255)) & 0xFF) << 16) |
								((getARGB(x, y) >>  8) & 0xFF) + ((((int)(g*255)) & 0xFF) << 8)  |
								((getARGB(x, y) >>  0) & 0xFF) + ((((int)(b*255)) & 0xFF) << 0);
	}
	
	public Vector4f getColor4f(int x, int y) {
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			throw new CelestialGenericException("Index out of bounds");
		int color = pixels[y * width + x];
		return new Vector4f(((float)((color >> 16) & 0xFF))/255f, ((float)((color >> 8) & 0xFF))/255f, ((float)((color >> 0) & 0xFF))/255f, ((float)((color >> 24) & 0xFF))/255f);
	}
	
	public void setARGB(int x, int y, int argb) {
		if(locked) throw new CelestialGenericException("Buffer has been locked by host");
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			throw new CelestialGenericException("Index out of bounds");
		this.pixels[y * width + x] = argb;
	}
	
	public int getARGB(int x, int y) {
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			throw new CelestialGenericException("Index out of bounds");
		return pixels[y * width + x];
	}
	
	public int[] getARGB() {
		if(locked) throw new CelestialGenericException("Buffer has been locked by host");
		return pixels;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
}
