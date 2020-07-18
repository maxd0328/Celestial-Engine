package celestial.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.lwjgl.openal.AL10;

import celestial.core.EngineRuntime;
import celestial.error.CelestialGLException;
import celestial.serialization.SerializerImpl;
import celestial.vecmath.Vector3f;

public final class AudioChannel implements GLData {
	
	private static final long serialVersionUID = 1979982361440854118L;
	
	protected transient int channelID = 0;
	
	private AudioChannel() {
		if(EngineRuntime.getDataManager() == null) throw new CelestialGLException("Must create context before designating GL data");
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
		this.channelID = AL10.alGenSources();
	}
	
	@Override
	public void deallocate() {
		if(!allocated) return;
		allocated = false;
		stop();
		AL10.alDeleteSources(channelID);
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
	
	public void play(AudioBuffer buffer) {
		stop();
		if(buffer == null) return;
		buffer.bind(this);
		AL10.alSourcePlay(channelID);
	}
	
	public void stop() {
		AL10.alSourceStop(channelID);
	}
	
	public void pause() {
		AL10.alSourcePause(channelID);
	}
	
	public void resume() {
		AL10.alSourcePlay(channelID);
	}
	
	public void setVolume(float volume) {
		AL10.alSourcef(channelID, AL10.AL_GAIN, volume);
	}
	
	public void setPitch(float pitch) {
		AL10.alSourcef(channelID, AL10.AL_PITCH, pitch);
	}
	
	public void setLooping(boolean looping) {
		AL10.alSourcei(channelID, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
	}
	
	public void setPosition(Vector3f position) {
		AL10.alSource3f(channelID, AL10.AL_POSITION, position.x, position.y, position.z);
	}
	
	public void setVelocity(Vector3f velocity) {
		AL10.alSource3f(channelID, AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
	}
	
	public void setRolloff(float rolloff) {
		AL10.alSourcef(channelID, AL10.AL_ROLLOFF_FACTOR, rolloff);
	}
	
	public void setReference(float reference) {
		AL10.alSourcef(channelID, AL10.AL_REFERENCE_DISTANCE, reference);
	}
	
	public void setCutoff(float cutoff) {
		AL10.alSourcef(channelID, AL10.AL_MAX_DISTANCE, cutoff);
	}
	
	public boolean isPlaying() {
		return AL10.alGetSourcei(channelID, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		EngineRuntime.getDataManager().addData(this);
	}
	
	public static AudioChannel create() {
		return new AudioChannel();
	}
	
}
