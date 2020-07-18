package celestial.physics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.util.ObjectArrayList;
import celestial.data.DataReader;
import celestial.data.Heightmap;
import celestial.vecmath.Vector3f;

public final class CollisionMesh {
	
	private final CollisionShape shape;
	private boolean inertiaChanged;
	
	private CollisionMesh(CollisionShape shape) {
		this.shape = shape;
		this.inertiaChanged = false;
	}
	
	public CollisionShape getShape() {
		return shape;
	}
	
	public boolean isInertiaChanged() {
		return inertiaChanged;
	}
	
	public void setInertiaChanged(boolean inertiaChanged) {
		this.inertiaChanged = inertiaChanged;
	}
	
	public Vector3f getScaling() {
		return PhysicsUtil.toNative(shape.getLocalScaling(new javax.vecmath.Vector3f()));
	}
	
	public void setScaling(Vector3f scaling) {
		if(getScaling().equals(scaling)) return;
		this.shape.setLocalScaling(PhysicsUtil.toJavax(scaling));
		this.inertiaChanged = true;
	}
	
	public static CollisionMesh createSphere(float radius) {
		ObjectArrayList<javax.vecmath.Vector3f> vertices = new ObjectArrayList<javax.vecmath.Vector3f>();
		float[] data = DataReader.readf("C:\\Celestial\\resources\\sphere.obj", DataReader.DATA_FORMAT_OBJ, 0, 0);
		for(int i = 2 ; i < data.length ; i += 3) vertices.add(new javax.vecmath.Vector3f(data[i - 2] * radius, data[i - 1] * radius, data[i] * radius));
		return new CollisionMesh(new ConvexHullShape(vertices));
	}
	
	public static CollisionMesh createBox(Vector3f scaling) {
		return new CollisionMesh(new BoxShape(PhysicsUtil.toJavax(scaling)));
	}
	
	public static CollisionMesh createCylinder(Vector3f scaling) {
		ObjectArrayList<javax.vecmath.Vector3f> vertices = new ObjectArrayList<javax.vecmath.Vector3f>();
		float[] data = DataReader.readf("C:\\Celestial\\resources\\cylinder.obj", DataReader.DATA_FORMAT_OBJ, 0, 0);
		for(int i = 2 ; i < data.length ; i += 3) vertices.add(new javax.vecmath.Vector3f(data[i - 2] * scaling.x, data[i - 1] * scaling.y, data[i] * scaling.z));
		return new CollisionMesh(new ConvexHullShape(vertices));
	}
	
	public static CollisionMesh createCone(float radius, float height) {
		ObjectArrayList<javax.vecmath.Vector3f> vertices = new ObjectArrayList<javax.vecmath.Vector3f>();
		float[] data = DataReader.readf("C:\\Celestial\\resources\\cone.obj", DataReader.DATA_FORMAT_OBJ, 0, 0);
		for(int i = 2 ; i < data.length ; i += 3) vertices.add(new javax.vecmath.Vector3f(data[i - 2] * radius, data[i - 1] * height * 0.5f, data[i] * radius));
		return new CollisionMesh(new ConvexHullShape(vertices));
	}
	
	public static CollisionMesh createConvexHull(float[] data) {
		ObjectArrayList<javax.vecmath.Vector3f> vertices = new ObjectArrayList<javax.vecmath.Vector3f>();
		for(int i = 2 ; i < data.length ; i += 3) vertices.add(new javax.vecmath.Vector3f(data[i - 2], data[i - 1], data[i]));
		return new CollisionMesh(new ConvexHullShape(vertices));
	}
	
	public static CollisionMesh createBvh(float[] vertices, int[] indices) {
		if(indices.length > 0) {
			IndexedMesh mesh = new IndexedMesh();
			mesh.numTriangles = indices.length / 3;
			mesh.triangleIndexBase = ByteBuffer.allocateDirect(indices.length * Integer.BYTES).order(ByteOrder.nativeOrder());
			mesh.triangleIndexBase.rewind();
			mesh.triangleIndexBase.asIntBuffer().put(indices);
			mesh.triangleIndexStride = 3 * Integer.BYTES;
			mesh.numVertices = vertices.length / 3;
			mesh.vertexBase = ByteBuffer.allocateDirect(vertices.length * Float.BYTES).order(ByteOrder.nativeOrder());
			mesh.vertexBase.rewind();
			mesh.vertexBase.asFloatBuffer().put(vertices);
			mesh.vertexStride = 3 * Float.BYTES;
			
			TriangleIndexVertexArray arr = new TriangleIndexVertexArray();
			arr.addIndexedMesh(mesh);
			
			CollisionShape shape = new BvhTriangleMeshShape(arr, true);
			return new CollisionMesh(shape);
		}
		return new CollisionMesh(new BoxShape(new javax.vecmath.Vector3f(0, 0, 0)));
	}
	
	public static CollisionMesh createHeightmap(Heightmap heightmap) {
		int vertCountX = heightmap.getWidth(), vertCountY = heightmap.getHeight();
		int totalVerts = vertCountX * vertCountY, totalTriangles = 2 * (vertCountX - 1) * (vertCountY - 1);
		
		ByteBuffer geometry = ByteBuffer.allocateDirect(totalVerts * 3 * 4).order(ByteOrder.nativeOrder());
		ByteBuffer indices = ByteBuffer.allocateDirect(totalTriangles * 3 * 4).order(ByteOrder.nativeOrder());
		
		float[][] height = heightmap.getHeights(0);
		for(int iX = 0 ; iX < vertCountX ; ++iX) {
			for(int iY = 0 ; iY < vertCountY ; ++iY) {
				float dX = ((float) iX / (float) (vertCountX - 1)), dY = verifyHeight(height[iX][iY]), dZ = ((float) iY / (float) (vertCountY - 1));
				int index = (iX + iY * vertCountX) * 3;
				geometry.putFloat((index + 0) * 4, dX);
				geometry.putFloat((index + 1) * 4, dY);
				geometry.putFloat((index + 2) * 4, dZ);
			}
		}
		indices.clear();
		for(int iX = 0 ; iX < vertCountX - 1 ; ++iX) {
			for(int iY = 0 ; iY < vertCountY - 1 ; ++iY) {
				indices.putInt(iY * vertCountX + iX);
				indices.putInt(iY * vertCountX + iX + 1);
				indices.putInt((iY + 1) * vertCountX + iX + 1);
				
				indices.putInt(iY * vertCountX + iX);
				indices.putInt((iY + 1) * vertCountX + iX + 1);
				indices.putInt((iY + 1) * vertCountX + iX);
			}
		}
		indices.flip();
		
		TriangleIndexVertexArray arr = new TriangleIndexVertexArray(totalTriangles, indices, 3 * 4, totalVerts, geometry, 3 * 4);
		BvhTriangleMeshShape bvh = new BvhTriangleMeshShape(arr, true);
		CollisionShape shape = bvh;
		
		return new CollisionMesh(shape);
	}
	
	private static float verifyHeight(float height) {
		return height == 0 ? 0.1f : height;
	}
	
}
