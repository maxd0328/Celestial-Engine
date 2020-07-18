package celestial.vecmath;

public final class Matrix4f extends Matrix<Matrix4f> {
	
	private static final long serialVersionUID = -4696043430189154104L;
	
	public float m00, m01, m02, m03;
	public float m10, m11, m12, m13;
	public float m20, m21, m22, m23;
	public float m30, m31, m32, m33;
	
	public Matrix4f() {
		super();
		setIdentity();
	}
	
	public Matrix4f(GenericMatrix src) {
		super();
		set(src);
	}
	
	public Matrix4f(float m00, float m01, float m02, float m03, float m10, float m11, float m12,
			float m13, float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
		this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
		this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
		this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
		this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33;
	}
	
	@Override
	public Matrix4f setIdentity() {
		this.m00 = 1; this.m01 = 0; this.m02 = 0; this.m03 = 0;
		this.m10 = 0; this.m11 = 1; this.m12 = 0; this.m13 = 0;
		this.m20 = 0; this.m21 = 0; this.m22 = 1; this.m23 = 0;
		this.m30 = 0; this.m31 = 0; this.m32 = 0; this.m33 = 1;
		return this;
	}
	
	@Override
	public Matrix4f setZero() {
		this.m00 = 0; this.m01 = 0; this.m02 = 0; this.m03 = 0;
		this.m10 = 0; this.m11 = 0; this.m12 = 0; this.m13 = 0;
		this.m20 = 0; this.m21 = 0; this.m22 = 0; this.m23 = 0;
		this.m30 = 0; this.m31 = 0; this.m32 = 0; this.m33 = 0;
		return this;
	}
	
	@Override
	public Matrix4f set(GenericMatrix src) {
		float[][] buffer = src.read(4);
		this.m00 = buffer[0][0]; this.m01 = buffer[0][1]; this.m02 = buffer[0][2]; this.m03 = buffer[0][3];
		this.m10 = buffer[1][0]; this.m11 = buffer[1][1]; this.m12 = buffer[1][2]; this.m13 = buffer[1][3];
		this.m20 = buffer[2][0]; this.m21 = buffer[2][1]; this.m22 = buffer[2][2]; this.m23 = buffer[2][3];
		this.m30 = buffer[3][0]; this.m31 = buffer[3][1]; this.m32 = buffer[3][2]; this.m33 = buffer[3][3];
		return this;
	}
	
	@Override
	public Matrix4f negate() {
		this.m00 = -m00; this.m01 = -m01; this.m02 = -m02; this.m03 = -m03;
		this.m10 = -m10; this.m11 = -m11; this.m12 = -m12; this.m13 = -m13;
		this.m20 = -m20; this.m21 = -m21; this.m22 = -m22; this.m23 = -m23;
		this.m30 = -m30; this.m31 = -m31; this.m32 = -m32; this.m33 = -m33;
		return this;
	}
	
	@Override
	public Matrix4f transpose() {
		float m00 = this.m00;
		float m01 = this.m10;
		float m02 = this.m20;
		float m03 = this.m30;
		float m10 = this.m01;
		float m11 = this.m11;
		float m12 = this.m21;
		float m13 = this.m31;
		float m20 = this.m02;
		float m21 = this.m12;
		float m22 = this.m22;
		float m23 = this.m32;
		float m30 = this.m03;
		float m31 = this.m13;
		float m32 = this.m23;
		float m33 = this.m33;

		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m03 = m03;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m13 = m13;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
		this.m23 = m23;
		this.m30 = m30;
		this.m31 = m31;
		this.m32 = m32;
		this.m33 = m33;
		return this;
	}
	
