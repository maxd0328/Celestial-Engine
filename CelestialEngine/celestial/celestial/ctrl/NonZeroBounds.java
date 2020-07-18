package celestial.ctrl;

import celestial.beans.property.Property;

public final class NonZeroBounds<E> implements PropertyBounds<E> {
	
	private static final long serialVersionUID = 8315357421302817350L;
	
	private final Class<E> type;
	private final FloatConverter<E> converter;
	
	private Property<E>[] properties;
	
	public NonZeroBounds(Class<E> type, FloatConverter<E> converter) {
		this.type = type;
		this.converter = converter;
	}
	
	@Override
	public void loadProperties(@SuppressWarnings("unchecked") Property<E>... properties) {
		this.properties = properties;
	}
	
	@Override
	public void validateBounds() {
		boolean nonZero = false;
		for(Property<E> property : properties)
			for(int i = 0 ; i < converter.indices() ; ++i)
				if(converter.convert(property.get(), i) != 0)
					nonZero = true;
		
		if(!nonZero) {
			properties[0].set(converter.convertBack(1f));
		}
	}
	
	@Override
	public Class<E> getType() {
		return type;
	}
	
	@Override
	public NonZeroBounds<E> duplicate() {
		return new NonZeroBounds<E>(type, converter);
	}
	
	public FloatConverter<E> getConverter() {
		return converter;
	}
	
	@Override
	public Property<E>[] getProperties() {
		return properties;
	}
	
}
