package celestial.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import celestial.error.CelestialGenericException;
import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;

public final class DataReader {
	
	public static final int DATA_FORMAT_OBJ  = 0x0;
	public static final int DATA_FORMAT_VAO  = 0x1;
	public static final int DATA_FORMAT_HMAP = 0x2;
	
	public static float[] readf(String src, int format, int lodIndex, int attrib) {
		if(format == DATA_FORMAT_OBJ) return readOBJ(src).getFloatArray()[attrib];
		else if(format == DATA_FORMAT_VAO) return toFloatArray(readVAO(src, lodIndex, attrib));
		else if(format == DATA_FORMAT_HMAP) return getHMapFloat(src, lodIndex, attrib);
		else throw new CelestialGenericException("Invalid format");
	}
	
	public static int[] readi(String src, int format, int lodIndex, int attrib) {
		if(format == DATA_FORMAT_OBJ) return readOBJ(src).getIntArray()[attrib];
		else if(format == DATA_FORMAT_VAO) return toIntArray(readVAO(src, lodIndex, attrib));
		else if(format == DATA_FORMAT_HMAP) return getHMapInt(src, lodIndex, attrib);
		else throw new CelestialGenericException("Invalid format");
	}
	
	private static float[] toFloatArray(byte[] arr) {
		float[] newArr = new float[arr.length / 4];
		for(int i = 0 ; i < newArr.length ; ++i) {
			byte[] byteArr = {arr[i * 4], arr[i * 4 + 1], arr[i * 4 + 2], arr[i * 4 + 3]};
			newArr[i] = ByteBuffer.wrap(byteArr).getFloat();
		}
		return newArr;
	}
	
	private static int[] toIntArray(byte[] arr) {
		int[] newArr = new int[arr.length / 4];
		for(int i = 0 ; i < newArr.length ; ++i) {
			byte[] byteArr = {arr[i * 4], arr[i * 4 + 1], arr[i * 4 + 2], arr[i * 4 + 3]};
			newArr[i] = ByteBuffer.wrap(byteArr).getInt();
		}
		return newArr;
	}
	
	private static float[] getHMapFloat(String src, int lodIndex, int attrib) {
		Heightmap map = Heightmap.createFromSource(src);
		switch(attrib) {
		case 0: return map.getVertexArray(lodIndex);
		case 1: return map.getTexCoordArray(lodIndex);
		case 2: return map.getNormalArray(lodIndex);
		case 3: return map.getTangentArray(lodIndex);
		default: throw new CelestialGenericException("Invalid attribute");
		}
	}
	
	
	private static int[] getHMapInt(String src, int lodIndex, int attrib) {
		Heightmap map = Heightmap.createFromSource(src);
		switch(attrib) {
		case 4: return map.getIndexArray(lodIndex);
		default: throw new CelestialGenericException("Invalid attribute");
		}
	}
	
