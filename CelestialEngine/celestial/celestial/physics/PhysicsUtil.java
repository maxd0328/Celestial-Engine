package celestial.physics;

import celestial.vecmath.Quat4f;
import celestial.vecmath.Matrix3f;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public final class PhysicsUtil {
	
	public static javax.vecmath.Vector2f toJavax(Vector2f v) {
		return new javax.vecmath.Vector2f(v.x, v.y);
	}
	
	public static javax.vecmath.Vector3f toJavax(Vector3f v) {
		return new javax.vecmath.Vector3f(v.x, v.y, v.z);
	}
	
	public static javax.vecmath.Vector4f toJavax(Vector4f v) {
		return new javax.vecmath.Vector4f(v.x, v.y, v.z, v.w);
	}
	
	public static javax.vecmath.Matrix3f toJavax(Matrix3f m) {
		javax.vecmath.Matrix3f mat = new javax.vecmath.Matrix3f();
		mat.m00 = m.m00; mat.m01 = m.m01; mat.m02 = m.m02;
		mat.m10 = m.m10; mat.m11 = m.m11; mat.m12 = m.m12;
		mat.m20 = m.m20; mat.m21 = m.m21; mat.m22 = m.m22;
		return mat;
	}
	
	public static javax.vecmath.Matrix4f toJavax(Matrix4f m) {
		javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
		mat.m00 = m.m00; mat.m01 = m.m01; mat.m02 = m.m02; mat.m03 = m.m03;
		mat.m10 = m.m10; mat.m11 = m.m11; mat.m12 = m.m12; mat.m13 = m.m13;
		mat.m20 = m.m20; mat.m21 = m.m21; mat.m22 = m.m22; mat.m23 = m.m23;
		mat.m30 = m.m30; mat.m31 = m.m31; mat.m32 = m.m32; mat.m33 = m.m33;
		return mat;
	}
	
	public static javax.vecmath.Quat4f toJavax(Quat4f q) {
		return new javax.vecmath.Quat4f(q.x, q.y, q.z, q.w);
	}
	
	public static Vector2f toNative(javax.vecmath.Vector2f v) {
		return new Vector2f(v.x, v.y);
	}
	
	public static Vector3f toNative(javax.vecmath.Vector3f v) {
		return new Vector3f(v.x, v.y, v.z);
	}
	
	public static Vector4f toNative(javax.vecmath.Vector4f v) {
		return new Vector4f(v.x, v.y, v.z, v.w);
	}
	
	public static Matrix3f toNative(javax.vecmath.Matrix3f m) {
		Matrix3f mat = new Matrix3f();
		mat.m00 = m.m00; mat.m01 = m.m01; mat.m02 = m.m02;
		mat.m10 = m.m10; mat.m11 = m.m11; mat.m12 = m.m12;
		mat.m20 = m.m20; mat.m21 = m.m21; mat.m22 = m.m22;
		return mat;
	}
	
	public static Matrix4f toNative(javax.vecmath.Matrix4f m) {
		Matrix4f mat = new Matrix4f();
		mat.m00 = m.m00; mat.m01 = m.m01; mat.m02 = m.m02; mat.m03 = m.m03;
		mat.m10 = m.m10; mat.m11 = m.m11; mat.m12 = m.m12; mat.m13 = m.m13;
		mat.m20 = m.m20; mat.m21 = m.m21; mat.m22 = m.m22; mat.m23 = m.m23;
		mat.m30 = m.m30; mat.m31 = m.m31; mat.m32 = m.m32; mat.m33 = m.m33;
		return mat;
	}
	
	public static Quat4f toNative(javax.vecmath.Quat4f q) {
		return new Quat4f(q.x, q.y, q.z, q.w);
	}
	
	public static Vector3f toEuler(Quat4f quat) {
		Matrix4f mat = quat.toRotationMatrix();
		float t1 = (float) Math.atan2(mat.m12, mat.m22);
		float c2 = (float) Math.sqrt(mat.m00 * mat.m00 + mat.m01 * mat.m01);
		float t2 = (float) Math.atan2(-mat.m02, c2);
		float s1 = (float) Math.sin(t1);
		float c1 = (float) Math.cos(t1);
		float t3 = (float) Math.atan2(s1 * mat.m20 - c1 * mat.m10, c1 * mat.m11 - s1 * mat.m21);
		return new Vector3f((float) Math.toDegrees(-t1), (float) Math.toDegrees(-t2), (float) Math.toDegrees(-t3));
	}
	
	public static Quat4f toQuaternion(Vector3f vec) {
		Matrix4f mat = new Matrix4f();
		mat.rotate((float) Math.toRadians(vec.x), new Vector3f(1, 0, 0));
		mat.rotate((float) Math.toRadians(vec.y), new Vector3f(0, 1, 0));
		mat.rotate((float) Math.toRadians(vec.z), new Vector3f(0, 0, 1));
		Quat4f quat = new Quat4f().set(mat);
		return quat;
	}
	
}
