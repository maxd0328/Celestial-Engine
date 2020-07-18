package celestial.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.core.Modifier;
import celestial.ctrl.PropertyController;
import celestial.data.GLData;
import celestial.data.Sampler;
import celestial.render.RenderConstraints;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.Shader;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector3f;
import mod.celestial.filter.AlphaModifier;
import mod.celestial.light.Light;
import mod.celestial.mesh.AbstractMeshModifier;
import mod.celestial.misc.CameraModifier;

public final class ShadowMapSystem {
	
	public static final ShadowMapSystem INSTANCE = new ShadowMapSystem();
	
	private final Property<Float> updateFrequency;
	
	private final ArrayList<Light> lights = new ArrayList<>();
	private final ArrayList<ShadowMap> maps = new ArrayList<>();
	private boolean active = false;
	
	private int framesSinceRender = 0;
	private int mapIndex = 0;
	
	private ShadowMapSystem() {
		this.updateFrequency = Properties.createFloatProperty(2f);
	}
	
	public float getUpdateFrequency() {
		return updateFrequency.get();
	}
	
	public void setUpdateFrequency(float updateFrequency) {
		this.updateFrequency.set(updateFrequency);
	}
	
	public Property<Float> updateFrequencyProperty() {
		return updateFrequency;
	}
	
	public int getNumShadowMaps() {
		return maps.size();
	}
	
	public ShadowMap getShadowMap(Light light) {
		for(ShadowMap map : maps)
			if(map.getLight().equals(light))
				return map;
		return null;
	}
	
	public void resetAll() {
		for(ShadowMap map : maps)
			map.requestReset();
	}
	
	public void registerLight(Light light) {
		active = true;
		if(lights.contains(light))
			lights.remove(light);
		lights.add(light);
	}
	
	public void registerOut(RenderPacket pckt) {
		if(!active)
			return;
		active = false;
		
		maps.removeIf(e -> !lights.contains(e.getLight()) || e.needsReinstantiation());
		for(Light light : lights) {
			if(!hasExistingMap(light)) {
				if(light.castsPointShadows())
					maps.addAll(Arrays.asList(ShadowMapCube.createShadowCubeMap(light)));
				else
					maps.add(new ShadowMap2D(light));
			}
		}
		
		lights.clear();
		
		int mapsToRender = maps.size();
		if(mapsToRender == 0)
			return;
		
		if(updateFrequency.get() == 0) {
			for(ShadowMap map : maps)
				if(!map.isRendered())
					renderMap(map, pckt);
			return;
		}
		
		float mapFactor = updateFrequency.get();
		if(mapFactor >= 1) {
			int mapsPerFrame = Math.min((int) Math.floor(mapFactor), mapsToRender);
			
			for(int i = 0 ; i < mapsPerFrame ; ++i) {
				if(mapIndex >= mapsToRender) mapIndex = 0;
				renderMap(maps.get(mapIndex++), pckt);
			}
		}
		else {
			int framesPerMap = (int) Math.floor(1f / mapFactor);
			
			if(framesSinceRender >= framesPerMap) {
				framesSinceRender = 0;
				if(mapIndex >= mapsToRender) mapIndex = 0;
				renderMap(maps.get(mapIndex++), pckt);
			}
			else framesSinceRender++;
		}
	}
	
	public boolean needsToRender() {
		return active;
	}
	
	public boolean containsData(GLData data) {
		if(!(data instanceof Sampler))
			return false;
		
		for(ShadowMap map : maps)
			if(map.containsFrameBuffer((Sampler) data))
				return true;
		return false;
	}
	
	private boolean hasExistingMap(Light light) {
		for(ShadowMap map : maps)
			if(map.getLight().equals(light))
				return true;
		return false;
	}
	
	private void renderMap(ShadowMap map, RenderPacket pckt) {
		RenderConstraints constraints = new RenderConstraints();
		constraints.getModifierPredicates().add(e -> !e.isColorOnly());
		
		CEObject camera = map.createCamera(pckt);
		camera.getModifier(CameraModifier.class).updateViewMatrix(camera);
		if(map instanceof ShadowMapCube) {
			Modifier mod = new LinearDepthModifier(map.getLight().getPosition());
			for(CEObject obj : pckt.getUnalteredScene())
				if(obj.getModifier(AbstractMeshModifier.class) != null)
					obj.addModifier(mod);
			pckt.getRenderer().render(pckt, camera, true, map.toRenderOutput(), constraints);
			for(CEObject obj : pckt.getUnalteredScene())
				obj.removeModifier(mod);
		}
		else pckt.getRenderer().render(pckt, camera, true, map.toRenderOutput(), constraints);
	}
	
	public static final class CubeFaceCameraModifier extends CameraModifier {
		
		private static final long serialVersionUID = -7596510927161153159L;
		
		public CubeFaceCameraModifier(CameraModifier src) {
			super(90.0f, src.getOrthoWidth(), src.getOrthoHeight(), src.getNearPlane() * 5, src.getFarPlane(), 1f, 1f);
		}
		
