package celestial.data;

public final class DataUtil {
	
	public static VertexArray fromOBJ(String src, int drawType) {
		return VertexArray.create(VertexBuffer.createIndexBuffer(drawType).allocatei(src, DataReader.DATA_FORMAT_OBJ, 0, 4),
				VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 0),
				VertexBuffer.create(2, drawType).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 1),
				VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 2),
				VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_OBJ, 0, 3));
	}
	
	public static VertexArray[] fromVAO(String src, int drawType, int lodCount) {
		VertexArray[] arr = new VertexArray[lodCount];
		for(int i = 0 ; i < lodCount ; ++i) {
			arr[i] = VertexArray.create(VertexBuffer.createIndexBuffer(drawType).allocatei(src, DataReader.DATA_FORMAT_VAO, i, 4),
					VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_VAO, i, 0),
					VertexBuffer.create(2, drawType).allocatef(src, DataReader.DATA_FORMAT_VAO, i, 1),
					VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_VAO, i, 2),
					VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_VAO, i, 3));
		}
		return arr;
	}
	
	public static VertexArray[] fromHMAP(String src, int drawType, int lodCount) {
		VertexArray[] arr = new VertexArray[lodCount];
		for(int i = 0 ; i < lodCount ; ++i) {
			arr[i] = VertexArray.create(VertexBuffer.createIndexBuffer(drawType).allocatei(src, DataReader.DATA_FORMAT_HMAP, i, 4),
					VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_HMAP, i, 0),
					VertexBuffer.create(2, drawType).allocatef(src, DataReader.DATA_FORMAT_HMAP, i, 1),
					VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_HMAP, i, 2),
					VertexBuffer.create(3, drawType).allocatef(src, DataReader.DATA_FORMAT_HMAP, i, 3));
		}
		return arr;
	}
	
	public static VertexArray[] newVAO(int drawType, int lodCount, int indexCount, int vertexCount) {
		VertexArray[] arr = new VertexArray[lodCount];
		for(int i = 0 ; i < lodCount ; ++i) {
			arr[i] = VertexArray.create(VertexBuffer.createIndexBuffer(drawType).allocatei(new int[indexCount]),
					VertexBuffer.create(3, drawType).allocatef(new float[vertexCount * 3]),
					VertexBuffer.create(2, drawType).allocatef(new float[vertexCount * 2]),
					VertexBuffer.create(3, drawType).allocatef(new float[vertexCount * 3]),
					VertexBuffer.create(3, drawType).allocatef(new float[vertexCount * 3]));
		}
		return arr;
	}
	
	public static VertexArray[] newHMAP(int drawType, int lodCount, int width, int height) {
		VertexArray[] arr = new VertexArray[lodCount];
		Heightmap map = Heightmap.createEmpty(width, height);
		for(int i = 0 ; i < lodCount ; ++i) {
			arr[i] = VertexArray.create(VertexBuffer.createIndexBuffer(drawType).allocatei(map.getIndexArray(i)),
					VertexBuffer.create(3, drawType).allocatef(map.getVertexArray(i)),
					VertexBuffer.create(2, drawType).allocatef(map.getTexCoordArray(i)),
					VertexBuffer.create(3, drawType).allocatef(map.getNormalArray(i)),
					VertexBuffer.create(3, drawType).allocatef(map.getTangentArray(i)));
		}
		return arr;
	}
	
}