	private static OBJData readOBJ(String src) {
		try {
			FileReader isr = null;
			File objFile = new File(src);
			isr = new FileReader(objFile);
			BufferedReader reader = new BufferedReader(isr);
			String line;
			List<Vertex> vertices = new ArrayList<Vertex>();
			List<Vector2f> textures = new ArrayList<Vector2f>();
			List<Vector3f> normals = new ArrayList<Vector3f>();
			List<Integer> indices = new ArrayList<Integer>();
			
			while (true) {
				line = reader.readLine();
				if (line.startsWith("v ")) {
					String[] currentLine = line.split(" ");
					Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]),
							(float) Float.valueOf(currentLine[3]));
					Vertex newVertex = new Vertex(vertices.size(), vertex);
					vertices.add(newVertex);

				} else if (line.startsWith("vt ")) {
					String[] currentLine = line.split(" ");
					Vector2f texture = new Vector2f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]));
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					String[] currentLine = line.split(" ");
					Vector3f normal = new Vector3f((float) Float.valueOf(currentLine[1]),
							(float) Float.valueOf(currentLine[2]),
							(float) Float.valueOf(currentLine[3]));
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					break;
				}
			}
			while (line != null && line.startsWith("f ")) {
				String[] currentLine = line.split(" ");
				String[] vertex1 = getVertexData(currentLine[1]);
				String[] vertex2 = getVertexData(currentLine[2]);
				String[] vertex3 = getVertexData(currentLine[3]);
				Vertex v0 = processVertex(vertex1, vertices, indices);
				Vertex v1 = processVertex(vertex2, vertices, indices);
				Vertex v2 = processVertex(vertex3, vertices, indices);
				calculateTangents(v0, v1, v2, textures);
				line = reader.readLine();
			}
			reader.close();
			removeUnusedVertices(vertices);
			float[] verticesArray = new float[vertices.size() * 3];
			float[] texturesArray = new float[vertices.size() * 2];
			float[] normalsArray = new float[vertices.size() * 3];
			float[] tangentsArray = new float[vertices.size() * 3];
			convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray, tangentsArray);
			int[] indicesArray = convertIndicesListToArray(indices);
			
			OBJData data = new OBJData();
			data.vertices = verticesArray;
			data.textureCoords = texturesArray;
			data.normals = normalsArray;
			data.tangents = tangentsArray;
			data.indices = indicesArray;
			
			return data;
		}
		catch(IOException ex) {
			throw new CelestialGenericException("Error reading OBJ file");
		}
	}
	
	private static String[] getVertexData(String vertex) {
		if(vertex.contains("/"))
			return vertex.split("/");
		else return new String[] {vertex, "0", "0"};
	}
	
	private static void calculateTangents(Vertex v0, Vertex v1, Vertex v2,
			List<Vector2f> textures) {
		if(textures.size() == 0)
			return;
		Vector3f delatPos1 = Vector3f.sub(v1.position, v0.position, null);
		Vector3f delatPos2 = Vector3f.sub(v2.position, v0.position, null);
		Vector2f uv0 = textures.get(v0.textureIndex);
		Vector2f uv1 = textures.get(v1.textureIndex);
		Vector2f uv2 = textures.get(v2.textureIndex);
		Vector2f deltaUv1 = Vector2f.sub(uv1, uv0, null);
		Vector2f deltaUv2 = Vector2f.sub(uv2, uv0, null);

		float r = 1.0f / (deltaUv1.x * deltaUv2.y - deltaUv1.y * deltaUv2.x);
		delatPos1.scale(deltaUv2.y);
		delatPos2.scale(deltaUv1.y);
		Vector3f tangent = Vector3f.sub(delatPos1, delatPos2, null);
		tangent.scale(r);
		v0.tangents.add(tangent);
		v1.tangents.add(tangent);
		v2.tangents.add(tangent);
	}

	private static Vertex processVertex(String[] vertex, List<Vertex> vertices,
			List<Integer> indices) {
		int index = Integer.parseInt(vertex[0]) - 1;
		Vertex currentVertex = vertices.get(index);
		int textureIndex = Integer.parseInt(vertex[1]) - 1;
		int normalIndex = Integer.parseInt(vertex[2]) - 1;
		if (!currentVertex.isSet()) {
			currentVertex.textureIndex = textureIndex;
			currentVertex.normalIndex = normalIndex;
			indices.add(index);
			return currentVertex;
		} else {
			return dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
		}
	}

	private static int[] convertIndicesListToArray(List<Integer> indices) {
		int[] indicesArray = new int[indices.size()];
		for (int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}

	private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
			List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
			float[] normalsArray, float[] tangentsArray) {
		float furthestPoint = 0;
		for (int i = 0; i < vertices.size(); i++) {
			Vertex currentVertex = vertices.get(i);
			if (currentVertex.length > furthestPoint) {
				furthestPoint = currentVertex.length;
			}
			Vector3f position = currentVertex.position;
			Vector2f textureCoord = textures.size() == 0 ? new Vector2f() : textures.get(currentVertex.textureIndex);
			Vector3f normalVector = normals.size() == 0 ? new Vector3f() : normals.get(currentVertex.normalIndex);
			Vector3f tangent = currentVertex.averagedTangent;
			verticesArray[i * 3] = position.x;
			verticesArray[i * 3 + 1] = position.y;
			verticesArray[i * 3 + 2] = position.z;
			texturesArray[i * 2] = textureCoord.x;
			texturesArray[i * 2 + 1] = 1 - textureCoord.y;
			normalsArray[i * 3] = normalVector.x;
			normalsArray[i * 3 + 1] = normalVector.y;
			normalsArray[i * 3 + 2] = normalVector.z;
			tangentsArray[i * 3] = tangent.x;
			tangentsArray[i * 3 + 1] = tangent.y;
			tangentsArray[i * 3 + 2] = tangent.z;
		}
		return furthestPoint;
	}

	private static Vertex dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex,
			int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
		if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			indices.add(previousVertex.index);
			return previousVertex;
		} else {
			Vertex anotherVertex = previousVertex.duplicateVertex;
			if (anotherVertex != null) {
				return dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex,
						newNormalIndex, indices, vertices);
			} else {
				Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.position);
				duplicateVertex.textureIndex = newTextureIndex;
				duplicateVertex.normalIndex = newNormalIndex;
				previousVertex.duplicateVertex = duplicateVertex;
				vertices.add(duplicateVertex);
				indices.add(duplicateVertex.index);
				return duplicateVertex;
			}

		}
	}

	private static void removeUnusedVertices(List<Vertex> vertices) {
		for (Vertex vertex : vertices) {
			vertex.averageTangents();
			if (!vertex.isSet()) {
				vertex.textureIndex = 0;
				vertex.normalIndex = 0;
			}
		}
	}
	
	private static byte[] readVAO(String src, int lodIndex, int attrib) {
		try {
			byte[] bytes = Files.readAllBytes(new File(src).toPath());
			int pointer = 0, relLocation = 0, vaoCount = readInt(bytes, pointer++ * 4);
			
			ArrayList<VBODescriptor> descriptors = new ArrayList<VBODescriptor>();
			
			for(int i = 0 ; i < vaoCount ; ++i) {
				int vboCount = readInt(bytes, pointer++ * 4);
				
				for(int j = 0 ; j < vboCount ; ++j) {
					int length = readInt(bytes, pointer++ * 4);
					descriptors.add(new VBODescriptor(i, j, relLocation, length));
					relLocation += length;
				}
			}
			
			for(VBODescriptor desc : descriptors) {
				if(desc.vaoID == lodIndex && desc.vboID == attrib) {
					byte[] output = new byte[desc.length];
					for(int i = 0 ; i < output.length ; ++i) output[i] = bytes[desc.location + pointer * 4 + i];
					return output;
				}
			}
			return null;
		}
		catch(IOException ex) {
			throw new CelestialGenericException("Error while reading VAO file");
		}
	}
	
	private static int readInt(byte[] arr, int offset) {
		return ByteBuffer.wrap(new byte[] {arr[offset], arr[offset + 1], arr[offset + 2], arr[offset + 3]}).getInt();
	}
	
	private static class OBJData {
		
		private float[] vertices;
		private float[] textureCoords;
		private float[] normals;
		private float[] tangents;
		private int[] indices;
		
		private float[][] getFloatArray() {
			return new float[][] {vertices, textureCoords, normals, tangents, null};
		}
		
		private int[][] getIntArray() {
			return new int[][] {null, null, null, null, indices};
		}
		
	}
	
	private static class Vertex {
		
		private static final int NO_INDEX = -1;
		
		private Vector3f position;
		private int textureIndex = NO_INDEX;
		private int normalIndex = NO_INDEX;
		private Vertex duplicateVertex = null;
		private int index;
		private float length;
		private List<Vector3f> tangents = new ArrayList<Vector3f>();
		private Vector3f averagedTangent = new Vector3f();
		
		Vertex(int index,Vector3f position){
			this.index = index;
			this.position = position;
			this.length = position.length();
		}
		
		private void averageTangents(){
			if(tangents.isEmpty()){
				return;
			}
			for(Vector3f tangent : tangents){
				Vector3f.add(averagedTangent, tangent, averagedTangent);
			}
			averagedTangent.normalize();
		}
		
		public boolean isSet(){
			return textureIndex != NO_INDEX && normalIndex != NO_INDEX;
		}
		
		public boolean hasSameTextureAndNormal(int textureIndexOther, int normalIndexOther){
			return textureIndexOther==textureIndex && normalIndexOther==normalIndex;
		}

	}
	
	private static class VBODescriptor {
		
		private int vaoID;
		private int vboID;
		
		private int location;
		private int length;
		
		private VBODescriptor(int vaoID, int vboID, int location, int length) {
			this.vaoID = vaoID;
			this.vboID = vboID;
			this.location = location;
			this.length = length;
		}
		
	}
	
}
