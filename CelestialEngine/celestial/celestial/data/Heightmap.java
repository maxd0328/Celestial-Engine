package celestial.data;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;

import celestial.error.CelestialGenericException;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;

public class Heightmap implements java.io.Serializable {
	
	private static final long serialVersionUID = 8176262289276779723L;
	
	private final String src;
	private final float[][] dataSrc;
	private final int width;
	private final int height;
	
	private Heightmap(String src, int width, int height) {
		if(width < 1 || height < 1) throw new CelestialGenericException("Heightmap algorithms require a width and height of at least 1");
		this.src = src;
		this.dataSrc = null;
		this.width = (int) Math.pow(2, width) + 1;
		this.height = (int) Math.pow(2, height) + 1;
	}
	
	private Heightmap(float[][] dataSrc, int width, int height) {
		if(width < 1 || height < 1) throw new CelestialGenericException("Heightmap algorithms require a width and height of at least 1");
		this.src = null;
		this.dataSrc = dataSrc;
		this.width = (int) Math.pow(2, width) + 1;
		this.height = (int) Math.pow(2, height) + 1;
	}
	
	public float[] getVertexArray(int lodIndex) {
		if(lodIndex < 0) throw new CelestialGenericException("LOD index must be positive");
		float[][] heights = src == null ? loadEmpty(lodIndex) : loadFromSource(src, lodIndex);
		int width = heights.length, height = heights[0].length;
		
		float[] vertices = new float[width * height * 3];
		
		for(int y = 0 ; y < height ; ++y) {
			for(int x = 0 ; x < width ; ++x) {
				vertices[(y * width + x) * 3] = ((float) x / (width - 1) * 2f - 1f) * (lodIndex + 1);
				vertices[(y * width + x) * 3 + 1] = heights[x][y];
				vertices[(y * width + x) * 3 + 2] = (float) y / (height - 1) * 2f - 1f * (lodIndex + 1);
			}
		}
		return vertices;
	}
	
	public float[] getTexCoordArray(int lodIndex) {
		if(lodIndex < 0) throw new CelestialGenericException("LOD index must be positive");
		float[][] heights = src == null ? loadEmpty(lodIndex) : loadFromSource(src, lodIndex);
		int width = heights.length, height = heights.length == 0 ? 0 : heights[0].length;
		
		float[] texCoords = new float[width * height * 2];
		
		for(int y = 0 ; y < height ; ++y) {
			for(int x = 0 ; x < width ; ++x) {
				texCoords[(y * width + x) * 2] = (float) x / ((float) width - 1);
				texCoords[(y * width + x) * 2 + 1] = (float) y / ((float) height - 1);
			}
		}
		return texCoords;
	}
	
	public float[] getNormalArray(int lodIndex) {
		if(lodIndex < 0) throw new CelestialGenericException("LOD index must be positive");
		float[][] heights = src == null ? loadEmpty(lodIndex) : loadFromSource(src, lodIndex);
		int width = heights.length, height = heights.length == 0 ? 0 : heights[0].length;
		
		float[] normals = new float[width * height * 3];
		
		for(int y = 0 ; y < height ; ++y) {
			for(int x = 0 ; x < width ; ++x) {
				float heightL = x == 0 ? heights[0][y] + (heights[0][y] - heights[1][y]) : heights[x - 1][y];
				float heightR = x == width - 1 ? heights[x][y] + (heights[x][y] - heights[x - 1][y]) : heights[x + 1][y];
				float heightD = y == 0 ? heights[x][0] + (heights[x][0] - heights[x][1]) : heights[x][y - 1];
				float heightU = y == height - 1 ? heights[x][y] + (heights[x][y] - heights[x][y - 1]) : heights[x][y + 1];
				
				Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
				normals[(y * width + x) * 3] = normal.x;
				normals[(y * width + x) * 3 + 1] = normal.y;
				normals[(y * width + x) * 3 + 2] = normal.z;
			}
		}
		return normals;
	}
	
