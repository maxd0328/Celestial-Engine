package celestial.shader;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL20;

import celestial.vecmath.*;
import celestial.data.VertexBuffer;
import celestial.error.CelestialGLException;

public final class GLSLCommunicator {
	
	private final HashMap<String, Integer> attributes;
	private final HashMap<String, Uniform<?>> uniforms;
	private final int samplerCount;
	
	protected GLSLCommunicator(int programID, ArrayList<ShaderAttribute> attributes, ArrayList<ShaderAttribute> uniforms) {
		this.attributes = new HashMap<String, Integer>();
		this.uniforms = new HashMap<String, Uniform<?>>();
		
		for(int i = 0 ; i < attributes.size() ; ++i) this.attributes.put(attributes.get(i).getName(), i);
		
		for(ShaderAttribute uniform : uniforms) {
			switch(uniform.getType()) {
			case "sampler1D":
			case "sampler2D":
			case "sampler3D":
			case "samplerCube":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindInteger("uni_" + uniform.getName(i), programID));
				break;
			case "float":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindFloat("uni_" + uniform.getName(i), programID));
				break;
			case "vec2":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindVec2("uni_" + uniform.getName(i), programID));
				break;
			case "vec3":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindVec3("uni_" + uniform.getName(i), programID));
				break;
			case "vec4":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindVec4("uni_" + uniform.getName(i), programID));
				break;
			case "mat2":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindMat2("uni_" + uniform.getName(i), programID));
				break;
			case "mat3":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindMat3("uni_" + uniform.getName(i), programID));
				break;
			case "mat4":
				for(int i = 0 ; i < uniform.getCount() ; ++i)
					this.uniforms.put(uniform.getName(i), Uniform.bindMat4("uni_" + uniform.getName(i), programID));
				break;
			default:
				throw new CelestialGLException("GLSL datatype '" + uniform.getType() + "' is not supported");
			}
		}
		GL20.glValidateProgram(programID);
		
		GL20.glUseProgram(programID);
		int textureCounter = 0;
		for(ShaderAttribute uniform : uniforms) {
			if(uniform.getType().contains("sampler")) for(int i = 0 ; i < uniform.getCount() ; ++i) store1i(uniform.getName(i), textureCounter++);
		}
		if(textureCounter > 32) throw new CelestialGLException("Sampler count has exceeded the OpenGL limit of 32 bound samplers");
		this.samplerCount = textureCounter;
		GL20.glUseProgram(0);
	}
	
	public int getUniformComponentCount() {
		int componentCount = 0;
		for(String s : uniforms.keySet()) componentCount += uniforms.get(s).componentCount();
		return componentCount;
	}
	
	public int getSamplerCount() {
		return samplerCount;
	}
	
	public Integer getAttributeLocation(String attribute) {
		return attributes.get(attribute);
	}
	
	public VertexBuffer[] sortAttribs(UnsortedAttrib... attribs) {
		VertexBuffer[] arr = new VertexBuffer[attributes.size()];
		for(String identifier : attributes.keySet()) {
			for(int i = 0 ; i < attribs.length ; ++i) if(identifier.equals(attribs[i].getIdentifier())) arr[attributes.get(identifier)] = attribs[i].getBuffer();
		}
		return arr;
	}
	
	@SuppressWarnings("unchecked")
	public void store1i(String identifier, int value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance((Integer) value)) ((Uniform<Integer>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public void store1f(String identifier, float value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance((Float) value)) ((Uniform<Float>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public void store2f(String identifier, Vector2f value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Vector2f(value))) ((Uniform<Vector2f>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public void store3f(String identifier, Vector3f value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Vector3f(value))) ((Uniform<Vector3f>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public void store4f(String identifier, Vector4f value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Vector4f(value))) ((Uniform<Vector4f>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public void store2x2f(String identifier, Matrix2f value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Matrix2f(value))) ((Uniform<Matrix2f>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public void store3x3f(String identifier, Matrix3f value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Matrix3f(value))) ((Uniform<Matrix3f>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public void store4x4f(String identifier, Matrix4f value) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Matrix4f(value))) ((Uniform<Matrix4f>) uniform).load(value);
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Integer load1i(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance((Integer) 0)) return ((Uniform<Integer>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Float load1f(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance((Integer) 0)) return ((Uniform<Float>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Vector2f load2f(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Vector2f())) return ((Uniform<Vector2f>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Vector3f load3f(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Vector3f())) return ((Uniform<Vector3f>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Vector4f load4f(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Vector4f())) return ((Uniform<Vector4f>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Matrix2f load2x2f(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Matrix2f())) return ((Uniform<Matrix2f>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Matrix3f load3x3f(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Matrix3f())) return ((Uniform<Matrix3f>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	@SuppressWarnings("unchecked")
	public Matrix4f load4x4f(String identifier) {
		Uniform<?> uniform = uniforms.get(identifier);
		if(uniform == null) throw new CelestialGLException("Uniform of identifier '" + identifier + "' does not exist");
		if(uniform.getCtype().isInstance(new Matrix4f())) return ((Uniform<Matrix4f>) uniform).getValue();
		else throw new CelestialGLException("Uniform of identifier '" + identifier + "' is not a member of the requested type");
	}
	
	public void storearr1i(String identifier, int index, int value) {
		store1i(String.format("%s[%d]", identifier, index), value);
	}
	
	public void storearr1f(String identifier, int index, float value) {
		store1f(String.format("%s[%d]", identifier, index), value);
	}
	
	public void storearr2f(String identifier, int index, Vector2f value) {
		store2f(String.format("%s[%d]", identifier, index), value);
	}
	
	public void storearr3f(String identifier, int index, Vector3f value) {
		store3f(String.format("%s[%d]", identifier, index), value);
	}
	
	public void storearr4f(String identifier, int index, Vector4f value) {
		store4f(String.format("%s[%d]", identifier, index), value);
	}
	
	public void storearr2x2f(String identifier, int index, Matrix2f value) {
		store2x2f(String.format("%s[%d]", identifier, index), value);
	}
	
	public void storearr3x3f(String identifier, int index, Matrix3f value) {
		store3x3f(String.format("%s[%d]", identifier, index), value);
	}
	
	public void storearr4x4f(String identifier, int index, Matrix4f value) {
		store4x4f(String.format("%s[%d]", identifier, index), value);
	}
	
	public Integer loadarr1i(String identifier, int index) {
		return load1i(String.format("%s[%d]", identifier, index));
	}
	
	public Float loadarr1f(String identifier, int index) {
		return load1f(String.format("%s[%d]", identifier, index));
	}
	
	public Vector2f loadarr2f(String identifier, int index) {
		return load2f(String.format("%s[%d]", identifier, index));
	}
	
	public Vector3f loadarr3f(String identifier, int index) {
		return load3f(String.format("%s[%d]", identifier, index));
	}
	
	public Vector4f loadarr4f(String identifier, int index) {
		return load4f(String.format("%s[%d]", identifier, index));
	}
	
	public Matrix2f loadarr2x2f(String identifier, int index) {
		return load2x2f(String.format("%s[%d]", identifier, index));
	}
	
	public Matrix3f loadarr3x3f(String identifier, int index) {
		return load3x3f(String.format("%s[%d]", identifier, index));
	}
	
	public Matrix4f loadarr4x4f(String identifier, int index) {
		return load4x4f(String.format("%s[%d]", identifier, index));
	}
	
	public static class UnsortedAttrib implements java.io.Serializable {
		
		private static final long serialVersionUID = 8966108683978824196L;
		
		private final String identifier;
		private final VertexBuffer buffer;
		
		public UnsortedAttrib(String identifier, VertexBuffer buffer) {
			this.identifier = identifier;
			this.buffer = buffer;
		}
		
		public String getIdentifier() {
			return identifier;
		}
		
		public VertexBuffer getBuffer() {
			return buffer;
		}
		
	}
	
}
