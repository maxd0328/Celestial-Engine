package celestial.shader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import celestial.core.Modifier;
import celestial.data.VertexBuffer;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;
import celestial.util.Factory;
import mod.celestial.filter.AlphaModifier;
import mod.celestial.filter.CubeMapDiffuseModifier;
import mod.celestial.filter.FogEffectModifier;
import mod.celestial.filter.FragFogEffectModifier;
import mod.celestial.filter.GradientModifier;
import celestial.shadow.ShadowMapSystem.LinearDepthModifier;
import mod.celestial.filter.MeshDistortionModifier;
import mod.celestial.filter.NormalOverrideModifier;
import mod.celestial.filter.ProjectiveMappingModifier;
import mod.celestial.filter.StaticColorModifier;
import mod.celestial.filter.TintModifier;
import mod.celestial.material.InterpolatedBSDFModifier;
import mod.celestial.material.InterpolatedRDFModifier;
import mod.celestial.material.ProceduralBSDFModifier;
import mod.celestial.material.ProceduralRDFModifier;
import mod.celestial.mesh.BoxMeshModifier;
import mod.celestial.mesh.BvhTriangleMeshModifier;
import mod.celestial.mesh.ConeMeshModifier;
import mod.celestial.mesh.CrossSectionMeshModifier;
import mod.celestial.mesh.CylinderMeshModifier;
import mod.celestial.mesh.HeightmapMeshModifier;
import mod.celestial.mesh.PlaneMeshModifier;
import mod.celestial.mesh.PointMeshModifier;
import mod.celestial.mesh.SphereMeshModifier;
import mod.celestial.render.CubicEnvironmentModifier;
import mod.celestial.render.DepthEffectModifier;
import mod.celestial.render.PlanarReflectionModifier;
import mod.celestial.render.PlanarRefractionModifier;
import mod.celestial.texture.AbstractMapModifier;
import mod.celestial.texture.AmbientOcclusionMapModifier;
import mod.celestial.texture.BlendMapModifier;
import mod.celestial.texture.BumpMapModifier;
import mod.celestial.texture.DiffuseMapModifier;
import mod.celestial.texture.DistortionMapModifier;
import mod.celestial.texture.IlluminationMapModifier;
import mod.celestial.texture.MetallicMapModifier;
import mod.celestial.texture.RoughnessMapModifier;
import mod.celestial.texture.SubsurfaceMapModifier;

public final class UnifiedShader {
	
	private final Map<Integer, ShaderModule> modules;
	private final Map<ShaderTemplate, Shader> shaders;
	private final int version;
	
	private final Map<Integer, String> sortedAttributes = new HashMap<Integer, String>();
	private boolean created = false;
	
	public UnifiedShader(UnifiedShader shader, int version) {
		this.modules = new HashMap<>(shader.modules);
		this.shaders = new HashMap<>(shader.shaders);
		this.version = version;
	}
	
	public UnifiedShader(int version) {
		this.modules = new HashMap<>();
		this.shaders = new HashMap<>();
		this.version = version;
	}
	
	public Map<Integer, ShaderModule> getModules() {
		return new HashMap<>(modules);
	}
	
	public int getVersion() {
		return version;
	}
	
	public UnifiedShader withDefaults() {
		with(PlaneMeshModifier.FACTORY);
		with(BoxMeshModifier.FACTORY);
		with(CylinderMeshModifier.FACTORY);
		with(SphereMeshModifier.FACTORY);
		with(ConeMeshModifier.FACTORY);
		with(PointMeshModifier.FACTORY);
		with(CrossSectionMeshModifier.FACTORY);
		with(HeightmapMeshModifier.FACTORY);
		with(BvhTriangleMeshModifier.FACTORY);
		with(BlendMapModifier.FACTORY);
		with(DiffuseMapModifier.FACTORY);
		with(BumpMapModifier.FACTORY);
		with(AmbientOcclusionMapModifier.FACTORY);
		with(MetallicMapModifier.FACTORY);
		with(RoughnessMapModifier.FACTORY);
		with(SubsurfaceMapModifier.FACTORY);
		with(StaticColorModifier.FACTORY);
		with(TintModifier.FACTORY);
		with(AlphaModifier.FACTORY);
		with(NormalOverrideModifier.FACTORY);
		with(IlluminationMapModifier.FACTORY);
		with(DistortionMapModifier.FACTORY);
		with(ProjectiveMappingModifier.FACTORY);
		with(PlanarReflectionModifier.FACTORY);
		with(PlanarRefractionModifier.FACTORY);
		with(CubicEnvironmentModifier.FACTORY);
		with(DepthEffectModifier.FACTORY);
		with(GradientModifier.FACTORY);
		with(FogEffectModifier.FACTORY);
		with(FragFogEffectModifier.FACTORY);
		with(MeshDistortionModifier.FACTORY);
		with(ProceduralBSDFModifier.FACTORY);
		with(InterpolatedBSDFModifier.FACTORY);
		with(ProceduralRDFModifier.FACTORY);
		with(InterpolatedRDFModifier.FACTORY);
		with(LinearDepthModifier.FACTORY);
		with(CubeMapDiffuseModifier.FACTORY);
		with(AbstractMapModifier.MAP_GLOBAL_MODULE);
		return this;
	}
	