	public float[] getTangentArray(int lodIndex) {
		if(lodIndex < 0) throw new CelestialGenericException("LOD index must be positive");
		float[][] heights = src == null ? loadEmpty(lodIndex) : loadFromSource(src, lodIndex);
		int width = heights.length, height = heights.length == 0 ? 0 : heights[0].length;
		
		float[] vertices = getVertexArray(lodIndex);
		float[] texCoords = getTexCoordArray(lodIndex);
		float[] tangents = new float[width * height * 3];
		ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		for(int y = 0 ; y < height ; ++y) for(int x = 0 ; x < width ; ++x) vertexList.add(new Vertex((y * width + x) * 3));
		
		for(int y = 0 ; y < height - 1 ; ++y) {
			for(int x = 0 ; x < width - 1 ; ++x) {
				int topLeft = (y * width) + x;
				int topRight = topLeft + 1;
				int bottomLeft = ((y + 1) * width) + x;
				int bottomRight = bottomLeft + 1;
				calculateTangents(getByIndex(vertexList, topLeft*3), getByIndex(vertexList, bottomLeft*3), getByIndex(vertexList, topRight*3), vertices, texCoords);
				calculateTangents(getByIndex(vertexList, topRight*3), getByIndex(vertexList, bottomLeft*3), getByIndex(vertexList, bottomRight*3), vertices, texCoords);
			}
		}
		
		for(Vertex v : vertexList) {
			v.averageTangents();
			tangents[v.index] = v.averagedTangent.x;
			tangents[v.index+1] = v.averagedTangent.y;
			tangents[v.index+2] = v.averagedTangent.z;
		}
		return tangents;
	}
	
