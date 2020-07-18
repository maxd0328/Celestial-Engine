package studio.celestial.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import celestial.beans.driver.Driver;
import celestial.beans.property.Property;
import celestial.error.CelestialContextException;
import celestial.util.Factory;

public final class StudioDriverSystem {
	
	private final ArrayList<DriverEditorFactory> factories;
	private final ArrayList<Property<?>> driverSubjects;
	private String driverSubjectName = "";
	
	private DriverEditor currentEditor = null;
	private Factory<? extends Driver> currentFactory = null;
	
	private DriverSystemState state = DriverSystemState.NO_CHANGE;
	private boolean editEnabled = false; // True when drivers should be syncing with the editor
	private boolean editMultiple = false;
	
	public StudioDriverSystem(DriverEditorFactory... factories) {
		this.driverSubjects = new ArrayList<Property<?>>();
		this.factories = new ArrayList<DriverEditorFactory>();
		
		for(DriverEditorFactory factory : factories)
			this.factories.add(factory);
		
		close();
	}
	
	public ArrayList<DriverEditorFactory> getFactories() {
		return factories;
	}
	
	public ArrayList<Property<?>> getCurrentDriverSubjects() {
		return driverSubjects;
	}
	
	public String getDriverSubjectName() {
		return driverSubjectName;
	}
	
	public DriverEditor getCurrentEditor() {
		return currentEditor;
	}
	
	public Factory<? extends Driver> getCurrentFactory() {
		return currentFactory;
	}
	
	public boolean isEditMultiple() {
		return editMultiple;
	}
	
	public void setEditMultiple(boolean editMultiple) {
		this.editMultiple = editMultiple;
	}
	
	public void open(String driverSubjectName, Collection<Property<?>> driverSubjects) {
		this.driverSubjectName = driverSubjectName;
		if(driverSubjects.size() == 0 && !editMultiple) {
			editEnabled = false;
			this.driverSubjects.clear();
			return;
		}
		else if(driverSubjects.size() == 0) return;
		
		if(!editMultiple) this.driverSubjects.clear();
		for(Property<?> driverSubject : driverSubjects) {
						  /* driver system != null */
			if(driverSubject.getDriver() != null && !this.driverSubjects.contains(driverSubject)) this.driverSubjects.add(driverSubject);
			else this.driverSubjects.removeIf(i -> i == driverSubject);
		}
		
		if(this.driverSubjects.size() > 1) editEnabled = false;
		else editEnabled = true;
		
		createEditor();
	}
	
	public void open(String driverSubjectName, Property<?>... driverSubjects) {
		open(driverSubjectName, Arrays.asList(driverSubjects));
	}
	
	public void close() {
		editEnabled = false;
		this.driverSubjects.clear();
		
		createEditor();
	}
	
	public <T extends Driver> void reinstantiateDrivers(Factory<T> factory) {
		if(factory == null) return;
		for(Property<?> driverSubject : driverSubjects)
			driverSubject.getDriver().set(factory.build());
		
		editEnabled = true;
		
		createEditor();
	}
	
	private boolean isNullType() {
		return driverSubjects.size() == 0 || !editEnabled || driverSubjects.get(0).getDriver().get() == null || getPrimaryType() == null;
	}
	
	private void createEditor() {
		if(driverSubjects.size() == 0) state = DriverSystemState.INACTIVE;
		else if(!editEnabled) state = DriverSystemState.EDIT_DISABLED;
		else if(driverSubjects.get(0).getDriver().get() == null) state = DriverSystemState.DRIVER_DISABLED;
		else state = DriverSystemState.DRIVER_ENABLED;
		
		if(isNullType()) {
			currentEditor = null;
			currentFactory = null;
		}
		else {
			Class<?> primaryType = getPrimaryType();
			boolean done = false;
			for(DriverEditorFactory factory : factories) {
				if(factory.getType().isAssignableFrom(primaryType)) {
					Driver[] arr = new Driver[driverSubjects.size()];
					for(int i = 0 ; i < arr.length ; ++i) arr[i] = driverSubjects.get(i).getDriver().get();
					
					currentEditor = factory.create(arr);
					currentFactory = factory.getDriverFactory();
					done = true;
					break;
				}
			}
			
			if(!done) {
				driverSubjects.clear();
				currentEditor = null;
				currentFactory = null;
				throw new CelestialContextException("Driver type not supported");
			}
		}
	}
	
	public Class<?> getPrimaryType() {
		for(Property<?> driverSubject : driverSubjects)
			if(driverSubject.getDriver().get() != null) return driverSubject.getDriver().get().getClass();
		return null;
	}
	
	public DriverSystemState queryState() {
		DriverSystemState state = this.state;
		this.state = DriverSystemState.NO_CHANGE;
		return state;
	}
	
	public void update() {
		if(currentEditor != null) currentEditor.update();
	}
	
	public static enum DriverSystemState implements java.io.Serializable {
		
		NO_CHANGE,
		
		INACTIVE,
		
		EDIT_DISABLED,
		
		DRIVER_DISABLED,
		
		DRIVER_ENABLED;
		
	}
	
}