	public <T extends Modifier> UnifiedShader with(Factory<T> factory) {
		Modifier mod = factory.build();
		modules.put(mod.getID(), Modifier.accessShaderModule(mod));
		return this;
	}
	
	public <T extends Modifier> UnifiedShader without(Factory<T> factory) {
		Modifier mod = factory.build();
		modules.remove(mod.getID());
		return this;
	}
	
	public void with(ShaderModule module) {
		modules.put(module.getID(), module);
	}
	
	public UnifiedShader create() {
		created = true;
		ArrayList<ShaderAttribute> attribs = new ArrayList<>();
		for(ShaderModule module : modules.values())
			for(ShaderAttribute in : module.getInputs())
				if(!attribs.contains(in))
					attribs.add(in);
		
		int index = 0;
		for(ShaderAttribute attrib : attribs)
			this.sortedAttributes.put(index++, attrib.getName());
		
		return this;
	}
	
	public Shader activateShader(ShaderTemplate template) {
		if(!created) throw new IllegalStateException("Must create shader before using it");
		if(template.getModifiers().size() == 0)
			return null;
		
		if(!shaders.containsKey(template)) {
			Collection<ShaderModule> shaderModules = new ArrayList<>(), dependencies = new ArrayList<>();
			for(Modifier mod : template.getModifiers()) {
				if(this.modules.containsKey(mod.getID())) {
					ShaderModule module = this.modules.get(mod.getID());
					shaderModules.add(module);
					addDependencies(dependencies, module);
				}
			}
			shaders.put(template, new Shader(this, shaderModules, dependencies, version));
		}
		
		Shader shader = shaders.get(template);
		shader.activate();
		return shader;
	}
	
	public void deactivateShader(ShaderTemplate template) {
		if(!created) throw new IllegalStateException("Must create shader before using it");
		if(template.getModifiers().size() == 0 || !shaders.containsKey(template))
			return;
		
		shaders.get(template).deactivate();
	}
	
	public VertexBuffer[] sortAttribs(UnsortedAttrib... attribs) {
		if(!created) throw new IllegalStateException("Must create shader before using it");
		VertexBuffer[] arr = new VertexBuffer[sortedAttributes.size()];
		for(int id : sortedAttributes.keySet()) {
			for(int i = 0 ; i < attribs.length ; ++i)
				if(sortedAttributes.get(id).equals(attribs[i].getIdentifier()))
					arr[id] = attribs[i].getBuffer();
		}
		return arr;
	}
	
	public void sortAttribs(Collection<ShaderAttribute> attribs) {
		Collection<ShaderAttribute> result = new ArrayList<>();
		for(int i = 0 ; i < sortedAttributes.size(); ++i) {
			ShaderAttribute attrib = getByName(attribs, sortedAttributes.get(i));
			if(attrib != null)
				result.add(getByName(attribs, sortedAttributes.get(i)));
		}
		
		attribs.clear();
		attribs.addAll(result);
	}
	
	private ShaderAttribute getByName(Collection<ShaderAttribute> attribs, String name) {
		for(ShaderAttribute attrib : attribs)
			if(attrib.getName().equals(name))
				return attrib;
		return null;
	}
	
	private void addDependencies(Collection<ShaderModule> dest, ShaderModule src) {
		for(String dependency : src.getDependencies()) {
			for(int id : modules.keySet()) {
				ShaderModule module = modules.get(id);
				if(module.getIdentifier() != null && module.getIdentifier().equals(dependency)) {
					dest.add(module);
					addDependencies(dest, module);
					break;
				}
			}
		}
	}
	
}
