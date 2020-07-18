package celestial.beans.property;

import celestial.vecmath.Vector2f;
import celestial.vecmath.Vector3f;
import celestial.vecmath.Vector4f;
import celestial.util.IGetter;
import celestial.util.ISetter;

/**
 * A utility class containing several implementations
 * of internal and external properties, allowing for the
 * creation and management of properties with virtually
 * any type.
 * <p>
 * This class also contains utility methods to manipulate
 * properties, such as creating inverse properties for
 * boolean properties.
 * 
 * @author Max D
 */
public final class Properties {
	
	/**
	 * A boolean implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalBooleanProperty extends ExternalProperty<Boolean> {
		
		private static final long serialVersionUID = 7569118361404635221L;
		
		/**
		 * Creates a new external boolean property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalBooleanProperty(IGetter<Boolean> getter, ISetter<Boolean> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Boolean> getPropertyType() {
			return Boolean.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalBooleanProperty(ExternalBooleanProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalBooleanProperty clone() {
			return new ExternalBooleanProperty(this);
		}
		
	}
	
	/**
	 * A byte implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalByteProperty extends ExternalProperty<Byte> {
		
		private static final long serialVersionUID = -7550178385986834464L;
		
		/**
		 * Creates a new external byte property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalByteProperty(IGetter<Byte> getter, ISetter<Byte> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Byte> getPropertyType() {
			return Byte.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalByteProperty(ExternalByteProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalByteProperty clone() {
			return new ExternalByteProperty(this);
		}
		
	}
	
	/**
	 * A short implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalShortProperty extends ExternalProperty<Short> {
		
		private static final long serialVersionUID = -3831593166215310485L;
		
		/**
		 * Creates a new external short property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalShortProperty(IGetter<Short> getter, ISetter<Short> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Short> getPropertyType() {
			return Short.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalShortProperty(ExternalShortProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalShortProperty clone() {
			return new ExternalShortProperty(this);
		}
		
	}
	
	/**
	 * An integer implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalIntegerProperty extends ExternalProperty<Integer> {
		
		private static final long serialVersionUID = -8783379683673127084L;
		
		/**
		 * Creates a new external integer property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalIntegerProperty(IGetter<Integer> getter, ISetter<Integer> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Integer> getPropertyType() {
			return Integer.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalIntegerProperty(ExternalIntegerProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalIntegerProperty clone() {
			return new ExternalIntegerProperty(this);
		}
		
	}
	
	/**
	 * A long implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalLongProperty extends ExternalProperty<Long> {
		
		private static final long serialVersionUID = 2038728305568708339L;
		
		/**
		 * Creates a new external long property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalLongProperty(IGetter<Long> getter, ISetter<Long> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Long> getPropertyType() {
			return Long.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalLongProperty(ExternalLongProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalLongProperty clone() {
			return new ExternalLongProperty(this);
		}
		
	}
	
	/**
	 * A float implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalFloatProperty extends ExternalProperty<Float> {
		
		private static final long serialVersionUID = -9039745132367507761L;
		
		/**
		 * Creates a new external float property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalFloatProperty(IGetter<Float> getter, ISetter<Float> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Float> getPropertyType() {
			return Float.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalFloatProperty(ExternalFloatProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalFloatProperty clone() {
			return new ExternalFloatProperty(this);
		}
		
	}
	
	/**
	 * A double implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalDoubleProperty extends ExternalProperty<Double> {
		
		private static final long serialVersionUID = 8373518500454512820L;
		
		/**
		 * Creates a new external double property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalDoubleProperty(IGetter<Double> getter, ISetter<Double> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Double> getPropertyType() {
			return Double.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalDoubleProperty(ExternalDoubleProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalDoubleProperty clone() {
			return new ExternalDoubleProperty(this);
		}
		
	}
	
	/**
	 * A string implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalStringProperty extends ExternalProperty<String> {
		
		private static final long serialVersionUID = 5432055301085778871L;
		
		/**
		 * Creates a new external string property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalStringProperty(IGetter<String> getter, ISetter<String> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<String> getPropertyType() {
			return String.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalStringProperty(ExternalStringProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalStringProperty clone() {
			return new ExternalStringProperty(this);
		}
		
	}
	
	/**
	 * A Vec2 implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalVec2Property extends ExternalProperty<Vector2f> {
		
		private static final long serialVersionUID = 5432055301085778871L;
		
		/**
		 * Creates a new external vec2 property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalVec2Property(IGetter<Vector2f> getter, ISetter<Vector2f> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Vector2f> getPropertyType() {
			return Vector2f.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalVec2Property(ExternalVec2Property src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalVec2Property clone() {
			return new ExternalVec2Property(this);
		}
		
	}
	
	/**
	 * A Vec3 implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalVec3Property extends ExternalProperty<Vector3f> {
		
		private static final long serialVersionUID = 5432055301085778871L;
		
		/**
		 * Creates a new external vec3 property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalVec3Property(IGetter<Vector3f> getter, ISetter<Vector3f> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Vector3f> getPropertyType() {
			return Vector3f.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalVec3Property(ExternalVec3Property src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalVec3Property clone() {
			return new ExternalVec3Property(this);
		}
		
	}
	
	/**
	 * A Vec4 implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class ExternalVec4Property extends ExternalProperty<Vector4f> {
		
		private static final long serialVersionUID = 5432055301085778871L;
		
		/**
		 * Creates a new external vec4 property given the getter
		 * and setter implementations.
		 * 
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public ExternalVec4Property(IGetter<Vector4f> getter, ISetter<Vector4f> setter) {
			super(getter, setter);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Vector4f> getPropertyType() {
			return Vector4f.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private ExternalVec4Property(ExternalVec4Property src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public ExternalVec4Property clone() {
			return new ExternalVec4Property(this);
		}
		
	}
	
	/**
	 * A generic implementation for the <a href="#{@link}">
	 * {@link ExternalProperty}</a> class.
	 * 
	 * @see ExternalProperty
	 * @author Max D
	 */
	public static final class GenericExternalProperty<T> extends ExternalProperty<T> {
		
