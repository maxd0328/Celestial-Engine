package celestial.shader;

import java.util.ArrayList;

public final class ProgramModule {
	
	private final String function;
	private final String global;
	
	private final ArrayList<ShaderAttribute> uniforms, globals;
	
	public ProgramModule(String function, String global, ArrayList<ShaderAttribute> uniforms, ArrayList<ShaderAttribute> globals) {
		this.function = function;
		this.global = global;
		this.uniforms = uniforms;
		this.globals = globals;
	}
	
	public ProgramModule(String function, String global) {
		this(function, new ArrayList<ShaderAttribute>(), new ArrayList<ShaderAttribute>());
	}
	
	public ProgramModule(String function, ArrayList<ShaderAttribute> uniforms, ArrayList<ShaderAttribute> globals) {
		this(function, "", uniforms, globals);
	}
	
	public ProgramModule(String function) {
		this(function, "");
	}
	
	public String getFunction() {
		return function;
	}
	
	public String getGlobal() {
		return global;
	}
	
	public ArrayList<ShaderAttribute> getUniforms() {
		return new ArrayList<ShaderAttribute>(uniforms);
	}
	
	public ArrayList<ShaderAttribute> getGlobals() {
		return new ArrayList<ShaderAttribute>(globals);
	}
	
}
