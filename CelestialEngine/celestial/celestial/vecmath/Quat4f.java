package celestial.vecmath;

public final class Quat4f extends Vector<Quat4f> {
	
	private static final long serialVersionUID = 2059802252289524891L;
	
	private static final double EPS = 0.000001;
	
	public float x, y, z, w;
	
	public Quat4f() {
		super();
		setIdentity();
	}
	
	public Quat4f(float x, float y, float z, float w) {
		super();
		set(x, y, z, w);
	}
	
	public Quat4f(Vector3f axis, float ang) {
		this(axis.x, axis.y, axis.z, ang);
	}
	
	public Quat4f(GenericVector src) {
		super();
		set(src);
	}
	
	public Vector3f getAxis() {
		return new Vector3f(x, y, z);
	}
	
	public Quat4f setIdentity() {
		return set(0, 0, 0, 1);
	}
	
	public Quat4f set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Quat4f set(GenericVector src) {
		float[] buffer = src.read(4);
		return set(buffer[0], buffer[1], buffer[2], buffer[3]);
	}
	
	@Override
	public float lengthSquared() {
		return (x * x) + (y * y) + (z * z) + (w * w);
	}
	
	@Override
	public Quat4f negate() {
		this.x = -x;
		this.y = -y;
		this.z = -z;
		this.w = -w;
		return this;
	}
	
	@Override
	public Quat4f scale(float amount) {
		this.x *= amount;
		this.y *= amount;
		this.z *= amount;
		this.w *= amount;
		return this;
	}
	
	@Override
	protected float[] readRaw() {
		return new float[] {x, y, z, w};
	}
	
	public void invert() {
		float norm = 1.0f / (this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z);
		this.w *= norm;
		this.x *= -norm;
		this.y *= -norm;
		this.z *= -norm;
	}
	
	public float dot(float x, float y, float z, float w) {
		return (this.x * x) + (this.y * y) + (this.z * z) + (this.w * w);
	}
	
	public float dot(GenericVector src) {
		float[] buffer = src.read(4);
		return dot(buffer[0], buffer[1], buffer[2], buffer[3]);
	}
	
	public Quat4f set(float angle, Vector3f axis) {
		x = axis.x;
		y = axis.y;
		z = axis.z;
		float n = (float) Math.sqrt(x * x + y * y + z * z);
		if(n == 0) throw new VecmathException("Cannot divide by zero");
		float s = (float) (Math.sin(0.5 * angle) / n);
		x *= s;
		y *= s;
		z *= s;
		w = (float) Math.cos(0.5 * angle);
		return this;
	}
	
	public Quat4f set(GenericMatrix matrix) {
		Matrix3f m = new Matrix3f(matrix);
		m.transpose();
		
		float s;
		float tr = m.m00 + m.m11 + m.m22;
		if (tr >= 0.0) {
			s = (float) Math.sqrt(tr + 1.0);
			w = s * 0.5f;
			s = 0.5f / s;
			x = (m.m21 - m.m12) * s;
			y = (m.m02 - m.m20) * s;
			z = (m.m10 - m.m01) * s;
		} else {
			float max = Math.max(Math.max(m.m00, m.m11), m.m22);
			if (max == m.m00) {
				s = (float) Math.sqrt(m.m00 - (m.m11 + m.m22) + 1.0);
				x = s * 0.5f;
				s = 0.5f / s;
				y = (m.m01 + m.m10) * s;
				z = (m.m20 + m.m02) * s;
				w = (m.m21 - m.m12) * s;
			} else if (max == m.m11) {
				s = (float) Math.sqrt(m.m11 - (m.m22 + m.m00) + 1.0);
				y = s * 0.5f;
				s = 0.5f / s;
				z = (m.m12 + m.m21) * s;
				x = (m.m01 + m.m10) * s;
				w = (m.m02 - m.m20) * s;
			} else {
				s = (float) Math.sqrt(m.m22 - (m.m00 + m.m11) + 1.0);
				z = s * 0.5f;
				s = 0.5f / s;
				x = (m.m20 + m.m02) * s;
				y = (m.m12 + m.m21) * s;
				w = (m.m10 - m.m01) * s;
			}
		}
		return this;
	}
	
	public Quat4f conjugate() {
		this.x = -x;
		this.y = -y;
		this.z = -z;
		return this;
	}
	
	public static Quat4f mul(Quat4f left, Quat4f right, Quat4f dest) {
		if (dest == null) dest = new Quat4f();
		dest.set(left.x * right.w + left.w * right.x + left.y * right.z
				- left.z * right.y, left.y * right.w + left.w * right.y
				+ left.z * right.x - left.x * right.z, left.z * right.w
				+ left.w * right.z + left.x * right.y - left.y * right.x,
				left.w * right.w - left.x * right.x - left.y * right.y
				- left.z * right.z);
		return dest;
	}
	
