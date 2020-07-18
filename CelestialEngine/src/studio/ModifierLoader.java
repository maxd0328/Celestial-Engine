package studio;

import mod.celestial.filter.*;
import mod.celestial.input.*;
import mod.celestial.light.*;
import mod.celestial.mesh.*;
import mod.celestial.misc.*;
import mod.celestial.physics.*;
import mod.celestial.render.*;
import mod.celestial.sound.*;
import mod.celestial.texture.*;
import studio.celestial.core.StudioInterface;

public class ModifierLoader {
	
	public static void loadModifiers() {
		
		StudioInterface.getInstantiation().addModifierCategory("Filter");
		StudioInterface.getInstantiation().addModifier("Filter", "Alpha Modifier", "Sets output alpha value of all pixels in\nthe object.", AlphaModifier.FACTORY, AlphaModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Frag Fog Effect Modifier", "See 'Fog Effect Modifier'. Works similarly,\nbut on the per-fragment level. Good for\nlarge flat objects.", FragFogEffectModifier.FACTORY, FragFogEffectModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Fog Effect Modifier", "Simulates fog by fading an object's color\nto a single static color as distance increases.", FogEffectModifier.FACTORY, FogEffectModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Gradient Modifier", "Applies a color gradient to the object.\nThis gradient is measured by the distance\nover a linein object space.", GradientModifier.FACTORY, GradientModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Mesh Distortion Modifier", "Uses functions of trigonometry tocreate\na vertex distortion over a given axis in object\nspace.", MeshDistortionModifier.FACTORY, MeshDistortionModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Normal Override Modifier", "Overrides all object-space normals in an\nobject to a single static normal in order to\nsimulate fake lighting.", NormalOverrideModifier.FACTORY, NormalOverrideModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Projective Mapping Modifier", "Overrides an object's texture coordinates\nin order to map textures relative to the screen\ncoordinates.", ProjectiveMappingModifier.FACTORY, ProjectiveMappingModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Static Color Modifier", "Sets all pixels of this object to show\na single static color.", StaticColorModifier.FACTORY, StaticColorModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Tint Modifier", "Adds a tint to the object's color measured by a\nblend factor. This modifier does not affect\nalpha values.", TintModifier.FACTORY, TintModifier.class);
		StudioInterface.getInstantiation().addModifier("Filter", "Wireframe Modifier", "Sets the draw mode of this object to be\nwireframe mode. No triangles of this object\nwill befilled.", WireframeModifier.FACTORY, WireframeModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Input");
		StudioInterface.getInstantiation().addModifier("Input", "Key Transform Modifier", "Causes the object to transform by given\ndimensions as long as a certain key is pressed.", KeyTransformModifier.FACTORY, KeyTransformModifier.class);
		StudioInterface.getInstantiation().addModifier("Input", "Mouse Transform Modifier", "Causes the object to transform by given\ndimensions as long as a certain mouse\nbutton is down.", MouseTransformModifier.FACTORY, MouseTransformModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Light");
		StudioInterface.getInstantiation().addModifier("Light", "Directional Light Modifier", "Causes the object to emit a single-drectional\nlight which does not attenuate.", DirectionalLightModifier.FACTORY, DirectionalLightModifier.class);
		StudioInterface.getInstantiation().addModifier("Light", "Point Light Modifier", "Causes the object to emit an omni-directional,\nattenuating light source from a single position.", PointLightModifier.FACTORY, PointLightModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Mesh");
		StudioInterface.getInstantiation().addModifier("Mesh", "Box Mesh Modifier", meshDesc("cubic-shaped"), BoxMeshModifier.FACTORY, BoxMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Bvh Triangle Mesh Modifier", meshDesc("custom triangle-mesh"), BvhTriangleMeshModifier.FACTORY, BvhTriangleMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Cone Mesh Modifier", meshDesc("cone-shaped"), ConeMeshModifier.FACTORY, ConeMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Cross Section Mesh Modifier", meshDesc("cross-section"), CrossSectionMeshModifier.FACTORY, CrossSectionMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Cylinder Mesh Modifier", meshDesc("cylindrical"), CylinderMeshModifier.FACTORY, CylinderMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Heightmap Mesh Modifier", meshDesc("custom heightmap"), HeightmapMeshModifier.FACTORY, HeightmapMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Plane Mesh Modifier", meshDesc("flat plane"), PlaneMeshModifier.FACTORY, PlaneMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Point Mesh Modifier", "Adds a viewport-aligned quad mesh to the\napplied object. As with all mesh modifiers,\nmake sure that it is ordered after all\ncolor related modifiers.", PointMeshModifier.FACTORY, PointMeshModifier.class);
		StudioInterface.getInstantiation().addModifier("Mesh", "Sphere Mesh Modifier", meshDesc("sphere-shaped"), SphereMeshModifier.FACTORY, SphereMeshModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Misc");
		StudioInterface.getInstantiation().addModifier("Misc", "Camera Modifier", "Gives the object the ability to act as an in-scene camera\nwhich can be used in a render call. A camera-enabled\nobject is required in every scene in order for a render\nto take place.", CameraModifier.FACTORY, CameraModifier.class);
		StudioInterface.getInstantiation().addModifier("Misc", "Dynamic Emitter Modifier", "Causes the object to emit references of another object\nat a given frequency. The emitted objects are actively\nmaintained by the emitter after they are emitted.", DynamicEmitterModifier.FACTORY, DynamicEmitterModifier.class);
		StudioInterface.getInstantiation().addModifier("Misc", "PyScript Modifier", "The primary method of Python scripting in Celestial Game\nStudio. This modifier binds the object to a new or\npre-existing script which will be executed regularly.", PyScriptModifier.FACTORY, PyScriptModifier.class);
		StudioInterface.getInstantiation().addModifier("Misc", "Static Emitter Modifier", "Causes the object to emit references of another object\nat a given frequency. The emitted objects are not\nmaintained and should be treated as such.", StaticEmitterModifier.FACTORY, StaticEmitterModifier.class);
		StudioInterface.getInstantiation().addModifier("Misc", "Template Modifier", "An empty modifier that provides no unique\nimplementation. It is used in development as\nthe baseline off which all modifiers are built.", TemplateModifier.FACTORY, TemplateModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Physics");
		StudioInterface.getInstantiation().addModifier("Physics", "Box Collision Object Modifier", colObjDesc("cubic-shaped"), BoxCollisionObjectModifier.FACTORY, BoxCollisionObjectModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Box Rigid Body Modifier", rigidBodyDesc("cubic-shaped"), BoxRigidBodyModifier.FACTORY, BoxRigidBodyModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Bvh Collision Object Modifier", colObjDesc("custom triangle-mesh"), BvhCollisionObjectModifier.FACTORY, BvhCollisionObjectModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Cone Collision Object Modifier", colObjDesc("cone-shaped"), ConeCollisionObjectModifier.FACTORY, ConeCollisionObjectModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Cone Rigid Body Modifier", rigidBodyDesc("cone-shaped"), ConeRigidBodyModifier.FACTORY, ConeRigidBodyModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Convex Hull Rigid Body Modifier", rigidBodyDesc("custom convex"), ConvexHullRigidBodyModifier.FACTORY, ConvexHullRigidBodyModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Cylinder Collision Object Modifier", colObjDesc("cylindrical"), CylinderCollisionObjectModifier.FACTORY, CylinderCollisionObjectModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Cylinder Rigid Body Modifier", rigidBodyDesc("cylindrical"), CylinderRigidBodyModifier.FACTORY, CylinderRigidBodyModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Heightmap Collision Object Modifier", colObjDesc("custom heightmap"), HeightmapCollisionObjectModifier.FACTORY, HeightmapCollisionObjectModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Plane Collision Object Modifier", colObjDesc("flat plane"), PlaneCollisionObjectModifier.FACTORY, PlaneCollisionObjectModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Plane Rigid Body Modifier", rigidBodyDesc("flat plane"), PlaneRigidBodyModifier.FACTORY, PlaneRigidBodyModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Sphere Collision Object Modifier", colObjDesc("sphere-shaped"), SphereCollisionObjectModifier.FACTORY, SphereCollisionObjectModifier.class);
		StudioInterface.getInstantiation().addModifier("Physics", "Sphere Rigid Body Modifier", rigidBodyDesc("sphere-shaped"), SphereRigidBodyModifier.FACTORY, SphereRigidBodyModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Render");
		StudioInterface.getInstantiation().addModifier("Render", "Cubic Environment Modifier", "Gives this object the ability to reflect and\n refract the scene around it in all\ndirections. It is the most expensive FBO\nrender modifier available, and should be\nused lightly.", CubicEnvironmentModifier.FACTORY, CubicEnvironmentModifier.class);
		StudioInterface.getInstantiation().addModifier("Render", "Depth Effect Modifier", "Gives this object a variety of planar\ndepth-related effects. It uses a\nplanar refraction buffer, so only requires\none additional render of the scene.", DepthEffectModifier.FACTORY, DepthEffectModifier.class);
		StudioInterface.getInstantiation().addModifier("Render", "Planar Reflection Modifier", "Causes this object to refract the\nscene underneath of it. It uses a\nplanar refraction buffer, so only requires\none additional render of the scene.", PlanarReflectionModifier.FACTORY, PlanarReflectionModifier.class);
		StudioInterface.getInstantiation().addModifier("Render", "Planar Refraction Modifier", "Causes this object to reflect the\nscene above it. It uses a planar\nrefrection buffer, and so only requires\none additional render of the scene.", PlanarRefractionModifier.FACTORY, PlanarRefractionModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Sound");
		StudioInterface.getInstantiation().addModifier("Sound", "Sound Area Modifier", "Gives this object the ability to emit a 2D sound on\nloop over a certain area that fades out after a given\ndistance.", SoundAreaModifier.FACTORY, SoundAreaModifier.class);
		StudioInterface.getInstantiation().addModifier("Sound", "Sound Loop 3D Modifier", "Gives this object the ability to emit a 3D sound\nfrom any given point in the world. This sound\ndecreases exponentially with distance.", SoundLoop3DModifier.FACTORY, SoundLoop3DModifier.class);
		StudioInterface.getInstantiation().addModifier("Sound", "Sound Node Modifier", "Gives this object the ability to emit a 2D sound on\nloop over a certain area that has a volume controlled\nby an inner and outer radius defining the volume\nfade zones.", SoundNodeModifier.FACTORY, SoundNodeModifier.class);
		
		StudioInterface.getInstantiation().addModifierCategory("Texture");
		StudioInterface.getInstantiation().addModifier("Texture", "Blend Map Modifier", "Registers a four-channel blend map on this object,\nallowing it to use different maps over different parts\nof the object. Make sure to order this map before\nall other maps.", BlendMapModifier.FACTORY, BlendMapModifier.class);
		StudioInterface.getInstantiation().addModifier("Texture", "Diffuse Map Modifier", "Gives this object a basic color map from a specified\nimage. This object will have its color value set to that\nof the image.", DiffuseMapModifier.FACTORY, DiffuseMapModifier.class);
		StudioInterface.getInstantiation().addModifier("Texture", "Distortion Map Modifier", "Uses one of four distortion map slots to load a DUDV\ndistortion image for use by other maps. The distortion\nunit of this map can be applied to any other sampler.", DistortionMapModifier.FACTORY, DistortionMapModifier.class);
		StudioInterface.getInstantiation().addModifier("Texture", "Illumination Map Modifier", "Similar to a diffuse map, this object is a basic\ncolor map, but uses additive blending to\napply the color from the image. If it is a\nsingle-channel map, it will only control\nbrightness.", IlluminationMapModifier.FACTORY, IlluminationMapModifier.class);
		
	}
	
	private static String meshDesc(String type) {
		return	"Adds a " + type + " mesh to the applied\n" +
				"object. As with all mesh modifiers, make\n" +
				"sure that it is ordered after all color\n" +
				"related modifiers.";
	}
	
	private static String colObjDesc(String type) {
		return	"Adds a " + type + " collision mesh to the\n" +
				"applied object, allowing rigid body objects\n" +
				"to collide with this object.";
	}
	
	private static String rigidBodyDesc(String type) {
		return	"Gives this object a physical " + type + " shape\n" +
				"as well as giving object the ability to behave\n" +
				"as if it were affected by physics, allowing it\n" +
				"to interact with other rigid bodies as well\n" +
				"as collision objects.";
	}
	
}
