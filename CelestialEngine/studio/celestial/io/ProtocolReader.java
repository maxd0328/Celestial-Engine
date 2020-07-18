package celestial.io;

import java.io.InputStream;

public final class ProtocolReader {
	
	private final Protocol protocol;
	private final URL url;
	
	public ProtocolReader(URL url) {
		this.protocol = Protocol.CUSTOM;
		this.url = url;
	}
	
	public ProtocolReader(String directive) {
		int index = directive.indexOf(':');
		if(index <= 0)
			throw new IllegalArgumentException("No protocol specified");
		if(directive.length() == index + 1)
			throw new IllegalArgumentException("No URL specified");
		
		String sProtocol = directive.substring(0, index).trim();
		String sURL = directive.substring(index + 1);
		
		this.protocol = Protocol.fromString(sProtocol);
		this.url = protocol.createURL(sURL);
	}
	
	public Protocol getProtocol() {
		return protocol;
	}
	
	public URL getURL() {
		return url;
	}
	
	public byte[] read() {
		return url == null ? new byte[] {} : url.read();
	}
	
	public String readUTF() {
		return url == null ? "" : url.readUTF();
	}
	
	public InputStream toInputStream() {
		return url == null ? null : url.toInputStream();
	}
	
}
