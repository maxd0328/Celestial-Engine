package celestial.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public final class SerializationSystem {
	
	private final ArrayList<Persistable<?>> serializationTable;
	private final ArrayList<Serializable> nativeSerializationTable;
	
	public SerializationSystem(Persistable<?>[] serializationTable, Serializable... nativeSerializationTable) {
		this.serializationTable = new ArrayList<Persistable<?>>();
		this.nativeSerializationTable = new ArrayList<Serializable>();
		
		for(Persistable<?> persistable : serializationTable) this.serializationTable.add(persistable);
		for(Serializable serializable : nativeSerializationTable) this.nativeSerializationTable.add(serializable);
	}
	
	public Collection<Persistable<?>> getSerializationTable() {
		return serializationTable;
	}
	
	public Collection<Serializable> getNativeSerializationTable() {
		return nativeSerializationTable;
	}
	
	public void serialize(Serializer serializer) {
		for(Persistable<?> output : serializationTable)
			serializer.write(output);
		for(Serializable output : nativeSerializationTable)
			serializer.write(output);
		serializer.flush();
	}
	
	public Object[] deserialize(Deserializer deserializer) {
		Object[] arr = new Object[serializationTable.size() + nativeSerializationTable.size()];
		
		int i = -1;
		for(Persistable<?> input : serializationTable)
			arr[++i] = deserializer.read(input);
		for(int j = 0 ; j < nativeSerializationTable.size() ; ++j)
			arr[++i] = deserializer.readNative();
		
		return arr;
	}
	
}