	@Override
	public Matrix4f invert() {
		float determinant = determinant();
		if(determinant != 0) {
			float determinant_inv = 1f/determinant;
			
			float t00 =  determinant3x3(this.m11, this.m12, this.m13, this.m21, this.m22, this.m23, this.m31, this.m32, this.m33);
			float t01 = -determinant3x3(this.m10, this.m12, this.m13, this.m20, this.m22, this.m23, this.m30, this.m32, this.m33);
			float t02 =  determinant3x3(this.m10, this.m11, this.m13, this.m20, this.m21, this.m23, this.m30, this.m31, this.m33);
			float t03 = -determinant3x3(this.m10, this.m11, this.m12, this.m20, this.m21, this.m22, this.m30, this.m31, this.m32);
			
			float t10 = -determinant3x3(this.m01, this.m02, this.m03, this.m21, this.m22, this.m23, this.m31, this.m32, this.m33);
			float t11 =  determinant3x3(this.m00, this.m02, this.m03, this.m20, this.m22, this.m23, this.m30, this.m32, this.m33);
			float t12 = -determinant3x3(this.m00, this.m01, this.m03, this.m20, this.m21, this.m23, this.m30, this.m31, this.m33);
			float t13 =  determinant3x3(this.m00, this.m01, this.m02, this.m20, this.m21, this.m22, this.m30, this.m31, this.m32);
			
			float t20 =  determinant3x3(this.m01, this.m02, this.m03, this.m11, this.m12, this.m13, this.m31, this.m32, this.m33);
			float t21 = -determinant3x3(this.m00, this.m02, this.m03, this.m10, this.m12, this.m13, this.m30, this.m32, this.m33);
			float t22 =  determinant3x3(this.m00, this.m01, this.m03, this.m10, this.m11, this.m13, this.m30, this.m31, this.m33);
			float t23 = -determinant3x3(this.m00, this.m01, this.m02, this.m10, this.m11, this.m12, this.m30, this.m31, this.m32);
			
			float t30 = -determinant3x3(this.m01, this.m02, this.m03, this.m11, this.m12, this.m13, this.m21, this.m22, this.m23);
			float t31 =  determinant3x3(this.m00, this.m02, this.m03, this.m10, this.m12, this.m13, this.m20, this.m22, this.m23);
			float t32 = -determinant3x3(this.m00, this.m01, this.m03, this.m10, this.m11, this.m13, this.m20, this.m21, this.m23);
			float t33 =  determinant3x3(this.m00, this.m01, this.m02, this.m10, this.m11, this.m12, this.m20, this.m21, this.m22);

			this.m00 = t00*determinant_inv;
			this.m11 = t11*determinant_inv;
			this.m22 = t22*determinant_inv;
			this.m33 = t33*determinant_inv;
			this.m01 = t10*determinant_inv;
			this.m10 = t01*determinant_inv;
			this.m20 = t02*determinant_inv;
			this.m02 = t20*determinant_inv;
			this.m12 = t21*determinant_inv;
			this.m21 = t12*determinant_inv;
			this.m03 = t30*determinant_inv;
			this.m30 = t03*determinant_inv;
			this.m13 = t31*determinant_inv;
			this.m31 = t13*determinant_inv;
			this.m32 = t23*determinant_inv;
			this.m23 = t32*determinant_inv;
		}
		return this;
	}
	
	@Override
	public float determinant() {
		float f =
				m00 * ((m11 * m22 * m33 + m12 * m23 * m31 + m13 * m21 * m32)
					- m13 * m22 * m31
					- m11 * m23 * m32
					- m12 * m21 * m33);
			f -= m01 * ((m10 * m22 * m33 + m12 * m23 * m30 + m13 * m20 * m32)
					- m13 * m22 * m30
					- m10 * m23 * m32
					- m12 * m20 * m33);
			f += m02 * ((m10 * m21 * m33 + m11 * m23 * m30 + m13 * m20 * m31)
					- m13 * m21 * m30
					- m10 * m23 * m31
					- m11 * m20 * m33);
			f -= m03 * ((m10 * m21 * m32 + m11 * m22 * m30 + m12 * m20 * m31)
					- m12 * m21 * m30
					- m10 * m22 * m31
					- m11 * m20 * m32);
			return f;
	}
	
	private static float determinant3x3(float t00, float t01, float t02, float t10, float t11, float t12, float t20, float t21, float t22) {
		return t00 * (t11 * t22 - t12 * t21)
				+ t01 * (t12 * t20 - t10 * t22)
				+ t02 * (t10 * t21 - t11 * t20);
	}
	
	@Override
	protected float[][] readRaw() {
		return new float[][] {
			{m00, m01, m02, m03},
			{m10, m11, m12, m13},
			{m20, m21, m22, m23},
			{m30, m31, m32, m33}
		};
	}
	
	public Vector4f transform(Vector4f vec) {
		return transform(vec, null);
	}
	
