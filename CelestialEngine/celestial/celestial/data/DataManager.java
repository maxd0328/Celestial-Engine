package celestial.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;

import celestial.core.CEObject;
import celestial.core.EngineRuntime;
import celestial.shading.ShadingSystem;
import celestial.shadow.ShadowMapSystem;
import celestial.ui.Graphic;
import celestial.ui.UISystem;
import celestial.util.Factory;
import celestial.vecmath.Vector3f;

public abstract class DataManager {
	
	protected final ArrayList<GLData> data;
	
	DataManager() {
		this.data = new ArrayList<GLData>();
	}
	
	public abstract void update(CEObject camera, List<ShadingSystem> shadingSystems, UISystem uiSystem, Collection<CEObject> objects);
	
	public void update(CEObject camera, List<ShadingSystem> shadingSystems, UISystem uiSystem, CEObject... objects) {
		update(camera, shadingSystems, uiSystem, Arrays.asList(objects));
	}
	
	public void destroy() {
		for(GLData data : data) data.deallocate();
	}
	
	protected void addData(GLData data) {
		this.data.add(data);
		data.allocate();
	}
	
	public static class StaticDataManager extends DataManager {
		
		public static final Factory<StaticDataManager> FACTORY = () -> new StaticDataManager();
		
		public StaticDataManager() {
			super();
		}
		
		@Override
		public void update(CEObject camera, List<ShadingSystem> shadingSystems, UISystem uiSystem, Collection<CEObject> objects) {
			for(GLData data : data) data.allocate();
		}
		
	}
	
	public static class ClusterDataManager extends DataManager {
		
		public static final Factory<ClusterDataManager> FACTORY = () -> new ClusterDataManager();
		
		public ClusterDataManager() {
			super();
		}
		boolean has = false;
		@Override
		public void update(CEObject camera, List<ShadingSystem> shadingSystems, UISystem uiSystem, Collection<CEObject> objects) {
			for(;;) {
				try {
					for(GLData data : data) {
						boolean allocate = false;
						if(data == Graphic.getDefaultVAO() || data == ShadingSystem.getVAO() || data
								== EngineRuntime.getPostProcessingBuffer() || ShadowMapSystem.INSTANCE.containsData(data))
							allocate = true;
						else for(CEObject obj : objects) {
							if(obj.containsData(data)) {
								allocate = true;
								break;
							}
						}
						if(!allocate) {
							for(ShadingSystem shadingSystem : shadingSystems) {
								if(shadingSystem.containsData(data)) {
									allocate = true;
									break;
								}
							}
						}
						if(!allocate) allocate = uiSystem.containsData(data);
						
						if(allocate) data.allocate();
						else data.deallocate();
					}
					
					break;
				}
				catch(ConcurrentModificationException ex) {
				}
			}
		}
		
	}
	
	public static class DynamicDataManager extends DataManager {
		
		public static final Factory<DynamicDataManager> FACTORY = () -> new DynamicDataManager();
		
		public DynamicDataManager() {
			super();
		}
		
		@Override
		public void update(CEObject camera, List<ShadingSystem> shadingSystems, UISystem uiSystem, Collection<CEObject> objects) {
			for(;;) {
				try {
					for(GLData data : data) {
						boolean allocate = false;
						
						if(data == Graphic.getDefaultVAO() || data == ShadingSystem.getVAO() || data
								== EngineRuntime.getPostProcessingBuffer() || ShadowMapSystem.INSTANCE.containsData(data))
							allocate = true;
						else for(CEObject obj : objects) {
							if(obj.containsData(data) && Math.max(0, Vector3f.sub(obj.getPosition(), camera.getPosition()).length()
									- obj.getConstraints().getFrustumRadius() * obj.getMaxScale()) < obj.getConstraints().getCullDistance()) {
								allocate = true;
								break;
							}
						}
						if(!allocate) {
							for(ShadingSystem shadingSystem : shadingSystems) {
								if(shadingSystem.containsData(data)) {
									allocate = true;
									break;
								}
							}
						}
						if(!allocate) allocate = uiSystem.containsData(data);
						
						if(allocate) data.allocate();
						else data.deallocate();
					}
					
					break;
				}
				catch(ConcurrentModificationException ex) {
				}
			}
		}
		
	}
	
}
