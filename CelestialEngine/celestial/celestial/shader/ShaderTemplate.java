package celestial.shader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import celestial.core.Modifier;

public final class ShaderTemplate {
	
	private final ArrayList<Modifier> modifiers;
	
	public ShaderTemplate(Collection<Modifier> modifiers) {
		this.modifiers = new ArrayList<>(modifiers);
	}
	
	public ShaderTemplate(Modifier... modifiers) {
		this(Arrays.asList(modifiers));
	}
	
	public Collection<Modifier> getModifiers() {
		return modifiers;
	}
	
	public int[] toIDStream() {
		int[] stream = new int[modifiers.size()];
		int i = 0;
		
		for(Modifier mod : modifiers)
			stream[i++] = mod.getID();
		
		return stream;
	}
	
	@Override
	public String toString() {
		String list = "";
		int[] arr = toIDStream();
		for(int i = 0 ; i < arr.length ; ++i)
			list += arr[i] + " ";
		return "ShaderTemplate: " + list.substring(0, list.length() - 1);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(toIDStream());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ShaderTemplate))
			return false;
		
		ShaderTemplate st = (ShaderTemplate) o;
		int[] s0 = toIDStream(), s1 = st.toIDStream();
		if(s0.length != s1.length)
			return false;
		
		for(int i = 0 ; i < s0.length ; ++i)
			if(s0[i] != s1[i])
				return false;
		
		return true;
	}
	
}
