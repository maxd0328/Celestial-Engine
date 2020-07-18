package celestial.core;

import java.awt.Canvas;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

import celestial.data.ColorDepth;
import celestial.data.DataManager;
import celestial.data.FrameBuffer;
import celestial.error.CelestialContextException;
import celestial.error.CelestialGLException;
import celestial.error.CelestialGenericException;
import celestial.glutil.GLDisplayMode;
import celestial.glutil.GLViewport;
import celestial.shader.Shader;
import celestial.vecmath.Vector3f;

public final class EngineRuntime {
	
	public static final int DATATYPE_VERTEX_ARRAYS  = 9932;
	public static final int DATATYPE_ARRAY_ELEMENTS = 9933;
	
	public static final int DRAWTYPE_TRIANGLES      = 9934;
	public static final int DRAWTYPE_TRIANGLE_STRIP = 9935;
	public static final int DRAWTYPE_TRIANGLE_FAN   = 9936;
	
	private EngineRuntime() {}
	
	private static GLDisplayMode dispMode = null;
	private static GLViewport viewport = null;
	private static boolean created;
	
	private static DataManager manager;
	
	private static FrameBuffer postProcessingBuffer;
	
	public static void create(GLDisplayMode dispMode, GLViewport viewport, int glMajVer, int glMinVer,
			boolean forwardCompatible, boolean profileCore, int sampleCount, int depthBits, DataManager manager) {
		if(dispMode == null || viewport == null || manager == null) throw new CelestialGenericException("One or more parameters is null");
		if(created) throw new CelestialGLException("Display already created");
		
		EngineRuntime.manager = manager;
		
		try {
			ContextAttribs attribs = new ContextAttribs(glMajVer, glMinVer).withForwardCompatible(forwardCompatible).withProfileCore(profileCore);
			
			if(dispMode.isFullscreen() && !dispMode.isFullscreenCapable()) throw new CelestialContextException("Invalid display mode: " + dispMode);
			Display.setDisplayMode(dispMode.isFullscreen() ? dispMode.getFSDisplay() : new DisplayMode(dispMode.getWidth(), dispMode.getHeight()));
			if(dispMode.isFullscreen()) Display.setFullscreen(true);
			
			Display.create(new PixelFormat().withSamples(sampleCount).withDepthBits(depthBits), attribs);
			GL11.glEnable(GL13.GL_MULTISAMPLE);
			
			GL11.glViewport(viewport.fitMinX(dispMode.getWidth()), viewport.fitMinY(dispMode.getHeight()),
					viewport.fitMaxX(dispMode.getWidth()), viewport.fitMaxY(dispMode.getHeight()));
			
			AL.create();
			AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
			
			postProcessingBuffer = FrameBuffer.create(dispMode.getWidth(), dispMode.getHeight(), false, false, ColorDepth.RGBA16_HDR);
			
			SystemInput.init();
			
			EngineRuntime.dispMode = dispMode;
			EngineRuntime.viewport = viewport;
			created = true;
			lastFrameTime = Sys.getTime() * 1000 / Sys.getTimerResolution();
		}
		catch(LWJGLException ex) {
			throw new CelestialContextException(ex);
		}
		
	}
	
	private static float delta;
	private static long lastFrameTime;
	public static void update(int fpsCap) {
		Display.update();
		Display.sync(fpsCap);
		
		SystemInput.update();
		long currentFrameTime = timeMsec();
		delta = (currentFrameTime - lastFrameTime) / 1000f;
		lastFrameTime = currentFrameTime;
		if(dispMode.getWidth() != Display.getWidth() || dispMode.getHeight() != Display.getHeight()) {
			dispMode = new GLDisplayMode(Display.getWidth(), Display.getHeight(), dispMode.isFullscreen());
			dispAlignViewport();
		}
	}
	
