package celestial.vecmath;

public final class Vector4f extends Vector<Vector4f> {
	
	private static final long serialVersionUID = -1609239721796595029L;
	
	public float x, y, z, w;
	
	public Vector4f() {
		super();
		setIdentity();
	}
	
	public Vector4f(float val) {
		super();
		set(val, val, val, val);
	}
	
	public Vector4f(float x, float y, float z, float w) {
		super();
		set(x, y, z, w);
	}
	
	public Vector4f(GenericVector src) {
		super();
		set(src);
	}
	
	public Vector4f(Vector3f src, float w) {
		super();
		set(new Vector4f(src.x, src.y, src.z, w));
	}
	
	public Vector4f(Vector2f src, float z, float w) {
		super();
		set(new Vector4f(src.x, src.y, z, w));
	}
	
	public Vector4f setIdentity() {
		return set(0, 0, 0, 0);
	}
	
	public Vector4f set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Vector4f set(GenericVector src) {
		float[] buffer = src.read(4);
		return set(buffer[0], buffer[1], buffer[2], buffer[3]);
	}
	
	@Override
	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z) + (w * w);
	}
	
	@Override
	public Vector4f negate() {
		this.x = -x;
		this.y = -y;
		this.z = -z;
		this.w = -w;
		return this;
	}
	
	@Override
	public Vector4f scale(float amount) {
		this.x *= amount;
		this.y *= amount;
		this.z *= amount;
		this.w *= amount;
		return this;
	}
	
	public Vector4f scale(Vector4f amount) {
		this.x *= amount.x;
		this.y *= amount.y;
		this.z *= amount.z;
		this.w *= amount.w;
		return this;
	}
	
	@Override
	protected float[] readRaw() {
		return new float[] {x, y, z, w};
	}
	
	public Vector4f translate(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}
	
	public Vector4f translate(GenericVector src) {
		float[] buffer = src.read(4);
		return translate(buffer[0], buffer[1], buffer[2], buffer[3]);
	}
	
	public float dot(float x, float y, float z, float w) {
		return (this.x * x) + (this.y * y) + (this.z * z) + (this.w * w);
	}
	
	public float dot(GenericVector src) {
		float[] buffer = src.read(4);
		return dot(buffer[0], buffer[1], buffer[2], buffer[3]);
	}
	
	public static Vector4f add(GenericVector a, GenericVector b) {
		return add(a, b, null);
	}
	
	public static Vector4f add(GenericVector a, GenericVector b, Vector4f dest) {
		if(dest == null) dest = new Vector4f();
		float[] sA = a.read(4), sB = b.read(4);
		dest.set(sA[0] + sB[0], sA[1] + sB[1], sA[2] + sB[2], sA[3] + sB[3]);
		return dest;
	}
	
	public static Vector4f sub(GenericVector a, GenericVector b) {
		return sub(a, b, null);
	}
	
	public static Vector4f sub(GenericVector a, GenericVector b, Vector4f dest) {
		if(dest == null) dest = new Vector4f();
		float[] sA = a.read(4), sB = b.read(4);
		dest.set(sA[0] - sB[0], sA[1] - sB[1], sA[2] - sB[2], sA[3] - sB[3]);
		return dest;
	}
	
	public static Vector4f mul(GenericVector a, float b) {
		return mul(a, b, null);
	}
	
	public static Vector4f mul(GenericVector a, float b, Vector4f dest) {
		if(dest == null) dest = new Vector4f();
		float[] sA = a.read(4);
		dest.set(sA[0] * b, sA[1] * b, sA[2] * b, sA[3] * b);
		return dest;
	}
	
	public static Vector4f div(GenericVector a, float b) {
		return div(a, b, null);
	}
	
	public static Vector4f div(GenericVector a, float b, Vector4f dest) {
		if(dest == null) dest = new Vector4f();
		float[] sA = a.read(4);
		dest.set(sA[0] / b, sA[1] / b, sA[2] / b, sA[3] / b);
		return dest;
	}
	
	public static float angle(Vector4f a, Vector4f b) {
		float dls = a.dot(b) / (a.length() * b.length());
		if (dls < -1f)
			dls = -1f;
		else if (dls > 1.0f)
			dls = 1.0f;
		return (float)Math.acos(dls);
	}
	
	public Vector4f clone() {
		return new Vector4f(this);
	}
	
}
