package celestial.ui.geometry;

public enum Alignment {
	
	NONE,
	
	TOP_LEFT,
	
	CENTER_LEFT,
	
	BOTTOM_LEFT,
	
	TOP_CENTER,
	
	CENTER,
	
	BOTTOM_CENTER,
	
	TOP_RIGHT,
	
	CENTER_RIGHT,
	
	BOTTOM_RIGHT;
	
	public HAlignment toHoriz() {
		switch(this) {
		case NONE:
			return HAlignment.NONE;
		case TOP_LEFT:
		case CENTER_LEFT:
		case BOTTOM_LEFT:
			return HAlignment.LEFT;
		case TOP_CENTER:
		case CENTER:
		case BOTTOM_CENTER:
			return HAlignment.CENTER;
		case TOP_RIGHT:
		case CENTER_RIGHT:
		case BOTTOM_RIGHT:
			return HAlignment.RIGHT;
		}
		return null;
	}
	
	public VAlignment toVertical() {
		switch(this) {
		case NONE:
			return VAlignment.NONE;
		case TOP_LEFT:
		case TOP_CENTER:
		case TOP_RIGHT:
			return VAlignment.TOP;
		case CENTER_LEFT:
		case CENTER:
		case CENTER_RIGHT:
			return VAlignment.CENTER;
		case BOTTOM_LEFT:
		case BOTTOM_CENTER:
		case BOTTOM_RIGHT:
			return VAlignment.BOTTOM;
		}
		return null;
	}
	
	public static Alignment merge(HAlignment h, VAlignment v) {
		switch(h) {
		case NONE:
			return NONE;
		case LEFT:
			switch(v) {
			case NONE:
				return NONE;
			case TOP:
				return TOP_LEFT;
			case CENTER:
				return CENTER_LEFT;
			case BOTTOM:
				return BOTTOM_LEFT;
			}
		case CENTER:
			switch(v) {
			case NONE:
				return NONE;
			case TOP:
				return TOP_CENTER;
			case CENTER:
				return CENTER;
			case BOTTOM:
				return BOTTOM_CENTER;
			}
		case RIGHT:
			switch(v) {
			case NONE:
				return NONE;
			case TOP:
				return TOP_RIGHT;
			case CENTER:
				return CENTER_RIGHT;
			case BOTTOM:
				return BOTTOM_RIGHT;
			}
		}
		return null;
	}
	
}
