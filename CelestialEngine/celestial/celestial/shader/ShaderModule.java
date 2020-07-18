package celestial.shader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ShaderModule {
	
	private final String identifier;
	private final ProgramModule vertProgram;
	private final ProgramModule fragProgram;
	private final int ID;
	
	private final ArrayList<ShaderAttribute> inputs, pvAttribs;
	private final ArrayList<String> dependencies = new ArrayList<String>();
	
	public ShaderModule(String identifier, ProgramModule vertProgram, ProgramModule fragProgram,
			int ID, ArrayList<ShaderAttribute> inputs, ArrayList<ShaderAttribute> pvAttribs) {
		this.identifier = identifier;
		this.vertProgram = vertProgram;
		this.fragProgram = fragProgram;
		this.ID = ID;
		this.inputs = inputs;
		this.pvAttribs = pvAttribs;
	}
	
	public ShaderModule(String identifier, ProgramModule vertProgram, ProgramModule fragProgram, int ID) {
		this(identifier, vertProgram, fragProgram, ID, new ArrayList<ShaderAttribute>(), new ArrayList<ShaderAttribute>());
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public ProgramModule getVertProgram() {
		return vertProgram;
	}
	
	public ProgramModule getFragProgram() {
		return fragProgram;
	}
	
	public int getID() {
		return ID;
	}
	
	public ArrayList<ShaderAttribute> getInputs() {
		return new ArrayList<ShaderAttribute>(inputs);
	}
	
	public ArrayList<ShaderAttribute> getAttribs() {
		return new ArrayList<ShaderAttribute>(pvAttribs);
	}
	
	public List<String> getDependencies() {
		return dependencies;
	}
	
	public ShaderModule withDependencies(String... dependencies) {
		this.dependencies.addAll(Arrays.asList(dependencies));
		return this;
	}
	
	protected ProgramModule getProgram(int index) {
		return new ProgramModule[] {vertProgram, fragProgram} [index];
	}
	
	protected ArrayList<ShaderAttribute> getInputs(int index) {
		return index == 0 ? getInputs() : getAttribs();
	}
	
	protected ArrayList<ShaderAttribute> getOutputs(int index) {
		return index == 0 ? getAttribs() : new ArrayList<ShaderAttribute>();
	}
	
	public static ArrayList<ShaderAttribute> attribs(ShaderAttribute...attributes) {
		return new ArrayList<ShaderAttribute>(Arrays.asList(attributes));
	}
	
}
