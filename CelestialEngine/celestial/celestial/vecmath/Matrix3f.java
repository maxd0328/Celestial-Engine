package celestial.vecmath;

public final class Matrix3f extends Matrix<Matrix3f> {
	
	private static final long serialVersionUID = 8880485088286438365L;
	
	public float m00, m01, m02;
	public float m10, m11, m12;
	public float m20, m21, m22;
	
	public Matrix3f() {
		super();
		setIdentity();
	}
	
	public Matrix3f(GenericMatrix src) {
		super();
		set(src);
	}
	
	public Matrix3f(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
	}
	
	@Override
	public Matrix3f setIdentity() {
		this.m00 = 1; this.m01 = 0; this.m02 = 0;
		this.m10 = 0; this.m11 = 1; this.m12 = 0;
		this.m20 = 0; this.m21 = 0; this.m22 = 1;
		return this;
	}
	
	@Override
	public Matrix3f setZero() {
		this.m00 = 0; this.m01 = 0; this.m02 = 0;
		this.m10 = 0; this.m11 = 0; this.m12 = 0;
		this.m20 = 0; this.m21 = 0; this.m22 = 0;
		return this;
	}
	
	@Override
	public Matrix3f set(GenericMatrix src) {
		float[][] buffer = src.read(3);
		this.m00 = buffer[0][0]; this.m01 = buffer[0][1]; this.m02 = buffer[0][2];
		this.m10 = buffer[1][0]; this.m11 = buffer[1][1]; this.m12 = buffer[1][2];
		this.m20 = buffer[2][0]; this.m21 = buffer[2][1]; this.m22 = buffer[2][2];
		return this;
	}
	
	@Override
	public Matrix3f negate() {
		this.m00 = -m00; this.m01 = -m01; this.m02 = -m02;
		this.m10 = -m10; this.m11 = -m11; this.m12 = -m12;
		this.m20 = -m20; this.m21 = -m21; this.m22 = -m22;
		return this;
	}
	
	@Override
	public Matrix3f transpose() {
		float m00 = this.m00;
		float m01 = this.m10;
		float m02 = this.m20;
		float m10 = this.m01;
		float m11 = this.m11;
		float m12 = this.m21;
		float m20 = this.m02;
		float m21 = this.m12;
		float m22 = this.m22;

		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
		return this;
	}
	
	@Override
	public Matrix3f invert() {
		float determinant = determinant();
		if(determinant != 0) {
			float determinant_inv = 1f/determinant;
			float t00 = m11 * m22 - m12* m21;
			float t01 = - m10 * m22 + m12 * m20;
			float t02 = m10 * m21 - m11 * m20;
			float t10 = - m01 * m22 + m02 * m21;
			float t11 = m00 * m22 - m02 * m20;
			float t12 = - m00 * m21 + m01 * m20;
			float t20 = m01 * m12 - m02 * m11;
			float t21 = -m00 * m12 + m02 * m10;
			float t22 = m00 * m11 - m01 * m10;
			
			m00 = t00*determinant_inv;
			m11 = t11*determinant_inv;
			m22 = t22*determinant_inv;
			m01 = t10*determinant_inv;
			m10 = t01*determinant_inv;
			m20 = t02*determinant_inv;
			m02 = t20*determinant_inv;
			m12 = t21*determinant_inv;
			m21 = t12*determinant_inv;
		}
		return this;
	}
	
	@Override
	public float determinant() {
		return	m00 * (m11 * m22 - m12 * m21)
				+ m01 * (m12 * m20 - m10 * m22)
				+ m02 * (m10 * m21 - m11 * m20);
	}
	
	@Override
	protected float[][] readRaw() {
		return new float[][] {
			{m00, m01, m02},
			{m10, m11, m12},
			{m20, m21, m22}
		};
	}
	
	public Vector3f transform(Vector3f vec) {
		return transform(vec, null);
	}
	
	public Vector3f transform(Vector3f vec, Vector3f dest) {
		if(dest == null) dest = new Vector3f();
		
		float x = m00 * vec.x + m10 * vec.y + m20 * vec.z;
		float y = m01 * vec.x + m11 * vec.y + m21 * vec.z;
		float z = m02 * vec.x + m12 * vec.y + m22 * vec.z;
		
		dest.set(x, y, z);
		return dest;
	}
	
	public static Matrix3f add(Matrix3f left, Matrix3f right) {
		return add(left, right, null);
	}
	
