package celestial.vecmath;

public final class Vector2f extends Vector<Vector2f> {
	
	private static final long serialVersionUID = 4804579076399779777L;
	
	public float x, y;
	
	public Vector2f() {
		super();
		setIdentity();
	}
	
	public Vector2f(float val) {
		super();
		set(val, val);
	}
	
	public Vector2f(float x, float y) {
		super();
		set(x, y);
	}
	
	public Vector2f(GenericVector src) {
		super();
		set(src);
	}
	
	public Vector2f setIdentity() {
		return set(0, 0);
	}
	
	public Vector2f set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vector2f set(GenericVector src) {
		float[] buffer = src.read(2);
		return set(buffer[0], buffer[1]);
	}
	
	@Override
	public float lengthSquared() {
		return (x * x) + (y * y);
	}
	
	@Override
	public Vector2f negate() {
		this.x = -x;
		this.y = -y;
		return this;
	}

	@Override
	public Vector2f scale(float amount) {
		this.x *= amount;
		this.y *= amount;
		return this;
	}
	
	public Vector2f scale(Vector2f amount) {
		this.x *= amount.x;
		this.y *= amount.y;
		return this;
	}
	
	@Override
	protected float[] readRaw() {
		return new float[] {x, y};
	}
	
	public Vector2f translate(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	public Vector2f translate(GenericVector src) {
		float[] buffer = src.read(2);
		return translate(buffer[0], buffer[1]);
	}
	
	public float dot(float x, float y) {
		return (this.x * x) + (this.y * y);
	}
	
	public float dot(GenericVector src) {
		float[] buffer = src.read(2);
		return dot(buffer[0], buffer[1]);
	}
	
	public static Vector2f add(GenericVector a, GenericVector b) {
		return add(a, b, null);
	}
	
	public static Vector2f add(GenericVector a, GenericVector b, Vector2f dest) {
		if(dest == null) dest = new Vector2f();
		float[] sA = a.read(2), sB = b.read(2);
		dest.set(sA[0] + sB[0], sA[1] + sB[1]);
		return dest;
	}
	
	public static Vector2f sub(GenericVector a, GenericVector b) {
		return sub(a, b, null);
	}
	
	public static Vector2f sub(GenericVector a, GenericVector b, Vector2f dest) {
		if(dest == null) dest = new Vector2f();
		float[] sA = a.read(2), sB = b.read(2);
		dest.set(sA[0] - sB[0], sA[1] - sB[1]);
		return dest;
	}
	
	public static Vector2f mul(GenericVector a, float b) {
		return mul(a, b, null);
	}
	
	public static Vector2f mul(GenericVector a, float b, Vector2f dest) {
		if(dest == null) dest = new Vector2f();
		float[] sA = a.read(2);
		dest.set(sA[0] * b, sA[1] * b);
		return dest;
	}
	
	public static Vector2f div(GenericVector a, float b) {
		return div(a, b, null);
	}
	
	public static Vector2f div(GenericVector a, float b, Vector2f dest) {
		if(dest == null) dest = new Vector2f();
		float[] sA = a.read(2);
		dest.set(sA[0] / b, sA[1] / b);
		return dest;
	}
	
	public static float angle(Vector2f a, Vector2f b) {
		float dls = a.dot(b) / (a.length() * b.length());
		if (dls < -1f)
			dls = -1f;
		else if (dls > 1.0f)
			dls = 1.0f;
		return (float)Math.acos(dls);
	}
	
	public Vector2f clone() {
		return new Vector2f(this);
	}
	
}
