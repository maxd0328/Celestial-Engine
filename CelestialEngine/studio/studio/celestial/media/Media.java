package studio.celestial.media;

import java.io.IOException;
import java.io.ObjectInputStream;

import celestial.data.AudioBuffer;
import celestial.data.ImageSampler;
import celestial.data.VertexArray;
import celestial.data.GLData;
import celestial.error.CelestialContextException;
import celestial.serialization.SerializerImpl;
import celestial.util.KVEntry;

public final class Media extends Resource {
	
	private static final long serialVersionUID = -1824887709738598748L;
	
	private final MediaType type;
	
	public Media(String name, MediaType type, GLData... data) {
		super(name, new MediaSource(data), type);
		this.type = type;
		
		validateMediaType();
	}
	
	public MediaSource getMediaSource() {
		((MediaSource) super.getSource()).updateIdentifier(super.getName());
		return (MediaSource) super.getSource();
	}
	
	public MediaType getType() {
		return type;
	}
	
	private void validateMediaType() {
		if(!type.isValidDataLength(getMediaSource().read().length))
			throw new CelestialContextException("Invalid media data array length");
		if(!type.getDataClass().isInstance(getMediaSource().read()[0]))
			throw new CelestialContextException("Unmatching media type");
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		super.mediaBox = new MediaBox(this, type);
	}
	
	public static enum MediaType {
		
		OBJ,
		
		DAE,
		
		VAO,
		
		HMAP,
		
		PNG,
		
		WAV;
		
		public boolean isValidDataLength(int length) {
			switch(this) {
			case OBJ:
			case DAE:
			case VAO:
			case HMAP:
				return length >= 1;
			case PNG:
			case WAV:
				return length == 1;
			}
			return length > 0;
		}
		
		public Class<? extends GLData> getDataClass() {
			switch(this) {
			case OBJ:
			case DAE:
			case VAO:
			case HMAP:
				return VertexArray.class;
			case PNG:
				return ImageSampler.class;
			case WAV:
				return AudioBuffer.class;
			}
			return GLData.class;
		}
		
		@SuppressWarnings("unchecked")
		public static KVEntry<String, MediaType>[] toTypeArray() {
			return (KVEntry<String, MediaType>[]) new KVEntry[] {
					new KVEntry<String, MediaType>("OBJ Model", OBJ),
					new KVEntry<String, MediaType>("DAE Model", DAE),
					new KVEntry<String, MediaType>("VAO Model", VAO),
					new KVEntry<String, MediaType>("HMAP Model", HMAP),
					new KVEntry<String, MediaType>("PNG Image", PNG),
					new KVEntry<String, MediaType>("WAV Sound", WAV)
			};
		}
		
		@SuppressWarnings("unchecked")
		public static KVEntry<String, MediaType>[] toEditableTypeArray() {
			return (KVEntry<String, MediaType>[]) new KVEntry[] {
					new KVEntry<String, MediaType>("VAO Model", VAO),
					new KVEntry<String, MediaType>("HMAP Model", HMAP),
					new KVEntry<String, MediaType>("PNG Image", PNG)
			};
		}
		
	}
	
	public static final class MediaSource implements Source {
		
		private static final long serialVersionUID = -1744669643004175596L;
		
		private final GLData[] data;
		
		public MediaSource(GLData... data) {
			this.data = data;
		}
		
		private void updateIdentifier(String identifier) {
			for(GLData glData : data) glData.setUserPointer((java.io.Serializable) identifier);
		}
		
		@Override
		public GLData[] read() {
			return data;
		}
		
	}
	
}
