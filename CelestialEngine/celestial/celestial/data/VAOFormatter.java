package celestial.data;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import celestial.error.CelestialGenericException;

public class VAOFormatter implements java.io.Serializable {
	
	private static final long serialVersionUID = 3133664351007371254L;
	
	private final ArrayList<ArrayList<ArrayList<Byte>>> output;
	
	public VAOFormatter() {
		this.output = new ArrayList<ArrayList<ArrayList<Byte>>>();
	}
	
	public void nextVAO() {
		output.add(new ArrayList<ArrayList<Byte>>());
	}
	
	public void nextVBO() {
		if(output.size() == 0) nextVAO();
		output.get(output.size() - 1).add(new ArrayList<Byte>());
	}
	
	public void put(float[] data) {
		if(output.size() == 0 || output.get(output.size() - 1).size() == 0) nextVBO();
		ByteBuffer buffer = ByteBuffer.allocate(4 * data.length);
		for(int i = 0 ; i < data.length ; ++i) buffer.putFloat(data[i]);
		byte[] arr = buffer.array();
		
		for(int i = 0 ; i < arr.length ; ++i) output.get(output.size() - 1).get(output.get(output.size() - 1).size() - 1).add(arr[i]);
	}
	
	public void put(int[] data) {
		if(output.size() == 0 || output.get(output.size() - 1).size() == 0) nextVBO();
		ByteBuffer buffer = ByteBuffer.allocate(4 * data.length);
		for(int i = 0 ; i < data.length ; ++i) buffer.putInt(data[i]);
		byte[] arr = buffer.array();
		
		for(int i = 0 ; i < arr.length ; ++i) output.get(output.size() - 1).get(output.get(output.size() - 1).size() - 1).add(arr[i]);
	}
	
	public void export(String dest) {
		for(ArrayList<ArrayList<Byte>> vbo : new ArrayList<ArrayList<ArrayList<Byte>>>(output)) {
			for(ArrayList<Byte> list : new ArrayList<ArrayList<Byte>>(vbo)) {
				if(list.isEmpty()) vbo.remove(list);
			}
			if(vbo.isEmpty()) output.remove(vbo);
		}
		
		try {
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(dest));
			
			stream.writeInt(output.size());
			for(ArrayList<ArrayList<Byte>> vbo : output) {
				stream.writeInt(vbo.size());
				for(ArrayList<Byte> bytes : vbo) stream.writeInt(bytes.size());
			}
			
			for(ArrayList<ArrayList<Byte>> vbo : output) for(ArrayList<Byte> list : vbo) for(Byte b : list) stream.writeByte(b);
			stream.close();
		}
		catch (IOException e) {
			throw new CelestialGenericException("Cannot output to destination");
		}
	}
	
}
