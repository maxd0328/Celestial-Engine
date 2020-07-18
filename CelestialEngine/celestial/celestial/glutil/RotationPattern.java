package celestial.glutil;

import celestial.beans.property.SelectiveProperty.PropertySelection;

public enum RotationPattern {
	
	ROTATION_PATTERN_XYZ,
	
	ROTATION_PATTERN_ZYX,
	
	ROTATION_PATTERN_YZX,
	
	ROTATION_PATTERN_ZXY;
	
	@SuppressWarnings("unchecked")
	public static PropertySelection<RotationPattern>[] toSelectionList() {
		return (PropertySelection<RotationPattern>[]) new PropertySelection[] {
				new PropertySelection<RotationPattern>("XYZ", ROTATION_PATTERN_XYZ),
				new PropertySelection<RotationPattern>("ZYX", ROTATION_PATTERN_ZYX),
				new PropertySelection<RotationPattern>("YZX", ROTATION_PATTERN_YZX),
				new PropertySelection<RotationPattern>("ZXY", ROTATION_PATTERN_ZXY)
		};
	}
	
}
