package studio.celestial.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.DataManager;
import celestial.data.DataManager.StaticDataManager;
import celestial.glutil.GLDisplayMode;
import celestial.glutil.GLViewport;
import celestial.serialization.SerializerImpl;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;
import studio.celestial.binding.DirectBinding;
import studio.celestial.binding.PropertyBinding;

public final class ProjectConfigurations implements java.io.Serializable {
	
	private static final long serialVersionUID = 3809054321690409204L;
	
	private final ProjectConfigurationBean configBean;
	private transient ArrayList<PropertyBinding<?>> bindings;
	private boolean paused;
	
	public ProjectConfigurations(ProjectConfigurationBean configBean) {
		this.configBean = configBean;
		this.paused = false;
		setup();
	}
	
	public ProjectConfigurationBean getConfigBean() {
		return configBean;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public void pause() {
		this.paused = true;
	}
	
	public void resume() {
		this.paused = false;
	}
	
	public void update() {
		for(PropertyBinding<?> binding : bindings) binding.update();
	}
	
	private void setup() {
		this.bindings = new ArrayList<PropertyBinding<?>>();
		this.bindings.add(new DirectBinding<Boolean>(() -> paused, s -> paused = s, () -> GLRequestSystem.getRenderer().isPaused(),
				s -> GLRequestSystem.getRenderer().setPaused(s)).withControlThread(PropertyBinding.GL_THREAD_ID));
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		setup();
	}
	
	public static final class ProjectConfigurationBean implements java.io.Serializable {
		
		private static final long serialVersionUID = -3062161543258827708L;
		
		private String projectName;
		private Property<Vector3f> clearColor;
		private GLDisplayMode displayMode;
		private GLViewport viewport;
		private Factory<? extends DataManager> dataManager;
		private int glMajVer;
		private int glMinVer;
		private boolean forwardCompatibility;
		private boolean profileCore;
		private int sampleCount;
		private int bitDepth;
		private int fpsSync;
		private String title;
		private boolean vsync;
		private boolean showMouse;
		private boolean resizable;
		
		public ProjectConfigurationBean() {
			this("New Project", Properties.createVec3Property(new Vector3f(0.3f)), new GLDisplayMode(800, 600, false), new GLViewport(0, 0, 1, 1),
					StaticDataManager.FACTORY, 3, 2, true, true, 4, 24, 60, "Game", false, true, true);
		}
		
		public ProjectConfigurationBean(String projectName, Property<Vector3f> clearColor, GLDisplayMode displayMode,
				GLViewport viewport, Factory<? extends DataManager> dataManager, int glMajVer, int glMinVer,
				boolean forwardCompatibility, boolean profileCore, int sampleCount, int bitDepth, int fpsSync, String title,
				boolean vsync, boolean showMouse, boolean resizable) {
			this.projectName = projectName;
			this.clearColor = clearColor;
			this.displayMode = displayMode;
			this.viewport = viewport;
			this.dataManager = dataManager;
			this.glMajVer = glMajVer;
			this.glMinVer = glMinVer;
			this.forwardCompatibility = forwardCompatibility;
			this.profileCore = profileCore;
			this.sampleCount = sampleCount;
			this.bitDepth = bitDepth;
			this.fpsSync = fpsSync;
			this.title = title;
			this.vsync = vsync;
			this.showMouse = showMouse;
			this.resizable = resizable;
		}
		
		public String getProjectName() {
			return projectName;
		}
		
		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}
		
		public Property<Vector3f> getClearColor() {
			return clearColor;
		}
		
		public void setClearColor(Property<Vector3f> clearColor) {
			this.clearColor = clearColor;
		}
		
		public GLDisplayMode getDisplayMode() {
			return displayMode;
		}
		
		public void setDisplayMode(GLDisplayMode displayMode) {
			this.displayMode = displayMode;
		}
		
		public GLViewport getViewport() {
			return viewport;
		}
		
		public void setViewport(GLViewport viewport) {
			this.viewport = viewport;
		}
		
		public Factory<? extends DataManager> getDataManager() {
			return dataManager;
		}
		
		public void setDataManager(Factory<? extends DataManager> dataManager) {
			this.dataManager = dataManager;
		}
		
		public int getGlMajVer() {
			return glMajVer;
		}
		
		public void setGlMajVer(int glMajVer) {
			this.glMajVer = glMajVer;
		}
		
		public int getGlMinVer() {
			return glMinVer;
		}
		
		public void setGlMinVer(int glMinVer) {
			this.glMinVer = glMinVer;
		}
		
		public boolean isForwardCompatibility() {
			return forwardCompatibility;
		}
		
		public void setForwardCompatibility(boolean forwardCompatibility) {
			this.forwardCompatibility = forwardCompatibility;
		}
		
		public boolean isProfileCore() {
			return profileCore;
		}
		
		public void setProfileCore(boolean profileCore) {
			this.profileCore = profileCore;
		}
		
		public int getSampleCount() {
			return sampleCount;
		}
		
		public void setSampleCount(int sampleCount) {
			this.sampleCount = sampleCount;
		}
		
		public int getBitDepth() {
			return bitDepth;
		}
		
		public void setBitDepth(int bitDepth) {
			this.bitDepth = bitDepth;
		}
		
		public int getFpsSync() {
			return fpsSync;
		}
		
		public void setFpsSync(int fpsSync) {
			this.fpsSync = fpsSync;
		}
		
		public String getTitle() {
			return title;
		}
		
		public void setTitle(String title) {
			this.title = title;
		}
		
		public boolean isVsync() {
			return vsync;
		}
		
		public void setVsync(boolean vsync) {
			this.vsync = vsync;
		}
		
		public boolean isShowMouse() {
			return showMouse;
		}
		
		public void setShowMouse(boolean showMouse) {
			this.showMouse = showMouse;
		}
		
		public boolean isResizable() {
			return resizable;
		}
		
		public void setResizable(boolean resizable) {
			this.resizable = resizable;
		}
		
	}
	
}
