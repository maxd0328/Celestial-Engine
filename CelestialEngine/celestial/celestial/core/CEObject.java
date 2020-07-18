package celestial.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.GLData;
import celestial.error.CelestialGenericException;
import celestial.glutil.RotationPattern;
import celestial.render.RenderPacket;
import celestial.render.Renderer;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderTemplate;
import celestial.util.Event;
import celestial.util.Predicate;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.mesh.AbstractMeshModifier;

/**
 * WARNING: Comparable implementation uses a static referencing of CERenderer, multiple renderer instantiations could affect
 * results of object comparisons.
 * 
 * @author Max Derbenwick
 */
public class CEObject implements Comparable<CEObject>, java.io.Serializable {
	
	private static final long serialVersionUID = 884022409458801571L;
	
	public static final int CONFIGURATION_COUNT = 32;
	
	protected final ArrayList<Modifier> modifiers;
	
	protected final Property<String> identifier;
	protected final Property<Vector3f> position, rotation, scale;
	protected final Property<Boolean> enabled;
	protected final ObjectConstraints constraints;
	
	protected final Property<CEObject> parent;
	protected final Property<Boolean> translateOnly;
	protected final Property<Boolean> uprightParent;
	
	protected final Property<Float>[] configurations;
	
	protected final Collection<Event> preRenderEvents;
	protected final Collection<Event> postRenderEvents;
	
	@SuppressWarnings("unchecked")
	public CEObject(String identifier, Vector3f position, Vector3f rotation, Vector3f scale, ObjectConstraints constraints, Modifier... modifiers) {
		this.modifiers = new ArrayList<Modifier>();
		for(Modifier modifier : modifiers) addModifier(modifier);
		
		this.identifier = Properties.createStringProperty(identifier);
		this.position = Properties.createVec3Property(position);
		this.rotation = Properties.createVec3Property(rotation);
		this.scale = Properties.createVec3Property(scale);
		this.enabled = Properties.createBooleanProperty(true);
		this.constraints = constraints;
		
		this.parent = Properties.createProperty(CEObject.class);
		this.translateOnly = Properties.createBooleanProperty();
		this.uprightParent = Properties.createBooleanProperty();
		
		this.configurations = (Property<Float>[]) Array.newInstance(Property.class, CONFIGURATION_COUNT);
		for(int i = 0 ; i < configurations.length ; ++i) configurations[i] = Properties.createFloatProperty();
		
		this.preRenderEvents = new ArrayList<>();
		this.postRenderEvents = new ArrayList<>();
	}
	
	@SuppressWarnings("unchecked")
	public CEObject(CEObject obj) {
		this.modifiers = new ArrayList<Modifier>();
		for(Modifier modifier : obj.modifiers) addModifier(modifier.duplicate());
		
		this.identifier = obj.identifier.clone();
		this.position = obj.position.clone();
		this.rotation = obj.rotation.clone();
		this.scale = obj.scale.clone();
		this.constraints = new ObjectConstraints(obj.getConstraints().getFrustumRadius(), obj.getConstraints()
				.getCullDistance(), obj.getConstraints().getFboCullDistance(), obj.getConstraints().getLimitedFboCullDistance());
		this.enabled = obj.enabled.clone();
		this.configurations = (Property<Float>[]) Array.newInstance(Property.class, CONFIGURATION_COUNT);
		for(int i = 0 ; i < configurations.length ; ++i) configurations[i] = obj.configurations[i].clone();
		this.parent = obj.parent.clone();
		this.translateOnly = obj.translateOnly.clone();
		this.uprightParent = obj.uprightParent.clone();
		
		this.preRenderEvents = new ArrayList<>(obj.preRenderEvents);
		this.postRenderEvents = new ArrayList<>(obj.postRenderEvents);
	}
	
	public String getIdentifier() {
		return identifier.get();
	}
	
	public void setIdentifier(String identifier) {
		this.identifier.set(identifier);
	}
	
	public Property<String> identifierProperty() {
		return identifier;
	}
	
