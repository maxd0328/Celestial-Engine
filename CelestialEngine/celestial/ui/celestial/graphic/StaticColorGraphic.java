package ui.celestial.graphic;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.GLData;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.ui.Graphic;
import celestial.shader.Shader;
import celestial.vecmath.Vector3f;

public final class StaticColorGraphic extends Graphic {
	
	private static final Shader SHADER = new Shader(Shader.getProgramSegment(StaticColorGraphic.class.getResource("graphic.glsl"), "staticColorV"),
			Shader.getProgramSegment(StaticColorGraphic.class.getResource("graphic.glsl"), "staticColorF"), ShaderModule.attribs(ShaderAttribute.$("vec2 position")),
			ShaderModule.attribs(ShaderAttribute.$("vec3 color"), ShaderAttribute.$("mat4 transform")));
	
	private final Property<Vector3f> color;
	
	public StaticColorGraphic(Vector3f color) {
		super(SHADER);
		this.color = Properties.createVec3Property(color);
	}
	
	@Override
	protected void preRender() {
		SHADER.getCommunicator().store3f("color", color.get());
	}
	
	@Override
	protected void postRender() {
	}
	
	@Override
	public void update() {
		color.update();
	}
	
	@Override
	public boolean containsData(GLData data) {
		return false;
	}
	
	public Vector3f getColor() {
		return color.get();
	}
	
	public void setColor(Vector3f color) {
		this.color.set(color);
	}
	
	public Property<Vector3f> colorProperty() {
		return color;
	}
	
}
