package celestial.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import celestial.beans.property.SelectiveProperty.PropertySelection;
import celestial.core.EngineRuntime;
import celestial.error.CelestialGLException;
import celestial.glutil.GLDisplayMode;
import celestial.render.RenderOutput;
import celestial.serialization.SerializerImpl;

public final class FrameBuffer implements Sampler, GLData {
	
	private static final long serialVersionUID = 7950166587316641788L;
	
	@SuppressWarnings("unchecked")
	public static final PropertySelection<GLDisplayMode>[] RECOMMENDED_DISPLAY_MODES = new PropertySelection[] {
			new PropertySelection<GLDisplayMode>("160x90", new GLDisplayMode(160, 90, false)),
			new PropertySelection<GLDisplayMode>("320x180", new GLDisplayMode(320, 180, false)),
			new PropertySelection<GLDisplayMode>("640x360", new GLDisplayMode(640, 360, false)),
			new PropertySelection<GLDisplayMode>("800x600", new GLDisplayMode(800, 600, false)),
			new PropertySelection<GLDisplayMode>("1024x768", new GLDisplayMode(1024, 768, false)),
			new PropertySelection<GLDisplayMode>("1280x720", new GLDisplayMode(1280, 720, false)),
			new PropertySelection<GLDisplayMode>("1600x900", new GLDisplayMode(1600, 900, false)),
			new PropertySelection<GLDisplayMode>("1920x1080", new GLDisplayMode(1920, 1080, false))
	};
	
	private final int width, height;
	private final boolean noColorTexture, useDepthBuffer;
	private final ColorDepth bitDepth;
	private transient int textureID = 0, depthID = 0, bufferID = 0, dbufID = 0;
	private boolean toggle = false;
	
	protected FrameBuffer(int width, int height, boolean noColorTexture, boolean noDepthTexture) {
		this(width, height, noColorTexture, noDepthTexture, ColorDepth.RGBA16_HDR);
	}
	
	protected FrameBuffer(int width, int height, boolean noColorTexture, boolean noDepthTexture, ColorDepth bitDepth) {
		if(EngineRuntime.getDataManager() == null) throw new CelestialGLException("Must create context before designating GL data");
		this.width = width;
		this.height = height;
		this.noColorTexture = noColorTexture;
		this.useDepthBuffer = noDepthTexture;
		this.bitDepth = bitDepth;
		EngineRuntime.getDataManager().addData(this);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getTextureID() {
		return textureID;
	}
	
	public int getDepthID() {
		return depthID;
	}
	
	public int getBufferID() {
		return bufferID;
	}
	
	public int getDBufID() {
		return dbufID;
	}
	
	public void fboBind() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, bufferID);
		EngineRuntime.dispOverrideViewport(0, 0, width, height);
	}
	
	public void fboUnbind() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		EngineRuntime.dispAlignViewport();
	}
	
	public FrameBuffer sampleTexture() {
		toggle = false;
		return this;
	}
	
	public FrameBuffer sampleDepth() {
		toggle = true;
		return this;
	}
	
	@Override
	public void bind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		bind();
	}
	
	@Override
	public void bind() {
		GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_2D, toggle ? depthID : textureID);
	}
	
	public RenderOutput toRenderOutput() {
		return new RenderOutput() {
			@Override
			public void bind() {
				fboBind();
			}

			@Override
			public void unbind() {
				fboUnbind();
			}
		};
	}
	
	private transient boolean allocated = false;
	
	@Override
	public boolean isAllocated() {
		return allocated;
	}
	
	@Override
	public void allocate() {
		if(allocated) return;
		int bufferID = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, bufferID);
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		
		int textureID = Integer.MIN_VALUE;
		if(!noColorTexture) {
			textureID = GL11.glGenTextures();
			GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_2D, textureID);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, bitDepth.toGL(), width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureID, 0);
		}
		
		int depthID = Integer.MIN_VALUE, dbufID = Integer.MIN_VALUE;
		if(!useDepthBuffer) {
			depthID = GL11.glGenTextures();
			GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_2D, depthID);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthID, 0);
		}
		else {
			dbufID = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, dbufID);
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, dbufID);
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		allocated = true;
		
		this.textureID = textureID;
		this.depthID = depthID;
		this.bufferID = bufferID;
		this.dbufID = dbufID;
	}
	
	@Override
	public void deallocate() {
		if(!allocated) return;
		allocated = false;
		GL30.glDeleteFramebuffers(bufferID);
		GL11.glDeleteTextures(textureID);
		GL11.glDeleteTextures(depthID);
		GL30.glDeleteRenderbuffers(dbufID);
	}
	
	private Serializable userPtr = null;
	
	@Override
	public Serializable getUserPointer() {
		return userPtr;
	}
	
	@Override
	public void setUserPointer(Serializable userPtr) {
		this.userPtr = userPtr;
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		EngineRuntime.getDataManager().addData(this);
	}
	
	public static FrameBuffer create(int width, int height, boolean noColorTexture, boolean noDepthTexture) {
		return new FrameBuffer(width, height, noColorTexture, noDepthTexture);
	}
	
	public static FrameBuffer create(int width, int height, boolean noColorTexture, boolean noDepthTexture, ColorDepth bitDepth) {
		return new FrameBuffer(width, height, noColorTexture, noDepthTexture, bitDepth);
	}
	
}
