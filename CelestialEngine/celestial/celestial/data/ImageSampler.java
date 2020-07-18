package celestial.data;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;
import celestial.core.EngineRuntime;
import celestial.error.CelestialGLException;
import celestial.error.CelestialGenericException;
import celestial.serialization.SerializerImpl;

public final class ImageSampler implements Sampler, GLData {
	
	private static final long serialVersionUID = 177917466408755092L;
	
	public static final int TYPE_TEXTURE_1D = GL11.GL_TEXTURE_1D;
	public static final int TYPE_TEXTURE_2D = GL11.GL_TEXTURE_2D;
	public static final int TYPE_TEXTURE_3D = GL12.GL_TEXTURE_3D;
	public static final int TYPE_TEXTURE_CUBE_MAP = GL13.GL_TEXTURE_CUBE_MAP;
	
	private transient int textureID = 0;
	private final int type;
	private final ImageAllocator allocator;
	
	ImageSampler(int type, ImageAllocator allocator) {
		if(EngineRuntime.getDataManager() == null) throw new CelestialGLException("Must create context before designating GL data");
		this.type = type;
		this.allocator = allocator;
		setBufferState(true);
		EngineRuntime.getDataManager().addData(this);
	}
	
	public int getTextureID() {
		return textureID;
	}
	
	public void bind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		bind();
	}
	
	public void bind() {
		GL11.glBindTexture(type, textureID);
	}
	
	private transient boolean allocated = false;
	
	@Override
	public boolean isAllocated() {
		return allocated;
	}
	
	@Override
	public void allocate() {
		if(allocated) return;
		this.textureID = allocator.allocate();
		allocated = true;
	}
	
	@Override
	public void deallocate() {
		if(!allocated) return;
		allocated = false;
		GL11.glDeleteTextures(textureID);
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
	
	public boolean hasBuffer() {
		return allocator.buffer != null;
	}
	
	public boolean hasCubeMapBuffers() {
		return allocator.buffers != null;
	}
	
	public ImageBuffer getBuffer() {
		return allocator.buffer;
	}
	
	public ImageBuffer[] getCubeMapBuffers() {
		return allocator.buffers;
	}
	
	public void unlockBuffers() {
		setBufferState(false);
	}
	
	private void setBufferState(boolean state) {
		if(allocator.buffer != null) allocator.buffer.locked = state;
		if(allocator.buffers != null) for(int i = 0 ; i < allocator.buffers.length ; ++i) if(allocator.buffers[i] != null) allocator.buffers[i].locked = state;
	}
	
	public void reallocate() {
		if(allocator.buffer != null) {
			bind();
			GL11.glTexSubImage2D(type, 0, 0, 0, allocator.buffer.getWidth(), allocator.buffer.getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, allocator.buffer.toByteBuffer());
			setBufferState(true);
		}
		else if(allocator.buffers != null) {
			bind();
			for (int i = 0; i < allocator.buffers.length; i++)
				GL11.glTexSubImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, 0, 0, allocator.buffers[i].getWidth(), 
						allocator.buffers[i].getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, allocator.buffers[i].toByteBuffer());
			setBufferState(true);
		}
		else throw new CelestialGLException("Sampler must be created with a buffer");
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		EngineRuntime.getDataManager().addData(this);
	}
	
	public static ImageSampler create(int type) {
		return new ImageSampler(type, new ImageAllocator(null, null, null, null));
	}
	
	public static ImageSampler create(String src) {
		if(src == null) return create(TYPE_TEXTURE_2D);
		return new ImageSampler(TYPE_TEXTURE_2D, new ImageAllocator(src, null, null, null));
	}
	
	public static ImageSampler create(ImageBuffer buffer) {
		if(buffer == null) return create(TYPE_TEXTURE_2D);
		return new ImageSampler(TYPE_TEXTURE_2D, new ImageAllocator(null, buffer, null, null));
	}
	
	public static ImageSampler create(String... cubeMapTextures) {
		if(cubeMapTextures == null) return create(TYPE_TEXTURE_CUBE_MAP);
		ImageSampler sampler = new ImageSampler(TYPE_TEXTURE_CUBE_MAP, new ImageAllocator(null, null, cubeMapTextures, null));
		return sampler;
	}
	
	public static ImageSampler create(ImageBuffer... cubeMapTextures) {
		if(cubeMapTextures == null) return create(TYPE_TEXTURE_CUBE_MAP);
		ImageSampler sampler = new ImageSampler(TYPE_TEXTURE_CUBE_MAP, new ImageAllocator(null, null, null, cubeMapTextures));
		return sampler;
	}
	
	public static final class ImageAllocator implements java.io.Serializable {
		
		private static final long serialVersionUID = -531080056189037419L;
		
		private final String src;
		private final ImageBuffer buffer;
		private final String[] srcs;
		private final ImageBuffer[] buffers;
		
		private ImageAllocator(String src, ImageBuffer buffer, String[] srcs, ImageBuffer[] buffers) {
			this.src = src;
			this.buffer = buffer;
			this.srcs = srcs;
			this.buffers = buffers;
		}
		
		private int allocate() {
			if(src != null) {
				ImageBuffer buffer;
				try { buffer = new ImageBuffer(ImageIO.read(new File(src))); }
				catch(Exception ex) { throw new CelestialGenericException("Unable to read resource \'" + src + "\'"); }
				int texID = GL11.glGenTextures();
				GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_2D, texID);
				GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GL11.glTexImage2D(ImageSampler.TYPE_TEXTURE_2D, 0, GL11.GL_RGBA8, buffer.getWidth(), buffer.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer.toByteBuffer());
				
				GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0);
				if(GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
					float amount = Math.min(4f, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
				}
				return texID;
			}
			else if(buffer != null) {
				int texID = GL11.glGenTextures();
				GL11.glBindTexture(ImageSampler.TYPE_TEXTURE_2D, texID);
				GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				GL11.glTexParameteri(ImageSampler.TYPE_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GL11.glTexImage2D(ImageSampler.TYPE_TEXTURE_2D, 0, GL11.GL_RGBA8, buffer.getWidth(), buffer.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer.toByteBuffer());
				
				GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0);
				if(GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
					float amount = Math.min(4f, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
					GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
				}
				return texID;
			}
			else if(srcs != null) {
				int texID = GL11.glGenTextures();
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);
				for (int i = 0; i < srcs.length; i++) {
					ImageBuffer buffer;
					try { buffer = new ImageBuffer(ImageIO.read(new File(srcs[i]))); }
					catch(Exception ex) { throw new CelestialGenericException("Unable to read resource"); }
					GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, buffer.getWidth(), buffer.getHeight(), 0,
							GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer.toByteBuffer());
				}
				GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				return texID;
			}
			else if(buffers != null) {
				int texID = GL11.glGenTextures();
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);
				for (int i = 0; i < buffers.length; i++)
					GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, buffers[i].getWidth(), buffers[i].getHeight(), 0,
							GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffers[i].toByteBuffer());
				GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				return texID;
			}
			else return GL11.glGenTextures();
		}
		
	}
	
}
