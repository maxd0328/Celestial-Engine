package testScene;

import org.lwjgl.input.Keyboard;
import celestial.beans.driver.LinearDriver;
import celestial.beans.driver.SmoothDriver;
import celestial.beans.property.Properties;
import celestial.core.CEObject;
import celestial.core.EngineRuntime;
import celestial.core.ObjectConstraints;
import celestial.data.DataManager;
import celestial.data.DataReader;
import celestial.data.ImageSampler;
import celestial.data.VertexBuffer;
import celestial.glutil.GLDisplayMode;
import celestial.glutil.GLViewport;
import celestial.render.RenderConstraints;
import celestial.render.Renderer;
import celestial.render.impl.InstancedRenderer;
import celestial.scene.Scene;
import celestial.shader.UnifiedShader;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.shading.Attribute;
import celestial.shading.ShadingNode;
import celestial.shading.ShadingSystem;
import celestial.vecmath.ColorPalette;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.light.SpotLightModifier;
import mod.celestial.light.SunLightModifier;
import mod.celestial.material.ProceduralBSDFModifier;
import mod.celestial.mesh.AbstractMeshModifier;
import mod.celestial.mesh.BoxMeshModifier;
import mod.celestial.mesh.BvhTriangleMeshModifier;
import mod.celestial.mesh.BvhTriangleMeshModifier.BvhTriangleVAO;
import mod.celestial.mesh.PlaneMeshModifier;
import mod.celestial.misc.CameraModifier;
import mod.celestial.misc.EyeSpaceModifier;
import mod.celestial.misc.ScriptableModifier;
import mod.celestial.physics.BoxRigidBodyModifier;
import mod.celestial.physics.BvhCollisionObjectModifier;
import mod.celestial.physics.SphereRigidBodyModifier;
import mod.celestial.render.DepthEffectModifier;
import mod.celestial.render.PlanarReflectionModifier;
import mod.celestial.render.PlanarRefractionModifier;
import mod.celestial.texture.BumpMapModifier;
import mod.celestial.texture.DiffuseMapModifier;
import mod.celestial.texture.DistortionMapModifier;
import node.celestial.color.ReinhardToneMapNode;
import node.celestial.composite.AdditiveCompositeNode;
import node.celestial.filter.BrightnessContrastNode;
import node.celestial.filter.GaussianHBlurNode;
import node.celestial.filter.GaussianVBlurNode;
import node.celestial.filter.LuminanceExtractionNode;
import node.celestial.input.SourceColorInputNode;
import nonEuclidian.PlanarPortalModifier;

public class TestScene {
	
	private static final String DIR = "C:\\Users\\maxim\\OneDrive\\Desktop\\Scenes\\testScene\\";
	