	public static Matrix3f add(Matrix3f left, Matrix3f right, Matrix3f dest) {
		if(dest == null) dest = new Matrix3f();

		dest.m00 = left.m00 + right.m00;
		dest.m01 = left.m01 + right.m01;
		dest.m02 = left.m02 + right.m02;
		dest.m10 = left.m10 + right.m10;
		dest.m11 = left.m11 + right.m11;
		dest.m12 = left.m12 + right.m12;
		dest.m20 = left.m20 + right.m20;
		dest.m21 = left.m21 + right.m21;
		dest.m22 = left.m22 + right.m22;

		return dest;
	}
	
	public static Matrix3f sub(Matrix3f left, Matrix3f right) {
		return sub(left, right, null);
	}
	
	public static Matrix3f sub(Matrix3f left, Matrix3f right, Matrix3f dest) {
		if(dest == null) dest = new Matrix3f();

		dest.m00 = left.m00 - right.m00;
		dest.m01 = left.m01 - right.m01;
		dest.m02 = left.m02 - right.m02;
		dest.m10 = left.m10 - right.m10;
		dest.m11 = left.m11 - right.m11;
		dest.m12 = left.m12 - right.m12;
		dest.m20 = left.m20 - right.m20;
		dest.m21 = left.m21 - right.m21;
		dest.m22 = left.m22 - right.m22;

		return dest;
	}
	
	public static Matrix3f mul(Matrix3f left, Matrix3f right) {
		return mul(left, right, null);
	}
	
	public static Matrix3f mul(Matrix3f left, Matrix3f right, Matrix3f dest) {
		if (dest == null) dest = new Matrix3f();

		float m00 = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02;
		float m01 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02;
		float m02 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02;
		float m10 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12;
		float m11 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12;
		float m12 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12;
		float m20 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22;
		float m21 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22;
		float m22 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22;

		dest.m00 = m00;
		dest.m01 = m01;
		dest.m02 = m02;
		dest.m10 = m10;
		dest.m11 = m11;
		dest.m12 = m12;
		dest.m20 = m20;
		dest.m21 = m21;
		dest.m22 = m22;

		return dest;
	}
	
	public Matrix3f add(GenericMatrix src) {
		return add(this, new Matrix3f(src), this);
	}
	
	public Matrix3f sub(GenericMatrix src) {
		return sub(this, new Matrix3f(src), this);
	}
	
	public Matrix3f mul(GenericMatrix src) {
		return mul(this, new Matrix3f(src), this);
	}
	
	public Vector3f toEulerRotation() {
		float t1 = (float) Math.atan2(m12, m22);
		float c2 = (float) Math.sqrt(m00 * m00 + m01 * m01);
		float t2 = (float) Math.atan2(-m02, c2);
		float s1 = (float) Math.sin(t1);
		float c1 = (float) Math.cos(t1);
		float t3 = (float) Math.atan2(s1 * m20 - c1 * m10, c1 * m11 - s1 * m21);
		return new Vector3f((float) Math.toDegrees(-t1), (float) Math.toDegrees(-t2), (float) Math.toDegrees(-t3));
	}
	
	public Matrix3f scale(float factor) {
		this.m00 *= factor;
		this.m01 *= factor;
		this.m02 *= factor;
		this.m10 *= factor;
		this.m11 *= factor;
		this.m12 *= factor;
		this.m20 *= factor;
		this.m21 *= factor;
		this.m22 *= factor;
		return this;
	}
	
	public Matrix3f scale(Vector3f vec) {
		this.m00 *= vec.x;
		this.m01 *= vec.x;
		this.m02 *= vec.x;
		this.m10 *= vec.y;
		this.m11 *= vec.y;
		this.m12 *= vec.y;
		this.m20 *= vec.z;
		this.m21 *= vec.z;
		this.m22 *= vec.z;
		return this;
	}
	
	public Matrix3f normalize() {
		Vector3f r0 = (m00 == 0 && m10 == 0 && m20 == 0) ? new Vector3f(m00, m10, m20) : new Vector3f(m00, m10, m20).normalize();
		this.m00 = r0.x;
		this.m10 = r0.y;
		this.m20 = r0.z;
		
		Vector3f r1 = (m01 == 0 && m11 == 0 && m21 == 0) ? new Vector3f(m01, m11, m21) : new Vector3f(m01, m11, m21).normalize();
		this.m01 = r1.x;
		this.m11 = r1.y;
		this.m21 = r1.z;
		
		Vector3f r2 = (m02 == 0 && m12 == 0 && m22 == 0) ? new Vector3f(m02, m12, m22) : new Vector3f(m02, m12, m22).normalize();
		this.m02 = r2.x;
		this.m12 = r2.y;
		this.m22 = r2.z;
		return this;
	}
	
	public Matrix3f clone() {
		return new Matrix3f(this);
	}
	
}