	public static Quat4f mulInverse(Quat4f left, Quat4f right, Quat4f dest) {
		float n = right.lengthSquared();
		if(n == 0) throw new VecmathException("Cannot divide by zero");
		
		n = (n == 0.0 ? n : 1 / n);
		if (dest == null) dest = new Quat4f();
		dest
			.set((left.x * right.w - left.w * right.x - left.y
						* right.z + left.z * right.y)
					* n, (left.y * right.w - left.w * right.y - left.z
						* right.x + left.x * right.z)
					* n, (left.z * right.w - left.w * right.z - left.x
						* right.y + left.y * right.x)
					* n, (left.w * right.w + left.x * right.x + left.y
						* right.y + left.z * right.z)
					* n);

		return dest;
	}
	
	public Quat4f mul(Quat4f q) {
		return mul(this, q, this);
	}
	
	public Quat4f mulInverse(Quat4f q) {
		return mulInverse(this, q, this);
	}
	
	public static Quat4f interpolate(Quat4f left, Quat4f right, float alpha, Quat4f dest) {
		double dot,s1,s2,om,sinom;
		if(dest == null) dest = new Quat4f();
		
		dot = right.x*left.x + right.y*left.y + right.z*left.z + right.w*left.w;
		
		if (dot < 0) {
			left.x = -left.x;  left.y = -left.y;  left.z = -left.z;  left.w = -left.w;
			dot = -dot;
		}

		if ( (1.0 - dot) > EPS) {
			om = Math.acos(dot);
			sinom = Math.sin(om);
			s1 = Math.sin((1.0-alpha)*om)/sinom;
			s2 = Math.sin( alpha*om)/sinom;
		}
		else {
			s1 = 1.0 - alpha;
			s2 = alpha;
		}
		float w = (float)(s1*left.w + s2*right.w);
		float x = (float)(s1*left.x + s2*right.x);
		float y = (float)(s1*left.y + s2*right.y);
		float z = (float)(s1*left.z + s2*right.z);
		dest.set(x, y, z, w);
		return dest;
	}
	
	public Quat4f interpolate(Quat4f q, float alpha) {
		return interpolate(this, q, alpha, this);
	}
	
	public Matrix3f toRotation3Matrix() {
		Matrix3f matrix = new Matrix3f();
		
		matrix.m00 = (1.0f - 2.0f*this.y*this.y - 2.0f*this.z*this.z);
		matrix.m10 = (2.0f*(this.x*this.y + this.w*this.z));
		matrix.m20 = (2.0f*(this.x*this.z - this.w*this.y));
		
		matrix.m01 = (2.0f*(this.x*this.y - this.w*this.z));
		matrix.m11 = (1.0f - 2.0f*this.x*this.x - 2.0f*this.z*this.z);
		matrix.m21 = (2.0f*(this.y*this.z + this.w*this.x));
		
		matrix.m02 = (2.0f*(this.x*this.z + this.w*this.y));
		matrix.m12 = (2.0f*(this.y*this.z - this.w*this.x));
		matrix.m22 = (1.0f - 2.0f*this.x*this.x - 2.0f*this.y*this.y);
		
		return matrix;
	}
	
	public Matrix4f toRotationMatrix() {
		Matrix4f matrix = new Matrix4f();
		
		matrix.m00 = (1.0f - 2.0f*this.y*this.y - 2.0f*this.z*this.z);
		matrix.m10 = (2.0f*(this.x*this.y + this.w*this.z));
		matrix.m20 = (2.0f*(this.x*this.z - this.w*this.y));
		
		matrix.m01 = (2.0f*(this.x*this.y - this.w*this.z));
		matrix.m11 = (1.0f - 2.0f*this.x*this.x - 2.0f*this.z*this.z);
		matrix.m21 = (2.0f*(this.y*this.z + this.w*this.x));
		
		matrix.m02 = (2.0f*(this.x*this.z + this.w*this.y));
		matrix.m12 = (2.0f*(this.y*this.z - this.w*this.x));
		matrix.m22 = (1.0f - 2.0f*this.x*this.x - 2.0f*this.y*this.y);
		
		matrix.m03 = (float) 0.0;
		matrix.m13 = (float) 0.0;
		matrix.m23 = (float) 0.0;
		
		matrix.m30 = (float) 0.0;
		matrix.m31 = (float) 0.0;
		matrix.m32 = (float) 0.0;
		matrix.m33 = (float) 1.0;
		
		return matrix;
	}
	
	@Override
	public Quat4f clone() {
		return new Quat4f(this);
	}
	
}
