package celestial.io;

import java.io.InputStream;

public interface URL {
	
	public String get();
	
	public byte[] read();
	
	public String readUTF();
	
	public InputStream toInputStream();
	
}
