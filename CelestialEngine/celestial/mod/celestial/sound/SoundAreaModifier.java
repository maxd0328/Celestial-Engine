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
import studio.celestial.media.Media.MediaType;

public final class SoundAreaModifier extends Modifier {
	
	private static final long serialVersionUID = 815437029553074322L;
	
	public static final Factory<SoundAreaModifier> FACTORY = () -> new SoundAreaModifier(null, 0f, 0f, 0f, 1f, 0f, 1f);
	
	private static final HashMap<CEObject, SoundAreaData> CHANNELS = new HashMap<CEObject, SoundAreaData>();
	private static boolean readyForRelease = false;
	
	private final Property<AudioBuffer> buffer;
	private final Property<Float> boundX;
	private final Property<Float> boundY;
	private final Property<Float> boundZ;
	private final Property<Float> volumeDelta;
	private final Property<Float> volume;
	private final Property<Float> pitch;
	
	public SoundAreaModifier(AudioBuffer buffer, float boundX, float boundY, float boundZ, float volumeDelta, float volume, float pitch) {
		super(false, true, false);
		this.buffer = Properties.createProperty(AudioBuffer.class, buffer);
		this.boundX = Properties.createFloatProperty(boundX);
		this.boundY = Properties.createFloatProperty(boundY);
		this.boundZ = Properties.createFloatProperty(boundZ);
		this.volumeDelta = Properties.createFloatProperty(volumeDelta);
		this.volume = Properties.createFloatProperty(volume);
		this.pitch = Properties.createFloatProperty(pitch);
	}
	
	private SoundAreaModifier(SoundAreaModifier src) {
		super(false, true, false);
		this.buffer = src.buffer.clone();
		this.boundX = src.boundX.clone();
		this.boundY = src.boundY.clone();
		this.boundZ = src.boundZ.clone();
		this.volumeDelta = src.volumeDelta.clone();
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
		boolean inRange = Math.abs(packet.getCamera().getPosition().x - obj.getPosition().x) <= boundX.get() &&
						  Math.abs(packet.getCamera().getPosition().y - obj.getPosition().y) <= boundY.get() &&
						  Math.abs(packet.getCamera().getPosition().z - obj.getPosition().z) <= boundZ.get();
		
		float volume = inRange ? 1f : 0f;
		if(!CHANNELS.containsKey(obj)) {
			if(packet.isPaused()) return;
			AudioChannel channel = AudioChannel.create();
			channel.setLooping(true);
			channel.setVolume(this.volume.get() * volume);
			
			if(volume > 0) {
				channel.setPitch(pitch.get());
				channel.setPosition(packet.getCamera().getPosition());
				channel.play(buffer.get());
			}
			CHANNELS.put(obj, new SoundAreaData(channel, 0, true, volume > 0));
		}
		else {
			volume = CHANNELS.get(obj).prevVolume;
			if(inRange) volume += volumeDelta.get();
			else volume -= volumeDelta.get();
			volume = Math.min(1, Math.max(0, volume));
			CHANNELS.get(obj).prevVolume = volume;
			
			CHANNELS.get(obj).active = true;
			AudioChannel channel = CHANNELS.get(obj).channel;
			if(packet.isPaused()) {
				CHANNELS.get(obj).pause();
				return;
			}
			else CHANNELS.get(obj).resume();
			channel.setVolume(this.volume.get() * volume);
			if(volume > 0) {
				channel.setPitch(pitch.get());
				channel.setPosition(packet.getCamera().getPosition());
				if(!channel.isPlaying()) CHANNELS.get(obj).play(buffer.get());
			}
		}
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		buffer.update(!packet.isPaused());
		boundX.update(!packet.isPaused());
		boundY.update(!packet.isPaused());
		boundZ.update(!packet.isPaused());
		volumeDelta.update(!packet.isPaused());
		volume.update(!packet.isPaused());
		pitch.update(!packet.isPaused());
		
		if(readyForRelease) {
			for(CEObject _obj : new ArrayList<CEObject>(CHANNELS.keySet())) {
				if(!CHANNELS.get(_obj).active) CHANNELS.remove(_obj);
				else CHANNELS.get(_obj).active = false;
			}
			readyForRelease = false;
		}
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Audio Media", Properties.createProperty(GLData[].class, () -> new GLData[] {buffer.get()}, s -> buffer.set((AudioBuffer) s[0])));
		ctrl.getProperty("Audio Media").setUserPointer(new MediaType[] {MediaType.WAV});
		ctrl.withProperty("Area Bound X", boundX);
		ctrl.withProperty("Area Bound Y", boundY);
		ctrl.withProperty("Area Bound Z", boundZ);
		
		ctrl.withProperty("Volume", volume);
		ctrl.withProperty("Volume Delta", volumeDelta);
		ctrl.withProperty("Pitch", pitch);
		ctrl.withPropertyBounds(new IntervalBounds<Float>(Float.class, 0f, IntervalType.INCLUSIVE, Float.MAX_VALUE, IntervalType.INCLUSIVE, 1f), 3);
		return ctrl;
	}
	
	@Override
	public boolean containsData(GLData data) {
		for(CEObject obj : CHANNELS.keySet()) if(CHANNELS.get(obj).channel == data) return true;
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
	
	public float getBoundX() {
		return boundX.get();
	}
	
	public void setBoundX(float boundX) {
		this.boundX.set(boundX);
	}
	
	public Property<Float> boundXProperty() {
		return boundX;
	}
	
	public float getBoundY() {
		return boundY.get();
	}
	
	public void setBoundY(float boundY) {
		this.boundY.set(boundY);
	}
	
	public Property<Float> boundYProperty() {
		return boundY;
	}
	
	public float getBoundZ() {
		return boundZ.get();
	}
	
	public void setBoundZ(float boundZ) {
		this.boundZ.set(boundZ);
	}
	
	public Property<Float> boundZProperty() {
		return boundZ;
	}
	
	public float getVolumeDelta() {
		return volumeDelta.get();
	}
	
	public void setVolumeDelta(float volumeDelta) {
		this.volumeDelta.set(volumeDelta);
	}
	
	public Property<Float> volumeDeltaProperty() {
		return volumeDelta;
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
		return new SoundAreaModifier(this);
	}
	
	private static final class SoundAreaData implements java.io.Serializable {
		
		private static final long serialVersionUID = 7629896813488851672L;
		
		private AudioChannel channel;
		private float prevVolume;
		private boolean active;
		
		private boolean played;
		private boolean paused = false;
		
		public SoundAreaData(AudioChannel channel, float prevVolume, boolean active, boolean played) {
			this.channel = channel;
			this.prevVolume = prevVolume;
			this.active = active;
			this.played = played;
		}
		
		public void play(AudioBuffer buffer) {
			played = true;
			channel.play(buffer);
		}
		
		public void pause() {
			if(played && !paused) {
				paused = true;
				channel.pause();
			}
		}
		
		public void resume() {
			if(played && paused) {
				paused = false;
				channel.resume();
			}
		}
		
	}
	
}