		public void setFace(int index, CEObject cameraObj) {
			switch(index) {
				case 0: cameraObj.setRotation(new Vector3f(   0,  90, 0));
						break;
				case 1: cameraObj.setRotation(new Vector3f(   0, -90, 0));
						break;
				case 2: cameraObj.setRotation(new Vector3f( -90, 180, 0));
						break;
				case 3: cameraObj.setRotation(new Vector3f(  90, 180, 0));
						break;
				case 4: cameraObj.setRotation(new Vector3f(   0, 180, 0));
						break;
				case 5: cameraObj.setRotation(new Vector3f(   0,   0, 0));
						break;
			}
		}
		
		@Override
		public void updateViewMatrix(CEObject obj) {
			super.getViewMatrix().setIdentity();
			super.getViewMatrix().rotate((float) Math.toRadians(180.0f), new Vector3f(0, 0, 1));
			super.getViewMatrix().rotate((float) Math.toRadians(obj.getRotation().x), new Vector3f(1, 0, 0));
			super.getViewMatrix().rotate((float) Math.toRadians(obj.getRotation().y), new Vector3f(0, 1, 0));
			Vector3f negativeCameraPos = new Vector3f(obj.getPosition()).negate();
			super.getViewMatrix().translate(negativeCameraPos);
		}
		
		@Override
		protected Matrix4f createProjectionMatrix() {
			final float aspectRatio = 1f; // cube face
			float y_scale = (float) ((1f / Math.tan(Math.toRadians(super.getFOV() / 2f))));
			float x_scale = y_scale / aspectRatio;
			float frustum_length = super.getFarPlane() - super.getNearPlane();

			Matrix4f projectionMat = new Matrix4f();
			projectionMat.m00 = x_scale;
			projectionMat.m11 = y_scale;
			projectionMat.m22 = -((super.getFarPlane() + super.getNearPlane()) / frustum_length);
			projectionMat.m23 = -1;
			projectionMat.m32 = -((2 * super.getNearPlane() * super.getFarPlane()) / frustum_length);
			projectionMat.m33 = 0;
			return projectionMat;
		}
		
	}
	
	static final class CustomCameraModifier extends CameraModifier {
		
		private static final long serialVersionUID = -8852083845293934943L;
		
		public CustomCameraModifier(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
			super(1f, 1f, 1f, 1f, 1f, 1f, 1f);
			super.getViewMatrix().set(viewMatrix);
			super.getProjectionMatrix().set(projectionMatrix);
		}
		
		@Override
		public void updateViewMatrix(CEObject obj) {
		}
		
		@Override
		protected Matrix4f createProjectionMatrix() {
			return super.getProjectionMatrix();
		}
		
	}
	
	public static final class LinearDepthModifier extends Modifier {
		
		private static final long serialVersionUID = 7824834718345673321L;
		
		public static final Factory<LinearDepthModifier> FACTORY = () -> new LinearDepthModifier(new Vector3f());
		
		private final Property<Vector3f> center;
		
		private LinearDepthModifier(Vector3f center) {
			super(false, false, false);
			this.center = Properties.createVec3Property(center);
		}
		
		private LinearDepthModifier(LinearDepthModifier src) {
			super(false, false, false);
			this.center = src.center.clone();
		}
		
		protected ShaderModule getShaderModule() {
			ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs(ShaderAttribute.$("vec3 linearDepthCenter"), ShaderAttribute.$("float linearDepthFarPlane"));
			ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
			ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs();
			ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
			return new ShaderModule("linearDepth",
					new ProgramModule(Shader.getProgramSegment(AlphaModifier.class.getResource("filter.glsl"), "linearDepthV"), vUniforms, vGlobals),
					new ProgramModule(Shader.getProgramSegment(AlphaModifier.class.getResource("filter.glsl"), "linearDepthF"), fUniforms, fGlobals),
					getID(), ShaderModule.attribs(),
					ShaderModule.attribs(ShaderAttribute.$("float linearDepthDistance")));
		}
		
		protected void preRender(RenderPacket packet, CEObject obj) {
			packet.getShader().getCommunicator().store3f("linearDepthCenter", center.get());
			packet.getShader().getCommunicator().store1f("linearDepthFarPlane", packet.getCamera().getModifier(CameraModifier.class).getFarPlane());
		}
		
		protected void render(RenderPacket packet, CEObject obj) {}
		
		protected void postRender(RenderPacket packet, CEObject obj) {}
		
		protected void update0(UpdatePacket packet, CEObject obj) {}
		
		protected void update1(UpdatePacket packet, CEObject obj) {
			center.update(!packet.isPaused());
		}
		
		public PropertyController getPropertyController(ISceneSystem system) {
			PropertyController ctrl = new PropertyController();
			ctrl.withProperty("Center", center);
			return ctrl;
		}
		
		public Vector3f getCenter() {
			return center.get();
		}
		
		public void setCenter(Vector3f center) {
			this.center.set(center);
		}
		
		public Property<Vector3f> centerProperty() {
			return center;
		}
		
		public Modifier duplicate() {
			return new LinearDepthModifier(this);
		}
		
	}

	
}