	public static void main(String[] args) {
		
		EngineRuntime.dispSetTitle("Test Scene");
		EngineRuntime.dispShowMouse(false);
		EngineRuntime.dispSetVSync(true);
		DataManager dataManager = new DataManager.ClusterDataManager();
		EngineRuntime.create(new GLDisplayMode(1920, 1080, true), new GLViewport(0, 0, 1, 1), 3, 2, true, true, 4, 24, dataManager);
		
		UnifiedShader shader = new UnifiedShader(450).withDefaults().with(PlanarPortalModifier.FACTORY).create();
		Renderer renderer = new InstancedRenderer(shader, ColorPalette.SKY_BLUE.clone().scale(0.8f));
		Scene scene = new Scene("Test Scene");
		
		CEObject player = new CEObject("Player", new Vector3f(0, 10, 14), new Vector3f(), new Vector3f(1.2f, 1f /* doesn't matter */, 1.2f), new ObjectConstraints(1.2f, 0));
		player.addModifier(new SphereRigidBodyModifier(1f, 0.5f, new Vector3f(0, -35, 0), 0));
		player.addModifier(new ScriptableModifier(new PlayerScript()));
		player.scaleProperty().subProperty(1).bind(Properties.readOnlyFloatProperty(() -> player.getConfiguration(30) / 2f));
		scene.getBaseLayer().addObject(player);
		
		CEObject cube = new CEObject("MrBonkers", new Vector3f(0, 10, 17), new Vector3f(), new Vector3f(1), new ObjectConstraints(1.5f, 1000));
		cube.addModifier(new DiffuseMapModifier(ImageSampler.create("res/texture.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		cube.addModifier(new BumpMapModifier(ImageSampler.create("res/normal.png"), 0, 1f, 1, 1, 0.5f, false, 0, new Vector2f(), false, false));
		cube.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.35f, 1f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		cube.addModifier(new BoxMeshModifier(true, true, new Vector3f()));
		cube.addModifier(new BoxRigidBodyModifier(1f, 0.5f, new Vector3f(0, -35, 0), 1f));
		scene.getBaseLayer().addObject(cube);
		
		CEObject camera = new CEObject("Camera", new Vector3f(0, 10 + 5f / 6f, 14), new Vector3f(), new Vector3f(1f),
				new ObjectConstraints(1, 0), new CameraModifier(70, 2, 2, 0.1f, 1000, 1f, 1f));
		camera.setParent(player, false, true);
		scene.getBaseLayer().addObject(camera);
		
		CEObject flashlight = new CEObject("Flashlight", new Vector3f(0, 5, 10), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000),
				new SpotLightModifier(new Vector3f(), new Vector3f(0, 0, -1), 30, 0.3f, true, 5f, new Vector3f(1f, 1f, 0.85f), 30f, true, 1000f, 1024));
		flashlight.getModifier(SpotLightModifier.class).intensityProperty().bind(player.configurationProperty(29));
		flashlight.getModifier(SpotLightModifier.class).directionProperty().subProperty(0).getDriver().set(new SmoothDriver(15f));
		flashlight.getModifier(SpotLightModifier.class).directionProperty().subProperty(1).getDriver().set(new SmoothDriver(15f));
		flashlight.getModifier(SpotLightModifier.class).directionProperty().subProperty(2).getDriver().set(new SmoothDriver(15f));
//		scene.getBaseLayer().addObject(flashlight);
		
		CEObject sky = new CEObject("Sky", camera.getPosition().clone(), new Vector3f(), new Vector3f(500), new ObjectConstraints(10000, 10000));
		sky.addModifier(new EyeSpaceModifier(new Vector3f()));
		sky.addModifier(new DiffuseMapModifier(ImageSampler.create("res/sky.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		sky.addModifier(new BvhTriangleMeshModifier(true, true, new Vector3f(), 1,
				new BvhTriangleVAO(VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei("res/sky.obj", DataReader.DATA_FORMAT_OBJ, 0, 4),
				new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/sky.obj", DataReader.DATA_FORMAT_OBJ, 0, 0)),
				new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/sky.obj", DataReader.DATA_FORMAT_OBJ, 0, 1)),
				new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/sky.obj", DataReader.DATA_FORMAT_OBJ, 0, 2)),
				new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef("res/sky.obj", DataReader.DATA_FORMAT_OBJ, 0, 3))
				)));
		scene.getBaseLayer().addObject(sky);
		
		SmoothDriver driver = new SmoothDriver(50f);
		player.rotationProperty().subProperty(0).getDriver().set(driver);
		player.rotationProperty().subProperty(1).getDriver().set(driver);
		
		CEObject sun = new CEObject("Sun", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		sun.addModifier(new SunLightModifier(new Vector3f(1, -2, 0.4f).normalize(), new Vector3f(1, 1, 1), 2.5f, false, 0.1f, 1f, 50f, 16384));
		scene.getBaseLayer().addObject(sun);
		
		CEObject bridge = new CEObject("Bridge", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		bridge.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "wood.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		bridge.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "woodNormal.png"), 0, 1f, 1, 1, 0.5f, false, 0, new Vector2f(), false, false));
		bridge.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.35f, 1f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(bridge, "bridge.obj", true);
		loadCollisionMesh(bridge, "bridgeCollider.obj");
		scene.getBaseLayer().addObject(bridge);
		
		CEObject wall = new CEObject("Wall", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		wall.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "concrete.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		wall.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "concreteNormal.png"), 0, 1f, 1, 1, 0.8f, false, 0, new Vector2f(), false, false));
		wall.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.4f, 1f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(wall, "wall.obj", true);
		loadCollisionMesh(wall, "wallCollider.obj");
		scene.getBaseLayer().addObject(wall);
		
		CEObject fence = new CEObject("Fence", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		fence.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "fence.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		fence.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "fenceNormal.png"), 0, 1f, 1, 1, 0.8f, false, 0, new Vector2f(), false, false));
		fence.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.5f, 1f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(fence, "fence.obj", false);
		fence.getModifier(AbstractMeshModifier.class).setBlendMode(AbstractMeshModifier.BLEND_MODE_ALPHA);
		loadCollisionMesh(fence, "fenceCollider.obj");
		scene.getBaseLayer().addObject(fence);
		
		CEObject dirt = new CEObject("Dirt", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		dirt.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "dirt.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		dirt.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "dirtNormal.png"), 0, 1f, 1, 1, 0.5f, false, 0, new Vector2f(), false, false));
		dirt.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.9f, 0.5f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(dirt, "dirt.obj", true);
		loadCollisionMesh(dirt, "dirtCollider.obj");
//		scene.getBaseLayer().addObject(dirt);
		
		CEObject crate = new CEObject("Crate", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		crate.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "crate.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		crate.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "crateNormal.png"), 0, 1f, 1, 1, 0.35f, false, 0, new Vector2f(), false, false));
		crate.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.4f, 0.4f, 0.5f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(crate, "crate.obj", true);
		loadCollisionMesh(crate, "crateCollider.obj");
		scene.getBaseLayer().addObject(crate);
		
		CEObject shed = new CEObject("Shed", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		shed.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "shed.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		shed.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "shedNormal.png"), 0, 1f, 1, 1, 0.35f, false, 0, new Vector2f(), false, false));
		shed.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.5f, 0.6f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(shed, "shed.obj", true);
		loadCollisionMesh(shed, "shedCollider.obj");
		scene.getBaseLayer().addObject(shed);
		
		CEObject lamp = new CEObject("Lamp", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		lamp.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "lamp.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		lamp.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "lampNormal.png"), 0, 1f, 1, 1, 0.35f, false, 0, new Vector2f(), false, false));
		lamp.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.0f, 0.4f, 1.0f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
//		lamp.addModifier(new SpotLightModifier(new Vector3f(14.7383f, 41.683f, -40.8014f), new Vector3f(0, -1, 0), 25, 0.7f, true, 5f, new Vector3f(1f, 1f, 0.7f), 400f, false, 0f, 1));
		loadMesh(lamp, "lamp.obj", true);
		loadCollisionMesh(lamp, "lampCollider.obj");
		scene.getBaseLayer().addObject(lamp);
		
		CEObject tower = new CEObject("Tower", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		tower.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "tower.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		tower.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "towerNormal.png"), 0, 1f, 1, 1, 0.7f, false, 0, new Vector2f(), false, false));
		tower.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.5f, 0.5f, 0.8f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(tower, "tower.obj", false);
		tower.getModifier(AbstractMeshModifier.class).setBlendMode(AbstractMeshModifier.BLEND_MODE_ALPHA);
		loadCollisionMesh(tower, "towerCollider.obj");
		scene.getBaseLayer().addObject(tower);
		
		CEObject cylinder = new CEObject("Cylinder", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		cylinder.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "cylinder.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		cylinder.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "cylinderNormal.png"), 0, 1f, 1, 1, 0.7f, false, 0, new Vector2f(), false, false));
		cylinder.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.6f, 0.5f, 1.0f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(cylinder, "cylinder.obj", true);
		loadCollisionMesh(cylinder, "cylinderCollider.obj");
		scene.getBaseLayer().addObject(cylinder);
		
		CEObject pipe = new CEObject("Pipe", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		pipe.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "pipe.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		pipe.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.5f, 0.55f, 1.0f, 0f, 1f, 0.05f, 1f, 0.04f, false, true));
		loadMesh(pipe, "pipe.obj", true);
		loadCollisionMesh(pipe, "pipeCollider.obj");
		scene.getBaseLayer().addObject(pipe);
		
		CEObject grate = new CEObject("Pipe", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		grate.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "pipe.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		grate.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "pipeNormal.png"), 0, 1f, 1, 1, 0.5f, false, 0, new Vector2f(), false, false));
		grate.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.5f, 0.7f, 1.0f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(grate, "grate.obj", true);
		loadCollisionMesh(grate, "grateCollider.obj");
		scene.getBaseLayer().addObject(grate);
		
		CEObject leverBase = new CEObject("LeverBase", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		leverBase.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "lamp.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		leverBase.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "lampNormal.png"), 0, 1f, 1, 1, 0.5f, false, 0, new Vector2f(), false, false));
		leverBase.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.2f, 0.2f, 1.0f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(leverBase, "leverBase.obj", true);
		scene.getBaseLayer().addObject(leverBase);
		
		CEObject lever = new CEObject("Lever", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		lever.addModifier(new DiffuseMapModifier(ImageSampler.create(DIR + "lever.png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		lever.addModifier(new BumpMapModifier(ImageSampler.create(DIR + "leverNormal.png"), 0, 1f, 1, 1, 0.5f, false, 0, new Vector2f(), false, false));
		lever.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 0.5f, 0.6f, 1.0f, 0f, 1f, 0.05f, 1f, 0.04f, false, false));
		loadMesh(lever, "lever.obj", true);
		scene.getBaseLayer().addObject(lever);
		
		CEObject water = new CEObject("Water", new Vector3f(0, 1.02443f, 0), new Vector3f(), new Vector3f(50), new ObjectConstraints(1000, 1000));
		water.addModifier(new DistortionMapModifier(ImageSampler.create("res/waterDUDV.png"), 0, 2, 0.01f, new Vector2f(), true, true));
		water.getModifier(DistortionMapModifier.class).offsetProperty().subProperty(0).getDriver().set(new LinearDriver(0.0002f));
		water.getModifier(DistortionMapModifier.class).offsetProperty().subProperty(1).getDriver().set(new LinearDriver(0.0002f));
		water.addModifier(new PlanarReflectionModifier(320 * 2, 180 * 2, new Vector3f(0, 1, 0), 1f, false, 0f, true, 0));
		water.addModifier(new PlanarRefractionModifier(320 * 2, 180 * 2, new Vector3f(0, 1, 0), 1f, true, 0.4f, true, 0));
		water.addModifier(new DepthEffectModifier(320 * 2, 180 * 2, new Vector3f(0, 1, 0), 0f, 1f, false, new Vector3f(), 1f, false, true, 0.3f, false, false, 0));
		water.addModifier(new BumpMapModifier(ImageSampler.create("res/waterNormal.png"), 0, 2, 1, 1, 2f, false, 0, new Vector2f(), true, true));
		water.getModifier(BumpMapModifier.class).offsetProperty().subProperty(0).bind(water.getModifier(DistortionMapModifier.class).offsetProperty().subProperty(0));
		water.getModifier(BumpMapModifier.class).offsetProperty().subProperty(1).bind(water.getModifier(DistortionMapModifier.class).offsetProperty().subProperty(1));
		water.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, 1.0f, 0.3f, 1f, 0f, 1f, 1.0f, 1.0f, 0.04f, false, false));
		water.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		water.getModifier(PlaneMeshModifier.class).setBlendMode(PlaneMeshModifier.BLEND_MODE_ALPHA);
	//	scene.getBaseLayer().addObject(water);
		
		CEObject portal0 = new CEObject("Portal0", new Vector3f(-20, 5, 25), new Vector3f(90, 0, -90), new Vector3f(4, 1, 6), new ObjectConstraints(1000, 1000));
		CEObject portal1 = new CEObject("Portal1", new Vector3f(20, 5, 25), new Vector3f(-90, 0, 90), new Vector3f(4, 1, 6), new ObjectConstraints(1000, 1000));
		
		portal0.addModifier(new PlanarPortalModifier(portal1, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), 4, 6, player, 1280, 720));
		portal0.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		scene.getBaseLayer().addObject(portal0);
		
		portal1.addModifier(new PlanarPortalModifier(portal0, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), 4, 6, player, 1280, 720));
		portal1.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		scene.getBaseLayer().addObject(portal1);
		
		ShadingSystem pps = new ShadingSystem(1920, 1080);
		ShadingNode input = new SourceColorInputNode();
		ShadingNode extract = new LuminanceExtractionNode();
		extract.getInputs().get(0).setLink(input.getOutputs().get(0));
		((Attribute) extract.getInputs().get(1)).getConstant().setValue(new Vector4f(0.5f));
		ShadingNode hblur = new GaussianHBlurNode(32, 20.0f, 2);
		ShadingNode vblur = new GaussianVBlurNode(32, 20.0f, 2);
		hblur.getInputs().get(0).setLink(extract.getOutputs().get(0));
		vblur.getInputs().get(0).setLink(hblur.getOutputs().get(0));
		ShadingNode bright = new BrightnessContrastNode(0.85f, 1.0f);
		bright.getInputs().get(0).setLink(vblur.getOutputs().get(0));
		ShadingNode add = new AdditiveCompositeNode();
		add.getInputs().get(0).setLink(input.getOutputs().get(0));
		add.getInputs().get(1).setLink(bright.getOutputs().get(0));
		ShadingNode hdr = new ReinhardToneMapNode(2.0f);
		hdr.getInputs().get(0).setLink(add.getOutputs().get(0));
		pps.getOutputNode().getInputs().get(0).setLink(hdr.getOutputs().get(0));
		scene.setPostProcessingSystem(pps);
		
		while(!EngineRuntime.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_F12)) {
			
			flashlight.setPosition(Vector3f.add(camera.getPosition(), camera.getForwardVector().scale(1.5f))
					.translate(camera.getRightVector().translate(camera.getUpVector().scale(-1f))));
			flashlight.getModifier(SpotLightModifier.class).setDirection(camera.getForwardVector().clone());
			renderer.render(scene, "Camera", new RenderConstraints());
			EngineRuntime.update(60);
			
		}
		
		EngineRuntime.destroy();
		
	}
	
	private static void loadMesh(CEObject obj, String model, boolean cullBackface) {
		obj.addModifier(new BvhTriangleMeshModifier(cullBackface, true, new Vector3f(), 1,
				new BvhTriangleVAO(VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei(DIR + model, DataReader.DATA_FORMAT_OBJ, 0, 4),
				new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(DIR + model, DataReader.DATA_FORMAT_OBJ, 0, 0)),
				new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(DIR + model, DataReader.DATA_FORMAT_OBJ, 0, 1)),
				new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(DIR + model, DataReader.DATA_FORMAT_OBJ, 0, 2)),
				new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(DIR + model, DataReader.DATA_FORMAT_OBJ, 0, 3))
				)));
	}
	
	private static void loadCollisionMesh(CEObject obj, String model) {
		obj.addModifier(new BvhCollisionObjectModifier(
				DataReader.readi(DIR + model, DataReader.DATA_FORMAT_OBJ, 0, 4),
				DataReader.readf(DIR + model, DataReader.DATA_FORMAT_OBJ, 0, 0), 0.5f));
	}
	
}
