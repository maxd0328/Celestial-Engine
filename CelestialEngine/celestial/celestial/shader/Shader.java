package celestial.shader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import celestial.data.VertexBuffer;
import celestial.error.CelestialGLException;
import celestial.error.CelestialGenericException;
import celestial.shader.GLSLCommunicator.UnsortedAttrib;

public final class Shader {
	
	private static final Collection<Shader> SHADERS = new ArrayList<Shader>();
	
	private final int programID, vertShaderID, fragShaderID;
	private final GLSLCommunicator communicator;
	private final UnifiedShader unifiedShader;
	
	private ArrayList<ShaderAttribute> attributes = new ArrayList<ShaderAttribute>(), uniforms = new ArrayList<ShaderAttribute>();
	
	public Shader(UnifiedShader unifiedShader, Collection<ShaderModule> inModules, Collection<ShaderModule> inDependencies, int version) {
		ArrayList<ShaderModule> modules = new ArrayList<>();
		for(ShaderModule module : inModules)
			if(!modules.contains(module))
				modules.add(module);
		
		ArrayList<ShaderModule> dependencies = new ArrayList<>();
		for(ShaderModule module : inDependencies)
			if(!dependencies.contains(module) && !modules.contains(module))
				dependencies.add(module);
		modules.addAll(dependencies);
		
		String vertShaderProgram = buildProgram(modules, dependencies, 0, version);
		String fragShaderProgram = buildProgram(modules, dependencies, 1, version);
		attributes = removeDuplicates(attributes);
		uniforms = removeDuplicates(uniforms);
		
//		int x = 0;
//		for(String s : vertShaderProgram.split("\n")) System.out.println(++x + ": " + s);
//		int y = 0;
//		for(String s : fragShaderProgram.split("\n")) System.out.println(++y + ": " + s);
		
		this.vertShaderID = loadShader(vertShaderProgram, GL20.GL_VERTEX_SHADER);
		this.fragShaderID = loadShader(fragShaderProgram, GL20.GL_FRAGMENT_SHADER);
		this.programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertShaderID);
		GL20.glAttachShader(programID, fragShaderID);
		
		unifiedShader.sortAttribs(attributes);
		for(int i = 0 ; i < attributes.size() ; ++i) GL20.glBindAttribLocation(programID, i, "absin_" + attributes.get(i).getName(attributes.get(i).getCount()));
		
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		
		inModules.removeAll(Collections.<ShaderModule>singletonList(null));
		this.communicator = new GLSLCommunicator(this.programID, attributes, uniforms);
		this.unifiedShader = unifiedShader;
		