	public int[] getIndexArray(int lodIndex) {
		if(lodIndex < 0) throw new CelestialGenericException("LOD index must be positive");
		float[][] heights = src == null ? loadEmpty(lodIndex) : loadFromSource(src, lodIndex);
		int width = heights.length, height = heights.length == 0 ? 0 : heights[0].length;
		
		int[] indices = new int[6 * (width - 1) * (height - 1)];
		
		int pointer = 0;
		for(int y = 0 ; y < height - 1 ; ++y) {
			for(int x = 0 ; x < width  - 1 ; ++x) {
				int topLeft = (y * width) + x;
				int topRight = topLeft + 1;
				int bottomLeft = ((y + 1) * width) + x;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return indices;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public float[][] getHeights(int lodIndex) {
		return src == null ? loadEmpty(lodIndex) : loadFromSource(src, lodIndex);
	}
	
	public VertexBuffer getVertexBuffer(boolean dynamic, int lodIndex) {
		if(src != null) return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC 
				: VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_HMAP, lodIndex, 0);
		else return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC : VertexBuffer.DRAW_TYPE_STATIC).allocatef(getVertexArray(lodIndex));
	}
	
	public VertexBuffer getTexCoordBuffer(boolean dynamic, int lodIndex) {
		if(src != null) return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC 
				: VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_HMAP, lodIndex, 1);
		else return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC : VertexBuffer.DRAW_TYPE_STATIC).allocatef(getTexCoordArray(lodIndex));
	}
	
	public VertexBuffer getNormalBuffer(boolean dynamic, int lodIndex) {
		if(src != null) return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC 
				: VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_HMAP, lodIndex, 2);
		else return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC : VertexBuffer.DRAW_TYPE_STATIC).allocatef(getNormalArray(lodIndex));
	}
	
	public VertexBuffer getTangentBuffer(boolean dynamic, int lodIndex) {
		if(src != null) return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC 
				: VertexBuffer.DRAW_TYPE_STATIC).allocatef(src, DataReader.DATA_FORMAT_HMAP, lodIndex, 3);
		else return VertexBuffer.create(3, dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC : VertexBuffer.DRAW_TYPE_STATIC).allocatef(getTangentArray(lodIndex));
	}
	
	public VertexBuffer getIndexBuffer(boolean dynamic, int lodIndex) {
		if(src != null) return VertexBuffer.createIndexBuffer(dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC 
				: VertexBuffer.DRAW_TYPE_STATIC).allocatei(src, DataReader.DATA_FORMAT_HMAP, lodIndex, 4);
		else return VertexBuffer.createIndexBuffer(dynamic ? VertexBuffer.DRAW_TYPE_DYNAMIC : VertexBuffer.DRAW_TYPE_STATIC).allocatei(getIndexArray(lodIndex));
	}
	
	private Vertex getByIndex(ArrayList<Vertex> list, int index) {
		for(Vertex v : list) {
			if(v.index == index) return v;
		}
		return null;
	}
	
	private void calculateTangents(Vertex v0, Vertex v1, Vertex v2, float[] vertices, float[] textureCoords) {
		Vector3f delatPos1 = Vector3f.sub(getPosition(v1, vertices), getPosition(v0, vertices), null);
		Vector3f delatPos2 = Vector3f.sub(getPosition(v2, vertices), getPosition(v0, vertices), null);
		Vector2f uv0 = new Vector2f(textureCoords[v0.index/3 * 2], textureCoords[v0.index/3 * 2 + 1]);
		Vector2f uv1 = new Vector2f(textureCoords[v1.index/3 * 2], textureCoords[v1.index/3 * 2 + 1]);
		Vector2f uv2 = new Vector2f(textureCoords[v2.index/3 * 2], textureCoords[v2.index/3 * 2 + 1]);
		Vector2f deltaUv1 = Vector2f.sub(uv1, uv0, null);
		Vector2f deltaUv2 = Vector2f.sub(uv2, uv0, null);

		float r = 1.0f / (deltaUv1.x * deltaUv2.y - deltaUv1.y * deltaUv2.x);
		delatPos1.scale(deltaUv2.y);
		delatPos2.scale(deltaUv1.y);
		Vector3f tangent = Vector3f.sub(delatPos1, delatPos2, null);
		tangent.scale(r);
		v0.addTangent(tangent);
		v1.addTangent(tangent);
		v2.addTangent(tangent);
	}
	
	private Vector3f getPosition(Vertex v, float[] vertices) {
		return new Vector3f(vertices[v.index], vertices[v.index+1], vertices[v.index+2]);
	}
	
	public static Heightmap createEmpty(int width, int height) {
		return new Heightmap((String) null, width, height);
	}
	
	public static Heightmap createFromSource(float[][] src, int width, int height) {
		return new Heightmap(src, width, height);
	}
	
	public static Heightmap createFromSource(String src) {
		return new Heightmap(src, 0, 0);
	}
	
	private float[][] loadEmpty(int lodIndex) {
		int width = this.width, height = this.height;
		int inc = 1;
		for(int i = 0 ; i < lodIndex ; ++i) {
			width -= width / 2;
			height -= height / 2;
			inc *= 2;
			if(width <= 1 || height <= 1) throw new CelestialGenericException("LOD index exceeded threshold");
		}
		if(dataSrc == null || dataSrc.length == 0 || dataSrc[0].length == 0) return new float[width][height];
		else {
			float[][] heights = new float[width][height];
			for(int x = 0 ; x < width ; ++x) {
				for(int y = 0 ; y < height ; ++y) {
					heights[x][y] = dataSrc[Math.min(x * inc, dataSrc.length - 1)][Math.min(y * inc, dataSrc[0].length - 1)];
				}
			}
			return heights;
		}
	}
	
	private float[][] loadFromSource(String src, int lodIndex) {
		try {
			byte[] bytes = Files.readAllBytes(new File(src).toPath());
			int pointer = 0;
			
			int width = (int) Math.pow(2, readInt(bytes, pointer++ * 4)) + 1;
			int height = (int) Math.pow(2, readInt(bytes, pointer++ * 4)) + 1;
			
			for(int i = 0 ; i < lodIndex ; ++i) {
				width -= width / 2;
				height -= height / 2;
				if(width <= 1 || height <= 1) throw new CelestialGenericException("LOD index exceeded threshold");
			}
			
			if(width < 3 || height < 3) throw new CelestialGenericException("Heightmap algorithms require a width and height of at least 1");
			
			float[][] heights = new float[width][height];
			
			for(int y = 0 ; y < height ; ++y) {
				for(int x = 0 ; x < width ; ++x) {
					heights[x][y] = readFloat(bytes, pointer++ * 4);
					pointer += Math.pow(2, lodIndex) - 1;
				}
				pointer += (Math.pow(2, lodIndex) - 1) * width;
			}
			return heights;
		}
		catch(IOException ex) {
			throw new CelestialGenericException("Error while reading VAO file");
		}
	}
	
	private int readInt(byte[] arr, int offset) {
		return ByteBuffer.wrap(new byte[] {arr[offset], arr[offset + 1], arr[offset + 2], arr[offset + 3]}).getInt();
	}
	
	private float readFloat(byte[] arr, int offset) {
		return ByteBuffer.wrap(new byte[] {arr[offset], arr[offset + 1], arr[offset + 2], arr[offset + 3]}).getFloat();
	}
	
	private static class Vertex implements java.io.Serializable {
		
		private static final long serialVersionUID = -4324951025809727L;
		
		public final int index;
		private final ArrayList<Vector3f> tangents;
		public Vector3f averagedTangent = new Vector3f(0,0,0);
		
		public Vertex(int index) {
			this.index = index;
			this.tangents = new ArrayList<Vector3f>();
		}
		
		public void addTangent(Vector3f tangent){
			tangents.add(tangent);
		}
		
		public void averageTangents() {
			if(tangents.isEmpty()){
				return;
			}
			for(Vector3f tangent : tangents){
				Vector3f.add(averagedTangent, tangent, averagedTangent);
			}
			averagedTangent.normalize();
		}
		
	}
	
}
