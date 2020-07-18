package celestial.shader;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import celestial.vecmath.Matrix2f;
import celestial.vecmath.Matrix3f;
import celestial.vecmath.Matrix4f;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;

public abstract class Uniform<T> {
	
	private final Class<T> ctype;
	private final int location;
	private T currentValue = null;
	
	Uniform(String identifier, int programID, Class<T> ctype) {
		this.ctype = ctype;
		this.location = GL20.glGetUniformLocation(programID, identifier);
		if(location == -1) System.out.println("CelestialGLWarning: Uniform value \'" + identifier + "\' was not loaded properly");
	}
	
	protected final int getLocation() {
		return location;
	}
	
	protected Class<T> getCtype() {
		return ctype;
	}
	
	protected T getValue() {
		return currentValue;
	}
	
	protected final void load(T value) {
		if(currentValue == null || !currentValue.equals(value)) {
			absLoad(value);
			currentValue = value;
		}
	}
	
	protected abstract void absLoad(T value);
	
	public abstract int componentCount();
	
	protected static Uniform<Integer> bindInteger(String identifier, int programID) {
		return new Uniform<Integer>(identifier, programID, Integer.class) {
			protected void absLoad(Integer value) {
				GL20.glUniform1i(super.getLocation(), value);
			}
			
			public int componentCount() {
				return 1;
			}
		};
	}
	
	protected static Uniform<Float> bindFloat(String identifier, int programID) {
		return new Uniform<Float>(identifier, programID, Float.class) {
			protected void absLoad(Float value) {
				GL20.glUniform1f(super.getLocation(), value);
			}
			
			public int componentCount() {
				return 1;
			}
		};
	}
	
	protected static Uniform<Vector2f> bindVec2(String identifier, int programID) {
		return new Uniform<Vector2f>(identifier, programID, Vector2f.class) {
			protected void absLoad(Vector2f value) {
				GL20.glUniform2f(super.getLocation(), value.x, value.y);
			}
			
			public int componentCount() {
				return 2;
			}
		};
	}
	
	protected static Uniform<Vector3f> bindVec3(String identifier, int programID) {
		return new Uniform<Vector3f>(identifier, programID, Vector3f.class) {
			protected void absLoad(Vector3f value) {
				GL20.glUniform3f(super.getLocation(), value.x, value.y, value.z);
			}
			
			public int componentCount() {
				return 3;
			}
		};
	}
	
	protected static Uniform<Vector4f> bindVec4(String identifier, int programID) {
		return new Uniform<Vector4f>(identifier, programID, Vector4f.class) {
			protected void absLoad(Vector4f value) {
				GL20.glUniform4f(super.getLocation(), value.x, value.y, value.z, value.w);
			}
			
			public int componentCount() {
				return 4;
			}
		};
	}
	
	private static final FloatBuffer BUFFER_MAT2 = BufferUtils.createFloatBuffer(2 * 2),
			BUFFER_MAT3 = BufferUtils.createFloatBuffer(3 * 3), BUFFER_MAT4 = BufferUtils.createFloatBuffer(4 * 4);
	
	protected static Uniform<Matrix2f> bindMat2(String identifier, int programID) {
		return new Uniform<Matrix2f>(identifier, programID, Matrix2f.class) {
			protected void absLoad(Matrix2f value) {
				value.store(BUFFER_MAT2);
				BUFFER_MAT2.flip();
				GL20.glUniformMatrix2(super.getLocation(), false, BUFFER_MAT2);
			}
			
			public int componentCount() {
				return 4;
			}
		};
	}
	
	protected static Uniform<Matrix3f> bindMat3(String identifier, int programID) {
		return new Uniform<Matrix3f>(identifier, programID, Matrix3f.class) {
			protected void absLoad(Matrix3f value) {
				value.store(BUFFER_MAT3);
				BUFFER_MAT3.flip();
				GL20.glUniformMatrix3(super.getLocation(), false, BUFFER_MAT3);
			}
			
			public int componentCount() {
				return 9;
			}
		};
	}
	
	protected static Uniform<Matrix4f> bindMat4(String identifier, int programID) {
		return new Uniform<Matrix4f>(identifier, programID, Matrix4f.class) {
			protected void absLoad(Matrix4f value) {
				value.store(BUFFER_MAT4);
				BUFFER_MAT4.flip();
				GL20.glUniformMatrix4(super.getLocation(), false, BUFFER_MAT4);
			}
			
			public int componentCount() {
				return 16;
			}
		};
	}
	
}
