package nonEuclidianDemo;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;
import celestial.beans.driver.SmoothDriver;
import celestial.beans.property.Properties;
import celestial.core.CEObject;
import celestial.core.EngineRuntime;
import celestial.core.ObjectConstraints;
import celestial.data.AudioBuffer;
import celestial.data.DataManager;
import celestial.data.ImageSampler;
import celestial.glutil.GLDisplayMode;
import celestial.glutil.GLViewport;
import celestial.glutil.RotationPattern;
import celestial.render.RenderConstraints;
import celestial.render.Renderer;
import celestial.render.impl.InstancedRenderer;
import celestial.scene.Scene;
import celestial.shader.UnifiedShader;
import celestial.shading.Attribute;
import celestial.shading.ShadingNode;
import celestial.shading.ShadingSystem;
import celestial.shadow.ShadowMapSystem;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import mod.celestial.filter.NormalOverrideModifier;
import mod.celestial.filter.StaticColorModifier;
import mod.celestial.light.PointLightModifier;
import mod.celestial.light.SpotLightModifier;
import mod.celestial.light.SunLightModifier;
import mod.celestial.mesh.AbstractMeshModifier;
import mod.celestial.mesh.PlaneMeshModifier;
import mod.celestial.misc.CameraModifier;
import mod.celestial.misc.ScriptableModifier;
import mod.celestial.physics.SphereRigidBodyModifier;
import mod.celestial.sound.SoundLoop3DModifier;
import mod.celestial.sound.SoundNodeModifier;
import mod.celestial.texture.IlluminationMapModifier;
import node.celestial.color.ReinhardToneMapNode;
import node.celestial.composite.AdditiveCompositeNode;
import node.celestial.filter.BrightnessContrastNode;
import node.celestial.filter.GaussianHBlurNode;
import node.celestial.filter.GaussianVBlurNode;
import node.celestial.filter.LuminanceExtractionNode;
import node.celestial.input.SourceColorInputNode;
import nonEuclidian.PlanarPortalModifier;

public class NonEuclidianScene {
	
	private static final float PLAYER_HEIGHT = 4f;
	private static final float PLAYER_WIDTH = 0.8f;
	
	private static final float LIGHTNING_FADE_SPEED = 0.01f;
	
	private static final float PORTAL_DISTANCE = 60f;
	private static final float PORTAL_RECURSE_DISTANCE = 30f;
	
	private static float lightningIntensity = 1f;
	private static boolean playedScaryTrigger = false;
	
	private static AudioBuffer windBuffer;
	
