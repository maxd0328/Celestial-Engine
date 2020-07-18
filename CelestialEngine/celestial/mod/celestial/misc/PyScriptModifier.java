package mod.celestial.misc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.Modifier;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.serialization.SerializerImpl;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;

public final class PyScriptModifier extends Modifier {
	
	private static final long serialVersionUID = -9019159999822660316L;
	
	public static final Factory<PyScriptModifier> FACTORY = () -> new PyScriptModifier(new ArrayList<PyVariable>(), new PyScript(""));
	
	private transient PythonInterpreter interp;
	private final ArrayList<PyVariable> variables;
	private PyScript script;
	
	public PyScriptModifier(ArrayList<PyVariable> variables, PyScript script) {
		super(false, true, false);
		this.interp = new PythonInterpreter();
		this.variables = variables;
		this.script = script;
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		recompile();
		PyObject preRender = interp.get("preRender");
		interp.set("packet", packet);
		interp.set("obj", obj);
		if(preRender != null) preRender.__call__();
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		recompile();
		PyObject render = interp.get("render");
		interp.set("packet", packet);
		interp.set("obj", obj);
		if(render != null) render.__call__();
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		recompile();
		PyObject postRender = interp.get("postRender");
		interp.set("packet", packet);
		interp.set("obj", obj);
		if(postRender != null) postRender.__call__();
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		recompile();
		for(PyVariable variable : this.variables) interp.set(variable.name, variable.value.get());
		PyObject update0 = interp.get("update0");
		interp.set("packet", packet);
		interp.set("obj", obj);
		if(update0 != null) update0.__call__();
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		recompile();
		PyObject update1 = interp.get("update1");
		interp.set("packet", packet);
		interp.set("obj", obj);
		if(update1 != null) update1.__call__();
		for(PyVariable variable : this.variables) variable.value.update();
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		return ctrl;
	}
	
	public ArrayList<PyVariable> getVariables() {
		return variables;
	}
	
	public PyScript getScript() {
		return script;
	}
	
	public void setScript(PyScript script) {
		this.script = script;
	}
	
	public Modifier duplicate() {
		ArrayList<PyVariable> variables = new ArrayList<PyVariable>(this.variables);
		for(PyVariable variable : this.variables) variables.add(new PyVariable(variable.name, variable.value));
		return new PyScriptModifier(variables, script);
	}
	
	private void recompile() {
		if(script.needsRecompile) {
			interp.exec(script.getSource());
			script.needsRecompile = false;
		}
	}
	
	@SerializerImpl
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.interp = new PythonInterpreter();
	}
	
	public static final class PyVariable implements java.io.Serializable {
		
		private static final long serialVersionUID = 1481333932835359345L;
		
		private final String name;
		private final Property<Float> value;
		
		public PyVariable(String name, float value) {
			this.name = name;
			this.value = Properties.createFloatProperty(value);
		}
		
		private PyVariable(String name, Property<Float> value) {
			this.name = name;
			this.value = value.clone();
		}
		
		public String getName() {
			return name;
		}
		
		public float getValue() {
			return value.get();
		}
		
		public void setValue(float value) {
			this.value.set(value);
		}
		
		public Property<Float> valueProperty() {
			return value;
		}
		
	}
	
	public static final class PyScript implements java.io.Serializable {
		
		private static final long serialVersionUID = 7474020380830577793L;
		
		private String src;
		private boolean needsRecompile;
		
		public PyScript(String src) {
			this.src = src;
			this.needsRecompile = true;
		}
		
		public void appendSource(String src) {
			this.src += src;
			this.needsRecompile = true;
		}
		
		public String getSource() {
			return src;
		}
		
		public void setSource(String src) {
			this.src = src;
			this.needsRecompile = true;
		}
		
	}
	
}