	public Vector3f getPosition() {
		return getPosition(RotationPattern.ROTATION_PATTERN_XYZ);
	}
	
	public Vector3f getPosition(RotationPattern pattern) {
		if(parent.get() != null && parent.get() != this) return getPositionRelativeToParent(pattern);
		else return new Vector3f(position.get());
	}
	
	public Vector3f getRotation() {
		if(parent.get() != null && parent.get() != this && !translateOnly.get()) return Vector3f.add(rotation.get(), parent.get().getRotation());
		return new Vector3f(rotation.get());
	}
	
	public Vector3f getScale() {
		if(parent.get() != null && parent.get() != this && !translateOnly.get()) {
			Vector3f a = scale.get(), b = parent.get().getScale();
			return new Vector3f(a.x * b.x, a.y * b.y, a.z * b.z);
		}
		else return scale.get();
	}
	
	public Vector3f getBasePosition() {
		return position.getBase();
	}
	
	public Vector3f getBaseRotation() {
		return rotation.getBase();
	}
	
	public Vector3f getBaseScale() {
		return scale.getBase();
	}
	
	public void setPosition(Vector3f position) {
		this.position.set(position);
	}
	
	public void setRotation(Vector3f rotation) {
		this.rotation.set(rotation);
	}
	
	public void setScale(Vector3f scale) {
		this.scale.set(scale);
	}
	
	public void setScale(float scale) {
		this.scale.set(new Vector3f(scale, scale, scale));
	}
	
	public Property<Vector3f> positionProperty() {
		return position;
	}
	
	public Property<Vector3f> rotationProperty() {
		return rotation;
	}
	
	public Property<Vector3f> scaleProperty() {
		return scale;
	}
	
	public float getMaxScale() {
		Vector3f scale = getScale();
		return Math.max(Math.max(scale.x, scale.y), scale.z);
	}
	
	public ObjectConstraints getConstraints() {
		return constraints;
	}
	
	public boolean isEnabled() {
		return enabled.get();
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}
	
	public void enable() {
		this.enabled.set(true);
	}
	
	public void disable() {
		this.enabled.set(false);
	}
	
	public Property<Boolean> enabledProperty() {
		return enabled;
	}
	
	public void increasePosition(float dx, float dy, float dz) {
		this.position.getBase().x += dx;
		this.position.getBase().y += dy;
		this.position.getBase().z += dz;
	}
	
	public void increaseRotation(float dx, float dy, float dz) {
		this.rotation.getBase().x += dx;
		this.rotation.getBase().y += dy;
		this.rotation.getBase().z += dz;
	}
	
	public void increaseScale(float dx, float dy, float dz) {
		this.scale.getBase().x += dx;
		this.scale.getBase().y += dy;
		this.scale.getBase().z += dz;
	}
	
	public void increasePosition(Vector3f position) {
		increasePosition(position.x, position.y, position.z);
	}
	
	public void increaseRotation(Vector3f rotation) {
		increaseRotation(rotation.x, rotation.y, rotation.z);
	}
	
	public void increaseScale(Vector3f scale) {
		increaseScale(scale.x, scale.y, scale.z);
	}
	
	public synchronized boolean addModifier(Modifier modifier) {
		boolean result;
		if(result = (modifier.isAllowMultiple() || getModifier(modifier.getClass()) == null)) modifiers.add(modifier);
		return result;
	}
	
	public synchronized boolean insertModifier(int index, Modifier modifier) {
		boolean result;
		if(result = (modifier.isAllowMultiple() || getModifier(modifier.getClass()) == null)) modifiers.add(index, modifier);
		return result;
	}
	