	public static void clear(Vector3f screen) {
		GL11.glClearColor(screen.x, screen.y, screen.z, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	public static float frameTime() {
		return delta;
	}
	
	public static float frameTimeRelative() {
		return delta * 60;
	}
	
	public static long timeMsec() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}
	
	public static long timeNsec() {
		return Sys.getTime() * 1000000000L / Sys.getTimerResolution();
	}
	
	public static void destroy() {
		Shader.destroyShaders();
		if(manager != null) manager.destroy();
		Display.destroy();
		AL.destroy();
	}
	
	public static void draw(int dataType, int drawType, int first, int count) {
		if(dataType == DATATYPE_VERTEX_ARRAYS) {
			if(drawType == DRAWTYPE_TRIANGLES)
				GL11.glDrawArrays(GL11.GL_TRIANGLES, first, count);
			else if(drawType == DRAWTYPE_TRIANGLE_STRIP)
				GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, first, count);
			else if(drawType == DRAWTYPE_TRIANGLE_FAN)
				GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, first, count);
			else
				throw new CelestialGenericException("Invalid argtype");
		}
		else if(dataType == DATATYPE_ARRAY_ELEMENTS) {
			if(drawType == DRAWTYPE_TRIANGLES)
				GL11.glDrawElements(GL11.GL_TRIANGLES, count, GL11.GL_UNSIGNED_INT, first);
			else if(drawType == DRAWTYPE_TRIANGLE_STRIP)
				GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, count, GL11.GL_UNSIGNED_INT, first);
			else if(drawType == DRAWTYPE_TRIANGLE_FAN)
				GL11.glDrawElements(GL11.GL_TRIANGLE_FAN, count, GL11.GL_UNSIGNED_INT, first);
			else
				throw new CelestialGenericException("Invalid argtype");
		}
		else
			throw new CelestialGenericException("Invalid argtype");
		return;
	}
	
	public static DataManager getDataManager() {
		return manager;
	}
	
	public static boolean isCloseRequested() {
		return Display.isCloseRequested();
	}
	
	public static void dispSetParent(Canvas parent) {
		try {
			Display.setParent(parent);
		}
		catch(LWJGLException ex) {
			throw new CelestialContextException(ex.getMessage());
		}
	}
	
	public static void dispShowMouse(boolean arg0) {
		Mouse.setGrabbed(!arg0);
	}
	
	public static void dispSetVSync(boolean arg0) {
		Display.setVSyncEnabled(arg0);
	}
	
	public static void dispSetResizable(boolean arg0) {
		Display.setResizable(arg0);
	}
	
	public static void dispSetTitle(String title) {
		Display.setTitle(title);
	}
	
	public static GLDisplayMode dispGetDisplayMode() {
		if(!created) throw new CelestialContextException("Display must be created before modifying");
		return dispMode;
	}
	
	public static GLViewport dispGetViewport() {
		if(!created) throw new CelestialContextException("Display must be created before modifying");
		return viewport;
	}
	
	public static void dispAlignViewport() {
		if(!created) throw new CelestialContextException("Display must be created before modifying");
		GL11.glViewport(viewport.fitMinX(dispMode.getWidth()), viewport.fitMinY(dispMode.getHeight()),
				viewport.fitMaxX(dispMode.getWidth()), viewport.fitMaxY(dispMode.getHeight()));
	}
	
	public static void dispOverrideViewport(int x, int y, int width, int height) {
		if(!created) throw new CelestialContextException("Display must be created before modifying");
		GL11.glViewport(x, y, width, height);
	}
	
	public static void dispResize(GLDisplayMode dispMode) {
		if(!created) throw new CelestialContextException("Display must be created before modifying");
		try {
			if(dispMode.isFullscreen() && !dispMode.isFullscreenCapable()) throw new CelestialContextException("Invalid display mode: " + dispMode);
			Display.setDisplayMode(dispMode.isFullscreen() ? dispMode.getFSDisplay() : new DisplayMode(dispMode.getWidth(), dispMode.getHeight()));
			if(dispMode.isFullscreen()) Display.setFullscreen(true);
			postProcessingBuffer = FrameBuffer.create(dispMode.getWidth(), dispMode.getHeight(), false, false, ColorDepth.RGBA16_HDR);
			dispAlignViewport();
			EngineRuntime.dispMode = dispMode;
		}
		catch(LWJGLException ex) {
			throw new CelestialContextException(ex);
		}
	}
	
	public static FrameBuffer getPostProcessingBuffer() {
		return postProcessingBuffer;
	}
	
}
