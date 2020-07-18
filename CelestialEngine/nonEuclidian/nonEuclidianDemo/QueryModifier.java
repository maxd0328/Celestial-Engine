package nonEuclidianDemo;

import celestial.core.Modifier;
import celestial.core.ObjectConstraints;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import celestial.core.CEObject;
import celestial.ctrl.PropertyController;
import celestial.render.RenderAbortException;
import celestial.render.RenderPacket;
import celestial.render.UpdatePacket;
import celestial.shader.ShaderModule;
import celestial.util.Factory;
import celestial.util.ISceneSystem;
import mod.celestial.mesh.AbstractMeshModifier;

public final class QueryModifier extends Modifier {
	
	private static final long serialVersionUID = -7607614641652263870L;
	
	public static final Factory<QueryModifier> FACTORY = () -> new QueryModifier();
	
	private final Query query;
	private int samplesPassed = 1;
	
	public QueryModifier() {
		super(false, true, false);
		this.query = new Query(GL15.GL_SAMPLES_PASSED);
	}
	
	private QueryModifier(QueryModifier src) {
		super(false, true, false);
		this.query = src.query;
	}
	
	protected ShaderModule getShaderModule() {
		return null;
	}
	
	protected void preRender(RenderPacket packet, CEObject obj) {
		if(packet.isFboRender()) return;
		
		if(query.isResultAvailable()) {
			samplesPassed = query.getResult();
		}
		if(!query.isInUse()) {
			Modifier mod = obj.getModifier(AbstractMeshModifier.class).duplicate();
			CEObject newObj = new CEObject(obj.getIdentifier() + "-QUERY", obj.getPosition(), obj.getRotation(), obj.getScale().clone().scale(1.2f), new ObjectConstraints(100000, 100000));
			newObj.addModifier(mod);
			GL11.glColorMask(false, false, false, false);
			GL11.glDepthMask(false);
			query.start();
			newObj.preRender(packet, 0);
			newObj.render(packet, 0);
			newObj.postRender(packet, 0);
			query.end();
			GL11.glDepthMask(true);
			GL11.glColorMask(true, true, true, true);
		}
		if(samplesPassed <= 0)
			throw new RenderAbortException();
	}
	
	protected void render(RenderPacket packet, CEObject obj) {}
	
	protected void postRender(RenderPacket packet, CEObject obj) {}
	
	protected void update0(UpdatePacket packet, CEObject obj) {}
	
	protected void update1(UpdatePacket packet, CEObject obj) {}
	
	public PropertyController getPropertyController(ISceneSystem system) {
		PropertyController ctrl = new PropertyController();
		return ctrl;
	}
	
	public Query getQuery() {
		return query;
	}
	
	public Modifier duplicate() {
		return new QueryModifier(this);
	}
	
}
