package celestial.data;

import java.io.Serializable;

public interface GLData extends java.io.Serializable {
	
	public boolean isAllocated();
	
	public void allocate();
	
	public void deallocate();
	
	public Serializable getUserPointer();
	
	public void setUserPointer(Serializable userPtr);
	
}
