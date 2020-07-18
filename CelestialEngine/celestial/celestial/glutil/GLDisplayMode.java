package celestial.glutil;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import celestial.error.CelestialGenericException;

public final class GLDisplayMode implements java.io.Serializable {
	
	private static final long serialVersionUID = -1537672581738792185L;
	
	private final int width, height;
	private final boolean fullscreen;
	
	public GLDisplayMode(int width, int height, boolean fullscreen) {
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean isFullscreen() {
		return fullscreen;
	}
	
	public boolean isFullscreenCapable() {
		return getFSDisplay() != null;
	}
	
	public DisplayMode getFSDisplay() {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			for(int i = 0 ; i < modes.length ; ++i) {
				if(modes[i].getWidth() == this.width && modes[i].getHeight() == this.height && modes[i].isFullscreenCapable()) return modes[i];
			}
			return null;
		} catch (LWJGLException e) {
			throw new CelestialGenericException(e);
		}
	}
	
	@Override
	public String toString() {
		return String.format("GLDisplayMode:[(%d, %d)]", width, height);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GLDisplayMode)) return false;
		GLDisplayMode g = (GLDisplayMode) o;
		return width == g.width && height == g.height && fullscreen == g.fullscreen;
	}
	
	public static GLDisplayMode[] getResolutions() {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			GLDisplayMode[] outModes = new GLDisplayMode[modes.length];
			for(int i = 0 ; i < modes.length ; ++i) outModes[i] = new GLDisplayMode(modes[i].getWidth(), modes[i].getHeight(), true);
			return outModes;
		} catch (LWJGLException e) {
			throw new CelestialGenericException(e);
		}
	}
	
	public static GLDisplayMode changeWidth(GLDisplayMode displayMode, int width) {
		return new GLDisplayMode(width, displayMode.height, displayMode.fullscreen);
	}
	
	public static GLDisplayMode changeHeight(GLDisplayMode displayMode, int height) {
		return new GLDisplayMode(displayMode.width, height, displayMode.fullscreen);
	}
	
	public static GLDisplayMode changeFullscreen(GLDisplayMode displayMode, boolean fullscreen) {
		return new GLDisplayMode(displayMode.width, displayMode.height, fullscreen);
	}
	
}