	public static void main(String[] args) {
		
		EngineRuntime.dispSetTitle("Non Euclidian Scene");
		EngineRuntime.dispShowMouse(false);
		EngineRuntime.dispSetVSync(true);
		DataManager dataManager = new DataManager.ClusterDataManager();
		EngineRuntime.create(new GLDisplayMode(1920, 1080, true), new GLViewport(0, 0, 1, 1), 3, 2, true, true, 4, 24, dataManager);
		
		UnifiedShader shader = new UnifiedShader(450).withDefaults().with(PlanarPortalModifier.FACTORY).create();
		Renderer renderer = new InstancedRenderer(shader, new Vector3f(0.05f));
		ShadowMapSystem.INSTANCE.setUpdateFrequency(0f);
		
		windBuffer = AudioBuffer.create("res/wind.wav");
		
		CEObject player = new CEObject("Player", new Vector3f(29.4951f, 3f, -63.513744f), new Vector3f(0, 180, 0),
				new Vector3f(PLAYER_WIDTH, PLAYER_HEIGHT / 2f, PLAYER_WIDTH), new ObjectConstraints(1.2f, 0));
		player.addModifier(new SphereRigidBodyModifier(1f, 0.5f, new Vector3f(0, -35, 0), 0));
		player.addModifier(new ScriptableModifier(new PlayerScript()));
		
		CEObject camera = new CEObject("Camera", new Vector3f(), new Vector3f(0, 180, 0),
				new Vector3f(1f), new ObjectConstraints(1, 0), new CameraModifier(70, 2, 2, 0.01f, 1000, 1f, 1f));
		camera.addModifier(new SoundNodeModifier(AudioBuffer.create("res/ambient.wav"), 10f, 20f, 0.75f, 0.8f));
		camera.addModifier(new SoundLoop3DModifier(AudioBuffer.create("res/scary.wav"), 1f, 1f, 350, 2, 1, false));
		camera.setParent(player, false, true);
		camera.setPosition(new Vector3f(0, 5f / 12f, 0));
		
		SmoothDriver driver = new SmoothDriver(50f);
		player.rotationProperty().subProperty(0).getDriver().set(driver);
		player.rotationProperty().subProperty(1).getDriver().set(driver);
		
		CEObject lightning = new CEObject("Lightning", new Vector3f(), new Vector3f(), new Vector3f(1), new ObjectConstraints(1000, 1000));
		lightning.addModifier(new SunLightModifier(new Vector3f(1, -2, 0.4f).normalize(), new Vector3f(0.9f, 0.8f, 1.0f), 0f, false, 0.1f, 1f, 50f, 16384));
		lightning.addModifier(new SoundLoop3DModifier(AudioBuffer.create("res/thunder.wav"), 1f, 1f, 350, 2, 1, false));
		lightning.positionProperty().bind(player.positionProperty());
		lightning.getModifier(SoundLoop3DModifier.class).pitchProperty().bind(Properties.readOnlyFloatProperty(() -> lightningIntensity * 0.4f + 0.6f));
		lightning.getModifier(SunLightModifier.class).intensityProperty().bind(Properties.readOnlyFloatProperty(() -> LightningController.getIntensity() * lightningIntensity));
		renderer.getScreen().bind(Properties.readOnlyVec3Property(() -> new Vector3f(0.05f).translate(lightning.getModifier
				(SunLightModifier.class).getColor().clone().scale(LightningController.getIntensity() * 0.1f * lightningIntensity))));
		
		CEObject scaryTrigger = new CEObject("Scary Trigger", new Vector3f(), new Vector3f(), new Vector3f(1f), new ObjectConstraints());
		scaryTrigger.addModifier(new ScriptableModifier((pckt, obj) -> {
			Vector3f targetPosition = SceneBuilder.fromBlenderCoords(new Vector3f(36.454f, 33.026f, -34.427f));
			Vector3f camPosition = pckt.getCamera().getPosition();
			if(Vector3f.sub(targetPosition, camPosition).length() <= 10.071f && !playedScaryTrigger) {
				playedScaryTrigger = true;
				camera.getModifier(SoundLoop3DModifier.class).replay(camera);
			}
		}));
		
		CEObject portal0_0 = new CEObject("portal0_0", SceneBuilder.fromBlenderCoords(new Vector3f(-25.588f, 18.449f, 3.2847f)),
				new Vector3f(90, 0, 90), new Vector3f(1.437f, 1f, 2.920f), new ObjectConstraints(2f, 30));
		portal0_0.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		portal0_0.addModifier(new SoundNodeModifier(windBuffer, 4f, 16f, 0.75f, 1f));
		CEObject portal0_1 = new CEObject("portal0_1", SceneBuilder.fromBlenderCoords(new Vector3f(-172.258f, -29.5226f, -44.5366f + 20)),
				new Vector3f(-90, 0, 0), new Vector3f(1.437f, 1f, 2.920f), new ObjectConstraints(2f, 30));
		portal0_1.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		portal0_1.addModifier(new SoundNodeModifier(windBuffer, 50f, 60f, 0.75f, 1f));
		CEObject portal0_1Back = new CEObject("portal0_1Back", SceneBuilder.fromBlenderCoords(new Vector3f(-172.258f, -29.6226f, -44.5366f + 20)),
				new Vector3f(-90, 0, 0), new Vector3f(3.437f, 1f, 3.920f), new ObjectConstraints(1000, 1000));
		portal0_1Back.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		
		CEObject portal1_0 = new CEObject("portal1_0", SceneBuilder.fromBlenderCoords(new Vector3f(-4.4694f, -11.012f - 0.025f, 3.28468f - 0.1105f)),
				new Vector3f(-90, 0, -90), new Vector3f(1.692f, 1f, 2.699f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal1_0.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		CEObject portal1_1 = new CEObject("portal1_1", SceneBuilder.fromBlenderCoords(new Vector3f(-0.459123f, -13.0983f, -34.0954f - 0.1105f)),
				new Vector3f(90, 0, 90), new Vector3f(1.692f, 1f, 2.699f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal1_1.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		
		CEObject portal2_0 = new CEObject("portal2_0", SceneBuilder.fromBlenderCoords(new Vector3f(-9.94016f, -22.3653f, -33.9118f)),
				new Vector3f(-90, 0, 0), new Vector3f(1.692f, 1f, 3.127f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal2_0.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		CEObject portal2_1 = new CEObject("portal2_1", SceneBuilder.fromBlenderCoords(new Vector3f(-19.7093f, -32.1571f, -33.9118f)),
				new Vector3f(90, 0, 90), new Vector3f(1.692f, 1f, 3.127f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal2_1.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		
		CEObject portal3_0 = new CEObject("portal3_0", SceneBuilder.fromBlenderCoords(new Vector3f(4.22395f, 11.1454f, 3.01304f)),
				new Vector3f(90, 0, 90), new Vector3f(1.580f, 1f, 2.699f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal3_0.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		CEObject portal3_1 = new CEObject("portal3_1", SceneBuilder.fromBlenderCoords(new Vector3f(7.89559f, 13.6772f, -34.3158f)),
				new Vector3f(-90, 0, -90), new Vector3f(1.580f, 1f, 2.699f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal3_1.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		
		CEObject portal4_0 = new CEObject("portal4_0", SceneBuilder.fromBlenderCoords(new Vector3f(26.9482f, 32.7721f, -33.8793f)),
				new Vector3f(-90, 0, -90), new Vector3f(1.5795f, 1f, 3.16484f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal4_0.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		CEObject portal4_1 = new CEObject("portal4_1", SceneBuilder.fromBlenderCoords(new Vector3f(26.4444f, 32.7825f, -67.2256f)),
				new Vector3f(90, 0, 90), new Vector3f(1.5795f, 1f, 3.16484f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal4_1.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		
		CEObject portal5_0 = new CEObject("portal5_0", SceneBuilder.fromBlenderCoords(new Vector3f(17.218f, 22.926f, -33.971f)),
				new Vector3f(-90, 0, 180), new Vector3f(1.5795f, 1f, 3.16484f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal5_0.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		CEObject portal5_1 = new CEObject("portal5_1", SceneBuilder.fromBlenderCoords(new Vector3f(36.277f, 22.926f, -67.326f)),
				new Vector3f(90, 0, 0), new Vector3f(1.5795f, 1f, 3.16484f), new ObjectConstraints(4f, PORTAL_DISTANCE, PORTAL_RECURSE_DISTANCE, PORTAL_RECURSE_DISTANCE));
		portal5_1.addModifier(new PlaneMeshModifier(true, true, new Vector3f()));
		
		portal0_0.addModifier(new PlanarPortalModifier(portal0_1, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal0_0.getScale().x, portal0_0.getScale().z, player, 1280, 720));
		portal0_1.addModifier(new PlanarPortalModifier(portal0_0, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal0_1.getScale().x, portal0_1.getScale().z, player, 1280, 720));
		portal1_0.addModifier(new PlanarPortalModifier(portal1_1, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal1_0.getScale().x, portal1_0.getScale().z, player, 1280, 720, portal2_0));
		portal1_1.addModifier(new PlanarPortalModifier(portal1_0, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal1_1.getScale().x, portal1_1.getScale().z, player, 1280, 720));
		portal2_0.addModifier(new PlanarPortalModifier(portal2_1, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal2_0.getScale().x, portal2_0.getScale().z, player, 1280, 720));
		portal2_1.addModifier(new PlanarPortalModifier(portal2_0, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal2_1.getScale().x, portal2_1.getScale().z, player, 1280, 720, portal1_1));
		portal3_0.addModifier(new PlanarPortalModifier(portal3_1, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal3_0.getScale().x, portal3_0.getScale().z, player, 1280, 720, portal5_0));
		portal3_1.addModifier(new PlanarPortalModifier(portal3_0, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal3_1.getScale().x, portal3_1.getScale().z, player, 1280, 720));
		portal4_0.addModifier(new PlanarPortalModifier(portal4_1, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal4_0.getScale().x, portal4_0.getScale().z, player, 1280, 720));
		portal4_1.addModifier(new PlanarPortalModifier(portal4_0, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal4_1.getScale().x, portal4_1.getScale().z, player, 1280, 720));
		portal5_0.addModifier(new PlanarPortalModifier(portal5_1, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal5_0.getScale().x, portal5_0.getScale().z, player, 1280, 720));
		portal5_1.addModifier(new PlanarPortalModifier(portal5_0, new Vector3f(0, 1, 0), new Vector3f(0, 0, -1), portal5_1.getScale().x, portal5_1.getScale().z, player, 1280, 720, portal3_1));
		ArrayList<CEObject> portals = new ArrayList<>();
		portals.addAll(Arrays.asList(portal0_0, portal0_1, portal1_0, portal1_1, portal2_0, portal2_1, portal3_0, portal3_1, portal4_0, portal4_1, portal5_0, portal5_1));
		for(CEObject portal : portals)
			portal.insertModifier(0, new QueryModifier());
		
		// BEGIN SCENE BUILDER -------------------------------------------------------------------------------------------------------------------------------------------
		
		root("Ground", true, true, true, 0.25f, 0.0f, 0.618f, 0.5f, true, false, false);
		root("Path", true, true, false, 0.1f, 0.517f, 0.441f, 0.5f, true, false, false);
		root("Fence", true, true, true, 0.1f, 0.0f, 0.5f, 0.5f, false, true, false);
		root("Short Brick Building", "building", true, true, true, 0.5f, 0.0f, 0.667f, 1.0f, true, false, false);
		root("Tall Brick Building", "building", true, true, true, 0.5f, 0.0f, 0.667f, 1.0f, true, false, false);
		root("Wood Building", "building0", true, true, true, 0.5f, 0.0f, 0.708f, 1.0f, true, false, false);
		root("Stone Building", "building1", true, true, true, 0.5f, 0.0f, 0.483f, 1.0f, true, false, false);
		root("Wires", false, false, false, 0f, 1.0f, 0.267f, 1.0f, true, false, false);
		SceneBuilder.getRootObject("Wires").addModifier(new StaticColorModifier(new Vector3f(0.012f)));
		root("Blood 0 Mesh", "blood", true, false, false, 0f, 1.0f, 0.7f, 1.0f, false, true, false);
		root("Bench", "wood", true, false, true, 0.5f, 0.0f, 0.618f, 1.0f, true, false, true);
		root("Light Post", "lamp", true, true, true, 0.5f, 0.0f, 0.5f, 0.5f, true, false, false);
		SceneBuilder.getRootObject("Light Post").addModifier(new SpotLightModifier(new Vector3f(4f, 35f, 0),
				new Vector3f(0, -1, 0), 55, 2.0f, true, 10f, new Vector3f(1f, 1f, 0.7f), 10f, false, 0f, 1));
		SceneBuilder.getRootObject("Light Post").addModifier(new IlluminationMapModifier(ImageSampler.create(SceneBuilder
				.TEXTURE_DIRECTORY + "lampIllumination.png"), 0, 1f, 1, 1, 5f, true, false, 0, new Vector2f(), false, false));
		SceneBuilder.putRootObject("Light Post 2", "lightPost", "lamp", "lampNormal", "lightPostCollider", 0.5f, 0.0f, 0.5f, 0.5f, true, false, false);
		SceneBuilder.getRootObject("Light Post 2").addModifier(new SpotLightModifier(new Vector3f(4f, 35f, 0),
				new Vector3f(0, -1, 0), 55, 2.0f, true, 10f, new Vector3f(1f, 1f, 0.7f), 10f, false, 0f, 1));
		SceneBuilder.getRootObject("Light Post 2").addModifier(new IlluminationMapModifier(ImageSampler.create(SceneBuilder
				.TEXTURE_DIRECTORY + "lampIllumination.png"), 0, 1f, 1, 1, 5f, true, false, 0, new Vector2f(), false, false));
		SceneBuilder.getRootObject("Light Post 2").getModifier(SpotLightModifier.class).intensityProperty().bind(Properties.readOnlyFloatProperty(() -> (float) Math.random() * 10f));
		SceneBuilder.getRootObject("Light Post 2").getModifier(IlluminationMapModifier.class).blendFactorProperty().bind(Properties
				.readOnlyFloatProperty(() -> SceneBuilder.getRootObject("Light Post 2").getModifier(SpotLightModifier.class).getIntensity() / 2));
		root("Pole", true, true, true, 0.5f, 0.0f, 0.667f, 0.5f, true, false, false);
		root("Radio Tower", "tower", true, true, true, 0.5f, 0.818f, 0.527f, 0.5f, false, true, false);
		root("Small Fence", "fence", true, true, true, 0.1f, 0.0f, 0.5f, 0.5f, false, true, false);
		root("Grass", true, false, false, 0f, 0.0f, 1.0f, 0.0f, false, true, false);
		SceneBuilder.getRootObject("Grass").insertModifier(0, new NormalOverrideModifier(new Vector3f(0, 1, 0)));
		root("Cliff", "rock", true, true, true, 0.5f, 0.0f, 0.642f, 0.5f, true, false, false);
		root("Skulls 0 Mesh", "skull", true, false, false, 0f, 0.0f, 0.8f, 0.0f, true, false, true);
		root("Blood 1 Mesh", "blood", true, false, false, 0f, 0.0f, 0.7f, 1.0f, false, true, false);
		SceneBuilder.getRootObject("Blood 1 Mesh").getModifier(AbstractMeshModifier.class).setRotationPattern(RotationPattern.ROTATION_PATTERN_YZX);
		root("Ledge Fence", "fence", true, true, true, 0.1f, 0.0f, 0.5f, 0.5f, false, true, false);
		root("Lantern", "smallLamp", true, false, true, 0f, 0.0f, 0.5f, 0.5f, true, false, true);
		SceneBuilder.getRootObject("Lantern").getModifier(AbstractMeshModifier.class).setRotationPattern(RotationPattern.ROTATION_PATTERN_YZX);
		SceneBuilder.getRootObject("Lantern").addModifier(new IlluminationMapModifier(ImageSampler.create(SceneBuilder
				.TEXTURE_DIRECTORY + "smallLamp.png"), 0, 1f, 1, 1, 5f, true, false, 0, new Vector2f(), false, false));
		SceneBuilder.getRootObject("Lantern").addModifier(new PointLightModifier(new Vector3f(0, 0.7f, 0), new Vector3f(1f, 0.5f, 0.2f), 2f, true, 10f, false, false, 0f, 1));
		root("Interior", "room", true, true, true, 0.5f, 0.0f, 0.709f, 1.0f, true, false, false);
		root("Enclosed Interior", "room", true, true, true, 0.5f, 0.0f, 0.709f, 1.0f, true, false, false);
		root("Roof", "room", true, true, true, 0.5f, 0.0f, 0.709f, 1.0f, false, false, false);
		root("Cabinet", true, true, true, 0.5f, 0.642f, 0.783f, 0.758f, true, false, false);
		SceneBuilder.getRootObject("Cabinet").getModifier(AbstractMeshModifier.class).setRotationPattern(RotationPattern.ROTATION_PATTERN_YZX);
		root("Carpet 1 Mesh", "carpet0", true, false, false, 0f, 0.0f, 0.650f, 0.5f, true, false, true);
		root("Carpet 0 Mesh", "carpet", true, false, false, 0f, 0.0f, 0.650f, 0.5f, true, false, true);
		SceneBuilder.putRootObject("Wood Table", "table", "wood", "woodNormal", "tableCollider", 0.5f, 0.0f, 0.8f, 1.0f, true, false, false);
		SceneBuilder.putRootObject("Metal Table", "table", "metal", "metalNormal", "tableCollider", 0.5f, 1.0f, 0.483f, 0.5f, true, false, false);
		SceneBuilder.putRootObject("Wood Chair", "chair", "wood", "woodNormal", "chairCollider", 0.5f, 0.0f, 0.8f, 1.0f, true, false, false);
		SceneBuilder.putRootObject("Metal Chair", "chair", "metal", "metalNormal", "chairCollider", 0.5f, 1.0f, 0.483f, 0.5f, true, false, false);
		root("Monitor", true, false, true, 0f, 0.0f, 0.5f, 0.5f, true, false, true);
		root("Keyboard", true, false, false, 0f, 0.0f, 0.85f, 0.5f, true, false, true);
		root("Skulls 1 Mesh", "skull", true, false, false, 0f, 0.0f, 0.8f, 0.0f, true, false, true);
		root("Blood 2 Mesh", "blood", true, false, false, 0f, 0.0f, 0.7f, 1.0f, false, true, false);
		root("Blades", "blade", true, false, false, 0.5f, 0.0f, 0.405f, 1.0f, true, false, true);
		root("Candle", true, false, false, 0.5f, 0.0f, 0.5f, 0.5f, true, false, false);
		SceneBuilder.getRootObject("Candle").addModifier(new IlluminationMapModifier(ImageSampler.create(SceneBuilder
				.TEXTURE_DIRECTORY + "candleIllumination.png"), 0, 1f, 1, 1, 3.5f, true, false, 0, new Vector2f(), false, false));
		SceneBuilder.getRootObject("Candle").addModifier(new PointLightModifier(new Vector3f(0, 0.4f, 0), new Vector3f(1f, 0.5f, 0.2f), 1f, true, 2f, false, false, 0f, 1));
		root("Light Square", "monitor", true, false, false, 0f, 0.0f, 0.5f, 0.5f, true, false, true);
		SceneBuilder.getRootObject("Light Square").addModifier(new PointLightModifier(new Vector3f(-10, 0, 0), new Vector3f(1f), 2f, true, 30f, false, false, 0f, 1));
		SceneBuilder.getRootObject("Light Square").getModifier(PointLightModifier.class).intensityProperty().bind(Properties.readOnlyFloatProperty(() -> (float) Math.random() + 0.5f));
		root("Blood 3 Mesh", "blood", true, false, false, 0f, 0.0f, 0.7f, 1.0f, false, true, false);
		SceneBuilder.getRootObject("Blood 3 Mesh").getModifier(AbstractMeshModifier.class).setRotationPattern(RotationPattern.ROTATION_PATTERN_YZX);
		root("Wood Pieces", "wood", true, true, true, 0.5f, 0.0f, 0.558f, 0.5f, true, false, false);
		root("Paradox", true, false, false, 0f, 0.0f, 0.7f, 1.0f, false, true, false);
		root("Carpet 2 Mesh", "carpet0", true, false, false, 0f, 0.0f, 0.650f, 0.5f, true, false, true);
		root("Pillow", true, true, true, 0.5f, 0.0f, 0.650f, 0.5f, true, false, false);
		root("Blood 4 Mesh", "blood", true, false, false, 0f, 0.0f, 0.7f, 1.0f, false, true, false);
		root("Bed", true, true, true, 0.5f, 0.0f, 0.664f, 0.5f, true, false, false);
		
		ref("Ground", new Vector3f(), new Vector3f(), new Vector3f(1.68948f, 1.68948f, -0.414179f));
		ref("Path", new Vector3f(-0.157135f, -0.063388f, 0.464779f), new Vector3f(), new Vector3f(32.137f));
		ref("Fence", new Vector3f(0.007335f, 0f, 14.4507f), new Vector3f(), new Vector3f(36.8922f, 36.8627f, -2.35202f));
		ref("Short Brick Building", new Vector3f(-15, -15, 13.026f), new Vector3f(), new Vector3f(11, 11, 12.713f));
		ref("Tall Brick Building", new Vector3f(15, 15, 17.583f), new Vector3f(), new Vector3f(11, 11, 17.229f));
		ref("Wood Building", new Vector3f(-15, 15, 11.402f), new Vector3f(), new Vector3f(11, 11, 11));
		ref("Stone Building", new Vector3f(15, -15, 14.567f), new Vector3f(), new Vector3f(11, 11, 14.201f));
		ref("Wires", new Vector3f(4.0004f, 24.371f, 26.755f), new Vector3f(-3.79f, 2.15f, 7.97f), new Vector3f(1f));
		ref("Blood 0 Mesh", new Vector3f(), new Vector3f(), new Vector3f(1f));
		ref("Bench", new Vector3f(-6.82595f, -3.07631f, 1.70948f), new Vector3f(), new Vector3f(0.074f, 0.074f, 0.568f));
		ref("Bench", new Vector3f(23.7565f, 26.9496f, 1.70948f), new Vector3f(), new Vector3f(0.074f, 0.074f, 0.568f));
		ref("Bench", new Vector3f(-15.0977f, -26.9775f, 1.70948f), new Vector3f(), new Vector3f(0.074f, -0.074f, 0.568f));
		ref("Light Post", new Vector3f(-6.7567f, -27.237f, 0.49881f), new Vector3f(0, 0, 270), new Vector3f(1.146f, 1.146f, 0.201f));
		ref("Light Post", new Vector3f(11.7693f, 2.84211f, 0.498813f), new Vector3f(0, 0, 270), new Vector3f(1.145f, 1.145f, 0.201f));
		ref("Light Post 2", new Vector3f(-11.7395f, -2.88328f, 0.498813f), new Vector3f(0, 0, 89.9f), new Vector3f(1.196f, 1.196f, 0.210f));
		ref("Light Post", new Vector3f(19.4991f, 27.0684f, 0.498813f), new Vector3f(0, 0, 450), new Vector3f(1.145f, 1.145f, 0.201f));
		ref("Pole", new Vector3f(31.394f, -31.383f, 0.41434f), new Vector3f(), new Vector3f(0.197f));
		ref("Radio Tower", new Vector3f(6.1189f, 6.0701f, 36.879f), new Vector3f(), new Vector3f(1.852f, 1.852f, 0.183f));
		ref("Small Fence", new Vector3f(-3.2144f, 11.244f, 2.6739f), new Vector3f(), new Vector3f(36.892f, 36.863f, -2.352f));
		ref("Grass", new Vector3f(0.033647f, 0.078537f, 0.464765f), new Vector3f(), new Vector3f(1f));
		ref("Cliff", new Vector3f(-175.069f, -8.08316f, -47.4167f + 20), new Vector3f(0, 0, -90), new Vector3f(28.854f, 19.383f, 28.854f));
		ref("Skulls 0 Mesh", new Vector3f(-171.09f, 7.4584f, -45.211f + 20), new Vector3f(0, 0, -90), new Vector3f(1f));
		ref("Blood 1 Mesh", new Vector3f(-178.24f, 1.2824f, -47.066f + 20), new Vector3f(13.6f, -2.57f, -88.9f), new Vector3f(1f));
		ref("Ledge Fence", new Vector3f(-193.82f, -7.7515f, -45.715f + 20), new Vector3f(0, 0, -90), new Vector3f(36.892f, 36.863f, -2.352f));
		ref("Lantern", new Vector3f(-172.75f, 2.2864f, -46.72f + 20), new Vector3f(-7.39f, -3.8f, -87.4f), new Vector3f(1f));
		ref("Interior", new Vector3f(-19.4824f, -22.6227f, -37.032f), new Vector3f(), new Vector3f(19.068f, 19.068f, 0.085f));
		ref("Interior", new Vector3f(26.7476f, 23.2181f, -37.032f), new Vector3f(0, 0, 180), new Vector3f(19.068f, 19.068f, 0.085f));
		ref("Enclosed Interior", new Vector3f(26.7476f, 23.2181f, -70.3853f), new Vector3f(0, 0, 90), new Vector3f(19.068f, 19.068f, 0.085f));
		ref("Roof", new Vector3f(-19.4824f, -22.6227f, -35.2563f), new Vector3f(0, 0, 0), new Vector3f(19.068f, 19.068f, 0.085f));
		ref("Roof", new Vector3f(26.7476f, 23.2181f, -35.2563f), new Vector3f(0, 0, 180), new Vector3f(19.068f, 19.068f, 0.085f));
		ref("Roof", new Vector3f(26.7476f, 23.2181f, -68.6096f), new Vector3f(0, 0, 90), new Vector3f(19.068f, 19.068f, 0.085f));
		ref("Cabinet", new Vector3f(-17.046f, -5.9349f, -34.934f), new Vector3f(0, 0, 30), new Vector3f(0.786f, 1f, 2.076f));
		ref("Cabinet", new Vector3f(-14.6f, -5.9f, -34.9f), new Vector3f(0, 0, 3.5f), new Vector3f(0.786f, 1f, 2.076f));
		ref("Cabinet", new Vector3f(-12.6f, -5.9f, -34.9f), new Vector3f(0, 0, -5.4f), new Vector3f(0.786f, 1f, 2.076f));
		ref("Cabinet", new Vector3f(-2.6f, -5.9f, -34.9f), new Vector3f(0.04f, 33.6f, 2.78f), new Vector3f(0.786f, 1f, 2.076f));
		ref("Carpet 1 Mesh", new Vector3f(-11.7f, -12.9f, -36.9f), new Vector3f(0, 0, -122), new Vector3f(3.133f, 5.053f, 5.053f));
		ref("Lantern", new Vector3f(-18.5f, -18.5f, -33.2f), new Vector3f(-0.1f, 0.4f, 179f), new Vector3f(1f));
		ref("Wood Table", new Vector3f(-16.2f, -19.2f, -35.444f), new Vector3f(0, 0, -52.7f), new Vector3f(1f));
		ref("Wood Chair", new Vector3f(-14.1f, -18.7f, -36.9f), new Vector3f(0, 0, -187), new Vector3f(1f));
		ref("Monitor", new Vector3f(-16.2f, -19.1f, -35.064f), new Vector3f(0, 0, 15.5f), new Vector3f(1f));
		ref("Keyboard", new Vector3f(-15.5f, -18.9f, -35.064f), new Vector3f(0, 0, 27.7f), new Vector3f(0.221f, 0.71f, -0.019f));
		ref("Carpet 0 Mesh", new Vector3f(-29.635f, -12.93f, -36.93f), new Vector3f(0, 0, -38.8f), new Vector3f(2.774f, 4.473f, 4.473f));
		ref("Wood Chair", new Vector3f(-26.93f, -16.298f, -36.947f), new Vector3f(0, 0, -245), new Vector3f(1f));
		ref("Wood Chair", new Vector3f(-33.728f, -10.331f, -36.947f), new Vector3f(0, 0, -26.7f), new Vector3f(1f));
		ref("Wood Table", new Vector3f(-30.668f, -6.5146f, -35.444f), new Vector3f(0, 0, 9.35f), new Vector3f(1f));
		ref("Lantern", new Vector3f(-30.166f, -6.6427f, -35.035f), new Vector3f(-0.118f, 0.426f, 143f), new Vector3f(1f));
		ref("Wood Table", new Vector3f(-36.206f, -30.653f, -35.444f), new Vector3f(0, 0, 101), new Vector3f(1f));
		ref("Lantern", new Vector3f(-37.71f, -25.067f, -33.76f), new Vector3f(-0.118f, 0.426f, 180f), new Vector3f(1f));
		ref("Skulls 1 Mesh", new Vector3f(-35.197f, -35.016f, -36.573f), new Vector3f(), new Vector3f(1f));
		ref("Blood 2 Mesh", new Vector3f(-29.228f, -32.044f, -33.925f), new Vector3f(), new Vector3f(1f));
		ref("Blades", new Vector3f(-25.213f, -35.284f, -36.903f), new Vector3f(), new Vector3f(0.764f, 0.764f, -0.034f));
		ref("Wood Table", new Vector3f(13.373f, 18.166f, -35.311f), new Vector3f(0, 0, -13.5f), new Vector3f(1.074f));
		ref("Wood Table", new Vector3f(20.9507f, 9.28903f, -35.2947f), new Vector3f(0, 0, -2.36f), new Vector3f(1.188f));
		ref("Wood Table", new Vector3f(14.4716f, 9.28903f, -35.2778f), new Vector3f(0, 0, 7.71f), new Vector3f(1.124f));
		ref("Wood Chair", new Vector3f(13.13f, 16.401f, -36.942f), new Vector3f(0, 0, -250), new Vector3f(1f));
		ref("Wood Chair", new Vector3f(14.042f, 11.273f, -36.942f), new Vector3f(0, 0, -111), new Vector3f(1f));
		ref("Wood Chair", new Vector3f(20.667f, 11.483f, -36.949f), new Vector3f(0, 0, -103), new Vector3f(1f));
		ref("Candle", new Vector3f(15.031f, 17.841f, -34.911f), new Vector3f(), new Vector3f(1.270f));
		ref("Candle", new Vector3f(15.922f, 9.7302f, -34.851f), new Vector3f(), new Vector3f(1.270f));
		ref("Cabinet", new Vector3f(34.5474f, 6.1237f, -34.9183f), new Vector3f(0.0f, 0.0f, deg(2.9f)), new Vector3f(0.7858f, 0.9995f, 2.0755f));
		ref("Cabinet", new Vector3f(30.5509f, 6.1237f, -34.9183f), new Vector3f(0.0f, -0.0f, deg(3.2f)), new Vector3f(0.7858f, 0.9995f, 2.0755f));
		ref("Cabinet", new Vector3f(28.4988f, 6.1237f, -34.9183f), new Vector3f(0.0f, -0.0f, deg(3.0f)), new Vector3f(0.7858f, 0.9995f, 2.0755f));
		ref("Cabinet", new Vector3f(40.3614f, 6.2799f, -34.9183f), new Vector3f(0.0f, 0.0f, deg(3.3f)), new Vector3f(0.78587f, 0.9995f, 2.0755f));
		ref("Cabinet", new Vector3f(38.3333f, 5.9676f, -34.9183f), new Vector3f(0.0f, -0.0f, deg(3.1f)), new Vector3f(0.7858f, 0.9995f, 2.07552f));
		ref("Cabinet", new Vector3f(30.5320f, 20.1293f, -36.2720f), new Vector3f(deg(-1.6f), deg(1.6f), deg(3.6f)), new Vector3f(0.7858f, 0.999f, 2.07552f));
		ref("Cabinet", new Vector3f(33.1167f, 19.9533f, -35.4066f), new Vector3f(-0.0f, deg(2.0f), deg(6.4f)), new Vector3f(0.7858f, 0.999f, 2.07552f));
		ref("Light Square", new Vector3f(45.3149f, 13.5939f, -33.4771f), new Vector3f(), new Vector3f(0.03f, 1.364f, 0.84f));
		ref("Candle", new Vector3f(41.319f, 40.064f, -36.954f), new Vector3f(), new Vector3f(1.27f));
		ref("Blood 3 Mesh", new Vector3f(41.016f, 41.752f, -35.257f), new Vector3f(89.6f, 1.13f, -0.386f), new Vector3f(0.925f));
		ref("Wood Pieces", new Vector3f(34.215f, 32.909f, -36.947f), new Vector3f(0, 0, -21.4f), new Vector3f(1f));
		ref("Paradox", new Vector3f(35.809f, 41.769f, -34.074f), new Vector3f(90, 0, 0), new Vector3f(4.967f, 4.516f, -1.824f));
		ref("Metal Table", new Vector3f(22.909330368041992f, 39.77126693725586f, -68.69511413574219f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-0.1442897468805313f)), new Vector3f(1.0738439559936523f, 1.0738439559936523f, 1.0738438367843628f));
		ref("Metal Table", new Vector3f(17.189739227294922f, 39.77126693725586f, -68.69511413574219f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-0.027471154928207397f)), new Vector3f(1.0738439559936523f, 1.0738439559936523f, 1.0738438367843628f));
		ref("Metal Table", new Vector3f(11.112459182739258f, 38.80731201171875f, -68.69511413574219f), new Vector3f(deg(0.0f), deg(-0.0f), deg(0.6939655542373657f)), new Vector3f(1.0738439559936523f, 1.0738439559936523f, 1.0738438367843628f));
		ref("Lantern", new Vector3f(8.489545822143555f, 32.74944305419922f, -66.07799530029297f), new Vector3f(deg(-0.00206240126863122f), deg(0.007436768151819706f), deg(3.122044563293457f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Metal Chair", new Vector3f(18.074308395385742f, 37.22275390625f, -70.28965759277344f), new Vector3f(deg(0.0f), deg(0.0f), deg(-4.35781717300415f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Metal Chair", new Vector3f(22.20332908630371f, 37.32275390625f, -70.28965759277344f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-5.0884013175964355f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Metal Chair", new Vector3f(12.708964347839355f, 36.57484436035156f, -70.28965759277344f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-3.22965931892395f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Lantern", new Vector3f(18.409957885742188f, 41.358306884765625f, -66.07799530029297f), new Vector3f(deg(-0.002062401035800576f), deg(0.007436767686158419f), deg(1.5512481927871704f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Monitor", new Vector3f(17.60887336730957f, 39.8303337097168f, -68.26884460449219f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-1.9072189331054688f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Keyboard", new Vector3f(17.376493453979492f, 39.1485710144043f, -68.26883697509766f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-1.6928356885910034f)), new Vector3f(0.2206057608127594f, 0.7102429866790771f, -0.018801754340529442f));
		ref("Monitor", new Vector3f(23.093975067138672f, 40.90553665161133f, -68.16046905517578f), new Vector3f(deg(-0.007897390983998775f), deg(-0.39556941390037537f), deg(-1.6898612976074219f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Keyboard", new Vector3f(22.46243667602539f, 38.17426681518555f, -68.47140502929688f), new Vector3f(deg(0.7765000462532043f), deg(0.0212074127048254f), deg(0.039258524775505066f)), new Vector3f(0.2206057608127594f, 0.7102429866790771f, -0.018801754340529442f));
		ref("Monitor", new Vector3f(10.845351219177246f, 38.82818603515625f, -68.26884460449219f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-0.6802169680595398f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Keyboard", new Vector3f(11.241166114807129f, 38.226409912109375f, -68.26883697509766f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-0.9777189493179321f)), new Vector3f(0.2206057608127594f, 0.7102429866790771f, -0.018801754340529442f));
		ref("Carpet 2 Mesh", new Vector3f(16.860206604003906f, 13.816944122314453f, -70.28680419921875f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-1.5702160596847534f)), new Vector3f(3.5944952964782715f, 5.797280311584473f, 5.797280311584473f));
		ref("Pillow", new Vector3f(19.87666893005371f, 8.534431457519531f, -70.83824157714844f), new Vector3f(deg(0.0f), deg(-0.0f), deg(0.15258897840976715f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Pillow", new Vector3f(14.172952651977539f, 8.534431457519531f, -70.83824157714844f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-0.04796550050377846f)), new Vector3f(1.0796550512313843f, 1.1466525793075562f, 1.0794954299926758f));
		ref("Pillow", new Vector3f(13.764415740966797f, 19.148277282714844f, -70.83824157714844f), new Vector3f(deg(0.0f), deg(-0.0f), deg(3.1749489307403564f)), new Vector3f(1.0096601247787476f, 1.0096601247787476f, 1.0096601247787476f));
		ref("Pillow", new Vector3f(19.523231506347656f, 19.148277282714844f, -70.83824157714844f), new Vector3f(deg(0.0f), deg(-0.0f), deg(3.2488248348236084f)), new Vector3f(1.0900846719741821f, 1.1577293872833252f, 1.0899235010147095f));
		ref("Candle", new Vector3f(16.819f, 13.845f, -70.279f), new Vector3f(), new Vector3f(1.27f));
		ref("Bed", new Vector3f(41.63258743286133f, 6.665035247802734f, -70.30010223388672f), new Vector3f(deg(0.0f), deg(-0.0f), deg(0.0660974383354187f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Bed", new Vector3f(41.53643035888672f, 10.46551513671875f, -70.30010223388672f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-0.1278219074010849f)), new Vector3f(0.9826346039772034f, 0.9826346039772034f, 0.9826345443725586f));
		ref("Bed", new Vector3f(42.180030822753906f, 14.571489334106445f, -70.30010223388672f), new Vector3f(deg(0.0f), deg(-0.0f), deg(0.18918859958648682f)), new Vector3f(1.0f, 1.0f, 1.0f));
		ref("Bed", new Vector3f(42.28612518310547f, 19.21874237060547f, -70.30010223388672f), new Vector3f(deg(0.0f), deg(-0.0f), deg(-0.1484767347574234f)), new Vector3f(1.048205018043518f, 1.048205018043518f, 1.048205018043518f));
		ref("Blood 4 Mesh", new Vector3f(41.75820541381836f, 14.083203315734863f, -68.34242248535156f), new Vector3f(deg(1.564015507698059f), deg(0.01964442990720272f), deg(-0.006741928867995739f)), new Vector3f(0.9247722029685974f, 0.9247722029685974f, 0.9247722029685974f));
		ref("Lantern", new Vector3f(44.8920783996582f, 13.527278900146484f, -65.96422576904297f), new Vector3f(deg(-0.0020624008029699326f), deg(0.007436767220497131f), deg(-0.01954817585647106f)), new Vector3f(1.0f, 1.0f, 1.0f));
		
		SceneBuilder.putObject(portal0_0);
		SceneBuilder.putObject(portal0_1Back);
		SceneBuilder.putObject(portal0_1);
		SceneBuilder.putObject(portal1_0);
		SceneBuilder.putObject(portal1_1);
		SceneBuilder.putObject(portal2_0);
		SceneBuilder.putObject(portal2_1);
		SceneBuilder.putObject(portal3_0);
		SceneBuilder.putObject(portal3_1);
		SceneBuilder.putObject(portal4_0);
		SceneBuilder.putObject(portal4_1);
		SceneBuilder.putObject(portal5_0);
		SceneBuilder.putObject(portal5_1);
		
		SceneBuilder.putObject(player);
		SceneBuilder.putObject(camera);
		SceneBuilder.putObject(lightning);
		SceneBuilder.putObject(scaryTrigger);
		Scene scene = SceneBuilder.toScene();
		
		// END SCENE BUILDER ---------------------------------------------------------------------------------------------------------------------------------------------
		
		ShadingSystem pps = new ShadingSystem(1920, 1080);
		ShadingNode input = new SourceColorInputNode();
		ShadingNode extract = new LuminanceExtractionNode();
		extract.getInputs().get(0).setLink(input.getOutputs().get(0));
		((Attribute) extract.getInputs().get(1)).getConstant().setValue(new Vector4f(0.5f));
		ShadingNode hblur = new GaussianHBlurNode(32, 10.0f, 2);
		ShadingNode vblur = new GaussianVBlurNode(32, 10.0f, 2);
		hblur.getInputs().get(0).setLink(extract.getOutputs().get(0));
		vblur.getInputs().get(0).setLink(hblur.getOutputs().get(0));
		ShadingNode bright = new BrightnessContrastNode(0.85f, 1.0f);
		bright.getInputs().get(0).setLink(vblur.getOutputs().get(0));
		ShadingNode add = new AdditiveCompositeNode();
		add.getInputs().get(0).setLink(input.getOutputs().get(0));
		add.getInputs().get(1).setLink(bright.getOutputs().get(0));
		ShadingNode hdr = new ReinhardToneMapNode(1.5f);
		hdr.getInputs().get(0).setLink(add.getOutputs().get(0));
		pps.getOutputNode().getInputs().get(0).setLink(hdr.getOutputs().get(0));
		scene.setPostProcessingSystem(pps);
		
		while(!EngineRuntime.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_F12)) {
			
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) player.getBaseScale().y = PLAYER_HEIGHT / 4f;
			else player.getBaseScale().y = PLAYER_HEIGHT / 2f;
			LightningController.update(scene);
			if(camera.getPosition().y <= -30 && lightningIntensity > 0f) lightningIntensity = Math.max(0, lightningIntensity - LIGHTNING_FADE_SPEED);
			else if(camera.getPosition().y > -30 && lightningIntensity < 1f) lightningIntensity = Math.min(1, lightningIntensity + LIGHTNING_FADE_SPEED);
			
			renderer.render(scene, "Camera", new RenderConstraints());
			EngineRuntime.update(60);
			
		}
		
		for(CEObject portal : portals)
			portal.getModifier(QueryModifier.class).getQuery().delete();
		EngineRuntime.destroy();
		
	}
	
	private static void root(String identifier, boolean texture, boolean normal, boolean collider, float normIntensity,
			float metallic, float roughness, float specular, boolean cullBackface, boolean alpha, boolean restrictTangents) {
		root(identifier, null, texture, normal, collider, normIntensity, metallic, roughness, specular, cullBackface, alpha, restrictTangents);
	}
	
	private static void root(String identifier, String texIdentifier, boolean texture, boolean normal, boolean collider,
			float normIntensity, float metallic, float roughness, float specular, boolean cullBackface, boolean alpha, boolean restrictTangents) {
		String[] split = identifier.split(" ");
		String name = Character.toLowerCase(split[0].charAt(0)) + split[0].substring(1);
		for(int i = 1 ; i < split.length ; ++i)
			name += split[i];
		SceneBuilder.putRootObject(identifier, name, texture ? (texIdentifier == null ? name : texIdentifier) : null, normal ? (texIdentifier == null
				? name : texIdentifier) + "Normal" : null, collider ? name + "Collider" : null, normIntensity, metallic, roughness, specular, cullBackface, alpha, restrictTangents);
	}
	
	private static void ref(String identifier, Vector3f position, Vector3f rotation, Vector3f scale) {
		SceneBuilder.putObject(identifier, position, rotation, scale);
	}
	
	private static float deg(float rad) {
		return (float) Math.toDegrees(rad);
	}
	
}
