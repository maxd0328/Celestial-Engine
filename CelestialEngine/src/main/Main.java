package main;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import celestial.beans.driver.SmoothDriver;
import celestial.core.CEObject;
import celestial.core.ObjectConstraints;
import celestial.core.EngineRuntime;
import celestial.render.RenderConstraints;
import celestial.render.Renderer;
import celestial.render.impl.InstancedRenderer;
import celestial.scene.Scene;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shader.UnifiedShader;
import celestial.shading.Attribute;
import celestial.shading.ShadingNode;
import celestial.shading.ShadingSystem;
import celestial.vecmath.ColorPalette;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.filter.StaticColorModifier;
import mod.celestial.input.KeyTransformModifier;
import mod.celestial.light.SpotLightModifier;
import mod.celestial.material.ProceduralBSDFModifier;
import mod.celestial.mesh.*;
import mod.celestial.misc.CameraModifier;
import mod.celestial.render.PlanarReflectionModifier;
import mod.celestial.texture.BumpMapModifier;
import mod.celestial.texture.DiffuseMapModifier;
import node.celestial.color.ReinhardToneMapNode;
import node.celestial.composite.AdditiveCompositeNode;
import node.celestial.filter.BrightnessContrastNode;
import node.celestial.filter.GaussianHBlurNode;
import node.celestial.filter.GaussianVBlurNode;
import node.celestial.filter.LuminanceExtractionNode;
import node.celestial.input.SourceColorInputNode;
import mod.celestial.mesh.BvhTriangleMeshModifier.BvhTriangleVAO;
import celestial.data.DataManager;
import celestial.data.DataReader;
import celestial.data.ImageSampler;
import celestial.glutil.GLDisplayMode;
import celestial.glutil.GLViewport;
import celestial.glutil.RotationPattern;
import celestial.data.VertexBuffer;

public class Main {
	
	public static void main(String[] args) throws IOException {
		
		EngineRuntime.dispSetTitle("Test Application");
		DataManager dataManager = new DataManager.ClusterDataManager();
		EngineRuntime.create(new GLDisplayMode(1280, 720, false), new GLViewport(0, 0, 1, 1), 3, 2, true, true, 4, 24, dataManager);
		
		UnifiedShader shader = new UnifiedShader(450).withDefaults();
		Renderer renderer = new InstancedRenderer(shader, ColorPalette.BLACK);
		Scene scene = new Scene("Default Scene");
		
		CEObject camera = new CEObject("Camera", new Vector3f(0, 0, 14), new Vector3f(), new Vector3f(1f),
				new ObjectConstraints(1, 0), new CameraModifier(70, 2, 2, 0.1f, 1000, 1f, 1f));
		camMove(camera);
		scene.getBaseLayer().addObject(camera);
		
		SmoothDriver driver = new SmoothDriver(15f);
		camera.positionProperty().subProperty(0).getDriver().set(driver);
		camera.positionProperty().subProperty(1).getDriver().set(driver);
		camera.positionProperty().subProperty(2).getDriver().set(driver);
		camera.rotationProperty().subProperty(0).getDriver().set(driver);
		camera.rotationProperty().subProperty(1).getDriver().set(driver);
		camera.rotationProperty().subProperty(2).getDriver().set(driver);
		
		CEObject light = new CEObject("Light", new Vector3f(0, 5, 10), new Vector3f(), new Vector3f(1), new ObjectConstraints(1, 0),
				new SpotLightModifier(new Vector3f(), new Vector3f(0, 0, -1), 25, 0.3f, true, 5f, new Vector3f(1f, 1f, 0.7f), 30f, false, 0f, 1));
		scene.getBaseLayer().addObject(light);
		
		CEObject dragon = new CEObject("Dragon", new Vector3f(0, -0.5f, 0), new Vector3f(11, 0, 0), new Vector3f(1), new ObjectConstraints(11, 1000));
		dragon.addModifier(new StaticColorModifier(ColorPalette.SKIN_LIGHT));
		dragon.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.4f, 1f, 0f, 1f, 0.0005f, 2.7f, 0.04f, false, true));
		dragon.addModifier(new BvhTriangleMeshModifier(true, true, new Vector3f(), 1,
				new BvhTriangleVAO(VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei("res/dragon.obj", DataReader.DATA_FORMAT_OBJ, 0, 4),
				new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/dragon.obj", DataReader.DATA_FORMAT_OBJ, 0, 0)),
				new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/dragon.obj", DataReader.DATA_FORMAT_OBJ, 0, 1)),
				new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/dragon.obj", DataReader.DATA_FORMAT_OBJ, 0, 2)),
				new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/dragon.obj", DataReader.DATA_FORMAT_OBJ, 0, 3))
				)));
