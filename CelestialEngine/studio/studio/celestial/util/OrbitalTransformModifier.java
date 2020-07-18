package studio.celestial.util;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import celestial.core.Modifier;
import celestial.ctrl.PropertyController;
import celestial.beans.driver.SmoothDriver;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.core.EngineRuntime;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;
import mod.celestial.misc.CameraModifier;
import mod.celestial.misc.CameraModifier.ProjectionState;

public final class OrbitalTransformModifier extends Modifier {
	
	private static final long serialVersionUID = 7672814810083090524L;
	
	public static final Factory<OrbitalTransformModifier> FACTORY = () -> new OrbitalTransformModifier(new Vector3f());
	
	private static final float SENSITIVITY = 0.2f;
	private static final float ZOOM_RATE = 0.00075f;
	private static final float ZOOM_GLIDE = 1.5f;
	private static final float PAN_SLOWNESS = 300;
	
	private final Property<Vector3f> center;
	private final Property<Float> angle;
	private final Property<Float> pitch;
	private float distance, deltaDistance;
	
	public OrbitalTransformModifier(Vector3f center) {
		super(false, false, false);
		this.center = Properties.createVec3Property(center);
		this.angle = Properties.createFloatProperty(225f);
		this.pitch = Properties.createFloatProperty(30f);
		this.distance = 20f;
		this.deltaDistance = 0f;
		
		SmoothDriver driver = new SmoothDriver(25f);
		this.center.subProperty(0).getDriver().set(driver);
		this.center.subProperty(1).getDriver().set(driver);
		this.center.subProperty(2).getDriver().set(driver);
		this.angle.getDriver().set(driver);
		this.pitch.getDriver().set(driver);
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		if(Mouse.isButtonDown(2)) {
			angle.setBase(angle.getBase() - Mouse.getDX() * SENSITIVITY * EngineRuntime.frameTimeRelative());
			pitch.setBase(pitch.getBase() - Mouse.getDY() * SENSITIVITY * EngineRuntime.frameTimeRelative());
		}
		pitch.setBase(Math.max(Math.min(pitch.getBase(), 90), -90));
		deltaDistance -= Mouse.getDWheel() * distance * ZOOM_RATE * EngineRuntime.frameTimeRelative();
		deltaDistance = Math.min(Math.max(deltaDistance, -distance * 0.2f), distance * 0.2f);
		distance += deltaDistance /= ZOOM_GLIDE * (float) Math.pow(1f / distance, 0.01f);
		distance = Math.min(Math.max(distance, 0.2f), 10000f);
		
		if(Mouse.isButtonDown(1)) {
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
				center.getBase().y -= (Mouse.getDY() / PAN_SLOWNESS) * distance;
			else {
				float mouseX = (Mouse.getDX() / PAN_SLOWNESS) * distance, mouseY = (Mouse.getDY() / PAN_SLOWNESS) * distance;
				
				center.getBase().x -= mouseY * Math.sin(Math.toRadians(angle.getBase()));
				center.getBase().z -= mouseY * Math.cos(Math.toRadians(angle.getBase()));
				center.getBase().z -= mouseX * Math.sin(Math.toRadians(180 - angle.getBase()));
				center.getBase().x -= mouseX * Math.cos(Math.toRadians(180 - angle.getBase()));
			}
		}
		
		float horiz = (float) (distance * Math.cos(Math.toRadians(pitch.get()))), vert = (float) (distance * Math.sin(Math.toRadians(pitch.get())));
		obj.increasePosition(center.get().x - obj.getPosition().x - (float) (horiz * Math.sin(Math.toRadians(angle.get()))),
				center.get().y - obj.getPosition().y + vert, center.get().z - obj.getPosition().z - (float) (horiz * Math.cos(Math.toRadians(angle.get()))));
		obj.setRotation(new Vector3f(pitch.get(), 180 - angle.get(), 0));
		
		if(obj.getModifier(CameraModifier.class) != null && obj.getModifier(CameraModifier.class).getProjectionState() == ProjectionState.ORTHOGRAPHIC) {
			obj.getModifier(CameraModifier.class).setOrthoWidth(distance * 0.5f);
			obj.getModifier(CameraModifier.class).setOrthoHeight(distance * 0.5f);
		}
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		center.update();
		angle.update();
		pitch.update();
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		return ctrl;
	}
	
	public Vector3f getCenter() {
		return center.get();
	}
	
	public Modifier duplicate() {
		return new OrbitalTransformModifier(new Vector3f(center.get()));
	}
	
}
