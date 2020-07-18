package nonEuclidian;

import celestial.core.Modifier;
import celestial.core.ObjectConstraints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.core.CEObjectReference;
import celestial.ctrl.FloatConverter;
import celestial.ctrl.NonZeroBounds;
import celestial.ctrl.PropertyController;
import celestial.data.FrameBuffer;
import celestial.data.GLData;
import celestial.render.RenderAbortException;
import celestial.render.RenderConstraints;
import celestial.render.RenderPacket;
import celestial.render.RenderReiterationException;
import celestial.render.UpdatePacket;
import celestial.scene.Layer;
import celestial.scene.Scene;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.ProgramModule;
import celestial.shader.Shader;
import celestial.util.Event;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.util.KVEntry;
import celestial.vecmath.Matrix3f;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Quat4f;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.mesh.AbstractMeshModifier;
import mod.celestial.misc.CameraModifier;
import mod.celestial.physics.AbstractRigidBodyModifier;

public final class PlanarPortalModifier extends Modifier {
	
	private static final long serialVersionUID = -2847327780660132079L;
	
	public static final Factory<PlanarPortalModifier> FACTORY = () -> new PlanarPortalModifier(null, new Vector3f(0, 0, -1), new Vector3f(0, 1, 0), 0f, 0f, null, 1, 1);
	
	private static final String GHOST_LAYER = "PlanarPortal.GhostLayer";
	
	private static final int RECURSE_LIMIT = 1;
	private static final Map<Integer, FrameBuffer> RECURSIVE_BUFFERS = new HashMap<>();
	private static int recurseLevel = 0;
	
	static {
		for(int i = 0 ; i < RECURSE_LIMIT ; ++i)
			RECURSIVE_BUFFERS.put(i, FrameBuffer.create(1820, 720, false, true));
	}
	
	private final Property<CEObject> connection;
	private final Property<Vector3f> forwardVector;
	private final Property<Vector3f> upVector;
	private final Property<Float> horizontalLimit;
	private final Property<Float> verticalLimit;
	private final Property<CEObject> cameraObject;
	
	private final FrameBuffer buffer;
	private final Collection<CEObject> allowedRecursions;
	private final Map<CEObject, Float> previousDistances = new HashMap<CEObject, Float>();
	private final Map<CEObject, KVEntry<Event, Event>> eventMap = new HashMap<>();
	private final Collection<CEObject> ghosts = new ArrayList<>();
	private final Vector3f prevCameraPosition = new Vector3f();
	
	private boolean frameRendered = false;
	
	public PlanarPortalModifier(CEObject connection, Vector3f forwardVector, Vector3f upVector,
			float horizontalLimit, float verticalLimit, CEObject cameraObject, int bufferWidth, int bufferHeight, CEObject... allowedRecursions) {
		super(false, false, true);
		this.connection = Properties.createProperty(CEObject.class, connection);
		this.forwardVector = Properties.createVec3Property(forwardVector);
		this.upVector = Properties.createVec3Property(upVector);
		this.horizontalLimit = Properties.createFloatProperty(horizontalLimit);
		this.verticalLimit = Properties.createFloatProperty(verticalLimit);
		this.cameraObject = Properties.createProperty(CEObject.class, cameraObject);
		this.buffer = FrameBuffer.create(bufferWidth, bufferHeight, false, true);
		this.allowedRecursions = Arrays.asList(allowedRecursions);
	}
	
