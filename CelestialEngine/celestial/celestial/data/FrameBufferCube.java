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

public final class FrameBufferCube implements Sampler, GLData {
	
	private static final long serialVersionUID = -5762551426271600664L;
	
	@SuppressWarnings("unchecked")
	public static final PropertySelection<GLDisplayMode>[] RECOMMENDED_DISPLAY_MODES = new PropertySelection[] {
			new PropertySelection<GLDisplayMode>("32x32", new GLDisplayMode(32, 32, false)),
			new PropertySelection<GLDisplayMode>("64x64", new GLDisplayMode(64, 64, false)),
			new PropertySelection<GLDisplayMode>("128x128", new GLDisplayMode(128, 128, false)),
			new PropertySelection<GLDisplayMode>("256x256", new GLDisplayMode(256, 256, false)),
			new PropertySelection<GLDisplayMode>("512x512", new GLDisplayMode(512, 512, false)),
			new PropertySelection<GLDisplayMode>("1024x1024", new GLDisplayMode(1024, 1024, false))
	};
	
	private final int size;
	private final boolean noColorTexture, useDepthBuffer;
	private final ColorDepth bitDepth;
	private transient int textureID = 0, depthID = 0, bufferID = 0, dbufID = 0;
	private int cubeFace = 0;
	private boolean toggle = false;
	
	protected FrameBufferCube(int size, boolean noColorTexture, boolean noDepthTexture) {
		this(size, noColorTexture, noDepthTexture, ColorDepth.RGBA16_HDR);
	}
	
	protected FrameBufferCube(int size, boolean noColorTexture, boolean noDepthTexture, ColorDepth bitDepth) {
		if(EngineRuntime.getDataManager() == null) throw new CelestialGLException("Must create context before designating GL data");
		this.size = size;
		this.noColorTexture = noColorTexture;
		this.useDepthBuffer = noDepthTexture;
		this.bitDepth = bitDepth;
		EngineRuntime.getDataManager().addData(this);
	}
	
	public int getSize() {
		return size;
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
	
	public int getCubeFace() {
		return cubeFace;
	}
	
	public void setCubeFace(int cubeFace) {
		this.cubeFace = cubeFace;
	}
	
	public void fboBind() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, bufferID);
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		EngineRuntime.dispOverrideViewport(0, 0, size, size);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + cubeFace, textureID, 0);
		if(!useDepthBuffer) GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + cubeFace, depthID, 0);
	}
	
	public void fboUnbind() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		EngineRuntime.dispAlignViewport();
	}
	
	public FrameBufferCube sampleTexture() {
		toggle = false;
		return this;
	}
	
	public FrameBufferCube sampleDepth() {
		toggle = true;
		return this;
	}
	
	public void bind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		bind();
	}
	
	public void bind() {
		GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_CUBE_MAP, toggle ? depthID : textureID);
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
			GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_CUBE_MAP, textureID);
			for(int i = 0 ; i < 6 ; ++i)
				GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitDepth.toGL(), size, size, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
			GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_CUBE_MAP, 0);
//			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureID, 0); // done when binding to choose which face (this is only 2D)
		}
		
		int depthID = Integer.MIN_VALUE, dbufID = Integer.MIN_VALUE;
		if(!useDepthBuffer) {
			depthID = GL11.glGenTextures();
			GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_CUBE_MAP, depthID);
			for(int i = 0 ; i < 6 ; ++i)
				GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL14.GL_DEPTH_COMPONENT24, size, size, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
			GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_CUBE_MAP, 0);
//			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthID, 0); // done when binding to choose which face (this is only 2D)
		}
		else {
			dbufID = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, dbufID);
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, size, size);
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
	
	public static FrameBufferCube create(int size, boolean noColorTexture, boolean noDepthTexture) {
		return new FrameBufferCube(size, noColorTexture, noDepthTexture);
	}
	
	public static FrameBufferCube create(int size, boolean noColorTexture, boolean noDepthTexture, ColorDepth bitDepth) {
		return new FrameBufferCube(size, noColorTexture, noDepthTexture, bitDepth);
	}
	
}
