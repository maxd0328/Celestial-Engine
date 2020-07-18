package celestial.data;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import celestial.core.EngineRuntime;
import celestial.error.CelestialGLException;
import celestial.error.CelestialGenericException;
import celestial.serialization.SerializerImpl;

public final class AudioBuffer implements GLData {
	
	private static final long serialVersionUID = 6377142439078348341L;
	
	private transient int bufferID = 0;
	private final String src;
	
	private AudioBuffer(String src) {
		if(EngineRuntime.getDataManager() == null) throw new CelestialGLException("Must create context before designating GL data");
		this.src = src;
		EngineRuntime.getDataManager().addData(this);
	}
	
	private transient boolean allocated = false;
	
	@Override
	public boolean isAllocated() {
		return allocated;
	}
	
	@Override
	public void allocate() {
		if(allocated) return;
		allocated = true;
		BufferedInputStream fileData;
		try { fileData = new BufferedInputStream(new FileInputStream(src));
		} catch (FileNotFoundException e) { throw new CelestialGenericException("Invalid buffer source"); }
		int bufferID = AL10.alGenBuffers();
		WaveData waveFile = WaveData.create(fileData);
		AL10.alBufferData(bufferID, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		this.bufferID = bufferID;
	}
	
	@Override
	public void deallocate() {
		if(!allocated) return;
		allocated = false;
		AL10.alDeleteBuffers(bufferID);
	}
	
	private Serializable userPtr = null;
	
	@Override
	public Serializable getUserPointer() {
		return userPtr;
	}
	
	@Override
	public void setUserPointer(Serializable userPtr) {
		this.userPtr = userPtr;
	}
	
	public void bind(AudioChannel channel) {
		AL10.alSourcei(channel.channelID, AL10.AL_BUFFER, bufferID);
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		EngineRuntime.getDataManager().addData(this);
	}
	
	public static AudioBuffer create(String src) {
		return new AudioBuffer(src);
	}
	
}