	public Vector4f transform(Vector4f vec, Vector4f dest) {
		if(dest == null) dest = new Vector4f();
		
		float x = m00 * vec.x + m10 * vec.y + m20 * vec.z + m30 * vec.w;
		float y = m01 * vec.x + m11 * vec.y + m21 * vec.z + m31 * vec.w;
		float z = m02 * vec.x + m12 * vec.y + m22 * vec.z + m32 * vec.w;
		float w = m03 * vec.x + m13 * vec.y + m23 * vec.z + m33 * vec.w;
		
		dest.set(x, y, z, w);
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
	
	public static Matrix4f add(Matrix4f left, Matrix4f right) {
		return add(left, right, null);
	}
	
	public static Matrix4f add(Matrix4f left, Matrix4f right, Matrix4f dest) {
		if(dest == null) dest = new Matrix4f();

		dest.m00 = left.m00 + right.m00;
		dest.m01 = left.m01 + right.m01;
		dest.m02 = left.m02 + right.m02;
		dest.m03 = left.m03 + right.m03;
		dest.m10 = left.m10 + right.m10;
		dest.m11 = left.m11 + right.m11;
		dest.m12 = left.m12 + right.m12;
		dest.m13 = left.m13 + right.m13;
		dest.m20 = left.m20 + right.m20;
		dest.m21 = left.m21 + right.m21;
		dest.m22 = left.m22 + right.m22;
		dest.m23 = left.m23 + right.m23;
		dest.m30 = left.m30 + right.m30;
		dest.m31 = left.m31 + right.m31;
		dest.m32 = left.m32 + right.m32;
		dest.m33 = left.m33 + right.m33;

		return dest;
	}
	
	public static Matrix4f sub(Matrix4f left, Matrix4f right) {
		return sub(left, right, null);
	}
	
	public static Matrix4f sub(Matrix4f left, Matrix4f right, Matrix4f dest) {
		if(dest == null) dest = new Matrix4f();

		dest.m00 = left.m00 - right.m00;
		dest.m01 = left.m01 - right.m01;
		dest.m02 = left.m02 - right.m02;
		dest.m03 = left.m03 - right.m03;
		dest.m10 = left.m10 - right.m10;
		dest.m11 = left.m11 - right.m11;
		dest.m12 = left.m12 - right.m12;
		dest.m13 = left.m13 - right.m13;
		dest.m20 = left.m20 - right.m20;
		dest.m21 = left.m21 - right.m21;
		dest.m22 = left.m22 - right.m22;
		dest.m23 = left.m23 - right.m23;
		dest.m30 = left.m30 - right.m30;
		dest.m31 = left.m31 - right.m31;
		dest.m32 = left.m32 - right.m32;
		dest.m33 = left.m33 - right.m33;

		return dest;
	}
	
	public static Matrix4f mul(Matrix4f left, Matrix4f right) {
		return mul(left, right, null);
	}
	
	public static Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f dest) {
		if (dest == null) dest = new Matrix4f();

		float m00 = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03;
		float m01 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03;
		float m02 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03;
		float m03 = left.m03 * right.m00 + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03;
		float m10 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12 + left.m30 * right.m13;
		float m11 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12 + left.m31 * right.m13;
		float m12 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12 + left.m32 * right.m13;
		float m13 = left.m03 * right.m10 + left.m13 * right.m11 + left.m23 * right.m12 + left.m33 * right.m13;
		float m20 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22 + left.m30 * right.m23;
		float m21 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22 + left.m31 * right.m23;
		float m22 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22 + left.m32 * right.m23;
		float m23 = left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * right.m22 + left.m33 * right.m23;
		float m30 = left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * right.m33;
		float m31 = left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * right.m33;
		float m32 = left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * right.m33;
		float m33 = left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * right.m33;

		dest.m00 = m00;
		dest.m01 = m01;
		dest.m02 = m02;
		dest.m03 = m03;
		dest.m10 = m10;
		dest.m11 = m11;
		dest.m12 = m12;
		dest.m13 = m13;
		dest.m20 = m20;
		dest.m21 = m21;
		dest.m22 = m22;
		dest.m23 = m23;
		dest.m30 = m30;
		dest.m31 = m31;
		dest.m32 = m32;
		dest.m33 = m33;

		return dest;
	}
	
	public Matrix4f add(GenericMatrix src) {
		return add(this, new Matrix4f(src), this);
	}
	
	public Matrix4f sub(GenericMatrix src) {
		return sub(this, new Matrix4f(src), this);
	}
	
