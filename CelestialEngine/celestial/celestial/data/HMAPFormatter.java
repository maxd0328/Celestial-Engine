package celestial.data;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import celestial.error.CelestialGenericException;

public class HMAPFormatter implements java.io.Serializable {
	
	private static final long serialVersionUID = -6903280298100418942L;
	
	private float[][] heights;
	
	public HMAPFormatter() {
	}
	
	public void putData(float[][] heights) {
		if(heights.length < 3) throw new CelestialGenericException("Invalid dimensions");
		if(heights[0].length < 3) throw new CelestialGenericException("Invalid dimensions");
		this.heights = heights;
	}
	
	public void export(String dest) {
		if(heights == null) throw new CelestialGenericException("No data has been inserted into HMAP formatter");
		
		try {
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(dest));
			
			stream.writeInt(findExponent(heights.length - 1));
			stream.writeInt(findExponent(heights[0].length - 1));
			
			for(int y = 0 ; y < heights[0].length ; ++y) {
				for(int x = 0 ; x < heights.length ; ++x) {
					stream.writeFloat(heights[x][y]);
				}
			}
			
			stream.close();
		}
		catch (IOException e) {
			throw new CelestialGenericException("Cannot output to destination");
		}
	}
	
	private int findExponent(int num) {
		int count = 0;
		while((num >>= 1) > 0) count++;
		return count;
	}
	
}
