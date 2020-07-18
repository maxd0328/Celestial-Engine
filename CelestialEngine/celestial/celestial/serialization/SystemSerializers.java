package celestial.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import celestial.error.CelestialGenericException;

public final class SystemSerializers {
	
	public static final class ByteArraySerializer extends StandardSerializer {
		
		private final ByteArrayOutputStream stream;
		
		public ByteArraySerializer() {
			this.stream = new ByteArrayOutputStream();
		}
		
		public byte[] toByteArray() {
			return stream.toByteArray();
		}
		
		@Override
		public void implWrite(byte... arr) {
			try {
				stream.write(arr);
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public void flush() {
			try {
				stream.flush();
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public void close() {
			try {
				stream.close();
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
	}
	
	public static final class ByteArrayDeserializer extends StandardDeserializer {
		
		private final ByteArrayInputStream stream;
		
		public ByteArrayDeserializer(byte[] arr) {
			this.stream = new ByteArrayInputStream(arr);
		}
		
		public ByteArrayDeserializer(byte[] arr, int off, int len) {
			this.stream = new ByteArrayInputStream(arr, off, len);
		}
		
		@Override
		public byte[] implRead(int len) {
			try {
				return stream.readNBytes(len);
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public void close() {
			try {
				stream.close();
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
	}
	
	public static final class FilesystemSerializer extends StandardSerializer {
		
		private final FileOutputStream stream;
		
		public FilesystemSerializer(String path) {
			try {
				this.stream = new FileOutputStream(path);
			}
			catch(FileNotFoundException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public void implWrite(byte... arr) {
			try {
				stream.write(arr);
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public void flush() {
			try {
				stream.flush();
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public void close() {
			try {
				stream.close();
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
	}
	
	public static final class FilesystemDeserializer extends StandardDeserializer {
		
		private final String path;
		private final FileInputStream stream;
		
		public FilesystemDeserializer(String path) {
			try {
				this.path = path;
				this.stream = new FileInputStream(path);
			}
			catch(FileNotFoundException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public byte[] implRead(int len) {
			try {
				return stream.readNBytes(len);
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		@Override
		public void close() {
			try {
				stream.close();
			}
			catch(IOException ex) {
				throw new CelestialGenericException(ex.getMessage(), ex);
			}
		}
		
		public String getPath() {
			return path;
		}
		
	}
	
}
