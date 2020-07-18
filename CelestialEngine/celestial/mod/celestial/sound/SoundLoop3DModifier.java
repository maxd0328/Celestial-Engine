package mod.celestial.sound;

import java.util.ArrayList;
import java.util.HashMap;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.ctrl.PropertyController;
import celestial.data.AudioBuffer;
import celestial.data.AudioChannel;
import celestial.data.GLData;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.util.KVEntry;
import celestial.vecmath.Vector3f;
import studio.celestial.media.Media.MediaType;

public final class SoundLoop3DModifier extends Modifier {
	
	private static final long serialVersionUID = 7884757117001084911L;
	
	public static final Factory<SoundLoop3DModifier> FACTORY = () -> new SoundLoop3DModifier(null, 1f, 1f, 350f, 0f, 1f, true);
	
	private static final HashMap<CEObject, KVEntry<AudioChannel, Boolean>> CHANNELS = new HashMap<CEObject, KVEntry<AudioChannel, Boolean>>();
	private static boolean readyForRelease = false;
	
	private final Property<AudioBuffer> buffer;
	private final Property<Float> rolloff;
	private final Property<Float> reference;
	private final Property<Float> cutoff;
	private final Property<Float> volume;
	private final Property<Float> pitch;
	private final Property<Boolean> looping;
	
	public SoundLoop3DModifier(AudioBuffer buffer, float rolloff, float reference, float cutoff, float volume, float pitch, boolean looping) {
		super(false, true, false);
		this.buffer = Properties.createProperty(AudioBuffer.class, buffer);
		this.rolloff = Properties.createFloatProperty(rolloff);
		this.reference = Properties.createFloatProperty(reference);
		this.cutoff = Properties.createFloatProperty(cutoff);
		this.volume = Properties.createFloatProperty(volume);
		this.pitch = Properties.createFloatProperty(pitch);
		this.looping = Properties.createBooleanProperty(looping);
	}
	
	private SoundLoop3DModifier(SoundLoop3DModifier src) {
		super(false, true, false);
		this.buffer = src.buffer.clone();
		this.rolloff = src.rolloff.clone();
		this.reference = src.reference.clone();
		this.cutoff = src.cutoff.clone();
		this.volume = src.volume.clone();
		this.pitch = src.pitch.clone();
		this.looping = src.looping.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	private Vector3f prevPosition = new Vector3f();
	protected void update0(UpdatePacket packet, CEObject obj) {
		readyForRelease = true;
		
		if(!CHANNELS.containsKey(obj)) {
			if(packet.isPaused()) return;
			AudioChannel channel = AudioChannel.create();
			channel.setLooping(looping.get());
			channel.setVolume(volume.get());
			channel.setPitch(pitch.get());
			channel.setRolloff(rolloff.get());
			channel.setReference(reference.get());
			channel.setCutoff(cutoff.get());
			channel.setPosition(obj.getPosition());
			channel.setVelocity(Vector3f.sub(obj.getPosition(), prevPosition));
			
			if(looping.get()) channel.play(buffer.get());
			CHANNELS.put(obj, new KVEntry<AudioChannel, Boolean>(channel, true));
		}
		else {
			CHANNELS.get(obj).setValue(true);
			AudioChannel channel = CHANNELS.get(obj).getKey();
			channel.setLooping(looping.get());
			channel.setVolume(volume.get());
			channel.setPitch(pitch.get());
			channel.setRolloff(rolloff.get());
			channel.setReference(reference.get());
			channel.setCutoff(cutoff.get());
			channel.setPosition(obj.getPosition());
			channel.setVelocity(Vector3f.sub(obj.getPosition(), prevPosition));
			if(packet.isPaused() && channel.isPlaying()) channel.pause();
			else if(!packet.isPaused() && !channel.isPlaying() && looping.get()) channel.resume();
		}
		prevPosition = obj.getPosition();
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		buffer.update(!packet.isPaused());
		rolloff.update(!packet.isPaused());
		reference.update(!packet.isPaused());
		cutoff.update(!packet.isPaused());
		volume.update(!packet.isPaused());
		pitch.update(!packet.isPaused());
		looping.update(!packet.isPaused());
		
		if(readyForRelease) {
			for(CEObject _obj : new ArrayList<CEObject>(CHANNELS.keySet())) {
				if(!CHANNELS.get(_obj).getValue()) CHANNELS.remove(_obj);
				else CHANNELS.get(_obj).setValue(false);
			}
			readyForRelease = false;
		}
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Audio Media", Properties.createProperty(GLData[].class, () -> new GLData[] {buffer.get()}, s -> buffer.set((AudioBuffer) s[0])));
		ctrl.getProperty("Audio Media").setUserPointer(new MediaType[] {MediaType.WAV});
		ctrl.withProperty("Rolloff Factor", rolloff);
		ctrl.withProperty("Reference Dist", reference);
		ctrl.withProperty("Cutoff Dist", cutoff);
		ctrl.withProperty("Volume", volume);
		ctrl.withProperty("Pitch", pitch);
		ctrl.withProperty("Looping", looping);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 5);
		return ctrl;
	}
	
	public void replay(CEObject obj) {
		if(!CHANNELS.containsKey(obj))
			return;
		CHANNELS.get(obj).getKey().stop();
		CHANNELS.get(obj).getKey().play(buffer.get());
	}
	
	@Override
	public boolean containsData(GLData data) {
		for(CEObject obj : CHANNELS.keySet()) if(CHANNELS.get(obj).getKey() == data) return true;
		return data == buffer.get();
	}
	
	public AudioBuffer getBuffer() {
		return buffer.get();
	}
	
	public void setBuffer(AudioBuffer buffer) {
		this.buffer.set(buffer);
	}
	
	public Property<AudioBuffer> bufferProperty() {
		return buffer;
	}
	
	public float getRolloff() {
		return rolloff.get();
	}
	
	public void setRolloff(float rolloff) {
		this.rolloff.set(rolloff);
	}
	
	public Property<Float> rolloffProperty() {
		return rolloff;
	}
	
	public float getReference() {
		return reference.get();
	}
	
	public void setReference(float reference) {
		this.reference.set(reference);
	}
	
	public Property<Float> referenceProperty() {
		return reference;
	}
	
	public float getCutoff() {
		return cutoff.get();
	}
	
	public void setCutoff(float cutoff) {
		this.cutoff.set(cutoff);
	}
	
	public Property<Float> cutoffProperty() {
		return cutoff;
	}
	
	public float getVolume() {
		return volume.get();
	}
	
	public void setVolume(float volume) {
		this.volume.set(volume);
	}
	
	public Property<Float> volumeProperty() {
		return volume;
	}
	
	public float getPitch() {
		return pitch.get();
	}
	
	public void setPitch(float pitch) {
		this.pitch.set(pitch);
	}
	
	public Property<Float> pitchProperty() {
		return pitch;
	}
	
	public boolean isLooping() {
		return looping.get();
	}
	
	public void setLooping(boolean looping) {
		this.looping.set(looping);
	}
	
	public Property<Boolean> loopingProperty() {
		return looping;
	}
	
	public Modifier duplicate() {
		return new SoundLoop3DModifier(this);
	}
	
}
