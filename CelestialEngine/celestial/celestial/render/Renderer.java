package celestial.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.core.EngineRuntime;
import celestial.error.CelestialGenericException;
import celestial.scene.Layer;
import celestial.scene.Scene;
import celestial.shader.UnifiedShader;
import celestial.shading.ShadingSystem;
import celestial.ui.UILayer;
import celestial.vecmath.Vector3f;
import mod.celestial.mesh.AbstractMeshModifier;
import mod.celestial.misc.CameraModifier;

public abstract class Renderer {
	
	public static Vector3f REFERENCE_POSITION_TMP = new Vector3f();
	
	private final UnifiedShader shader;
	private final Property<Vector3f> screen;
	private final ClipPlane[] clipPlanes;
	protected final PerformanceLogger logger;
	private final FrustumCuller culler;
	private RenderOutput output;
	
	private Scene scene = null;
	private boolean paused = false;
	
	private boolean ppsBound = false;
	private boolean outputBound = false;
	
	public Renderer(UnifiedShader shader, Vector3f screen) {
		if(shader == null) throw new CelestialGenericException("Shader cannot be null");
		this.shader = shader;
		this.screen = Properties.createVec3Property(screen);
		this.clipPlanes = new ClipPlane[8];
		this.logger = new PerformanceLogger();
		this.culler = new FrustumCuller();
		this.output = RenderOutput.SCREEN;
		for(int i = 0 ; i < clipPlanes.length ; ++i) clipPlanes[i] = new ClipPlane();
	}
	
	public final UnifiedShader getShader() {
		return shader;
	}
	
	public final Property<Vector3f> getScreen() {
		return screen;
	}
	
	public final int getClipPlaneCount() {
		return clipPlanes.length;
	}
	
	public final ClipPlane getClipPlane(int index) {
		return clipPlanes[index];
	}
	
	public final PerformanceLogger getLogger() {
		return logger;
	}
	
	public final RenderOutput getOutput() {
		return output;
	}
	
	public final Scene getScene() {
		return scene;
	}
	
	public final void setOutput(RenderOutput output) {
		this.output = output;
	}
	
	public final boolean isPaused() {
		return paused;
	}
	
	public final void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public final void pause() {
		this.paused = true;
	}
	
	public final void resume() {
		this.paused = false;
	}
	
	public final void render(Scene scene, String camera, RenderConstraints constraints) {
		if(scene == null) throw new CelestialGenericException("Scene cannot be null");
		if(output == null) output = RenderOutput.SCREEN;
		if(constraints == null) constraints = new RenderConstraints();
		this.scene = scene;
		
		this.logger.startTask("Render.TOTAL");
		this.logger.startTask("Render.ObjSort");
		
		ArrayList<CEObject> objList = new ArrayList<CEObject>();
		for(Layer layer : scene.getLayers()) if(layer.isEnabled()) objList.addAll(layer.getObjects());
		objList.removeAll(Collections.<CEObject>singletonList(null));
		
		CEObject camObj = null;
		for(CEObject obj : objList) {
			if(obj.getIdentifier().equals(camera) && obj.getModifier(CameraModifier.class) != null) camObj = obj;
		}
		if(camObj == null) throw new CelestialGenericException("Invalid camera identifier; no object found");
		Renderer.REFERENCE_POSITION_TMP = camObj.getPosition();
		
		ArrayList<CEObject> blendList = new ArrayList<CEObject>(); // Objects with blending enabled (should be rendered after all non-blended objects)
		ArrayList<CEObject> depthList = new ArrayList<CEObject>(); // Objects without depth testing (should be rendered absolutely last)
		for(CEObject obj : new ArrayList<CEObject>(objList)) {
			AbstractMeshModifier mesh = obj.getModifier(AbstractMeshModifier.class);
			if(mesh != null && !mesh.isDepthTest()) {
				objList.remove(obj);
				depthList.add(obj);
			}
			else if(mesh != null && (mesh.getBlendMode() == AbstractMeshModifier.BLEND_MODE_ALPHA || mesh.getBlendMode() == AbstractMeshModifier.BLEND_MODE_ADDITIVE)) {
				objList.remove(obj);
				blendList.add(obj);
			}
		}
		Collections.sort(blendList);
		objList.addAll(blendList);
		objList.addAll(depthList);
		ArrayList<CEObject> unalteredScene = new ArrayList<CEObject>(objList);
		
		culler.update(camObj);
		for(CEObject obj : new ArrayList<CEObject>(objList))
			if((Math.max(0, Vector3f.sub(camObj.getPosition(), obj.getPosition()).length()
					- obj.getConstraints().getFrustumRadius() * obj.getMaxScale()) >= obj.getConstraints().getCullDistance()
					|| obj.getConstraints().isCulled()) || !culler.isInFrustum(obj) || !constraints.validate(obj))
				objList.remove(obj);
		
		CEObject[] objArr = new CEObject[objList.size()];
		objList.toArray(objArr);
		
		RenderPacket renderPacket = new RenderPacket(this, camObj, null, false, unalteredScene, objArr);
		UpdatePacket updatePacket = new UpdatePacket(camObj, scene, paused);
		
		this.logger.finishTask("Render.ObjSort");
		this.logger.startTask("Render.CheckData");
		
		List<ShadingSystem> tmpShadingSystems = new ArrayList<ShadingSystem>(scene.getShadingSystems());
		if(scene.getPostProcessingSystem() != null) tmpShadingSystems.add(scene.getPostProcessingSystem());
		for(UILayer layer : scene.getGuiSystem().getLayers())
			if(layer.getPostProcessingSystem() != null)
				tmpShadingSystems.add(layer.getPostProcessingSystem());
		EngineRuntime.getDataManager().update(camObj, tmpShadingSystems, scene.getGuiSystem(), unalteredScene);
		
		this.logger.finishTask("Render.CheckData");
		this.logger.startTask("Render.Bind");
		
		if(scene.getPostProcessingSystem() == null) {
			output.bind();
			outputBound = true;
		}
		else {
			EngineRuntime.getPostProcessingBuffer().fboBind();
			ppsBound = true;
		}
		prepareOutput();
		
		this.logger.finishTask("Render.Bind");
		
		this.logger.startTask("Render.Update0");
		
		for(Layer layer : new ArrayList<>(scene.getLayers()))
			if(layer.isEnabled())
				for(CEObject obj : new ArrayList<>(layer.getObjects()))
					obj.update0(updatePacket);
		
		this.logger.finishTask("Render.Update0");
		this.logger.startTask("Render.SceneRender");
		
		implRender(renderPacket, constraints);
		
		this.logger.finishTask("Render.SceneRender");
		this.logger.startTask("Render.PostProcess");
		
		if(scene.getPostProcessingSystem() != null) {
			EngineRuntime.getPostProcessingBuffer().fboUnbind();
			output.bind();
			outputBound = true;
			ppsBound = false;
			scene.getPostProcessingSystem().renderResult();
		}
		
		this.logger.finishTask("Render.PostProcess");
		this.logger.startTask("Render.Update1");
		
		for(Layer layer : new ArrayList<>(scene.getLayers())) {
			if(layer.isEnabled()) {
				if(layer.getEvent() != null) layer.getEvent().perform(updatePacket);
				for(CEObject obj : new ArrayList<>(layer.getObjects())) obj.update1(updatePacket);
			}
		}
		for(ShadingSystem shadingSystem : scene.getShadingSystems())
			shadingSystem.update(updatePacket);
		if(scene.getPostProcessingSystem() != null)
			scene.getPostProcessingSystem().update(updatePacket);
		for(UILayer layer : scene.getGuiSystem().getLayers())
			if(layer.getPostProcessingSystem() != null)
				layer.getPostProcessingSystem().update(updatePacket);
		screen.update();
		
		this.logger.finishTask("Render.Update1");
		this.logger.startTask("Render.GUI");
		
		scene.getGuiSystem().renderUI(output);
		
		this.logger.finishTask("Render.GUI");
		this.logger.startTask("Render.Unbind");
		
		output.unbind();
		outputBound = false;
		
		this.logger.finishTask("Render.Unbind");
		this.logger.finishTask("Render.TOTAL");
		this.logger.nextFrame();
	}
	
