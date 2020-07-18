package nonEuclidianDemo;

import celestial.core.EngineRuntime;
import celestial.scene.Scene;
import mod.celestial.sound.SoundLoop3DModifier;

public class LightningController {
	
	private static final float LIGHTNING_MIN = 20.0f;
	private static final float LIGHTNING_MAX = 60.0f;
	
	private static final float WAIT_MIN = 600.0f;
	private static final float WAIT_MAX = 1800.0f;
	
	private static final float INTENSITY_MIN = 1.0f;
	private static final float INTENSITY_MAX = 6.0f;
	
	private static float timeUntilChange = WAIT_MIN;
	private static boolean lightning = false;
	
	public static void update(Scene scene) {
		if(timeUntilChange <= 0f) {
			lightning = !lightning;
			if(lightning) {
				timeUntilChange = (float) Math.random() * (LIGHTNING_MAX - LIGHTNING_MIN) + LIGHTNING_MIN;
				scene.getBaseLayer().getObject("Lightning").getModifier(SoundLoop3DModifier.class).replay(scene.getBaseLayer().getObject("Lightning"));
			}
			else
				timeUntilChange = (float) Math.random() * (WAIT_MAX - WAIT_MIN) + WAIT_MIN;
		}
		else
			timeUntilChange -= EngineRuntime.frameTimeRelative();
	}
	
	public static float getIntensity() {
		return lightning ? (float) Math.random() * (INTENSITY_MAX - INTENSITY_MIN) + INTENSITY_MIN : 0f;
	}
	
}
