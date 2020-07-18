package celestial.shading;

import java.util.HashMap;
import java.util.Map;

import celestial.data.ColorDepth;
import celestial.data.FrameBuffer;
import celestial.data.GLData;
import celestial.data.Sampler;

public class Output {
	
	private final ConnectorType type;
	private final ShadingNode src;
	
	private final Map<ShadingSystem, FrameBuffer> textures;
	private boolean alreadyRendered = false;
	
	public Output(ConnectorType type, ShadingNode src) {
		this.type = type;
		this.src = src;
		this.textures = new HashMap<>();
	}
	
	public ConnectorType getType() {
		return type;
	}
	
	public ShadingNode getSrc() {
		return src;
	}
	
	public Sampler obtainOutput(ShadingSystem system) {
		if(alreadyRendered)
			return textures.get(system);
		else {
			if(!textures.containsKey(system))
				textures.put(system, FrameBuffer.create(system.getTexWidth(), system.getTexHeight(), false, false, ColorDepth.RGBA16_HDR));
			FrameBuffer texture = textures.get(system);
			
			system.renderNode(src, this, texture);
			alreadyRendered = true;
			return texture;
		}
	}
	
	public boolean containsData(GLData data, ShadingSystem system) {
		return textures.get(system) == data;
	}
	
	void reset() {
		alreadyRendered = false;
	}
	
}