		SHADERS.add(this);
	}
	
	public Shader(String vertProgram, String fragProgram, Collection<ShaderAttribute> attributes, Collection<ShaderAttribute> uniforms) {
		this.attributes.clear();
		this.attributes.addAll(attributes);
		this.uniforms.clear();
		this.uniforms.addAll(uniforms);
		
		this.vertShaderID = loadShader(vertProgram, GL20.GL_VERTEX_SHADER);
		this.fragShaderID = loadShader(fragProgram, GL20.GL_FRAGMENT_SHADER);
		this.programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertShaderID);
		GL20.glAttachShader(programID, fragShaderID);
		
		for(int i = 0 ; i < this.attributes.size() ; ++i) GL20.glBindAttribLocation(programID, i, "in_" + this.attributes.get(i).getName(this.attributes.get(i).getCount()));
		
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		
		this.communicator = new GLSLCommunicator(this.programID, this.attributes, this.uniforms);
		this.unifiedShader = null;
		
		SHADERS.add(this);
	}
	
	public GLSLCommunicator getCommunicator() {
		return communicator;
	}
	
	public void activate() {
		GL20.glUseProgram(programID);
	}
	
	public void deactivate() {
		GL20.glUseProgram(0);
	}
	
	public void delete(){
		deactivate();
		GL20.glDetachShader(programID, vertShaderID);
		GL20.glDetachShader(programID, fragShaderID);
		GL20.glDeleteShader(vertShaderID);
		GL20.glDeleteShader(fragShaderID);
		GL20.glDeleteProgram(programID);
	}
	
	private String buildProgram(ArrayList<ShaderModule> modules, ArrayList<ShaderModule> dependencies, int accessId, int version) {
		modules.removeAll(Collections.<ShaderModule>singletonList(null));
		
		for(ShaderModule module : modules) attributes.addAll(module.getInputs());
		for(ShaderModule module : modules) uniforms.addAll(module.getProgram(accessId).getUniforms());
		
		String sharedVars = "#version " + version + "\n\n";
		SharedAttribArray array = new SharedAttribArray().build(modules, accessId);
		for(ShaderAttribute attrib : array.invars) sharedVars += "in " + attrib.toString(accessId == 0 ? "absin" : "att") + ";\n";
		for(ShaderAttribute attrib : array.outvars) sharedVars += "out " + attrib.toString(accessId == 0 ? "att" : "out") + ";\n";
		for(ShaderAttribute attrib : array.uniforms) sharedVars += "uniform " + attrib.toString("uni") + ";\n";
		for(ShaderAttribute attrib : array.globals) sharedVars += "/*global*/ " + attrib.toString("glb") + ";\n";
		
		String globals = accessId == 1 ? "out vec4 out_Color;\n" : "";
		String functions = "";
		
		for(ShaderModule genericModule : modules) {
			functions += "\n" + getAttribList(genericModule, accessId, array);
		}
		
		functions += "\nvoid main(void)\n{\n" + (accessId == 1 ? "out_Color = vec4(0.0, 0.0, 0.0, 1.0);\n" : "");
		if(accessId == 0) for(ShaderAttribute attrib : array.invars) functions += attrib.toString("in") + " = absin_" + attrib.getName() + ";\n";
		for(ShaderAttribute attrib : array.globals)
			if(attrib.getDefaultValue().length() > 0)
				functions += "glb_" + attrib.getName() + " = " + attrib.getDefaultValue() + ";\n";
		
		for(ShaderModule genericModule : modules) {
			if(genericModule.getProgram(accessId).getGlobal() != null && genericModule.getProgram(accessId).getGlobal().length() > 0)
				globals += genericModule.getProgram(accessId).getGlobal() + "\n";
			if(!dependencies.contains(genericModule) && genericModule.getProgram(accessId).getFunction() != null && genericModule.getProgram(accessId).getFunction().length() > 0)
				functions += "{\n" + genericModule.getProgram(accessId).getFunction() + "}\n";
		}
		
		functions += "}\n";
		
		return sharedVars + globals + functions;
	}
	
	private String getAttribList(ShaderModule genericModule, int accessId, SharedAttribArray array) {
		String output = "";
		/* Adds back non-duplicates to 'array' after adding them to the program so that 'array' can be used as a full list */
		for(ShaderAttribute attrib : genericModule.getInputs(accessId)) if(!array.invars.contains(attrib)) {
				output += String.format("in %s;\n", attrib.toString(accessId == 0 ? "absin" : "att"));
				array.invars.add(attrib);
		}
		for(ShaderAttribute attrib : genericModule.getOutputs(accessId)) if(!array.outvars.contains(attrib)) {
			output += String.format("out %s;\n", attrib.toString(accessId == 0 ? "att" : "out"));
			array.outvars.add(attrib);
		}
		for(ShaderAttribute attrib : genericModule.getProgram(accessId).getUniforms()) if(!array.uniforms.contains(attrib)) {
			output += String.format("uniform %s;\n", attrib.toString("uni"));
			array.uniforms.add(attrib);
		}
		for(ShaderAttribute attrib : genericModule.getProgram(accessId).getGlobals()) if(!array.globals.contains(attrib)) {
			output += String.format("/*global*/ %s;\n", attrib.toString("glb"));
			array.globals.add(attrib);
		}
		return output;
	}
	
	private int loadShader(String src, int type) {
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, src);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) throw new CelestialGLException("GLSL err : " + GL20.glGetShaderInfoLog(shaderID, 500));
		return shaderID;
	}
	
	private <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
		ArrayList<T> newList = new ArrayList<T>();
		
		for(T t : list) {
			if(!newList.contains(t)) newList.add(t);
		}
		
		return newList;
	}
	
	public UnifiedShader getUnifiedShader() {
		return unifiedShader;
	}
	
	public VertexBuffer[] sortAttribs(UnsortedAttrib... attribs) {
		if(unifiedShader != null)
			return unifiedShader.sortAttribs(attribs);
		else
			return communicator.sortAttribs(attribs);
	}
	
	private class SharedAttribArray {
		
		private final ArrayList<ShaderAttribute> invars, outvars, uniforms, globals;
		
		public SharedAttribArray() {
			this.invars = new ArrayList<ShaderAttribute>();
			this.outvars = new ArrayList<ShaderAttribute>();
			this.uniforms = new ArrayList<ShaderAttribute>();
			this.globals = new ArrayList<ShaderAttribute>();
		}
		
		public SharedAttribArray build(ArrayList<ShaderModule> modules, int accessId) {
			for(int i = 0 ; i < 4 ; ++i) {
				ArrayList<ShaderAttribute> sharedAttribs = new ArrayList<ShaderAttribute>(), total = new ArrayList<ShaderAttribute>();
				for(ShaderModule module : modules) total.addAll(getList(module, i, accessId));
				for(ShaderAttribute attrib : total) if(Collections.frequency(total, attrib) > 1 && !sharedAttribs.contains(attrib)) sharedAttribs.add(attrib);
				setList(sharedAttribs, i);
			}
			return this;
		}
		
		private ArrayList<ShaderAttribute> getList(ShaderModule module, int index, int accessId) {
			return index == 0 ? module.getInputs(accessId) : index == 1 ? module.getOutputs(accessId) : index == 2
					? module.getProgram(accessId).getUniforms() : module.getProgram(accessId).getGlobals();
		}
		
		private void setList(ArrayList<ShaderAttribute> list, int index) {
			if(index == 0) invars.addAll(list);
			else if(index == 1) outvars.addAll(list);
			else if(index == 2) uniforms.addAll(list);
			else globals.addAll(list);
		}
		
	}
	
	public static String getProgramSegment(URL url, String hash) {
		String str;
		try {
			str = new String(Files.readAllBytes(Paths.get(url.toURI())));
		} catch (IOException e) {
			throw new CelestialGenericException(e.getMessage());
		} catch (URISyntaxException e) {
			throw new CelestialGenericException(e.getMessage());
		}
		
		String[] lines = str.split(System.getProperty("line.separator"));
		String output = "";
		
		boolean reading = false;
		for(int i = 0 ; i < lines.length ; ++i) {
			if(lines[i].trim().startsWith("#")) reading = lines[i].trim().substring(1).trim().equals(hash);
			else if(reading) {
				if(lines[i].startsWith("\\")) lines[i] = lines[i].substring(1, lines[i].length());
				output += lines[i] + "\n";
			}
		}
		
		if(output.length() == 0) {
			System.out.println("WARNING: Program segment \"" + hash + "\" is either empty or could not be properly read");
			for(StackTraceElement s : Thread.currentThread().getStackTrace()) System.out.println("\t" + s.toString());
		}
		
		return output;
	}
	
	public static String getProgram(URL url) {
		String str;
		try {
			str = new String(Files.readAllBytes(Paths.get(url.toURI())));
		} catch (IOException e) {
			throw new CelestialGenericException(e.getMessage());
		} catch (URISyntaxException e) {
			throw new CelestialGenericException(e.getMessage());
		}
		
		return str;
	}
	
	public static void destroyShaders() {
		for(Shader shader : SHADERS)
			shader.delete();
	}
	
}
