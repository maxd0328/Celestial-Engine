package celestial.vecmath;

public final class Vector3f extends Vector<Vector3f> {
	
	private static final long serialVersionUID = -7577920426714425340L;
	
	public float x, y, z;
	
	public Vector3f() {
		super();
		setIdentity();
	}
	
	public Vector3f(float val) {
		super();
		set(val, val, val);
	}
	
	public Vector3f(float x, float y, float z) {
		super();
		set(x, y, z);
	}
	
	public Vector3f(GenericVector src) {
		super();
		set(src);
	}
	
	public Vector3f(Vector2f src, float z) {
		super();
		set(src.x, src.y, z);
	}
	
	public Vector3f setIdentity() {
		return set(0, 0, 0);
	}
	
	public Vector3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vector3f set(GenericVector src) {
		float[] buffer = src.read(3);
		return set(buffer[0], buffer[1], buffer[2]);
	}
	
	@Override
	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z);
	}
	
	@Override
	public Vector3f negate() {
		this.x = -x;
		this.y = -y;
		this.z = -z;
		return this;
	}
	
	@Override
	public Vector3f scale(float amount) {
		this.x *= amount;
		this.y *= amount;
		this.z *= amount;
		return this;
	}
	
	public Vector3f scale(Vector3f amount) {
		this.x *= amount.x;
		this.y *= amount.y;
		this.z *= amount.z;
		return this;
	}
	
	@Override
	protected float[] readRaw() {
		return new float[] {x, y, z};
	}
	
	public Vector3f translate(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vector3f translate(GenericVector src) {
		float[] buffer = src.read(3);
		return translate(buffer[0], buffer[1], buffer[2]);
	}
	
	public float dot(float x, float y, float z) {
		return (this.x * x) + (this.y * y) + (this.z * z);
	}
	
	public float dot(GenericVector src) {
		float[] buffer = src.read(3);
		return dot(buffer[0], buffer[1], buffer[2]);
	}
	
	public static float dot(GenericVector a, GenericVector b) {
		return new Vector3f(a).dot(b);
	}
	
	public static Vector3f add(GenericVector a, GenericVector b) {
		return add(a, b, null);
	}
	
	public static Vector3f add(GenericVector a, GenericVector b, Vector3f dest) {
		if(dest == null) dest = new Vector3f();
		float[] sA = a.read(3), sB = b.read(3);
		dest.set(sA[0] + sB[0], sA[1] + sB[1], sA[2] + sB[2]);
		return dest;
	}
	
	public static Vector3f sub(GenericVector a, GenericVector b) {
		return sub(a, b, null);
	}
	
	public static Vector3f sub(GenericVector a, GenericVector b, Vector3f dest) {
		if(dest == null) dest = new Vector3f();
		float[] sA = a.read(3), sB = b.read(3);
		dest.set(sA[0] - sB[0], sA[1] - sB[1], sA[2] - sB[2]);
		return dest;
	}
	
	public static Vector3f mul(GenericVector a, float b) {
		return mul(a, b, null);
	}
	
	public static Vector3f mul(GenericVector a, float b, Vector3f dest) {
		if(dest == null) dest = new Vector3f();
		float[] sA = a.read(3);
		dest.set(sA[0] * b, sA[1] * b, sA[2] * b);
		return dest;
	}
	
	public static Vector3f div(GenericVector a, float b) {
		return div(a, b, null);
	}
	
	public static Vector3f div(GenericVector a, float b, Vector3f dest) {
		if(dest == null) dest = new Vector3f();
		float[] sA = a.read(3);
		dest.set(sA[0] / b, sA[1] / b, sA[2] / b);
		return dest;
	}
	
	public static Vector3f cross(GenericVector a, GenericVector b) {
		return cross(a, b, null);
	}
	
	public static Vector3f cross(GenericVector a, GenericVector b, Vector3f dest) {
		if(dest == null) dest = new Vector3f();
		float[] left = a.read(3), right = b.read(3);
		dest.set(left[1] * right[2] - left[2] * right[1],
				right[0] * left[2] - right[2] * left[0],
				left[0] * right[1] - left[1] * right[0]);
		return dest;
	}
	
	public static float angle(Vector3f a, Vector3f b) {
		float dls = a.dot(b) / (a.length() * b.length());
		if (dls < -1f)
			dls = -1f;
		else if (dls > 1.0f)
			dls = 1.0f;
		return (float)Math.acos(dls);
	}
	
	public Vector3f clone() {
		return new Vector3f(this);
	}
	
}
