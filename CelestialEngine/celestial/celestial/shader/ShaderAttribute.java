package celestial.shader;

public final class ShaderAttribute {
	
	private final String type;
	private final String name;
	private String defaultValue;
	private int count;
	
	public ShaderAttribute(String type, String name, int count) {
		this.type = type;
		this.name = name;
		this.defaultValue = "";
		this.count = count;
	}
	
	public ShaderAttribute(String type, String name) {
		this(type, name, 1);
	}
	
	public String getType() {
		return type;
	}
	
	public String getName(int index) {
		return name + (count == 1 ? "" : "[" + index + "]");
	}
	
	public String getName() {
		return name + (count == 1 ? "" : "[" + count + "]");
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public ShaderAttribute withDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public int getCount() {
		return count;
	}
	
	public String toString(String nameAddition) {
		return type + " " + nameAddition + "_" + name + (count == 1 ? "" : "[" + count + "]");
	}
	
	@Override
	public String toString() {
		return type + " " + name + (count == 1 ? "" : "[" + count + "]");
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ShaderAttribute)) return false;
		ShaderAttribute o = (ShaderAttribute) obj;
		return o.name.equals(this.name) && o.getType().equals(this.type);
	}
	
	public static ShaderAttribute $(String var, int count) {
		return new ShaderAttribute(var.trim().split(" ")[0], var.trim().split(" ")[1], count);
	}
	
	public static ShaderAttribute $(String var) {
		return new ShaderAttribute(var.trim().split(" ")[0], var.trim().split(" ")[1]);
	}
	
}