	private PlanarPortalModifier(PlanarPortalModifier src) {
		super(false, false, true);
		this.connection = src.connection.clone();
		this.forwardVector = src.forwardVector.clone();
		this.upVector = src.upVector.clone();
		this.horizontalLimit = src.horizontalLimit.clone();
		this.verticalLimit = src.verticalLimit.clone();
		this.cameraObject = src.cameraObject.clone();
		this.buffer = FrameBuffer.create(src.buffer.getWidth(), src.buffer.getHeight(), false, true);
		this.allowedRecursions = new ArrayList<>(src.allowedRecursions);
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs(ShaderAttribute.$("sampler2D planarPortal"));
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		return new ShaderModule("",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("portal.glsl"), "planarPortalF"), fUniforms, fGlobals),
				getID(), ShaderModule.attribs(),
				ShaderModule.attribs()).withDependencies("mesh");
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(packet.isFboRender() && recurseLevel >= RECURSE_LIMIT) throw new RenderAbortException();
		CEObject camera = packet.getCamera();
		CEObject linkObj = this.connection.get();
		PlanarPortalModifier linkMod = linkObj == null ? null : linkObj.getModifier(PlanarPortalModifier.class);
		
		FrameBuffer buffer = !packet.isFboRender() ? this.buffer : RECURSIVE_BUFFERS.get(recurseLevel);
		
		if(linkObj != null && linkMod != null && !frameRendered) {
			Matrix4f transformation = getTransform(obj, false, true);
			Matrix4f rotationTransform = getTransform(obj, false, false);
			
			Matrix4f altTransformation = linkMod.getTransform(linkObj, true, true).invert();
			Matrix4f altRotationTransform = linkMod.getTransform(linkObj, true, false).invert();
			
			Vector3f cameraPosition = new Vector3f(transformation.transform(new Vector4f(camera.getPosition(), 1f)));
			Vector3f cameraPositionWorld = new Vector3f(altTransformation.transform(new Vector4f(cameraPosition, 1f)));
			
			Vector3f cameraForward = new Vector3f(rotationTransform.transform(new Vector4f(camera.getForwardVector(), 1f))).normalize();
			Vector3f cameraForwardWorldSpace = new Vector3f(altRotationTransform.transform(new Vector4f(cameraForward, 1f))).normalize();
			Vector3f cameraRotation = new Vector3f(calculatePitchAndYaw(cameraForwardWorldSpace), camera.getRotation().z);
			
			CEObject virtualCamera = new CEObject("PlanarPortal_TMP-Camera", cameraPositionWorld, cameraRotation, new Vector3f(1f), new ObjectConstraints());
			virtualCamera.addModifier(new CameraModifier(camera.getModifier(CameraModifier.class)));
			virtualCamera.getModifier(CameraModifier.class).updateViewMatrix(virtualCamera);
			Vector4f clipPlane = linkMod.calculateClipPlane(linkObj);
			
			RenderConstraints constraints = new RenderConstraints();
			constraints.getObjectPredicates().add(e -> e.getModifier(PlanarPortalModifier.class) == null || allowedRecursions.contains(e));
			
			packet.getRenderer().getClipPlane(0).set(clipPlane);
			if(packet.isFboRender()) recurseLevel++;
			packet.getRenderer().render(packet, virtualCamera, false, buffer.toRenderOutput(), constraints);
			if(packet.isFboRender()) recurseLevel--;
			packet.getRenderer().getClipPlane(0).disable();
			
			frameRendered = true;
			throw new RenderReiterationException();
		}
		
		int texID = packet.getShader().getCommunicator().load1i("planarPortal");
		buffer.sampleTexture();
		buffer.bind(texID);
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		frameRendered = false;
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		Layer ghostLayer = packet.getScene().getLayer(GHOST_LAYER);
		if(ghostLayer == null) {
			ghostLayer = new Layer(GHOST_LAYER);
			packet.getScene().addLayer(ghostLayer);
		}
		for(CEObject ghost : ghosts) {
			ghost.getModifier(AbstractRigidBodyModifier.class).getRestrictedObjects().remove(ghost);
			ghostLayer.removeObject(ghost);
		}
		ghosts.clear();
		
		Matrix4f transformation = getTransform(obj, false, true);
		Matrix4f rotationTransform = getTransform(obj, false, false);
		
		AbstractMeshModifier mod = obj.getModifier(AbstractMeshModifier.class);
		if(mod != null) {
			float distance = new Vector3f(transformation.transform(new Vector4f(packet.getCamera().getPosition(), 1f))).z;
			float nearPlane = packet.getCamera().getModifier(CameraModifier.class).getNearPlane() + Vector3f.sub(prevCameraPosition, packet.getCamera().getPosition()).length();
			
			if(Math.abs(distance) <= nearPlane)
				mod.setOffset(forwardVector.get().scale(nearPlane).negate());
			else mod.setOffset(new Vector3f());
			prevCameraPosition.set(packet.getCamera().getPosition());
		}
		
		for(CEObject body : getObjects(packet.getScene())) {
			if(body.getModifier(AbstractRigidBodyModifier.class) != null) {
				Vector3f position = new Vector3f(transformation.transform(new Vector4f(body.getPosition(), 1f)));
				float radius = body.getConstraints().getFrustumRadius() * body.getMaxScale();
				
				if(Math.abs(position.z) <= radius && Math.abs(position.x) <= Math.abs(horizontalLimit.get()) && Math.abs(position.y) <= Math.abs(verticalLimit.get())) {
					if(!eventMap.containsKey(obj)) {
						Event addClipPlane = new Event() {
							private static final long serialVersionUID = 2814931286258548110L;
							public void perform(Object...o) {
								((RenderPacket) o[1]).getRenderer().getClipPlane(1).set(calculateClipPlane(obj));
							}
						};
						Event removeClipPlane = new Event() {
							private static final long serialVersionUID = -5025413686216703482L;
							public void perform(Object...o) {
								((RenderPacket) o[1]).getRenderer().getClipPlane(1).disable();
							}
						};
						eventMap.put(obj, new KVEntry<>(addClipPlane, removeClipPlane));
					}
					if(!body.getPreRenderEvents().contains(eventMap.get(obj).getKey())) body.getPreRenderEvents().add(eventMap.get(obj).getKey());
					if(!body.getPostRenderEvents().contains(eventMap.get(obj).getValue())) body.getPostRenderEvents().add(eventMap.get(obj).getValue());
					
					CEObject linkObj = this.connection.get();
					PlanarPortalModifier linkMod = linkObj == null ? null : linkObj.getModifier(PlanarPortalModifier.class);
					if(linkObj != null && linkMod != null) {
						Matrix4f altTransformation = linkMod.getTransform(linkObj, true, true).invert();
						Matrix4f altRotationTransform = linkMod.getTransform(linkObj, true, false).invert();
						
						Vector3f worldSpace = new Vector3f(altTransformation.transform(new Vector4f(position.clone(), 1f)));
						
						Quat4f quat = new Quat4f().set(Matrix4f.fromEulerRotation(body.getRotation()));
						Vector3f axis = new Vector3f(rotationTransform.transform(new Vector4f(quat.getAxis(), 0f)));
						Vector3f axisWorld = new Vector3f(altRotationTransform.transform(new Vector4f(axis, 0f)));
						Vector3f rotation = new Quat4f(axisWorld, quat.w).toRotationMatrix().toEulerRotation();
						
						CEObject ghost = new CEObjectReference(body.getIdentifier() + ".Ghost", worldSpace, rotation, body.getScale(), body);
						ghost.getPreRenderEvents().add(o -> ((RenderPacket) o[1]).getRenderer().getClipPlane(1).set(linkMod.calculateClipPlane(linkObj)));
						ghost.getPostRenderEvents().add(o -> ((RenderPacket) o[1]).getRenderer().getClipPlane(1).disable());
						body.getModifier(AbstractRigidBodyModifier.class).getRestrictedObjects().add(ghost);
						ghostLayer.addObject(ghost);
						ghosts.add(ghost);
					}
				}
				else if(eventMap.containsKey(obj)) {
					body.getPreRenderEvents().remove(eventMap.get(obj).getKey());
					body.getPostRenderEvents().remove(eventMap.get(obj).getValue());
				}
			}
		}
	}
	
	@Override
	protected void update1All(UpdatePacket packet, CEObject obj) {
		Matrix4f transformation = getTransform(obj, false, true);
		Matrix4f rotationTransform = getTransform(obj, false, false);
		
		for(CEObject body : getObjects(packet.getScene())){
			if(body.getModifier(AbstractRigidBodyModifier.class) != null) {
				Vector3f position = new Vector3f(transformation.transform(new Vector4f(body.getPosition(), 1f)));
				
				float signedDistance = position.z;
				if(!previousDistances.containsKey(body)) {
					previousDistances.put(body, signedDistance);
					continue;
				}
				
				float prevSignedDistance = previousDistances.get(body);
				if(signedDistance < 0f && prevSignedDistance >= 0f && Math.abs(position.x) <= Math.abs(horizontalLimit.get()) && Math.abs(position.y) <= Math.abs(verticalLimit.get())) {
					CEObject linkObj = this.connection.get();
					PlanarPortalModifier linkMod = linkObj == null ? null : linkObj.getModifier(PlanarPortalModifier.class);
					if(linkObj != null && linkMod != null) {
						Matrix4f relTransformation = linkMod.getTransform(linkObj, false, true).invert();
						Matrix4f altTransformation = linkMod.getTransform(linkObj, true, true).invert();
						Matrix4f altRotationTransform = linkMod.getTransform(linkObj, true, false).invert();
						
						Vector3f worldSpace = new Vector3f(altTransformation.transform(new Vector4f(position.clone(), 1f)));
						Vector3f invWorldSpace = new Vector3f(relTransformation.transform(new Vector4f(position.clone(), 1f)));
						
						Vector3f rotation = new Vector3f();
						if(body == cameraObject.get()) {
							Vector3f objForward = new Vector3f(rotationTransform.transform(new Vector4f(body.getForwardVector(), 1f))).normalize();
							Vector3f objForwardWorldSpace = new Vector3f(altRotationTransform.transform(new Vector4f(objForward, 1f))).normalize();
							rotation.set(new Vector3f(calculatePitchAndYaw(objForwardWorldSpace), body.getRotation().z));
						}
						else {
							Quat4f quat = new Quat4f().set(Matrix4f.fromEulerRotation(body.getRotation()));
							Vector3f axis = new Vector3f(rotationTransform.transform(new Vector4f(quat.getAxis(), 0f)));
							Vector3f axisWorld = new Vector3f(altRotationTransform.transform(new Vector4f(axis, 0f)));
							rotation.set(new Quat4f(axisWorld, quat.w).toRotationMatrix().toEulerRotation());
						}
						
						Vector3f velocity = new Vector3f(rotationTransform.transform(new Vector4f(body
								.getModifier(AbstractRigidBodyModifier.class).getBody(body).getLinearVelocity(), 1f)));
						Vector3f velocityWorldSpace = new Vector3f(altRotationTransform.transform(new Vector4f(velocity, 1f)));
						
						body.getModifier(AbstractRigidBodyModifier.class).getBody(body).setSimulationPosition(worldSpace);
						body.getModifier(AbstractRigidBodyModifier.class).getBody(body).setLinearVelocity(velocityWorldSpace);
						if(body.getModifier(AbstractRigidBodyModifier.class).getBody(body).getAngularFactor() == 0f)
							body.setRotation(rotation);
						else
							body.getModifier(AbstractRigidBodyModifier.class).getBody(body).setSimulationRotation(rotation);
						body.getModifier(AbstractRigidBodyModifier.class).updateTransform(body);
						if(body == cameraObject.get()) {
							linkMod.prevCameraPosition.set(invWorldSpace);
							prevCameraPosition.set(invWorldSpace);
						}
					}
				}
				previousDistances.put(body, signedDistance);
			}
		}
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		connection.update(!packet.isPaused());
		forwardVector.update(!packet.isPaused());
		upVector.update(!packet.isPaused());
		horizontalLimit.update(!packet.isPaused());
		verticalLimit.update(!packet.isPaused());
		cameraObject.update(!packet.isPaused());
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		ctrl.withProperty("Connection", connection);
		ctrl.withProperty("Forward Vector", forwardVector).withPropertyBounds(new NonZeroBounds<Vector3f>(Vector3f.class, FloatConverter.VEC3_CONVERTER), 1);
		ctrl.withProperty("Up Vector", upVector).withPropertyBounds(new NonZeroBounds<Vector3f>(Vector3f.class, FloatConverter.VEC3_CONVERTER), 1);
		ctrl.withProperty("Horizontal Limit", horizontalLimit);
		ctrl.withProperty("Vertical Limit", verticalLimit);
		ctrl.withProperty("Camera Object", cameraObject);
		return ctrl;
	}
	
	@Override
	public boolean containsData(GLData data) {
		return data == buffer || RECURSIVE_BUFFERS.containsValue(data);
	}
	
	private Matrix4f getTransform(CEObject obj, boolean negate, boolean translate) {
		Vector3f forward = new Vector3f(obj.getRotationTransformation().transform(new Vector4f(forwardVector.get()))).normalize();
		if(negate) forward.negate();
		Vector3f up = new Vector3f(obj.getRotationTransformation().transform(new Vector4f(upVector.get()))).normalize();
		if(negate) up.negate();
		Vector3f right = Vector3f.cross(forward, up).clone();
		
		Matrix4f transform = new Matrix4f(new Matrix3f(
				right.x, up.x, forward.x,
				right.y, up.y, forward.y,
				right.z, up.z, forward.z
		));
		
		if(translate)
			transform.translate(obj.getPosition().clone().negate());
		return transform;
	}
	
	private Vector2f calculatePitchAndYaw(Vector3f direction) {
		Vector3f dir = direction.clone().normalize();
		float pitch = (float) -Math.acos(new Vector2f(dir.x, dir.z).length()) * (dir.y < 0 ? -1 : 1);
		float yaw = (float) -Math.toDegrees(((float) Math.atan(dir.x / dir.z)));
		yaw = dir.z > 0 ? yaw - 180 : yaw;
		return new Vector2f((float) Math.toDegrees(pitch), yaw);
	}
	
	private Vector4f calculateClipPlane(CEObject obj) {
		Vector3f normal = new Vector3f(obj.getRotationTransformation().transform(new Vector4f(forwardVector.get()))).normalize();
		float planeConstant = normal.x * obj.getPosition().x + normal.y * obj.getPosition().y + normal.z * obj.getPosition().z;
		
		Vector4f clipPlane = new Vector4f(normal.x, normal.y, normal.z, -planeConstant);
		return clipPlane;
	}
	
	private ArrayList<CEObject> getObjects(Scene scene) {
		ArrayList<CEObject> objects = new ArrayList<>();
		for(Layer layer : scene.getLayers())
			if(!layer.getIdentifier().equals(GHOST_LAYER))
				objects.addAll(layer.getObjects());
		return objects;
	}
	
	public CEObject getConnection() {
		return connection.get();
	}
	
	public void setConnection(CEObject connection) {
		this.connection.set(connection);
	}
	
	public Property<CEObject> connectionProperty() {
		return connection;
	}
	
	public Vector3f getForwardVector() {
		return forwardVector.get();
	}
	
	public void setForwardVector(Vector3f forwardVector) {
		this.forwardVector.set(forwardVector);
	}
	
	public Property<Vector3f> forwardVectorProperty() {
		return forwardVector;
	}
	
	public Vector3f getUpVector() {
		return upVector.get();
	}
	
	public void setUpVector(Vector3f upVector) {
		this.upVector.set(upVector);
	}
	
	public Property<Vector3f> upVectorProperty() {
		return upVector;
	}
	
	public float getHorizontalLimit() {
		return horizontalLimit.get();
	}
	
	public void setHorizontalLimit(float horizontalLimit) {
		this.horizontalLimit.set(horizontalLimit);
	}
	
	public Property<Float> horizontalLimitProperty() {
		return horizontalLimit;
	}
	
	public float getVerticalLimit() {
		return verticalLimit.get();
	}
	
	public void setVerticalLimit(float verticalLimit) {
		this.verticalLimit.set(verticalLimit);
	}
	
	public Property<Float> verticalLimitProperty() {
		return verticalLimit;
	}
	
	public FrameBuffer getBuffer() {
		return buffer;
	}
	
	public CEObject getCameraObject() {
		return cameraObject.get();
	}
	
	public void setCameraObject(CEObject cameraObject) {
		this.cameraObject.set(cameraObject);
	}
	
	public Property<CEObject> cameraObjectProperty() {
		return cameraObject;
	}
	
	public Modifier duplicate() {
		return new PlanarPortalModifier(this);
	}
	
}
