package studio.celestial.media;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import celestial.serialization.SerializerImpl;
import studio.celestial.media.Media.MediaType;

public class Resource implements MediaNode, java.io.Serializable {
	
	private static final long serialVersionUID = 8497081448041027898L;
	
	private String name;
	private final Source source;
	protected transient MediaBox mediaBox;
	
	private boolean queuedForDelete = false;
	
	public Resource(String name, Source source) {
		this.name = name;
		this.source = source;
		this.mediaBox = new MediaBox(this);
	}
	
	protected Resource(String name, Source source, MediaType type) {
		this.name = name;
		this.source = source;
		this.mediaBox = new MediaBox(this, type);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
		mediaBox.setIdentifier(name);
	}
	
	public Source getSource() {
		return source;
	}
	
	@Override
	public MediaBox getMediaBox() {
		return mediaBox;
	}
	
	@Override
	public void delete() {
		this.queuedForDelete = true;
	}
	
	@Override
	public boolean queuedForDelete() {
		return queuedForDelete;
	}
	
	@SerializerImpl
	private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.mediaBox = new MediaBox(this);
	}
	
	public static final class StringSource implements Source {
		
		private static final long serialVersionUID = -4669981025124257294L;
		
		private final String value;
		
		public StringSource(String value) {
			this.value = value;
		}
		
		@Override
		public String read() {
			return value;
		}
		
	}
	
	public static final class FileSource implements Source {
		
		private static final long serialVersionUID = -3439480222281298119L;
		
		private final String path;
		
		public FileSource(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}
		
		@Override
		public String read() {
			try {
				byte[] encoded = Files.readAllBytes(Paths.get(path));
				return new String(encoded, StandardCharsets.US_ASCII);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		
	}
	
}