	public Matrix4f mul(GenericMatrix src) {
		return mul(this, new Matrix4f(src), this);
	}
	
	public Matrix4f translate(Vector3f vec) {
		this.m30 += this.m00 * vec.x + this.m10 * vec.y + this.m20 * vec.z;
		this.m31 += this.m01 * vec.x + this.m11 * vec.y + this.m21 * vec.z;
		this.m32 += this.m02 * vec.x + this.m12 * vec.y + this.m22 * vec.z;
		this.m33 += this.m03 * vec.x + this.m13 * vec.y + this.m23 * vec.z;
		return this;
	}
	
	public Matrix4f translate(Vector2f vec) {
		this.m30 += this.m00 * vec.x + this.m10 * vec.y;
		this.m31 += this.m01 * vec.x + this.m11 * vec.y;
		this.m32 += this.m02 * vec.x + this.m12 * vec.y;
		this.m33 += this.m03 * vec.x + this.m13 * vec.y;
		return this;
	}
	
	public Matrix4f scale(Vector3f vec) {
		this.m00 *= vec.x;
		this.m01 *= vec.x;
		this.m02 *= vec.x;
		this.m03 *= vec.x;
		this.m10 *= vec.y;
		this.m11 *= vec.y;
		this.m12 *= vec.y;
		this.m13 *= vec.y;
		this.m20 *= vec.z;
		this.m21 *= vec.z;
		this.m22 *= vec.z;
		this.m23 *= vec.z;
		return this;
	}
	
	public Matrix4f rotate(float angle, Vector3f axis) {
		float c = (float) Math.cos(angle);
		float s = (float) Math.sin(angle);
		float oneminusc = 1.0f - c;
		float xy = axis.x*axis.y;
		float yz = axis.y*axis.z;
		float xz = axis.x*axis.z;
		float xs = axis.x*s;
		float ys = axis.y*s;
		float zs = axis.z*s;
		
		float f00 = axis.x*axis.x*oneminusc+c;
		float f01 = xy*oneminusc+zs;
		float f02 = xz*oneminusc-ys;
		
		float f10 = xy*oneminusc-zs;
		float f11 = axis.y*axis.y*oneminusc+c;
		float f12 = yz*oneminusc+xs;
		
		float f20 = xz*oneminusc+ys;
		float f21 = yz*oneminusc-xs;
		float f22 = axis.z*axis.z*oneminusc+c;
		
		Matrix4f src = new Matrix4f(this);
		float t00 = src.m00 * f00 + src.m10 * f01 + src.m20 * f02;
		float t01 = src.m01 * f00 + src.m11 * f01 + src.m21 * f02;
		float t02 = src.m02 * f00 + src.m12 * f01 + src.m22 * f02;
		float t03 = src.m03 * f00 + src.m13 * f01 + src.m23 * f02;
		float t10 = src.m00 * f10 + src.m10 * f11 + src.m20 * f12;
		float t11 = src.m01 * f10 + src.m11 * f11 + src.m21 * f12;
		float t12 = src.m02 * f10 + src.m12 * f11 + src.m22 * f12;
		float t13 = src.m03 * f10 + src.m13 * f11 + src.m23 * f12;
		this.m20 = src.m00 * f20 + src.m10 * f21 + src.m20 * f22;
		this.m21 = src.m01 * f20 + src.m11 * f21 + src.m21 * f22;
		this.m22 = src.m02 * f20 + src.m12 * f21 + src.m22 * f22;
		this.m23 = src.m03 * f20 + src.m13 * f21 + src.m23 * f22;
		this.m00 = t00;
		this.m01 = t01;
		this.m02 = t02;
		this.m03 = t03;
		this.m10 = t10;
		this.m11 = t11;
		this.m12 = t12;
		this.m13 = t13;
		return this;
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
	
	public static Matrix4f fromEulerRotation(Vector3f euler) {
		Matrix4f matrix = new Matrix4f();
		matrix.rotate((float) Math.toRadians(euler.x), new Vector3f(1, 0, 0));
		matrix.rotate((float) Math.toRadians(euler.y), new Vector3f(0, 1, 0));
		matrix.rotate((float) Math.toRadians(euler.z), new Vector3f(0, 0, 1));
		return matrix;
	}
	
	public Matrix4f clone() {
		return new Matrix4f(this);
	}
	
}
