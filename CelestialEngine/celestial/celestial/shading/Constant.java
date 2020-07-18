package celestial.shading;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;
import celestial.data.GLData;
import celestial.data.ImageBuffer;
import celestial.data.ImageSampler;
import celestial.data.Sampler;
import celestial.vecmath.Vector4f;

public final class Constant {
	
	private final ConnectorType type;
	private final Property<Vector4f> value;
	
	private ImageSampler image;
	private Vector4f prev;
	
	Constant(ConnectorType type, Vector4f value) {
		this.type = type;
		this.value = Properties.createVec4Property(value);
	}
	
	public ConnectorType getType() {
		return type;
	}
	
	public Vector4f getValue() {
		return value.get();
	}
	
	public void setValue(Vector4f value) {
		this.value.set(value);
	}
	
	public Property<Vector4f> valueProperty() {
		return value;
	}
	
	public Sampler obtainConstant() {
		Vector4f pixel = value.get();
		if(prev != null && prev.equals(pixel))
			return image;
		else {
			prev = new Vector4f(pixel);
			ImageBuffer buffer = new ImageBuffer(1, 1);
			buffer.setColor4f(0, 0, pixel.x, pixel.y, pixel.z, pixel.w);
			image = ImageSampler.create(buffer);
			
			return image;
		}
	}
	
	public boolean containsData(GLData data) {
		return data == image;
	}

}