	public synchronized ArrayList<Modifier> getModifiers() {
		return new ArrayList<Modifier>(modifiers);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Modifier> T getModifier(Class<T> type) {
		for(Modifier mod : modifiers) {
			if(type.isInstance(mod)) return (T) mod;
		}
		return null;
	}
	
	public synchronized void removeModifier(Modifier modifier) {
		this.modifiers.remove(modifier);
	}
	
	public synchronized void removeModifier(int index) {
		this.modifiers.remove(index);
	}
	
	public boolean containsData(GLData data) {
		boolean containsData = false;
		for(Modifier mod : modifiers) containsData = containsData || mod.containsData(data);
		return containsData;
	}
	
	public void preRender(RenderPacket packet, int index) {
		if(enabled.get()) modifiers.get(index).preRender(packet, this);
	}
	
	public void render(RenderPacket packet, int index) {
		if(enabled.get()) modifiers.get(index).render(packet, this);
	}
	
	public void postRender(RenderPacket packet, int index) {
		if(enabled.get()) modifiers.get(index).postRender(packet, this);
	}
	
	protected boolean updateToggle = false;
	
	public void update0(UpdatePacket packet) {
		if(!enabled.get()) return;
		if(updateToggle) return;
		updateToggle = true;
		for(Modifier mod : modifiers) mod.update0(packet, this);
	}
	
	public void update1(UpdatePacket packet) {
		if(!enabled.get()) return;
		if(!updateToggle) return;
		updateToggle = false;
		for(Modifier mod : modifiers) {
			mod.update1(packet, this);
			mod.update1All(packet, this);
		}
		
		identifier.update(!packet.isPaused());
		position.update(!packet.isPaused());
		rotation.update(!packet.isPaused());
		scale.update(!packet.isPaused());
		enabled.update(!packet.isPaused());
		parent.update(!packet.isPaused());
		translateOnly.update(!packet.isPaused());
		for(int i = 0 ; i < configurations.length ; ++i) configurations[i].update(!packet.isPaused());
	}
	
	public boolean isValidParent(CEObject obj) {
		if(obj == null) return true;
		else if(obj == this) return false;
		else if(obj.getParent() == null) return true;
		else if(obj.getParent() == this) return false;
		else return isValidParent(obj.getParent());
	}
	
	public void setParent(CEObject parent, boolean translateOnly, boolean uprightParent) {
		setParent(parent, translateOnly, uprightParent, RotationPattern.ROTATION_PATTERN_XYZ);
	}
	
	public void setParent(CEObject parent, boolean translateOnly, boolean uprightParent, RotationPattern pattern) {
		if(!isValidParent(parent)) return;
		if(this.parent.get() != null && this.parent.get() != this) {
			this.position.set(getPosition());
			this.rotation.set(getRotation());
			this.scale.set(getScale());
		}
		this.parent.set(parent);
		this.translateOnly.set(translateOnly);
		this.uprightParent.set(uprightParent);
		if(parent != null && parent != this) {
			this.position.getBase().translate(parent.getPosition().negate());
			if(!translateOnly) {
				this.position.getBase().x *= 1f / (parent.getScale().x != 0 ? parent.getScale().x : 1);
				this.position.getBase().y *= 1f / (parent.getScale().y != 0 ? parent.getScale().y : 1);
				this.position.getBase().z *= 1f / (parent.getScale().z != 0 ? parent.getScale().z : 1);
			}
			
			if(!uprightParent) {
				Matrix4f rotation = new Matrix4f();
				rotateMatrix(rotation, parent.getRotation(), pattern, true);
				if(!translateOnly) this.position.getBase().set(rotation.transform(new Vector4f(this.position.get())));
			}
			
			if(!translateOnly) {
				this.scale.getBase().x /= parent.getScale().x;
				this.scale.getBase().y /= parent.getScale().y;
				this.scale.getBase().z /= parent.getScale().z;
			}
			if(!translateOnly) this.rotation.getBase().translate(parent.getRotation().negate());
		}
	}
	
	public Vector3f getRotatedVector(Vector3f vec) {
		return getRotatedVector(vec, RotationPattern.ROTATION_PATTERN_XYZ, true, true, true);
	}
	
	public Vector3f getRotatedVector(Vector3f vec, boolean x, boolean y, boolean z) {
		return getRotatedVector(vec, RotationPattern.ROTATION_PATTERN_XYZ, x, y, z);
	}
	
	public Vector3f getRotatedVector(Vector3f vec, RotationPattern pattern) {
		return getRotatedVector(vec, pattern, true, true, true);
	}
	
	public Vector3f getRotatedVector(Vector3f vec, RotationPattern pattern, boolean x, boolean y, boolean z) {
		Matrix4f rotationMatrix = new Matrix4f();
		// Don't know why it required all the switching around but it works
		rotateMatrix(rotationMatrix, getRotation(), pattern, true);
		return new Vector3f(rotationMatrix.transform(new Vector4f(vec)));
	}
	
	public Vector3f getForwardVector() {
		return getRotatedVector(new Vector3f(0, 0, -1));
	}
	
	public Vector3f getUpVector() {
		return getRotatedVector(new Vector3f(0, 1, 0));
	}
	
	public Vector3f getRightVector() {
		return Vector3f.cross(getForwardVector(), getUpVector());
	}
	
	public Matrix4f getTransformation() {
		return getTransformation(RotationPattern.ROTATION_PATTERN_XYZ);
	}
	
	public Matrix4f getTransformation(RotationPattern pattern) {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(getPosition());
		rotateMatrix(matrix, getRotation(), pattern, false);
		matrix.scale(getScale());
		return matrix;
	}
	
	public Matrix4f getRotationTransformation() {
		return getRotationTransformation(RotationPattern.ROTATION_PATTERN_XYZ);
	}
	
	public Matrix4f getRotationTransformation(RotationPattern pattern) {
		Matrix4f matrix = new Matrix4f();
		rotateMatrix(matrix, getRotation(), pattern, false);
		return matrix;
	}
	
	public CEObject getParent() {
		return parent.get() == this ? null : parent.get();
	}
	
	public Property<CEObject> parentProperty() {
		return parent;
	}
	
	public boolean isTranslateOnly() {
		return parent.get() != null && translateOnly.get();
	}
	
	public Property<Boolean> translateOnlyProperty() {
		return translateOnly;
	}
	
	public boolean isUprightParent() {
		return uprightParent.get() && parent.get() != null;
	}
	
	public Property<Boolean> uprightParentProperty() {
		return uprightParent;
	}
	
	public float getConfiguration(int index) {
		if(index < 0 || index >= CONFIGURATION_COUNT) throw new CelestialGenericException("Configuration out of bounds");
		return configurations[index].get();
	}
	
	public void setConfiguration(int index, float value) {
		if(index < 0 || index >= CONFIGURATION_COUNT) throw new CelestialGenericException("Configuration out of bounds");
		configurations[index].set(value);
	}
	
	public Property<Float> configurationProperty(int index) {
		if(index < 0 || index >= CONFIGURATION_COUNT) throw new CelestialGenericException("Configuration out of bounds");
		return configurations[index];
	}
	
	public Collection<Event> getPreRenderEvents() {
		return preRenderEvents;
	}
	
	public Collection<Event> getPostRenderEvents() {
		return postRenderEvents;
	}
	
	public Vector3f closestPointOnPlane(float a, float b, float c, float d, int snappingAmount) {
		Vector3f snappedPosition = snappingAmount != 0 ? Vector3f.sub(getPosition(), new Vector3f(snappingAmount / 2)) : getPosition();
		if(snappingAmount != 0) snappedPosition = new Vector3f(snappedPosition.x - snappedPosition.x % snappingAmount,
				snappedPosition.y - snappedPosition.y % snappingAmount, snappedPosition.z - snappedPosition.z % snappingAmount);
		
		Vector3f planeNormal = new Vector3f(a, b, c).normalize();
		Vector3f planePoint = new Vector3f(-planeNormal.x * d, -planeNormal.y * d, -planeNormal.z * d);
		
		float signedDist = planeNormal.dot(Vector3f.sub(snappedPosition, planePoint));
		return Vector3f.sub(snappedPosition, planeNormal.scale(signedDist));
	}
	
	public ShaderTemplate toShaderTemplate() {
		return toShaderTemplate(e -> true);
	}
	
	public ShaderTemplate toShaderTemplate(Predicate<Modifier> predicate) {
		ArrayList<Modifier> modifiers = new ArrayList<>();
		this.modifiers.forEach(mod -> { if(mod.hasShader() && predicate.validate(mod)) modifiers.add(mod); });
		
		return new ShaderTemplate(modifiers);
	}
	
	private Vector3f getPositionRelativeToParent(RotationPattern pattern) {
		if(parent.get() == null) return new Vector3f(position.get());
		Vector3f position = this.position.get();

		if(!uprightParent.get()) {
			Matrix4f rotation = new Matrix4f();
			rotateMatrix(rotation, parent.get().getRotation(), pattern, false);
			if(!translateOnly.get()) position = new Vector3f(rotation.transform(new Vector4f(position)));
		}
		
		if(!translateOnly.get()) {
			position.x *= parent.get().getScale().x;
			position.y *= parent.get().getScale().y;
			position.z *= parent.get().getScale().z;
		}
		
		position.translate(parent.get().getPosition());
		return position;
	}
	
	private void rotateMatrix(Matrix4f matrix, Vector3f rotation, RotationPattern pattern, boolean inverse) {
		rotateMatrix(matrix, rotation, pattern, inverse, true, true, true);
	}
	
	private void rotateMatrix(Matrix4f matrix, Vector3f rotation, RotationPattern pattern, boolean inverse, boolean x, boolean y, boolean z) {
		if(!inverse) {
			switch(pattern) {
			case ROTATION_PATTERN_YZX:
				if(y) matrix.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				if(z) matrix.rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				if(x) matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				break;
			case ROTATION_PATTERN_ZXY:
				if(z) matrix.rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				if(x) matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				if(y) matrix.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				break;
			case ROTATION_PATTERN_ZYX:
				if(z) matrix.rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				if(y) matrix.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				if(x) matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				break;
			case ROTATION_PATTERN_XYZ:
			default:
				if(x) matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				if(y) matrix.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				if(z) matrix.rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				break;
			}
		}
		else {
			switch(pattern) {
			case ROTATION_PATTERN_YZX:
				if(x) matrix.rotate((float) -Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				if(z) matrix.rotate((float) -Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				if(y) matrix.rotate((float) -Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				break;
			case ROTATION_PATTERN_ZXY:
				if(y) matrix.rotate((float) -Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				if(x) matrix.rotate((float) -Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				if(z) matrix.rotate((float) -Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				break;
			case ROTATION_PATTERN_ZYX:
				if(x) matrix.rotate((float) -Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				if(y) matrix.rotate((float) -Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				if(z) matrix.rotate((float) -Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				break;
			case ROTATION_PATTERN_XYZ:
			default:
				if(z) matrix.rotate((float) -Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
				if(y) matrix.rotate((float) -Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
				if(x) matrix.rotate((float) -Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
				break;
			}
		}
	}
	
	@Override
	public int compareTo(CEObject o) {
		AbstractMeshModifier a = this.getModifier(AbstractMeshModifier.class), b = o.getModifier(AbstractMeshModifier.class);
		if(a != null && a.getBlendMode() != AbstractMeshModifier.BLEND_MODE_ALPHA && a.getBlendMode() != AbstractMeshModifier.BLEND_MODE_ADDITIVE) a = null;
		if(b != null && b.getBlendMode() != AbstractMeshModifier.BLEND_MODE_ALPHA && b.getBlendMode() != AbstractMeshModifier.BLEND_MODE_ADDITIVE) b = null;
		
		if(a == null && b != null) return -1;
		else if(a != null && b == null) return 1;
		else if(a == null && b == null) return 0;
		else {
			float distA = Vector3f.sub(getPosition(), Renderer.REFERENCE_POSITION_TMP).length();
			float distB = Vector3f.sub(o.getPosition(), Renderer.REFERENCE_POSITION_TMP).length();
			return distA > distB ? -1 : distA < distB ? 1 : 0;
		}
	}
	
}
