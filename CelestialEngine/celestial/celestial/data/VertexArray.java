package celestial.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import celestial.core.EngineRuntime;
import celestial.error.CelestialGLException;
import celestial.serialization.SerializerImpl;

public final class VertexArray implements GLData {
	
	private static final long serialVersionUID = 454827481147156610L;
	
	private transient int vaoID = 0;
	
	private final ArrayList<VertexBuffer> buffers;
	
	protected VertexArray(VertexBuffer indexBuffer, VertexBuffer...buffers) {
		if(EngineRuntime.getDataManager() == null) throw new CelestialGLException("Must create context before designating GL data");
		ArrayList<VertexBuffer> attribs = new ArrayList<VertexBuffer>();
		
		if(indexBuffer != null) {
			indexBuffer.setAttribID(-1);
			attribs.add(indexBuffer);
		}
		
		for(int i = 0 ; i < buffers.length ; ++i) {
			if(buffers[i] == null) continue;
			if(buffers[i].getType() == VertexBuffer.TYPE_ELEMENT_ARRAY) throw new CelestialGLException("Cannot pass element array buffer to standard slot");
			else buffers[i].setAttribID(i);
			attribs.add(buffers[i]);
		}
		
		this.buffers = attribs;
		
		EngineRuntime.getDataManager().addData(this);
	}
	
	public int getVaoID() {
		return vaoID;
	}
	
	public int getIndexCount() {
		return buffers.size() == 0 ? 0 : buffers.get(0).getSize() / buffers.get(0).getCoordinateSize();
	}
	
	public ArrayList<VertexBuffer> getBuffers() {
		return new ArrayList<VertexBuffer>(buffers);
	}
	
	public int[] getAttribs() {
		ArrayList<Integer> attribs = new ArrayList<Integer>();
		for(VertexBuffer buffer : buffers) if(buffer.getAttribID() >= 0) attribs.add(buffer.getAttribID());
		int[] arr = new int[attribs.size()];
		for(int i = 0 ; i < attribs.size() ; ++i) arr[i] = attribs.get(i);
		return arr;
	}
	
	public void bind() {
		if(!allocated) return;
		GL30.glBindVertexArray(vaoID);
	}
	
	public void unbind() {
		if(!allocated) return;
		GL30.glBindVertexArray(0);
	}
	
	public void bind(int...attributes) {
		if(!allocated) return;
		bind();
		for(int attrib : attributes) GL20.glEnableVertexAttribArray(attrib);
	}
	
	public void unbind(int...attributes) {
		if(!allocated) return;
		for(int attrib : attributes) GL20.glDisableVertexAttribArray(attrib);
		unbind();
	}
	
	private transient boolean allocated = false;
	
	@Override
	public boolean isAllocated() {
		return allocated;
	}
	
	@Override
	public void allocate() {
		if(allocated) return;
		allocated = true;
		this.vaoID = GL30.glGenVertexArrays();
		bind();
		for(VertexBuffer buffer : buffers) {
			if(buffer.getDataType() == VertexBuffer.DATATYPE_UNDETERMINED) throw new CelestialGLException("Cannot pass unallocated VBO to vertex array");
			buffer.allocate();
		}
		unbind();
	}
	
	@Override
	public void deallocate() {
		if(!allocated) return;
		allocated = false;
		for(VertexBuffer buffer : buffers) {
			buffer.deallocate();
		}
		GL30.glDeleteVertexArrays(vaoID);
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
	
	public static VertexArray create(VertexBuffer indexBuffer, VertexBuffer...buffers) {
		return new VertexArray(indexBuffer, buffers);
	}
	
}
