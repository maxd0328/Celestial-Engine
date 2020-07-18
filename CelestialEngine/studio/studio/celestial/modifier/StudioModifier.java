package studio.celestial.modifier;

import java.util.ArrayList;
import celestial.core.Modifier;
import celestial.ctrl.PropertyController;
import studio.celestial.core.GLRequestSystem;
import studio.celestial.core.StudioInterface;
import studio.celestial.impl.ModifierRepositoryPanel.ModifierItem;

public final class StudioModifier {
	
	private final ArrayList<Modifier> modifiers;
	private final PropertyController ctrl;
	private ModifierEditor editor;
	
	public StudioModifier(ArrayList<Modifier> modifiers, PropertyController ctrl) {
		this.modifiers = modifiers;
		this.ctrl = ctrl;
		this.editor = createEditor();
	}
	
	public StudioModifier(Modifier modifier) {
		this(singleton(modifier), modifier.getPropertyController(GLRequestSystem.getSceneManager()));
	}
	
	public ArrayList<Modifier> getModifiers() {
		return modifiers;
	}
	
	public PropertyController getCtrl() {
		return ctrl;
	}
	
	public ModifierEditor getEditor() {
		return editor;
	}
	
	public void update() {
		if(editor != null) editor.update();
	}
	
	public void deactivateAll() {
		if(editor != null) editor.deactivateAll();
	}
	
	private ModifierEditor createEditor() {
		ModifierItem item = StudioInterface.getInstantiation().getModifier(modifiers.get(0).getClass());
		if(item != null) {
			ModifierEditor editor = new ModifierEditor(this, item.getIdentifier(), item.getDescription());
			
			if(ctrl != null) {
				editor.initProperties(ctrl, item.getIdentifier());
				return editor;
			}
		}
		return null;
	}
	
	private static ArrayList<Modifier> singleton(Modifier mod) {
		ArrayList<Modifier> mods = new ArrayList<Modifier>();
		mods.add(mod);
		return mods;
	}
	
}
