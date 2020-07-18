package celestial.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import celestial.core.EngineRuntime;
import celestial.error.CelestialGLException;
import celestial.error.CelestialGenericException;
import celestial.serialization.SerializerImpl;

public final class VertexBuffer implements GLData {
	
	private static final long serialVersionUID = -7541146270837425441L;
	
	public static final int TYPE_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER;
	public static final int TYPE_ELEMENT_ARRAY = GL15.GL_ELEMENT_ARRAY_BUFFER;
	
	public static final int DRAW_TYPE_STATIC = GL15.GL_STATIC_DRAW;
	public static final int DRAW_TYPE_DYNAMIC = GL15.GL_DYNAMIC_DRAW;
	
	public static final int DATATYPE_UNDETERMINED = 0x0;
	public static final int DATATYPE_FLOAT32 = 0x1;
	public static final int DATATYPE_INT32 = 0x2;
	
	private transient int vboID = 0;
	private final int type;
	private int attribID = -1;
	private final int coordinateSize;
	private final int drawType;
	private int dataType = DATATYPE_UNDETERMINED;
	private int size = 0;
	
	private CEBufferAllocator allocator;
	
	protected VertexBuffer(int type, int coordinateSize, int drawType) {
		this.type = type;
		this.coordinateSize = coordinateSize;
		this.drawType = drawType;
	}
	
	public int getVboID() {
		return vboID;
	}
	
	public int getType() {
		return type;
	}
	
	public int getAttribID() {
		return attribID;
	}
	
	public int getCoordinateSize() {
		return coordinateSize;
	}
	
	public int getDrawType() {
		return drawType;
	}
	
	public int getDataType() {
		return dataType;
	}
	
	public int getSize() {
		return size;
	}
	
	public void bind() {
		GL15.glBindBuffer(type, vboID);
	}
	
	public void unbind() {
		GL15.glBindBuffer(type, 0);
	}
	
	private transient boolean allocated = false;
	
	public VertexBuffer allocatef(float[] data) {
		if(data == null) throw new CelestialGenericException("Data cannot be null");
		if(allocated) throw new CelestialGLException("Vertex buffer already allocated");
		this.allocator = new CEBufferAllocator(data, null, null, 0, 0, 0, 0);
		allocated = true;
		dataType = DATATYPE_FLOAT32;
		return this;
	}
	
	public VertexBuffer allocatei(int[] data) {
		if(data == null) throw new CelestialGenericException("Data cannot be null");
		if(allocated) throw new CelestialGLException("Vertex buffer already allocated");
		this.allocator = new CEBufferAllocator(null, data, null, 0, 0, 0, 0);
		allocated = true;
		dataType = DATATYPE_INT32;
		return this;
	}
	
	public VertexBuffer allocatef(String src, int format, int lodIndex, int attrib) {
		if(src == null) throw new CelestialGenericException("Data cannot be null");
		if(allocated) throw new CelestialGLException("Vertex buffer already allocated");
		this.allocator = new CEBufferAllocator(null, null, src, DATATYPE_FLOAT32, format, lodIndex, attrib);
		allocated = true;
		dataType = DATATYPE_FLOAT32;
		return this;
	}
	
	public VertexBuffer allocatei(String src, int format, int lodIndex, int attrib) {
		if(src == null) throw new CelestialGenericException("Data cannot be null");
		if(allocated) throw new CelestialGLException("Vertex buffer already allocated");
		this.allocator = new CEBufferAllocator(null, null, src, DATATYPE_INT32, format, lodIndex, attrib);
		allocated = true;
		dataType = DATATYPE_FLOAT32;
		return this;
	}
	
	public VertexBuffer storef(float[] data) {
		if(data == null) throw new CelestialGenericException("Data cannot be null");
		if(!allocated) throw new CelestialGLException("Vertex buffer not allocated");
		if(drawType != DRAW_TYPE_DYNAMIC) throw new CelestialGLException("Cannot overwrite non-dynamic data");
		if(dataType != DATATYPE_FLOAT32 || allocator.floatData == null) throw new CelestialGLException("Requested type does not match allocated type");
		
		allocator.floatData = data;
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		
		bind();
		GL15.glBufferSubData(type, 0, buffer);
		unbind();
		return this;
	}
	
	public VertexBuffer storei(int[] data) {
		if(data == null) throw new CelestialGenericException("Data cannot be null");
		if(!allocated) throw new CelestialGLException("Vertex buffer not allocated");
		if(drawType != DRAW_TYPE_DYNAMIC) throw new CelestialGLException("Cannot overwrite non-dynamic data");
		if(dataType != DATATYPE_INT32 || allocator.intData == null) throw new CelestialGLException("Requested type does not match allocated type");
		
		allocator.intData = data;
		
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		
		bind();
		GL15.glBufferSubData(type, 0, buffer);
		unbind();
		return this;
	}
	