		private static final long serialVersionUID = 3693985355289297052L;
		
		/**
		 * Creates a new external generic property given the getter
		 * and setter implementations.
		 * 
		 * @param type		The default class type for this property.
		 * @param getter	The getter implementation for this property.
		 * @param setter	The setter implementation for this property.
		 */
		public GenericExternalProperty(Class<T> type, IGetter<T> getter, ISetter<T> setter) {
			super(getter, setter, type);
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private GenericExternalProperty(GenericExternalProperty<T> src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public GenericExternalProperty<T> clone() {
			return new GenericExternalProperty<T>(this);
		}
		
	}
	
	/**
	 * A boolean implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalBooleanProperty extends InternalProperty<Boolean> {
		
		private static final long serialVersionUID = 5398337310320077685L;
		
		/**
		 * Creates a new internal boolean property with a default
		 * boolean value.
		 */
		public InternalBooleanProperty() {
			super(false);
		}
		
		/**
		 * Creates a new internal boolean property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalBooleanProperty(boolean value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Boolean> getPropertyType() {
			return Boolean.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalBooleanProperty(InternalBooleanProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalBooleanProperty clone() {
			return new InternalBooleanProperty(this);
		}
		
	}
	
	/**
	 * A byte implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalByteProperty extends InternalProperty<Byte> {
		
		private static final long serialVersionUID = -683697087119876345L;
		
		/**
		 * Creates a new internal byte property with a default
		 * byte value.
		 */
		public InternalByteProperty() {
			super((byte) 0);
		}
		
		/**
		 * Creates a new internal byte property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalByteProperty(byte value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Byte> getPropertyType() {
			return Byte.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalByteProperty(InternalByteProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalByteProperty clone() {
			return new InternalByteProperty(this);
		}
		
	}
	
	/**
	 * A short implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalShortProperty extends InternalProperty<Short> {
		
		private static final long serialVersionUID = 6609379420573565028L;
		
		/**
		 * Creates a new internal short property with a default
		 * short value.
		 */
		public InternalShortProperty() {
			super((short) 0);
		}
		
		/**
		 * Creates a new internal short property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalShortProperty(short value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Short> getPropertyType() {
			return Short.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalShortProperty(InternalShortProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalShortProperty clone() {
			return new InternalShortProperty(this);
		}
		
	}
	
	/**
	 * An integer implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalIntegerProperty extends InternalProperty<Integer> {
		
		private static final long serialVersionUID = 2234518668339170251L;
		
		/**
		 * Creates a new internal integer property with a default
		 * integer value.
		 */
		public InternalIntegerProperty() {
			super(0);
		}
		
		/**
		 * Creates a new internal integer property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalIntegerProperty(int value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Integer> getPropertyType() {
			return Integer.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalIntegerProperty(InternalIntegerProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalIntegerProperty clone() {
			return new InternalIntegerProperty(this);
		}
		
	}
	
	/**
	 * A long implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalLongProperty extends InternalProperty<Long> {
		
		private static final long serialVersionUID = -3264252549451440435L;
		
		/**
		 * Creates a new internal long property with a default
		 * long value.
		 */
		public InternalLongProperty() {
			super(0L);
		}
		
		/**
		 * Creates a new internal long property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalLongProperty(long value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Long> getPropertyType() {
			return Long.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalLongProperty(InternalLongProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalLongProperty clone() {
			return new InternalLongProperty(this);
		}
		
	}
	
	/**
	 * A float implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalFloatProperty extends InternalProperty<Float> {
		
		private static final long serialVersionUID = -9162381195312555328L;
		
		/**
		 * Creates a new internal float property with a default
		 * float value.
		 */
		public InternalFloatProperty() {
			super(0.0f);
		}
		
		/**
		 * Creates a new internal float property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalFloatProperty(float value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Float> getPropertyType() {
			return Float.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalFloatProperty(InternalFloatProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalFloatProperty clone() {
			return new InternalFloatProperty(this);
		}
		
	}
	
	/**
	 * A double implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalDoubleProperty extends InternalProperty<Double> {
		
		private static final long serialVersionUID = -4227540819994208457L;
		
		/**
		 * Creates a new internal double property with a default
		 * double value.
		 */
		public InternalDoubleProperty() {
			super(0.0d);
		}
		
		/**
		 * Creates a new internal double property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalDoubleProperty(double value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Double> getPropertyType() {
			return Double.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalDoubleProperty(InternalDoubleProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalDoubleProperty clone() {
			return new InternalDoubleProperty(this);
		}
		
	}
	
	/**
	 * A string implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalStringProperty extends InternalProperty<String> {
		
		private static final long serialVersionUID = 5037525417804245793L;
		
		/**
		 * Creates a new internal string property with a default
		 * string value.
		 */
		public InternalStringProperty() {
			super("");
		}
		
		/**
		 * Creates a new internal string property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalStringProperty(String value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<String> getPropertyType() {
			return String.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalStringProperty(InternalStringProperty src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalStringProperty clone() {
			return new InternalStringProperty(this);
		}
		
	}
	
	/**
	 * A vec2 implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalVec2Property extends InternalProperty<Vector2f> {
		
		private static final long serialVersionUID = 5037525417804245793L;
		
		/**
		 * Creates a new internal vec2 property with a default
		 * string value.
		 */
		public InternalVec2Property() {
			super(new Vector2f());
		}
		
		/**
		 * Creates a new internal vec2 property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalVec2Property(Vector2f value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Vector2f> getPropertyType() {
			return Vector2f.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalVec2Property(InternalVec2Property src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalVec2Property clone() {
			return new InternalVec2Property(this);
		}
		
	}
	
	/**
	 * A vec3 implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalVec3Property extends InternalProperty<Vector3f> {
		
		private static final long serialVersionUID = 5037525417804245793L;
		
		/**
		 * Creates a new internal vec3 property with a default
		 * string value.
		 */
		public InternalVec3Property() {
			super(new Vector3f());
		}
		
		/**
		 * Creates a new internal vec3 property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalVec3Property(Vector3f value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Vector3f> getPropertyType() {
			return Vector3f.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalVec3Property(InternalVec3Property src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalVec3Property clone() {
			return new InternalVec3Property(this);
		}
		
	}
	
	/**
	 * A vec4 implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class InternalVec4Property extends InternalProperty<Vector4f> {
		
		private static final long serialVersionUID = 5037525417804245793L;
		
		/**
		 * Creates a new internal vec4 property with a default
		 * string value.
		 */
		public InternalVec4Property() {
			super(new Vector4f());
		}
		
		/**
		 * Creates a new internal vec4 property with the given
		 * value argument.
		 * 
		 * @param value	The initial value for this property.
		 */
		public InternalVec4Property(Vector4f value) {
			super(value);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Vector4f> getPropertyType() {
			return Vector4f.class;
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private InternalVec4Property(InternalVec4Property src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public InternalVec4Property clone() {
			return new InternalVec4Property(this);
		}
		
	}
	
	/**
	 * A generic implementation for the <a href="#{@link}">
	 * {@link InternalProperty}</a> class.
	 * 
	 * @see InternalProperty
	 * @author Max D
	 */
	public static final class GenericInternalProperty<T> extends InternalProperty<T> {
		
		private static final long serialVersionUID = -7188254369131332631L;
		
		/**
		 * Creates a new internal generic property with a value
		 * of null.
		 */
		public GenericInternalProperty(Class<T> type) {
			this(type, null);
		}
		
		/**
		 * Creates a new internal generic property with the given
		 * value argument.
		 * 
		 * @param type	The default class type for this property.
		 * @param value	The initial value for this property.
		 */
		public GenericInternalProperty(Class<T> type, T value) {
			super(value, type);
		}
		
		/**
		 * A copy-constructor for this property.
		 * 
		 * @param src	The src to clone.
		 */
		private GenericInternalProperty(GenericInternalProperty<T> src) {
			super(src);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public GenericInternalProperty<T> clone() {
			return new GenericInternalProperty<T>(this);
		}
		
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Boolean> readOnlyBooleanProperty(IGetter<Boolean> getter) {
		return new ExternalBooleanProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Boolean> createBooleanProperty(IGetter<Boolean> getter, ISetter<Boolean> setter) {
		return new ExternalBooleanProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Boolean> createBooleanProperty() {
		return new InternalBooleanProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Boolean> createBooleanProperty(boolean value) {
		return new InternalBooleanProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Byte> readOnlyByteProperty(IGetter<Byte> getter) {
		return new ExternalByteProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Byte> createByteProperty(IGetter<Byte> getter, ISetter<Byte> setter) {
		return new ExternalByteProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Byte> createByteProperty() {
		return new InternalByteProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Byte> createByteProperty(byte value) {
		return new InternalByteProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Short> readOnlyShortProperty(IGetter<Short> getter) {
		return new ExternalShortProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Short> createShortProperty(IGetter<Short> getter, ISetter<Short> setter) {
		return new ExternalShortProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Short> createShortProperty() {
		return new InternalShortProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Short> createShortProperty(short value) {
		return new InternalShortProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Integer> readOnlyIntegerProperty(IGetter<Integer> getter) {
		return new ExternalIntegerProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Integer> createIntegerProperty(IGetter<Integer> getter, ISetter<Integer> setter) {
		return new ExternalIntegerProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Integer> createIntegerProperty() {
		return new InternalIntegerProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Integer> createIntegerProperty(int value) {
		return new InternalIntegerProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Long> readOnlyLongProperty(IGetter<Long> getter) {
		return new ExternalLongProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Long> createLongProperty(IGetter<Long> getter, ISetter<Long> setter) {
		return new ExternalLongProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Long> createLongProperty() {
		return new InternalLongProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Long> createLongProperty(long value) {
		return new InternalLongProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Float> readOnlyFloatProperty(IGetter<Float> getter) {
		return new ExternalFloatProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Float> createFloatProperty(IGetter<Float> getter, ISetter<Float> setter) {
		return new ExternalFloatProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Float> createFloatProperty() {
		return new InternalFloatProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Float> createFloatProperty(float value) {
		return new InternalFloatProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Double> readOnlyDoubleProperty(IGetter<Double> getter) {
		return new ExternalDoubleProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Double> createDoubleProperty(IGetter<Double> getter, ISetter<Double> setter) {
		return new ExternalDoubleProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Double> createDoubleProperty() {
		return new InternalDoubleProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Double> createDoubleProperty(double value) {
		return new InternalDoubleProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<String> readOnlyStringProperty(IGetter<String> getter) {
		return new ExternalStringProperty(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<String> createStringProperty(IGetter<String> getter, ISetter<String> setter) {
		return new ExternalStringProperty(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<String> createStringProperty() {
		return new InternalStringProperty();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<String> createStringProperty(String value) {
		return new InternalStringProperty(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Vector2f> readOnlyVec2Property(IGetter<Vector2f> getter) {
		return new ExternalVec2Property(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Vector2f> createVec2Property(IGetter<Vector2f> getter, ISetter<Vector2f> setter) {
		return new ExternalVec2Property(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Vector2f> createVec2Property() {
		return new InternalVec2Property();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Vector2f> createVec2Property(Vector2f value) {
		return new InternalVec2Property(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Vector3f> readOnlyVec3Property(IGetter<Vector3f> getter) {
		return new ExternalVec3Property(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Vector3f> createVec3Property(IGetter<Vector3f> getter, ISetter<Vector3f> setter) {
		return new ExternalVec3Property(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Vector3f> createVec3Property() {
		return new InternalVec3Property();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Vector3f> createVec3Property(Vector3f value) {
		return new InternalVec3Property(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static ExternalProperty<Vector4f> readOnlyVec4Property(IGetter<Vector4f> getter) {
		return new ExternalVec4Property(getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static ExternalProperty<Vector4f> createVec4Property(IGetter<Vector4f> getter, ISetter<Vector4f> setter) {
		return new ExternalVec4Property(getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @return	The newly created internal property.
	 */
	public static InternalProperty<Vector4f> createVec4Property() {
		return new InternalVec4Property();
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static InternalProperty<Vector4f> createVec4Property(Vector4f value) {
		return new InternalVec4Property(value);
	}
	
	/**
	 * Creates a new read-only external property with the given
	 * getter implementation.
	 * 
	 * @param type		The default class type for this generic property.
	 * @param getter	The getter implementation for this property.
	 * @return			The newly created read-only external property.
	 */
	public static <T> ExternalProperty<T> readOnlyProperty(Class<T> type, IGetter<T> getter) {
		return new GenericExternalProperty<T>(type, getter, readOnly());
	}
	
	/**
	 * Creates a new external property with the given getter and
	 * setter implementations.
	 * 
	 * @param type		The default class type for this generic property.
	 * @param getter	The getter implementation for this property.
	 * @param setter	The setter implementation for this property.
	 * @return			The newly created external property.
	 */
	public static <T> ExternalProperty<T> createProperty(Class<T> type, IGetter<T> getter, ISetter<T> setter) {
		return new GenericExternalProperty<T>(type, getter, setter);
	}
	
	/**
	 * Creates a new internal property with a default property
	 * value (as specified by Java standard).
	 * 
	 * @param type	The default class type for this generic property.
	 * @return		The newly created internal property.
	 */
	public static <T> InternalProperty<T> createProperty(Class<T> type) {
		return new GenericInternalProperty<T>(type);
	}
	
	/**
	 * Creates a new internal property with the given value
	 * argument.
	 * 
	 * @param type	The default class type for this generic property.
	 * @param value	The initial value for this property.
	 * @return		The newly created internal property.
	 */
	public static <T> InternalProperty<T> createProperty(Class<T> type, T value) {
		return new GenericInternalProperty<T>(type, value);
	}
	
	/**
	 * Returns an inverse form of this boolean property.
	 * <p>
	 * It shares the same memory bank and can therefore be used
	 * in the same way, but the get and set methods will always
	 * invert the property value.
	 * 
	 * @param property	The property to create an inverse of.
	 * @return			The inverse form of the property.
	 */
	public static Property<Boolean> asInverse(Property<Boolean> property) {
		return new WrapperProperty<Boolean>(property) {
			
			private static final long serialVersionUID = -3350557966917860694L;
			
			@Override
			protected Boolean process(Boolean value) { return !value; }
			
			@Override
			protected Boolean processInv(Boolean value) { return !value; }
			
			@Override
			public WrapperProperty<Boolean> clone() {
				return (WrapperProperty<Boolean>) asInverse(super.getProperty());
			}
			
		};
	}
	
	/**
	 * Returns a read-only setter implementation that throws an
	 * <a href="#{@link}">{@link UnsupportedOperationException}</a>.
	 * 
	 * @param <T>	The type of setter to create.
	 * @return		The newly created setter implementation.
	 */
	private static <T> ISetter<T> readOnly() {
		return s -> {
			throw new UnsupportedOperationException("set");
		};
	}
	
}
