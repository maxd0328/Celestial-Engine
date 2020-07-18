package celestial.shading;

import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public enum ConnectorType {
	
	FLOAT,
	
	INTEGER,
	
	VEC2,
	
	VEC3,
	
	VEC4;
	
	public static <T> Constant createConstant(T value) {
		if(value instanceof Float) return (Constant) new Constant(FLOAT, new Vector4f(new Vector3f((Float) value), 1f));
		if(value instanceof Integer) return (Constant) new Constant(INTEGER, new Vector4f(new Vector3f((Integer) value), 1f));
		if(value instanceof Vector2f) return (Constant) new Constant(VEC2, new Vector4f((Vector2f) value, 0f, 1f));
		if(value instanceof Vector3f) return (Constant) new Constant(VEC3, new Vector4f((Vector3f) value, 1f));
		if(value instanceof Vector4f) return (Constant) new Constant(VEC4, (Vector4f) value);
		throw new UnsupportedOperationException("No constant of type \'" + value.getClass() + "\' is available");
	}
	
	public int toInteger() {
		switch(this) {
		case FLOAT: return 0;
		case INTEGER: return 1;
		case VEC2: return 2;
		case VEC3: return 3;
		case VEC4: return 4;
		}
		return -1;
	}
	
	public boolean isCompatibleWith(ConnectorType other) {
		return this.toInteger() <= other.toInteger();
	}
	
}