	protected void setAttribID(int attribID) {
		this.attribID = attribID;
	}
	
	private boolean formallyAllocated = false;
	
	@Override
	public boolean isAllocated() {
		return formallyAllocated;
	}
	
	@Override
	public void allocate() {
		if(formallyAllocated) return;
		formallyAllocated = true;
		if(!allocated || allocator == null) throw new CelestialGLException("Vertex buffer not allocated");
		vboID = allocator.allocate(type, attribID, coordinateSize, drawType);
		size = allocator.currentSize;
	}
	
	@Override
	public void deallocate() {
		if(!formallyAllocated) return;
		formallyAllocated = false;
		GL15.glDeleteBuffers(vboID);
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
	
	public static VertexBuffer create(int coordinateSize, int drawType) {
		return new VertexBuffer(TYPE_ARRAY_BUFFER, coordinateSize, drawType);
	}
	
	public static VertexBuffer createIndexBuffer(int drawType) {
		return new VertexBuffer(TYPE_ELEMENT_ARRAY, 1, drawType);
	}
	
	public static class CEBufferAllocator implements java.io.Serializable {
		
		private static final long serialVersionUID = 5347960719615769575L;
		
		private float[] floatData;
		private int[] intData;
		
		private final String src;
		private final int datatype, format, lodIndex, attrib;
		
		private int currentSize = 0;
		
		private CEBufferAllocator(float[] floatData, int[] intData, String src, int datatype, int format, int lodIndex, int attrib) {
			this.floatData = floatData;
			this.intData = intData;
			this.src = src;
			this.datatype = datatype;
			this.format = format;
			this.lodIndex = lodIndex;
			this.attrib = attrib;
		}
		
		protected int allocate(int type, int attribID, int coordinateSize, int drawType) {
			if(floatData != null) {
				int vboID = GL15.glGenBuffers();
				GL15.glBindBuffer(type, vboID);
				
				FloatBuffer buffer = BufferUtils.createFloatBuffer(floatData.length);
				buffer.put(floatData);
				buffer.flip();
				
				GL15.glBufferData(type, buffer, drawType);
				this.currentSize = floatData.length;
				
				if(type != TYPE_ELEMENT_ARRAY) GL20.glVertexAttribPointer(attribID, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
				if(type != TYPE_ELEMENT_ARRAY) GL15.glBindBuffer(type, 0);
				return vboID;
			}
			else if(intData != null) {
				int vboID = GL15.glGenBuffers();
				GL15.glBindBuffer(type, vboID);
				
				IntBuffer buffer = BufferUtils.createIntBuffer(intData.length);
				buffer.put(intData);
				buffer.flip();
				
				GL15.glBufferData(type, buffer, drawType);
				this.currentSize = intData.length;
				
				if(type != TYPE_ELEMENT_ARRAY) GL30.glVertexAttribIPointer(attribID, coordinateSize, GL11.GL_INT, coordinateSize * Integer.BYTES, 0);
				if(type != TYPE_ELEMENT_ARRAY) GL15.glBindBuffer(type, 0);
				return vboID;
			}
			else if(src != null) {
				int vboID = GL15.glGenBuffers();
				GL15.glBindBuffer(type, vboID);
				if(datatype == DATATYPE_FLOAT32) {
					float[] data = DataReader.readf(src, format, lodIndex, attrib);
					FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
					buffer.put(data);
					buffer.flip();
					
					GL15.glBufferData(type, buffer, drawType);
					this.currentSize = data.length;
					if(type != TYPE_ELEMENT_ARRAY) GL20.glVertexAttribPointer(attribID, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
				}
				else if(datatype == DATATYPE_INT32) {
					int[] data = DataReader.readi(src, format, lodIndex, attrib);
					IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
					buffer.put(data);
					buffer.flip();
					
					GL15.glBufferData(type, buffer, drawType);
					this.currentSize = data.length;
					if(type != TYPE_ELEMENT_ARRAY) GL30.glVertexAttribIPointer(attribID, coordinateSize, GL11.GL_INT, coordinateSize * Integer.BYTES, 0);
				}
				else throw new CelestialGenericException("Invalid datatype");
				if(type != TYPE_ELEMENT_ARRAY) GL15.glBindBuffer(type, 0);
				return vboID;
			}
			else return GL15.glGenBuffers();
		}
		
	}
	
}
