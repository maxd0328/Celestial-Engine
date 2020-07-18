package celestial.render;

import java.util.ArrayList;
import java.util.Collection;
import celestial.core.CEObject;
import celestial.core.Modifier;
import celestial.util.Predicate;

public final class RenderConstraints {
	
	private static final int TRUE_I32  = 0x1;
	private static final int FALSE_I32 = 0x0;
	
	private final Collection<Predicate<CEObject>> objectPredicates;
	private final Collection<Predicate<Modifier>> modifierPredicates;
	
	public RenderConstraints() {
		this.objectPredicates = new ArrayList<>();
		this.modifierPredicates = new ArrayList<>();
	}
	
	public RenderConstraints(RenderConstraints src) {
		this.objectPredicates = new ArrayList<>(src.objectPredicates);
		this.modifierPredicates = new ArrayList<>(src.modifierPredicates);
	}
	
	public Collection<Predicate<CEObject>> getObjectPredicates() {
		return objectPredicates;
	}
	
	public Collection<Predicate<Modifier>> getModifierPredicates() {
		return modifierPredicates;
	}
	
	public boolean validate(CEObject obj) {
		int valid = TRUE_I32;
		for(Predicate<CEObject> predicate : objectPredicates)
			valid &= predicate.validate(obj) ? TRUE_I32 : FALSE_I32;
		return valid == TRUE_I32 ? true : false;
	}
	
	public boolean validate(Modifier mod) {
		int valid = TRUE_I32;
		for(Predicate<Modifier> predicate : modifierPredicates)
			valid &= predicate.validate(mod) ? TRUE_I32 : FALSE_I32;
		return valid == TRUE_I32 ? true : false;
	}
	
}
