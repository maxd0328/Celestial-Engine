package celestial.ctrl;

import java.io.Serializable;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public interface FloatConverter<E> extends Serializable {
	
	public static final FloatConverter<Integer> INTEGER_CONVERTER = new FloatConverter<Integer>() {
		
		private static final long serialVersionUID = -1085447509900911254L;
		
		@Override
		public float convert(Integer value, int index) {
			return (float) (int) value;
		}
		
		@Override
		public Integer convertBack(float value) {
			return (int) value;
		}
		
		@Override
		public int indices() { return 1; }
		
	};
	
	public static final FloatConverter<Float> FLOAT_CONVERTER = new FloatConverter<Float>() {
		
		private static final long serialVersionUID = 7102226922265592369L;
		
		@Override
		public float convert(Float value, int index) {
			return (float) value;
		}
		
		@Override
		public Float convertBack(float value) {
			return value;
		}
		
		@Override
		public int indices() { return 1; }
		
	};
	
	public static final FloatConverter<Vector2f> VEC2_CONVERTER = new FloatConverter<Vector2f>() {
		
		private static final long serialVersionUID = -7866856979293504319L;
		
		@Override
		public float convert(Vector2f value, int index) {
			return index == 0 ? value.x : value.y;
		}
		
		@Override
		public Vector2f convertBack(float value) {
			return new Vector2f(value, 0);
		}
		
		@Override
		public int indices() { return 2; }
		
	};
	
	public static final FloatConverter<Vector3f> VEC3_CONVERTER = new FloatConverter<Vector3f>() {
		
		private static final long serialVersionUID = 5170476303603662491L;
		
		@Override
		public float convert(Vector3f value, int index) {
			return index == 0 ? value.x : index == 1 ? value.y : value.z;
		}
		
		@Override
		public Vector3f convertBack(float value) {
			return new Vector3f(value, 0, 0);
		}
		
		@Override
		public int indices() { return 3; }
		
	};
	
	public static final FloatConverter<Vector4f> VEC4_CONVERTER = new FloatConverter<Vector4f>() {
		
		private static final long serialVersionUID = 5175106373290710682L;
		
		@Override
		public float convert(Vector4f value, int index) {
			return index == 0 ? value.x : index == 1 ? value.y : index == 2 ? value.z : value.w;
		}
		
		@Override
		public Vector4f convertBack(float value) {
			return new Vector4f(value, 0, 0, 0);
		}
		
		@Override
		public int indices() { return 4; }
		
	};
	
	public float convert(E value, int index);
	
	public E convertBack(float value);
	
	public int indices();
	
}
