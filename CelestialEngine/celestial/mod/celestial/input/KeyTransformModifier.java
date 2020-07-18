package mod.celestial.input;

import org.lwjgl.input.Keyboard;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.beans.property.SelectiveProperty.PropertySelection;
import celestial.core.SystemInput;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.glutil.RotationPattern;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Vector3f;

public final class KeyTransformModifier extends Modifier {
	
	private static final long serialVersionUID = -2861266371932456986L;
	
	public static final Factory<KeyTransformModifier> FACTORY = () -> new KeyTransformModifier(0, new Vector3f(),
			new Vector3f(), new Vector3f(), false, false, false, RotationPattern.ROTATION_PATTERN_XYZ);
	
	private final Property<Integer> glKey;
	private final Property<Vector3f> dp;
	private final Property<Vector3f> dr;
	private final Property<Vector3f> ds;
	private final Property<Boolean> localX;
	private final Property<Boolean> localY;
	private final Property<Boolean> localZ;
	private final Property<RotationPattern> pattern;
	
	public KeyTransformModifier(int glKey, Vector3f dp, Vector3f dr, Vector3f ds, boolean localX, boolean localY, boolean localZ, RotationPattern pattern) {
		super(false, true, false);
		this.glKey = Properties.createIntegerProperty(glKey);
		this.dp = Properties.createVec3Property(dp);
		this.dr = Properties.createVec3Property(dr);
		this.ds = Properties.createVec3Property(ds);
		this.localX = Properties.createBooleanProperty(localX);
		this.localY = Properties.createBooleanProperty(localY);
		this.localZ = Properties.createBooleanProperty(localZ);
		this.pattern = Properties.createProperty(RotationPattern.class, pattern);
	}
	
	private KeyTransformModifier(KeyTransformModifier src) {
		super(false, true, false);
		this.glKey = src.glKey.clone();
		this.dp = src.dp.clone();
		this.dr = src.dr.clone();
		this.ds = src.ds.clone();
		this.localX = src.localX.clone();
		this.localY = src.localY.clone();
		this.localZ = src.localZ.clone();
		this.pattern = src.pattern.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	@Override
	protected void update1All(UpdatePacket packet, CEObject obj) {
		if(!packet.isPaused() && Keyboard.isKeyDown(glKey.get())) {
			if(localX.get() || localY.get() || localZ.get())
				obj.increasePosition(obj.getRotatedVector(dp.get(), pattern.get(), localX.get(), localY.get(), localZ.get()));
			else obj.increasePosition(dp.get());
			obj.increaseRotation(dr.get());
			obj.increaseScale(ds.get());
		}
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		glKey.update(!packet.isPaused());
		dp.update(!packet.isPaused());
		dr.update(!packet.isPaused());
		ds.update(!packet.isPaused());
		localX.update(!packet.isPaused());
		localY.update(!packet.isPaused());
		localZ.update(!packet.isPaused());
		pattern.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Key ID", new SelectiveProperty<Integer>(glKey, getKeyMap()));
		ctrl.withProperty("D/DT Position", dp);
		ctrl.withProperty("D/DT Rotation", dr);
		ctrl.withProperty("D/DT Scale", ds);
		ctrl.withProperty("Localized X", localX);
		ctrl.withProperty("Localized Y", localY);
		ctrl.withProperty("Localized Z", localZ);
		ctrl.withProperty("Rotation Pattern", new SelectiveProperty<RotationPattern>(pattern, RotationPattern.toSelectionList()));
		return ctrl;
	}
	
	public int getGLKey() {
		return glKey.get();
	}
	
	public void setGLKey(int glKey) {
		this.glKey.set(glKey);
	}
	
	public Property<Integer> glKeyProperty() {
		return glKey;
	}
	
	public Vector3f getDPosition() {
		return dp.get();
	}
	
	public void setDPosition(Vector3f dp) {
		this.dp.set(dp);
	}
	
	public Property<Vector3f> dpProperty() {
		return dp;
	}
	
	public Vector3f getDRotation() {
		return dr.get();
	}
	
	public void setDRotation(Vector3f dr) {
		this.dr.set(dr);
	}
	
	public Property<Vector3f> drProperty() {
		return dr;
	}
	
	public Vector3f getDScale() {
		return ds.get();
	}
	
	public void setDScale(Vector3f ds) {
		this.ds.set(ds);
	}
	
	public Property<Vector3f> dsProperty() {
		return ds;
	}
	
	public boolean isLocalX() {
		return localX.get();
	}
	
	public void setLocalX(boolean localX) {
		this.localX.set(localX);
	}
	
	public Property<Boolean> localXProperty() {
		return localX;
	}
	
	public boolean isLocalY() {
		return localY.get();
	}
	
	public void setLocalY(boolean localY) {
		this.localY.set(localY);
	}
	
	public Property<Boolean> localYProperty() {
		return localY;
	}
	
	public boolean isLocalZ() {
		return localZ.get();
	}
	
	public void setLocalZ(boolean localZ) {
		this.localZ.set(localZ);
	}
	
	public Property<Boolean> localZProperty() {
		return localZ;
	}
	
	public RotationPattern getPattern() {
		return pattern.get();
	}
	
	public void setPattern(RotationPattern pattern) {
		this.pattern.set(pattern);
	}
	
	public Property<RotationPattern> patternProperty() {
		return pattern;
	}
	
	public Modifier duplicate() {
		return new KeyTransformModifier(this);
	}
	
	@SuppressWarnings("unchecked")
	private static final PropertySelection<Integer>[] getKeyMap() {
		PropertySelection<Integer>[] keyMap = (PropertySelection<Integer>[]) new PropertySelection[SystemInput.getKeyMap().size()];
		int i = 0;
		for(String name : SystemInput.getKeyMap().keySet()) {
			keyMap[i++] = new PropertySelection<Integer>(name, SystemInput.getKeyMap().get(name));
		}
		return keyMap;
	}
	
}