	private final Deque<RenderOutput> fboOutputs = new LinkedList<RenderOutput>();
	
	/**
	 * Should be called during the general render pass
	 */
	public final void render(RenderPacket packet, CEObject overrideCamera, boolean limitedFBO, RenderOutput fboOutput, RenderConstraints constraints) {
		if(overrideCamera != null && overrideCamera.getModifier(CameraModifier.class) == null) throw new CelestialGenericException("Invalid camera; no camera component");
		if(constraints == null) constraints = new RenderConstraints();
		ArrayList<CEObject> objList = new ArrayList<CEObject>(packet.getUnalteredScene());
		CEObject camera = overrideCamera != null ? overrideCamera : packet.getCamera();
		
		culler.update(camera);
		for(CEObject obj : new ArrayList<CEObject>(objList))
			if((Math.max(0, Vector3f.sub(camera.getPosition(), obj.getPosition()).length() - obj.getConstraints().getFrustumRadius()
					* obj.getMaxScale()) >= (limitedFBO ? obj.getConstraints().getLimitedFboCullDistance() : obj.getConstraints().getFboCullDistance())
					|| obj.getConstraints().isCulled()) || !culler.isInFrustum(obj) || !constraints.validate(obj))
				objList.remove(obj);
		CEObject[] objArr = new CEObject[objList.size()];
		objList.toArray(objArr);
		RenderPacket newPacket = new RenderPacket(packet.getRenderer(), camera,
				packet.getShader(), true, packet.getUnalteredScene(), objArr);
		
		if(fboOutput == null) fboOutput = RenderOutput.SCREEN;
		if(fboOutputs.size() > 0) fboOutputs.getLast().unbind();
		else if(ppsBound) EngineRuntime.getPostProcessingBuffer().fboUnbind();
		else if(outputBound && output != null) output.unbind();
		fboOutput.bind();
		fboOutputs.addLast(fboOutput);
		prepareOutput();
		implRender(newPacket, constraints);
		fboOutputs.pollLast();
		fboOutput.unbind();
		if(fboOutputs.size() > 0) fboOutputs.getLast().bind();
		else if(ppsBound) EngineRuntime.getPostProcessingBuffer().fboBind();
		else if(ppsBound && output != null) output.bind();
	}
	
	protected abstract void implRender(RenderPacket packet, RenderConstraints constraints);
	
	private final void prepareOutput() {
		EngineRuntime.clear(screen.get());
	}
	
	public final void loadClipPlanes() {
		for(int i = 0 ; i < clipPlanes.length ; ++i) {
			if(clipPlanes[i].isEnabled()) GraphicUtil.enable(GraphicUtil.GL_CLIP_DISTANCE0 + i);
			else {
				GraphicUtil.disable(GraphicUtil.GL_CLIP_DISTANCE0 + i);
				clipPlanes[i].setNormal(new Vector3f(0, -1, 0));
				clipPlanes[i].setDistance(Float.MAX_VALUE);
			}
		}
	}
	
}
