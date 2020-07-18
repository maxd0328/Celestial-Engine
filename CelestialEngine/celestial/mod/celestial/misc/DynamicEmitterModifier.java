package mod.celestial.misc;

import java.util.ArrayList;
import java.util.Random;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.beans.property.SelectiveProperty;
import celestial.beans.property.SelectiveProperty.PropertySelection;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.core.CEObjectReference;
import celestial.core.EngineRuntime;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public final class DynamicEmitterModifier extends Modifier {
	
	private static final long serialVersionUID = -2091742168463959693L;
	
	public static final int SPAWN_SHAPE_POINT  = 0x0;
	public static final int SPAWN_SHAPE_LINE   = 0x1;
	public static final int SPAWN_SHAPE_CIRCLE = 0x2;
	public static final int SPAWN_SHAPE_SPHERE = 0x3;
	
	public static final int FOLLOW_TYPE_TRAIL    = 0x0;
	public static final int FOLLOW_TYPE_INITIAL  = 0x1;
	public static final int FOLLOW_TYPE_CONSTANT = 0x2;
	
	public static final Factory<DynamicEmitterModifier> FACTORY = () -> new DynamicEmitterModifier(null, 0, SPAWN_SHAPE_POINT, FOLLOW_TYPE_TRAIL,
			new Vector2f(), 0f, new Vector3f(), 0f, new Vector3f(1), new Vector3f(), new Vector3f(), new Vector3f(), false, 0f, 0f, new Vector3f(), 0f, 0f, 0f, 0f);
	
	private final Property<CEObject> object;
	private final Property<Integer> layer;
	private final Property<Integer> spawnShape, followType;
	private final Property<Vector2f> spawnDirection;
	private final Property<Float> radius;
	private final Property<Vector3f> offset;
	private final Property<Float> frequency;
	private final Property<Vector3f> scale;
	private final Property<Vector3f> scaleError;
	private final Property<Vector3f> rotation;
	private final Property<Vector3f> rotationError;
	private final Property<Boolean> uniformScaleError;
	private final Property<Float> life;
	private final Property<Float> lifeError;
	private final Property<Vector3f> direction;
	private final Property<Float> deviation;
	private final Property<Float> speed;
	private final Property<Float> speedError;
	private final Property<Float> gravity;
	
	private final ArrayList<EmittedObject> emittedObjects = new ArrayList<EmittedObject>();
	
	private static final Random RANDOM = new Random();
	private static int generateCounter = -1;
	
	public DynamicEmitterModifier(CEObject object, int layer, int spawnShape, int followType, Vector2f spawnDirection, float radius,
			Vector3f offset, float frequency, Vector3f scale, Vector3f scaleError, Vector3f rotation, Vector3f rotationError,
			boolean uniformScaleError, float life, float lifeError, Vector3f direction, float deviation, float speed, float speedError, float gravity) {
		super(false, true, false);
		this.object = Properties.createProperty(CEObject.class, object);
		this.layer = Properties.createIntegerProperty(layer);
		this.spawnShape = Properties.createIntegerProperty(spawnShape);
		this.followType = Properties.createIntegerProperty(followType);
		this.spawnDirection = Properties.createVec2Property(spawnDirection);
		this.radius = Properties.createFloatProperty(radius);
		this.offset = Properties.createVec3Property(offset);
		this.frequency = Properties.createFloatProperty(frequency);
		this.scale = Properties.createVec3Property(scale);
		this.scaleError = Properties.createVec3Property(scaleError);
		this.rotation = Properties.createVec3Property(rotation);
		this.rotationError = Properties.createVec3Property(rotationError);
		this.uniformScaleError = Properties.createBooleanProperty(uniformScaleError);
		this.life = Properties.createFloatProperty(life);
		this.lifeError = Properties.createFloatProperty(lifeError);
		this.direction = Properties.createVec3Property(direction);
		this.deviation = Properties.createFloatProperty(deviation);
		this.speed = Properties.createFloatProperty(speed);
		this.speedError = Properties.createFloatProperty(speedError);
		this.gravity = Properties.createFloatProperty(gravity);
	}
	
	private DynamicEmitterModifier(DynamicEmitterModifier src) {
		super(false, true, false);
		this.object = src.object.clone();
		this.layer = src.layer.clone();
		this.spawnShape = src.spawnShape.clone();
		this.followType = src.followType.clone();
		this.spawnDirection = src.spawnDirection.clone();
		this.radius = src.radius.clone();
		this.offset = src.offset.clone();
		this.frequency = src.frequency.clone();
		this.scale = src.scale.clone();
		this.scaleError = src.scaleError.clone();
		this.rotation = src.rotation.clone();
		this.rotationError = src.rotationError.clone();
		this.uniformScaleError = src.uniformScaleError.clone();
		this.life = src.life.clone();
		this.lifeError = src.lifeError.clone();
		this.direction = src.direction.clone();
		this.deviation = src.deviation.clone();
		this.speed = src.speed.clone();
		this.speedError = src.speedError.clone();
		this.gravity = src.gravity.clone();
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		if(packet.isPaused()) return;
		float delta = EngineRuntime.frameTime(), count = delta * frequency.get();
		int emitFloored = (int) Math.floor(count);
		float emitPartial = delta % 1.0f;
		for(int i = 0 ; i < emitFloored ; ++i) generate(packet, obj);
		if(Math.random() < emitPartial) generate(packet, obj);
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		if(!packet.isPaused()) {
			for(EmittedObject emittedObject : new ArrayList<EmittedObject>(emittedObjects)) {
				emittedObject.velocity.y -= emittedObject.gravity * EngineRuntime.frameTimeRelative();
				emittedObject.object.increasePosition(emittedObject.velocity);
				if(followType.get() == FOLLOW_TYPE_CONSTANT) {
					Vector3f currentPosition = new Vector3f(obj.getTransformation().transform(new Vector4f(emittedObject.relativePosition, 1f)));
					emittedObject.object.increasePosition(Vector3f.sub(currentPosition, emittedObject.previousPosition));
					emittedObject.previousPosition = currentPosition;
				}
				emittedObject.elapsedTime += EngineRuntime.frameTime();
				if(emittedObject.elapsedTime > emittedObject.lifeLength) {
					packet.getScene().getLayer(layer.get()).removeObject(emittedObject.object);
					emittedObjects.remove(emittedObject);
				}
			}
		}
		
		object.update(!packet.isPaused());
		layer.update(!packet.isPaused());
		spawnShape.update(!packet.isPaused());
		followType.update(!packet.isPaused());
		spawnDirection.update(!packet.isPaused());
		radius.update(!packet.isPaused());
		offset.update(!packet.isPaused());
		frequency.update(!packet.isPaused());
		scale.update(!packet.isPaused());
		scaleError.update(!packet.isPaused());
		rotation.update(!packet.isPaused());
		rotationError.update(!packet.isPaused());
		uniformScaleError.update(!packet.isPaused());
		life.update(!packet.isPaused());
		lifeError.update(!packet.isPaused());
		direction.update(!packet.isPaused());
		deviation.update(!packet.isPaused());
		speed.update(!packet.isPaused());
		speedError.update(!packet.isPaused());
		gravity.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Emitted Object", Properties.createStringProperty(() -> object.get() == null ? "" : object.get().getIdentifier(), s -> object.set(system.getObject(s))));
		ctrl.withProperty("Target Layer", Properties.createStringProperty(() -> system.getCurrentScene()
				.getLayer(layer.get()).getIdentifier(), s -> layer.set(system.indexOf(system.getLayer(s)))));
		ctrl.withProperty("Spawn Shape", new SelectiveProperty<Integer>(spawnShape, new PropertySelection<>("Point", SPAWN_SHAPE_POINT),
				new PropertySelection<>("Line", SPAWN_SHAPE_LINE), new PropertySelection<>("Circle", SPAWN_SHAPE_CIRCLE), new PropertySelection<>("Sphere", SPAWN_SHAPE_SPHERE)));
		ctrl.withProperty("Follow Type", new SelectiveProperty<Integer>(followType, new PropertySelection<>("Trail", FOLLOW_TYPE_TRAIL),
				new PropertySelection<>("Initial", FOLLOW_TYPE_INITIAL), new PropertySelection<>("Constant", FOLLOW_TYPE_CONSTANT)));
		
		ctrl.withProperty("Spawn Direction", spawnDirection);
		ctrl.withProperty("Radius", radius);
		ctrl.withProperty("Offset", offset);
		ctrl.withProperty("Frequency", frequency);
		ctrl.withProperty("Scale", scale);
		ctrl.withProperty("Scale Error", scaleError);
		ctrl.withProperty("Rotation", rotation);
		ctrl.withProperty("Rotation Error", rotationError);
		ctrl.withProperty("Uniform Scale", uniformScaleError);
		ctrl.withProperty("Life Length", life);
		ctrl.withProperty("Life Error", lifeError);
		ctrl.withProperty("Direction", direction);
		ctrl.withProperty("Deviation", deviation);
		ctrl.withProperty("Speed", speed);
		ctrl.withProperty("Speed Error", speedError);
		ctrl.withProperty("Gravity Compliment", gravity);
		return ctrl;
	}
	
	public CEObject getObject() {
		return object.get();
	}
	
	public void setObject(CEObject object) {
		this.object.set(object);
	}
	
	public Property<CEObject> objectProperty() {
		return object;
	}
	
	public int getLayer() {
		return layer.get();
	}
	
	public void setLayer(int layer) {
		this.layer.set(layer);
	}
	
	public Property<Integer> layerProperty() {
		return layer;
	}
	
	public int getSpawnShape() {
		return spawnShape.get();
	}
	
	public void setSpawnShape(int spawnShape) {
		this.spawnShape.set(spawnShape);
	}
	
	public Property<Integer> spawnShapeProperty() {
		return spawnShape;
	}
	
	public int getFollowType() {
		return followType.get();
	}
	
	public void setFollowType(int followType) {
		this.followType.set(followType);
	}
	
	public Property<Integer> followTypeProperty() {
		return followType;
	}
	
	public Vector2f getSpawnDirection() {
		return spawnDirection.get();
	}
	
	public void setSpawnDirection(Vector2f spawnDirection) {
		this.spawnDirection.set(spawnDirection);
	}
	
	public Property<Vector2f> spawnDirectionProperty() {
		return spawnDirection;
	}
	
	public float getRadius() {
		return radius.get();
	}
	
	public void setRadius(float radius) {
		this.radius.set(radius);
	}
	
	public Property<Float> radiusProperty() {
		return radius;
	}
	
	public Vector3f getOffset() {
		return offset.get();
	}
	
	public void setOffset(Vector3f offset) {
		this.offset.set(offset);
	}
	
	public Property<Vector3f> offsetProperty() {
		return offset;
	}
	
	public float getFrequency() {
		return frequency.get();
	}
	
	public void setFrequency(float frequency) {
		this.frequency.set(frequency);
	}
	
	public Property<Float> frequencyProperty() {
		return frequency;
	}
	
	public Vector3f getScale() {
		return scale.get();
	}
	
	public void setScale(Vector3f scale) {
		this.scale.set(scale);
	}
	
	public Property<Vector3f> scaleProperty() {
		return scale;
	}
	
	public Vector3f getScaleError() {
		return scaleError.get();
	}
	
	public void setScaleError(Vector3f scaleError) {
		this.scaleError.set(scaleError);
	}
	
	public Property<Vector3f> scaleErrorProperty() {
		return scaleError;
	}
	
	public Vector3f getRotation() {
		return rotation.get();
	}
	
	public void setRotation(Vector3f rotation) {
		this.rotation.set(rotation);
	}
	
	public Property<Vector3f> rotationProperty() {
		return rotation;
	}
	
	public Vector3f getRotationError() {
		return rotationError.get();
	}
	
	public void setRotationError(Vector3f rotationError) {
		this.rotationError.set(rotationError);
	}
	
	public Property<Vector3f> rotationErrorProperty() {
		return rotationError;
	}
	
	public boolean isUniformScaleError() {
		return uniformScaleError.get();
	}
	
	public void setUniformScaleError(boolean uniformScaleError) {
		this.uniformScaleError.set(uniformScaleError);
	}
	
	public Property<Boolean> uniformScaleErrorProperty() {
		return uniformScaleError;
	}
	
	public float getLife() {
		return life.get();
	}
	
	public void setLife(float life) {
		this.life.set(life);
	}
	
	public Property<Float> lifeProperty() {
		return life;
	}
	
	public float getLifeError() {
		return lifeError.get();
	}
	
	public void setLifeError(float lifeError) {
		this.lifeError.set(lifeError);
	}
	
	public Property<Float> lifeErrorProperty() {
		return lifeError;
	}
	
	public Vector3f getDirection() {
		return direction.get();
	}
	
	public void setDirection(Vector3f direction) {
		this.direction.set(direction);
	}
	
	public Property<Vector3f> directionProperty() {
		return direction;
	}
	
	public float getDeviation() {
		return deviation.get();
	}
	
	public void setDeviation(float deviation) {
		this.deviation.set(deviation);
	}
	
	public Property<Float> deviationProperty() {
		return deviation;
	}
	
	public float getSpeed() {
		return speed.get();
	}
	
	public void setSpeed(float speed) {
		this.speed.set(speed);
	}
	
	public Property<Float> speedProperty() {
		return speed;
	}
	
	public float getSpeedError() {
		return speedError.get();
	}
	
	public void setSpeedError(float speedError) {
		this.speedError.set(speedError);
	}
	
	public Property<Float> speedErrorProperty() {
		return speedError;
	}
	
	public float getGravity() {
		return gravity.get();
	}
	
	public void setGravity(float gravity) {
		this.gravity.set(gravity);
	}
	
	public Property<Float> gravityProperty() {
		return gravity;
	}
	
	private Matrix4f prevTransformation = new Matrix4f();
	private void generate(UpdatePacket packet, CEObject obj) {
		Vector3f systemCenter = new Vector3f(obj.getTransformation().transform(new Vector4f(offset.get(), 1f)));
		if(spawnShape.get() == SPAWN_SHAPE_LINE) {
			Matrix4f rotation = new Matrix4f();
			rotation.rotate((float) Math.toRadians(spawnDirection.get().y), new Vector3f(0, 0, 1));
			rotation.rotate((float) Math.toRadians(spawnDirection.get().x), new Vector3f(0, 1, 0));
			float bounds = 2 * RANDOM.nextFloat() * radius.get() - radius.get();
			Vector3f pointer = new Vector3f(rotation.transform(new Vector4f(bounds, 0f, 0f, 1f)));
			Vector3f.add(pointer, offset.get(), pointer);
			Vector3f relative = new Vector3f(pointer);
			pointer = new Vector3f(obj.getTransformation().transform(new Vector4f(pointer, 1f)));
			emit(packet, obj, pointer, relative);
		}
		else if(spawnShape.get() == SPAWN_SHAPE_CIRCLE) {
			Matrix4f rotation = new Matrix4f();
			rotation.rotate((float) Math.toRadians(RANDOM.nextInt(360)), new Vector3f(0, 1, 0));
			Vector3f pointer = new Vector3f(rotation.transform(new Vector4f(1f, 0f, 0f, 1f)));
			pointer.scale(RANDOM.nextFloat() * radius.get());
			rotation.setIdentity();
			rotation.rotate((float) Math.toRadians(spawnDirection.get().x), new Vector3f(1, 0, 0));
			rotation.rotate((float) Math.toRadians(spawnDirection.get().y), new Vector3f(0, 0, 1));
			pointer = new Vector3f(rotation.transform(new Vector4f(pointer, 1f)));
			Vector3f.add(pointer, offset.get(), pointer);
			Vector3f relative = new Vector3f(pointer);
			pointer = new Vector3f(obj.getTransformation().transform(new Vector4f(pointer, 1f)));
			emit(packet, obj, pointer, relative);
		}
		else if(spawnShape.get() == SPAWN_SHAPE_SPHERE) {
			Matrix4f rotation = new Matrix4f();
			rotation.rotate((float) Math.toRadians(RANDOM.nextInt(360)), new Vector3f(0, 0, 1));
			rotation.rotate((float) Math.toRadians(RANDOM.nextInt(360)), new Vector3f(0, 1, 0));
			Vector3f pointer = new Vector3f(rotation.transform(new Vector4f(1f, 0f, 0f, 1f)));
			pointer.scale(RANDOM.nextFloat() * radius.get());
			emit(packet, obj, Vector3f.add(systemCenter, pointer), pointer);
		}
		else emit(packet, obj, systemCenter, offset.get());
		prevTransformation = obj.getTransformation();
	}
	
	private void emit(UpdatePacket packet, CEObject obj, Vector3f position, Vector3f relativePosition) {
		if(packet.getScene().getLayer(layer.get()) == null || object.get() == null) return;
		
		Vector3f rotation = new Vector3f(generateActual(this.rotation.get().x, rotationError.get().x),
				generateActual(this.rotation.get().y, rotationError.get().y), generateActual(this.rotation.get().z, rotationError.get().z));
		Vector3f scale;
		if(uniformScaleError.get()) {
			float error = (RANDOM.nextFloat() - 0.5f) * 2f * Math.max(scaleError.get().x, Math.max(scaleError.get().y, scaleError.get().z));
			scale = new Vector3f(this.scale.get().x + error, this.scale.get().y + error, this.scale.get().z + error);
		}
		else scale = new Vector3f(generateActual(this.scale.get().x, scaleError.get().x),
				generateActual(this.scale.get().y, scaleError.get().y), generateActual(this.scale.get().z, scaleError.get().z));
		CEObject newObject = new CEObjectReference(object.get().getIdentifier() + "-DynamicEmitter." + (++generateCounter), position, rotation, scale, object.get());
		packet.getScene().getLayer(layer.get()).addObject(newObject);
		
		Vector3f velocity = generateDeviatedVector(direction.get(), deviation.get());
		velocity.normalize();
		velocity.scale(generateActual(speed.get(), speedError.get()));
		velocity = new Vector3f(obj.getRotationTransformation().transform(new Vector4f(velocity, 1f)));
		
		if(followType.get() == FOLLOW_TYPE_INITIAL) {
			Vector3f prevPosition = new Vector3f(prevTransformation.transform(new Vector4f(relativePosition, 1f)));
			Vector3f.add(velocity, Vector3f.sub(position, prevPosition), velocity);
		}
		
		EmittedObject emittedObject = new EmittedObject(newObject, velocity, gravity.get(), generateActual(life.get(), lifeError.get()), relativePosition);
		emittedObjects.add(emittedObject);
	}
	
	private float generateActual(float center, float error) {
		 return center + (RANDOM.nextFloat() - 0.5f) * 2f * error;
	}
	
	private Vector3f generateDeviatedVector(Vector3f coneDirection, float angle) {
		float cosAngle = (float) Math.cos(angle);
		float theta = (float) (RANDOM.nextFloat() * 2f * Math.PI);
		float z = cosAngle + (RANDOM.nextFloat() * (1 - cosAngle));
		float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));
		
		Vector4f direction = new Vector4f(x, y, z, 1);
		if (coneDirection.x != 0 || coneDirection.y != 0 || (coneDirection.z != 1 && coneDirection.z != -1)) {
			Vector3f rotateAxis = Vector3f.cross(coneDirection, new Vector3f(0, 0, 1), null);
			float rotateAngle = (float) Math.acos(coneDirection.dot(new Vector3f(0, 0, 1)));
			Matrix4f rotationMatrix = new Matrix4f();
			rotationMatrix.rotate(-rotateAngle, rotateAxis);
			rotationMatrix.transform(direction, direction);
		} else if (coneDirection.z == -1) {
			direction.z *= -1;
		}
		return new Vector3f(direction);
	}
	
	public Modifier duplicate() {
		return new DynamicEmitterModifier(this);
	}
	
	private static final class EmittedObject implements java.io.Serializable {
		
		private static final long serialVersionUID = 1365580165076019917L;
		
		private final CEObject object;
		private final Vector3f velocity;
		private final float gravity;
		private final float lifeLength;
		private Vector3f previousPosition;
		private final Vector3f relativePosition;
		
		private float elapsedTime = 0f;
		
		private EmittedObject(CEObject object, Vector3f velocity, float gravity, float lifeLength, Vector3f relativePosition) {
			this.object = object;
			this.velocity = velocity;
			this.gravity = gravity;
			this.lifeLength = lifeLength;
			this.previousPosition = object.getPosition();
			this.relativePosition = relativePosition;
		}
		
	}
	
}
