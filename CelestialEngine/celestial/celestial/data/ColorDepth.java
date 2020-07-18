package celestial.data;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public enum ColorDepth implements java.io.Serializable {
	
	RGBA8_LDR,
	
	RGBA16_HDR,
	
	RGBA32_HDR;
	
	public int toGL() {
		switch(this) {
		case RGBA8_LDR:
			return GL11.GL_RGBA8;
		case RGBA16_HDR:
			return GL30.GL_RGBA16F;
		case RGBA32_HDR:
			return GL30.GL_RGBA32F;
		}
		return GL11.GL_RGBA8;
	}
	
}
