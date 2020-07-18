package nonEuclidianDemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import celestial.core.CEObject;
import celestial.core.CEObjectReference;
import celestial.core.ObjectConstraints;
import celestial.data.DataReader;
import celestial.data.ImageSampler;
import celestial.data.VertexBuffer;
import celestial.scene.Scene;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import mod.celestial.material.ProceduralBSDFModifier;
import mod.celestial.mesh.AbstractMeshModifier;
import mod.celestial.mesh.BvhTriangleMeshModifier;
import mod.celestial.mesh.BvhTriangleMeshModifier.BvhTriangleVAO;
import mod.celestial.physics.BvhCollisionObjectModifier;
import mod.celestial.texture.BumpMapModifier;
import mod.celestial.texture.DiffuseMapModifier;

public class SceneBuilder {
	
	public static final String TEXTURE_DIRECTORY = "res\\Scenes\\nonEuclidianScene\\";
	public static final String MESH_DIRECTORY = "res\\Scenes\\nonEuclidianScene\\meshes\\";
	public static final String COLLIDER_DIRECTORY = "res\\Scenes\\nonEuclidianScene\\colliders\\";
	
	private static final Map<String, CEObject> OBJECT_MAP = new HashMap<String, CEObject>();
	private static final Map<CEObject, Integer> CHILD_COUNTS = new HashMap<CEObject, Integer>();
	private static final List<CEObject> SCENE_LIST = new ArrayList<CEObject>();
	
	public static void putRootObject(String identifier, String model, String texture, String normal, String collider, float normIntensity,
			float metallic, float roughness, float specular, boolean cullBackface, boolean alpha, boolean restrictTangents) {
		CEObject root = new CEObject(identifier, new Vector3f(), new Vector3f(), new Vector3f(1f), new ObjectConstraints(1000, 1000));
		if(texture != null)
			root.addModifier(new DiffuseMapModifier(ImageSampler.create(TEXTURE_DIRECTORY + texture + ".png"), 0, 1f, 1, 1, false, 0, new Vector2f(), false, false));
		if(normal != null)
			root.addModifier(new BumpMapModifier(ImageSampler.create(TEXTURE_DIRECTORY + normal + ".png"), 0, 1f, 1, 1, normIntensity, false, 0, new Vector2f(), false, false));
		root.addModifier(new ProceduralBSDFModifier(0f, new Vector3f(), 1, 1, 0, 0, metallic, roughness, specular, 0f, 1f, 0.05f, 1f, 0.04f, false, restrictTangents));
		if(model != null) {
			loadMesh(root, model, cullBackface);
			if(alpha) root.getModifier(AbstractMeshModifier.class).setBlendMode(AbstractMeshModifier.BLEND_MODE_ALPHA);
		}
		if(collider != null)
			loadCollisionMesh(root, collider);
		OBJECT_MAP.put(identifier, root);
	}
	
	public static CEObject getRootObject(String identifier) {
		return OBJECT_MAP.get(identifier);
	}
	
	public static void putObject(String rootIdentifier, Vector3f position, Vector3f rotation, Vector3f scale) {
		CEObject root = OBJECT_MAP.get(rootIdentifier);
		if(CHILD_COUNTS.get(root) == null)
			CHILD_COUNTS.put(root, 0);
		int count = CHILD_COUNTS.get(root);
		CHILD_COUNTS.put(root, count + 1);
		CEObject child = new CEObjectReference(root.getIdentifier() + "." + count, fromBlenderCoords(position), fromBlenderCoords(rotation), fromBlenderScale(scale), root);
		SCENE_LIST.add(child);
	}
	
	public static void putObject(CEObject obj) {
		SCENE_LIST.add(obj);
	}
	
	public static Scene toScene() {
		Scene scene = new Scene("Non Euclidian Scene");
		for(CEObject obj : SCENE_LIST)
			scene.getBaseLayer().addObject(obj);
		return scene;
	}
	
	private static void loadMesh(CEObject obj, String model, boolean cullBackface) {
		obj.addModifier(new BvhTriangleMeshModifier(cullBackface, true, new Vector3f(), 1,
				new BvhTriangleVAO(VertexBuffer.createIndexBuffer(VertexBuffer.DRAW_TYPE_STATIC).allocatei(MESH_DIRECTORY + model + ".obj", DataReader.DATA_FORMAT_OBJ, 0, 4),
				new UnsortedAttrib("position", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(MESH_DIRECTORY + model + ".obj", DataReader.DATA_FORMAT_OBJ, 0, 0)),
				new UnsortedAttrib("texCoords", VertexBuffer.create(2, VertexBuffer.DRAW_TYPE_STATIC).allocatef(MESH_DIRECTORY + model + ".obj", DataReader.DATA_FORMAT_OBJ, 0, 1)),
				new UnsortedAttrib("normal", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(MESH_DIRECTORY + model + ".obj", DataReader.DATA_FORMAT_OBJ, 0, 2)),
				new UnsortedAttrib("tangent", VertexBuffer.create(3, VertexBuffer.DRAW_TYPE_STATIC).allocatef(MESH_DIRECTORY + model + ".obj", DataReader.DATA_FORMAT_OBJ, 0, 3))
				)));
	}
	
	private static void loadCollisionMesh(CEObject obj, String model) {
		obj.addModifier(new BvhCollisionObjectModifier(
				DataReader.readi(COLLIDER_DIRECTORY + model + ".obj", DataReader.DATA_FORMAT_OBJ, 0, 4),
				DataReader.readf(COLLIDER_DIRECTORY + model + ".obj", DataReader.DATA_FORMAT_OBJ, 0, 0), 0.5f));
	}
	
	public static Vector3f fromBlenderCoords(Vector3f coords) {
		return new Vector3f(coords.x, coords.z, -coords.y);
	}
	
	public static Vector3f fromBlenderScale(Vector3f coords) {
		return new Vector3f(coords.x, coords.z, coords.y);
	}
	
}
