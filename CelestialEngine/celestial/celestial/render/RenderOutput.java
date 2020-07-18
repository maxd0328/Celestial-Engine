package celestial.render;

import org.lwjgl.opengl.GL30;

import celestial.core.EngineRuntime;

public interface RenderOutput {
	
	public static final RenderOutput SCREEN = new RenderOutput() {
		@Override
		public void bind() {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			EngineRuntime.dispAlignViewport();
		}
		
		@Override
		public void unbind() {}
	};
	
	public void bind();
	
	public void unbind();
	
}
