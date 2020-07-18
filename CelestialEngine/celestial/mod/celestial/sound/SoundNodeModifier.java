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

public final class SoundNodeModifier extends Modifier {
	
	private static final long serialVersionUID = 2251384000080614619L;
	
	public static final Factory<SoundNodeModifier> FACTORY = () -> new SoundNodeModifier(null, 0f, 0f, 0f, 1f);
	
	private static final HashMap<CEObject, KVEntry<AudioChannel, Boolean>> CHANNELS = new HashMap<CEObject, KVEntry<AudioChannel, Boolean>>();
	private static boolean readyForRelease = false;
	
	private final Property<AudioBuffer> buffer;
	private final Property<Float> innerRadius;
	private final Property<Float> outerRadius;
	private final Property<Float> volume;
	private final Property<Float> pitch;
	
	public SoundNodeModifier(AudioBuffer buffer, float innerRadius, float outerRadius, float volume, float pitch) {
		super(false, true, false);
		this.buffer = Properties.createProperty(AudioBuffer.class, buffer);
		this.innerRadius = Properties.createFloatProperty(innerRadius);
		this.outerRadius = Properties.createFloatProperty(outerRadius);
		this.volume = Properties.createFloatProperty(volume);
		this.pitch = Properties.createFloatProperty(pitch);
	}
	
	private SoundNodeModifier(SoundNodeModifier src) {
		super(false, true, false);
		this.buffer = src.buffer.clone();
		this.innerRadius = src.innerRadius.clone();
		this.outerRadius = src.outerRadius.clone();
		this.volume = src.volume.clone();
		this.pitch = src.pitch.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		readyForRelease = true;
		float distance = Vector3f.sub(packet.getCamera().getPosition(), obj.getPosition()).length();
		float inRad = innerRadius.get(), outRad = inRad + outerRadius.get();
		float volume = this.volume.get() * (distance <= inRad ? 1 : distance >= outRad ? 0 : 1f - (distance - inRad) / (outRad - inRad));
		
		if(!CHANNELS.containsKey(obj)) {
			if(packet.isPaused()) return;
			AudioChannel channel = AudioChannel.create();
			channel.setLooping(true);
			channel.setVolume(volume);
			channel.setPitch(pitch.get());
			channel.setPosition(packet.getCamera().getPosition());
			
			channel.play(buffer.get());
			CHANNELS.put(obj, new KVEntry<AudioChannel, Boolean>(channel, true));
		}
		else {
			CHANNELS.get(obj).setValue(true);
			AudioChannel channel = CHANNELS.get(obj).getKey();
			channel.setVolume(volume);
			channel.setPitch(pitch.get());
			channel.setPosition(packet.getCamera().getPosition());
			if(packet.isPaused() && channel.isPlaying()) channel.pause();
			else if(!packet.isPaused() && !channel.isPlaying()) channel.resume();
		}
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		buffer.update(!packet.isPaused());
		innerRadius.update(!packet.isPaused());
		outerRadius.update(!packet.isPaused());
		volume.update(!packet.isPaused());
		pitch.update(!packet.isPaused());
		
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
		ctrl.withProperty("Inner Radius", innerRadius);
		ctrl.withProperty("Outer Radius", outerRadius);
		ctrl.withProperty("Volume", volume);
		ctrl.withProperty("Pitch", pitch);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 4);
		return ctrl;
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
	
	public float getInnerRadius() {
		return innerRadius.get();
	}
	
	public void setInnerRadius(float innerRadius) {
		this.innerRadius.set(innerRadius);
	}
	
	public Property<Float> innerRadiusProperty() {
		return innerRadius;
	}
	
	public float getOuterRadius() {
		return outerRadius.get();
	}
	
	public void setOuterRadius(float outerRadius) {
		this.outerRadius.set(outerRadius);
	}
	
	public Property<Float> outerRadiusProperty() {
		return outerRadius;
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
	
	public Modifier duplicate() {
		return new SoundNodeModifier(this);
	}
	
}