//		dragon.addModifier(new DynamicEmitterModifier(ball, 0, DynamicEmitterModifier.SPAWN_SHAPE_POINT, DynamicEmitterModifier.FOLLOW_TYPE_TRAIL, new Vector2f(), 1,
//				new Vector3f(-2, 7.5f, 0), 200, new Vector3f(0.2f), new Vector3f(0.1f), new Vector3f(), new Vector3f(), true, 4, 0, new Vector3f(-1, 0, 0), 0.2f, 0.4f, 0.1f, 0.005f));
		scene.getBaseLayer().addObject(dragon);
		
		CEObject plane = new CEObject("Plane", new Vector3f(0, -.5f, 0), new Vector3f(0, 0, 0), new Vector3f(20), new ObjectConstraints(1.414f, 1000));
//		plane.addModifier(new BlendMapModifier(CEImageSampler.create("res/blendMap.png"), 1, false, 0, new Vector2f(), false, false));
//		plane.addModifier(new DistortionMapModifier(ImageSampler.create("res/waterDUDV.png"), 0, 2, 0.01f, new Vector2f(), true, true));
		plane.addModifier(new DiffuseMapModifier(ImageSampler.create("res/texture.png"), 0, 8, 1, 1, true, 0, new Vector2f(), false, false));
//		plane.addModifier(new DiffuseMapModifier(CEImageSampler.create("res/mud.png"), 1, 30, 1, 1, false, 0, new Vector2f(), false, false));
//		plane.addModifier(new DiffuseMapModifier(CEImageSampler.create("res/path.png"), 3, 30, 1, 1, false, 0, new Vector2f(), false, false));
		plane.addModifier(new PlanarReflectionModifier(320 * 2, 180 * 2, new Vector3f(0, 1, 0), 0.8f, true, 1f, true, 0));
		plane.addModifier(new BumpMapModifier(ImageSampler.create("res/normal.png"), 0, 8, 1, 1, 1f, true, 0, new Vector2f(), false, false));
		plane.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0f, 0.4f, 1f, 0f, 1f, 0.0005f, 2.2f, 0.04f, false, false));
		plane.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		scene.getBaseLayer().addObject(plane);
		
		ShadingSystem pps = new ShadingSystem(1920, 1080);
		ShadingNode input = new SourceColorInputNode();
		ShadingNode extract = new LuminanceExtractionNode();
		extract.getInputs().get(0).setLink(input.getOutputs().get(0));
		((Attribute) extract.getInputs().get(1)).getConstant().setValue(new Vector4f(0.5f));
		ShadingNode hblur = new GaussianHBlurNode(32, 20.0f, 2);
		ShadingNode vblur = new GaussianVBlurNode(32, 20.0f, 2);
		hblur.getInputs().get(0).setLink(extract.getOutputs().get(0));
		vblur.getInputs().get(0).setLink(hblur.getOutputs().get(0));
		ShadingNode bright = new BrightnessContrastNode(1.2f, 1.0f);
		bright.getInputs().get(0).setLink(vblur.getOutputs().get(0));
		ShadingNode add = new AdditiveCompositeNode();
		add.getInputs().get(0).setLink(input.getOutputs().get(0));
		add.getInputs().get(1).setLink(bright.getOutputs().get(0));
		ShadingNode hdr = new ReinhardToneMapNode(2.0f);
		hdr.getInputs().get(0).setLink(add.getOutputs().get(0));
		pps.getOutputNode().getInputs().get(0).setLink(hdr.getOutputs().get(0));
		scene.setPostProcessingSystem(pps);
		
		while(!EngineRuntime.isCloseRequested()) {
			
//			light.setPosition(Vector3f.add(camera.getPosition(), camera.getForwardVector().scale(2)));
//			light.getModifier(SpotLightModifier.class).setDirection(camera.getForwardVector().clone());
			renderer.render(scene, "Camera", new RenderConstraints());
			EngineRuntime.update(60);
			
		}
		
		EngineRuntime.destroy();
		
	}
	
	private static void camMove(CEObject cam) {
		RotationPattern XYZ = RotationPattern.ROTATION_PATTERN_XYZ;
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_W, new Vector3f(0, 0, -.15f), new Vector3f(), new Vector3f(), true, true, true, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_S, new Vector3f(0, 0, .15f), new Vector3f(), new Vector3f(), true, true, true, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_A, new Vector3f(-.15f, 0, 0), new Vector3f(), new Vector3f(), true, true, true, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_D, new Vector3f(.15f, 0, 0), new Vector3f(), new Vector3f(), true, true, true, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_LSHIFT, new Vector3f(0, -.15f, 0), new Vector3f(), new Vector3f(), false, false, false, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_SPACE, new Vector3f(0, .15f, 0), new Vector3f(), new Vector3f(), false, false, false, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_LEFT, new Vector3f(), new Vector3f(0, -1, 0), new Vector3f(), false, false, false, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_RIGHT, new Vector3f(), new Vector3f(0, 1, 0), new Vector3f(), false, false, false, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_UP, new Vector3f(), new Vector3f(-1, 0, 0), new Vector3f(), false, false, false, XYZ));
		cam.addModifier(new KeyTransformModifier(Keyboard.KEY_DOWN, new Vector3f(), new Vector3f(1, 0, 0), new Vector3f(), false, false, false, XYZ));
	}
	
}
