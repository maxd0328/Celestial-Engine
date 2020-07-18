package celestial.io;

import java.io.IOException;
import java.io.InputStream;

public abstract class ImplURL implements URL {
	
	private final String src;
	
	protected ImplURL(String src) {
		this.src = src;
	}
	
	@Override
	public String get() {
		return src;
	}
	
	@Override
	public byte[] read() {
		try {
			return toInputStream().readAllBytes();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String readUTF() {
		return new String(read());
	}
	
	@Override
	public abstract InputStream toInputStream();
	
}
