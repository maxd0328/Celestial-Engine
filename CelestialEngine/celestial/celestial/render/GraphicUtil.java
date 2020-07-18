package celestial.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import celestial.error.CelestialGLException;

public final class GraphicUtil {
	
	public static final int GL_CULLING           = 4;
	public static final int GL_CULL_BACKFACE     = 5;
	public static final int GL_CULL_FRONTFACE    = 6;
	public static final int GL_ALPHA_BLENDING    = 7;
	public static final int GL_ADDITIVE_BLENDING = 8;
	public static final int GL_ANTIALIASING      = 9;
	public static final int GL_DEPTH_TESTING     = 10;
	public static final int GL_DEPTH_MASK        = 19;
	public static final int GL_CLIP_DISTANCE0    = -40;
	public static final int GL_CLIP_DISTANCE1    = -39;
	public static final int GL_CLIP_DISTANCE2    = -38;
	public static final int GL_CLIP_DISTANCE3    = -37;
	public static final int GL_CLIP_DISTANCE4    = -36;
	public static final int GL_CLIP_DISTANCE5    = -35;
	public static final int GL_CLIP_DISTANCE6    = -34;
	public static final int GL_CLIP_DISTANCE7    = -33;
	public static final int GL_BLENDING          = -41;
	
	public static void enable(int pname) {
		set(pname, true);
	}
	
	public static void disable(int pname) {
		set(pname, false);
	}
	
	private static void set(int pname, boolean val) {
		switch(pname) {
		case GL_CULLING:
			if(val) GL11.glEnable(GL11.GL_CULL_FACE);
			else GL11.glDisable(GL11.GL_CULL_FACE);
		case GL_CULL_BACKFACE:
			if(val) {
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glCullFace(GL11.GL_BACK);
			} else
				GL11.glDisable(GL11.GL_CULL_FACE);
			break;
		case GL_CULL_FRONTFACE:
			if(val) {
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glCullFace(GL11.GL_FRONT);
			} else
				GL11.glDisable(GL11.GL_CULL_FACE);
			break;
		case GL_ALPHA_BLENDING:
			if(val) {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			} else
				GL11.glDisable(GL11.GL_BLEND);
			break;
		case GL_ADDITIVE_BLENDING:
			if(val) {
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			} else
				GL11.glDisable(GL11.GL_BLEND);
			break;
		case GL_ANTIALIASING:
			if(val)
				GL11.glEnable(GL13.GL_MULTISAMPLE);
			else
				GL11.glDisable(GL13.GL_MULTISAMPLE);
			break;
		case GL_DEPTH_TESTING:
			if(val)
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			else
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			break;
		case GL_DEPTH_MASK:
			GL11.glDepthMask(val);
			break;
		case GL_CLIP_DISTANCE0:
		case GL_CLIP_DISTANCE1:
		case GL_CLIP_DISTANCE2:
		case GL_CLIP_DISTANCE3:
		case GL_CLIP_DISTANCE4:
		case GL_CLIP_DISTANCE5:
		case GL_CLIP_DISTANCE6:
		case GL_CLIP_DISTANCE7:
			if(val)
				GL11.glEnable(GL30.GL_CLIP_DISTANCE0 + (pname - GL_CLIP_DISTANCE0));
			else
				GL11.glDisable(GL30.GL_CLIP_DISTANCE0 + (pname - GL_CLIP_DISTANCE0));
			break;
		case GL_BLENDING:
			if(val)
				GL11.glEnable(GL11.GL_BLEND);
			else
				GL11.glDisable(GL11.GL_BLEND);
			break;
		default:
			throw new CelestialGLException("Invalid operand");
		}
	}
	
}
