package celestial.beans.driver;

import java.util.ArrayList;
import java.util.Arrays;

import celestial.core.EngineRuntime;
import celestial.error.CelestialGenericException;
import celestial.util.Factory;

public class KeyframeDriver extends Driver {
	
	private static final long serialVersionUID = -3237534323345456543L;
	
	public static final Factory<KeyframeDriver> FACTORY = () -> new KeyframeDriver(1);
	
	private int animLength;
	private final ArrayList<KeyFrame> keyFrames;
	
	private KeyFrame prev = new KeyFrame(0, 0), next = new KeyFrame(0, 0);
	private float animTime = 0;
	
	public KeyframeDriver(int animLength, KeyFrame...keyFrames) {
		this.animLength = animLength;
		this.keyFrames = new ArrayList<KeyFrame>(Arrays.asList(keyFrames));
	}
	
	public int getAnimLength() {
		return animLength;
	}
	
	public void setAnimLength(int animLength) {
		this.animLength = animLength;
	}
	
	public ArrayList<KeyFrame> getKeyFrames() {
		return keyFrames;
	}
	
	@Override
	protected void update() {
		ArrayList<Integer> frameTimes = new ArrayList<Integer>();
		for(KeyFrame frame : keyFrames) {
			if(frameTimes.contains(frame.getTime())) throw new CelestialGenericException("Cannot have multiple keyframes with identical timestamp");
			else frameTimes.add(frame.getTime());
		}
		
		if((animTime += EngineRuntime.frameTimeRelative()) >= animLength) {
			animTime = -1;
			next = getNext();
		}
		
		if(next != null && next.getTime() <= animTime) {
			prev = next;
			next = getNext();
		}
		
		if(next != null) {
			float distance = next.getValue() - prev.getValue();
			float scaled = distance / (next.getTime() - prev.getTime());
			float offset = scaled * getSmoothingFactor();
			
			super.value += offset;
		}
		
	}
	
	@Override
	protected KeyframeDriver clone() {
		KeyFrame[] arr = new KeyFrame[keyFrames.size()];
		for(int i = 0 ; i < arr.length ; ++i) arr[i] = new KeyFrame(keyFrames.get(i));
		return new KeyframeDriver(animLength, arr);
	}
	
	private KeyFrame getNext() {
		int searchDistance = Integer.MAX_VALUE;
		KeyFrame current = null;
		for(KeyFrame frame : keyFrames) if(frame.getTime() - animTime > 0 && frame.getTime() - animTime < searchDistance) {
			searchDistance = frame.getTime() - (int) animTime;
			current = frame;
		}
		return current;
	}
	
	private float getSmoothingFactor() {
		int delta = next.getTime() - prev.getTime();
		int time = (int) animTime - prev.getTime();
		float factor = (float) time / delta;
		
		if(factor <= 0.5f) return (factor / 0.5f) * 2;
		else return (1 - ((factor - 0.5f) / 0.5f)) * 2;
	}
	
	public static class KeyFrame implements java.io.Serializable {
		
		private static final long serialVersionUID = 1930192094444542667L;
		
		private int time;
		private float value;
		
		public KeyFrame(int time, float value) {
			this.time = time;
			this.value = value;
		}
		
		public KeyFrame(KeyFrame frame) {
			this.time = frame.time;
			this.value = frame.value;
		}
		
		public int getTime() {
			return time;
		}
		
		public void setTime(int time) {
			this.time = time;
		}
		
		public float getValue() {
			return value;
		}
		
		public void setValue(float value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return time + "ms";
		}
		
	}
	
}
