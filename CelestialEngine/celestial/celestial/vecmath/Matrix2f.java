package celestial.vecmath;

public final class Matrix2f extends Matrix<Matrix2f> {
	
	private static final long serialVersionUID = -2110029845672314598L;
	
	public float m00, m01;
	public float m10, m11;
	
	public Matrix2f() {
		super();
		setIdentity();
	}
	
	public Matrix2f(GenericMatrix src) {
		super();
		set(src);
	}
	
	@Override
	public Matrix2f setIdentity() {
		this.m00 = 1; this.m01 = 0;
		this.m10 = 0; this.m11 = 1;
		return this;
	}
	
	@Override
	public Matrix2f setZero() {
		this.m00 = 0; this.m01 = 0;
		this.m10 = 0; this.m11 = 0;
		return this;
	}
	
	@Override
	public Matrix2f set(GenericMatrix src) {
		float[][] buffer = src.read(2);
		this.m00 = buffer[0][0]; this.m01 = buffer[0][1];
		this.m10 = buffer[1][0]; this.m11 = buffer[1][1];
		return this;
	}
	
	@Override
	public Matrix2f negate() {
		this.m00 = -m00; this.m01 = -m01;
		this.m10 = -m10; this.m11 = -m11;
		return this;
	}
	
	@Override
	public Matrix2f transpose() {
		Matrix2f matrix = new Matrix2f(this);
		this.m01 = matrix.m10;
		this.m10 = matrix.m01;
		return this;
	}
	
	@Override
	public Matrix2f invert() {
		float determinant = determinant();
		if(determinant != 0) {
			float inv =  1f/determinant;
			float t00 =  m11*inv;
			float t01 = -m01*inv;
			float t11 =  m00*inv;
			float t10 = -m10*inv;
			
			m00 = t00;
			m01 = t01;
			m10 = t10;
			m11 = t11;
		}
		return this;
	}
	
	@Override
	public float determinant() {
		return m00 * m11 - m01 * m10;
	}
	
	@Override
	protected float[][] readRaw() {
		return new float[][] {
			{m00, m01},
			{m10, m11}
		};
	}
	
	public Vector2f transform(Vector2f vec) {
		return transform(vec, null);
	}
	
	public Vector2f transform(Vector2f vec, Vector2f dest) {
		if(dest == null) dest = new Vector2f();
		
		float x = m00 * vec.x + m10 * vec.y;
		float y = m01 * vec.x + m11 * vec.y;
		
		dest.set(x, y);
		return dest;
	}
	
	public static Matrix2f add(Matrix2f left, Matrix2f right) {
		return add(left, right, null);
	}
	
	public static Matrix2f add(Matrix2f left, Matrix2f right, Matrix2f dest) {
		if(dest == null) dest = new Matrix2f();

		dest.m00 = left.m00 + right.m00;
		dest.m01 = left.m01 + right.m01;
		dest.m10 = left.m10 + right.m10;
		dest.m11 = left.m11 + right.m11;

		return dest;
	}
	
	public static Matrix2f sub(Matrix2f left, Matrix2f right) {
		return sub(left, right, null);
	}
	
	public static Matrix2f sub(Matrix2f left, Matrix2f right, Matrix2f dest) {
		if(dest == null) dest = new Matrix2f();

		dest.m00 = left.m00 - right.m00;
		dest.m01 = left.m01 - right.m01;
		dest.m10 = left.m10 - right.m10;
		dest.m11 = left.m11 - right.m11;

		return dest;
	}
	
	public static Matrix2f mul(Matrix2f left, Matrix2f right) {
		return mul(left, right, null);
	}
	
	public static Matrix2f mul(Matrix2f left, Matrix2f right, Matrix2f dest) {
		if (dest == null) dest = new Matrix2f();

		float m00 = left.m00 * right.m00 + left.m10 * right.m01;
		float m01 = left.m01 * right.m00 + left.m11 * right.m01;
		float m10 = left.m00 * right.m10 + left.m10 * right.m11;
		float m11 = left.m01 * right.m10 + left.m11 * right.m11;

		dest.m00 = m00;
		dest.m01 = m01;
		dest.m10 = m10;
		dest.m11 = m11;

		return dest;
	}
	
	public Matrix2f add(GenericMatrix src) {
		return add(this, new Matrix2f(src), this);
	}
	
	public Matrix2f sub(GenericMatrix src) {
		return sub(this, new Matrix2f(src), this);
	}
	
	public Matrix2f mul(GenericMatrix src) {
		return mul(this, new Matrix2f(src), this);
	}
	
	public Matrix2f clone() {
		return new Matrix2f(this);
	}
	
}
