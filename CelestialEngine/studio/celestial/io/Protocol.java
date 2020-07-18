package celestial.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public enum Protocol {
	
	FILE,
	
	ZIP,
	
	CUSTOM;
	
	public URL createURL(final String sURL) {
		switch(this) {
		case FILE:
			return new ImplURL(sURL) {
				
				@Override
				public InputStream toInputStream() {
					try {
						return new FileInputStream(super.get());
					}
					catch(FileNotFoundException e) {
						throw new RuntimeException("No such file \'" + sURL + "\'");
					}
				}
				
			};
		case ZIP:
			return new ImplURL(sURL) {
				
				@Override
				public InputStream toInputStream() {
					int index = super.get().indexOf("|");
					if(index <= 0) throw new IllegalArgumentException("Invalid URL \'" + super.get() + "\'");
					
					try {
						ZipFile zip = new ZipFile(super.get().substring(0, index));
						String entryName = super.get().substring(index + 1).replace('/', '\\');
						
						for(ZipEntry entry : Collections.list(zip.entries())) {
							if(entry.getName().replace('/', '\\').equals(entryName))
								return zip.getInputStream(entry);
						}
						
						throw new IllegalArgumentException("No such zip entry found");
					}
					catch(IOException e) {
						throw new RuntimeException(e);
					}
					
				}
				
			};
		case CUSTOM:
		default:
			return null;
		}
	}
	
	public static Protocol fromString(String s) {
		switch(s) {
		case "file":
			return FILE;
		case "custom":
			return CUSTOM;
		}
		
		throw new IllegalArgumentException("No such protocol \'" + s + "\'");
	}
	
}
