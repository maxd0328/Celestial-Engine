package mod.celestial.texture;

import java.util.ArrayList;
import java.util.HashMap;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.core.CEObject;
import celestial.core.Modifier;
import celestial.ctrl.IntervalBounds;
import celestial.ctrl.IntervalBounds.IntervalType;
import celestial.ctrl.PropertyController;
import celestial.data.Sampler;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ProgramModule;
import celestial.shader.ShaderAttribute;
import celestial.shader.ShaderModule;
import celestial.shader.Shader;
import celestial.util.ISceneSystem;
import celestial.vecmath.GenericVector;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public abstract class AbstractMapModifier extends AbstractSamplerModifier {
	
	public static final ShaderModule MAP_GLOBAL_MODULE;
	
	static {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fGlobals = ShaderModule.attribs();
		fGlobals.add(ShaderAttribute.$("vec2 texCoords").withDefaultValue("att_texCoords"));
		MAP_GLOBAL_MODULE = new ShaderModule("map",
				new ProgramModule("", vUniforms, vGlobals),
				new ProgramModule("", Shader.getProgramSegment(AbstractMapModifier.class.getResource("texture.glsl"), "mapF_glb"), fUniforms, fGlobals),
				Modifier.uniqueID(), ShaderModule.attribs(), ShaderModule.attribs()).withDependencies("distortionMap");
	}
	
	private static final long serialVersionUID = -7956713790134532768L;
	
	protected final Property<Float> tileFactor;
	protected final Property<Integer> atlasWidth;
	protected final Property<Integer> atlasHeight;
	protected final Property<Vector2f> offset;
	protected final Property<Boolean> additiveMirrorX;
	protected final Property<Boolean> additiveMirrorY;
	
	private transient HashMap<MapProperty<?>, ShaderAttribute> properties;
	private transient ArrayList<ShaderAttribute> globals;
	
	public AbstractMapModifier(String mapIdentifier, boolean colorOnly, Sampler sampler, int textureUnit, float tileFactor, int atlasWidth,
			int atlasHeight, boolean distortEnabled, int distortUnit, Vector2f offset, boolean additiveMirrorX, boolean additiveMirrorY) {
		super(true, colorOnly, mapIdentifier, 4, sampler, textureUnit, distortEnabled, distortUnit);
		this.tileFactor = Properties.createFloatProperty(tileFactor);
		this.atlasWidth = Properties.createIntegerProperty(atlasWidth);
		this.atlasHeight = Properties.createIntegerProperty(atlasHeight);
		this.offset = Properties.createVec2Property(offset);
		this.additiveMirrorX = Properties.createBooleanProperty(additiveMirrorX);
		this.additiveMirrorY = Properties.createBooleanProperty(additiveMirrorY);
		
		this.properties = new HashMap<MapProperty<?>, ShaderAttribute>();
		this.globals = new ArrayList<ShaderAttribute>();
	}
	
	protected AbstractMapModifier(AbstractMapModifier src) {
		super(src);
		this.tileFactor = src.tileFactor.clone();
		this.atlasWidth = src.atlasWidth.clone();
		this.atlasHeight = src.atlasHeight.clone();
		this.offset = src.offset.clone();
		this.additiveMirrorX = src.additiveMirrorX.clone();
		this.additiveMirrorY = src.additiveMirrorY.clone();
		
		this.properties = new HashMap<MapProperty<?>, ShaderAttribute>();
		this.globals = new ArrayList<ShaderAttribute>();
	}
	
	protected ShaderModule getShaderModule() {
		ArrayList<ShaderAttribute> vUniforms = ShaderModule.attribs();
		ArrayList<ShaderAttribute> vGlobals = ShaderModule.attribs();
		ArrayList<ShaderAttribute> fUniforms = super.fUniforms(ShaderModule.attribs(ShaderAttribute.$("float " + mapIdentifier + "Enabled", 4),
				ShaderAttribute.$("float " + mapIdentifier + "Tile", 4), ShaderAttribute.$("vec3 " + mapIdentifier + "Atlas", 4),
				ShaderAttribute.$("vec2 " + mapIdentifier + "Offset", 4), ShaderAttribute.$("vec2 " + mapIdentifier + "Mirror", 4)));
		if(properties != null) for(ShaderAttribute attrib : properties.values()) fUniforms.add(attrib);
		ArrayList<ShaderAttribute> fGlobals = globals != null ? globals : ShaderModule.attribs();
		return new ShaderModule(mapIdentifier,
				new ProgramModule(Shader.getProgramSegment(getClass().getResource("texture.glsl"), "mapV"), vUniforms, vGlobals),
				new ProgramModule(mapIdentifier == null || mapIdentifier.length() == 0 ? "" : 
					Shader.getProgramSegment(getClass().getResource("texture.glsl"), mapIdentifier + "F"), fUniforms, fGlobals),
				getID(), super.inputs(ShaderModule.attribs()), super.pvAttribs(ShaderModule.attribs())).withDependencies("blendMap", "distortionMap", "map");
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		super.preRender(packet, obj);
		int textureUnit = this.textureUnit.get();
		while(textureUnit >= 4) textureUnit -= 4;
		while(textureUnit < 0) textureUnit += 4;
		packet.getShader().getCommunicator().store1f(mapIdentifier + "Enabled[" + textureUnit + "]", 1);
		packet.getShader().getCommunicator().store1f(mapIdentifier + "Tile[" + textureUnit + "]", tileFactor.get());
		packet.getShader().getCommunicator().store2f(mapIdentifier + "Offset[" + textureUnit + "]", offset.get());
		packet.getShader().getCommunicator().store2f(mapIdentifier + "Mirror[" + textureUnit + "]", new Vector2f(additiveMirrorX.get() ? 1f : 0f, additiveMirrorY.get() ? 1f : 0f));
		if(properties != null) for(MapProperty<?> prop : properties.keySet()) {
			Object value = prop.get();
			if(prop.get() instanceof Property<?>) {
				Object v = ((Property<?>) value).get();
				if(v instanceof Float) packet.getShader().getCommunicator().store1f(properties.get(prop).getName(), (Float) v);
				else if(v instanceof Vector2f) packet.getShader().getCommunicator().store2f(properties.get(prop).getName(), (Vector2f) v);
				else if(v instanceof Vector3f) packet.getShader().getCommunicator().store3f(properties.get(prop).getName(), (Vector3f) v);
				else if(v instanceof Vector4f) packet.getShader().getCommunicator().store4f(properties.get(prop).getName(), (Vector4f) v);
			}
			else if(value instanceof Float) packet.getShader().getCommunicator().store1f(properties.get(prop).getName(), (Float) value);
			else if(value instanceof Vector2f) packet.getShader().getCommunicator().store2f(properties.get(prop).getName(), (Vector2f) value);
			else if(value instanceof Vector3f) packet.getShader().getCommunicator().store3f(properties.get(prop).getName(), (Vector3f) value);
			else if(value instanceof Vector4f) packet.getShader().getCommunicator().store4f(properties.get(prop).getName(), (Vector4f) value);
		}
	}
	
	protected void render(RenderPacket packet, CEObject obj) {
		super.render(packet, obj);
		packet.getShader().getCommunicator().store3f(mapIdentifier + "Atlas[" + textureUnit.get() + "]",
				new Vector3f(atlasWidth.get(), atlasHeight.get(), obj.getConfiguration(textureUnit.get())));
	}
	
	protected void postRender(RenderPacket packet, CEObject obj) {
		super.postRender(packet, obj);
		packet.getShader().getCommunicator().store1f(mapIdentifier + "Enabled[0]", 0);
		packet.getShader().getCommunicator().store1f(mapIdentifier + "Enabled[1]", 0);
		packet.getShader().getCommunicator().store1f(mapIdentifier + "Enabled[2]", 0);
		packet.getShader().getCommunicator().store1f(mapIdentifier + "Enabled[3]", 0);
	}
	
	protected void update0(UpdatePacket packet, CEObject obj) {
		super.update0(packet, obj);
	}
	
	protected void update1(UpdatePacket packet, CEObject obj) {
		super.update1(packet, obj);
		tileFactor.update(!packet.isPaused());
		atlasWidth.update(!packet.isPaused());
		atlasHeight.update(!packet.isPaused());
		offset.update(!packet.isPaused());
		additiveMirrorX.update(!packet.isPaused());
		additiveMirrorY.update(!packet.isPaused());
		for(MapProperty<?> prop : properties.keySet()) {
			Object value = prop.get();
			if(value instanceof Property<?>) ((Property<?>) value).update(!packet.isPaused());
		}
	}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = super.getPropertyController(system);
		ctrl.withProperty("Tile Factor", tileFactor);
		ctrl.withProperty("Atlas Width", atlasWidth);
		ctrl.withProperty("Atlas Height", atlasHeight);
		ctrl.withPropertyBounds(new IntervalBounds<Integer>(Integer.class, 1, IntervalType.INCLUSIVE, Integer.MAX_VALUE, IntervalType.INCLUSIVE, 1), 2);
		
		ctrl.withProperty("Offset", offset);
		ctrl.withProperty("Additive Mirror X", additiveMirrorX);
		ctrl.withProperty("Additive Mirror Y", additiveMirrorY);
		return ctrl;
	}
	
	public float getTileFactor() {
		return tileFactor.get();
	}
	
	public void setTileFactor(float tileFactor) {
		this.tileFactor.set(tileFactor);
	}
	
	public Property<Float> tileFactorProperty() {
		return tileFactor;
	}
	
	public int getAtlasWidth() {
		return atlasWidth.get();
	}
	
	public void setAtlasWidth(int atlasWidth) {
		this.atlasWidth.set(atlasWidth);
	}
	
	public Property<Integer> atlasWidthProperty() {
		return atlasWidth;
	}
	
	public int getAtlasHeight() {
		return atlasHeight.get();
	}
	
	public void setAtlasHeight(int atlasHeight) {
		this.atlasHeight.set(atlasHeight);
	}
	
	public Property<Integer> atlasHeightProperty() {
		return atlasHeight;
	}
	
	public Vector2f getOffset() {
		return offset.get();
	}
	
	public void setOffset(Vector2f offset) {
		this.offset.set(offset);
	}
	
	public Property<Vector2f> offsetProperty() {
		return offset;
	}
	
	public boolean isAdditiveMirrorX() {
		return additiveMirrorX.get();
	}
	
	public void setAdditiveMirrorX(boolean additiveMirrorX) {
		this.additiveMirrorX.set(additiveMirrorX);
	}
	
	public Property<Boolean> additiveMirrorXProperty() {
		return additiveMirrorX;
	}
	
	public boolean isAdditiveMirrorY() {
		return additiveMirrorY.get();
	}
	
	public void setAdditiveMirrorY(boolean additiveMirrorY) {
		this.additiveMirrorY.set(additiveMirrorY);
	}
	
	public Property<Boolean> additiveMirrorYProperty() {
		return additiveMirrorY;
	}
	
	protected void putf(MapProperty<Float> property, ShaderAttribute attrib) {
		if(properties == null) this.properties = new HashMap<MapProperty<?>, ShaderAttribute>();
		this.properties.put(property, attrib);
	}
	
	protected void putv(MapProperty<? extends GenericVector> property, ShaderAttribute attrib) {
		if(properties == null) this.properties = new HashMap<MapProperty<?>, ShaderAttribute>();
		this.properties.put(property, attrib);
	}
	
	protected void putdf(MapProperty<Property<Float>> property, ShaderAttribute attrib) {
		if(properties == null) this.properties = new HashMap<MapProperty<?>, ShaderAttribute>();
		this.properties.put(property, attrib);
	}
	
	protected void putdv(MapProperty<Property<? extends GenericVector>> property, ShaderAttribute attrib) {
		if(properties == null) this.properties = new HashMap<MapProperty<?>, ShaderAttribute>();
		this.properties.put(property, attrib);
	}
	
	protected void putGlobal(ShaderAttribute attrib) {
		if(globals == null) this.globals = new ArrayList<ShaderAttribute>();
		this.globals.add(attrib);
	}
	
	protected void loadImplementation(String mapIdentifier) {
		super.loadImplementation(mapIdentifier, 4);
	}
	
	public static interface MapProperty<T> {
		
		public T get();
		
	}
	
}
