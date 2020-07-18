package celestial.ui.geometry;

import celestial.beans.property.Properties;
import celestial.beans.property.Property;

public final class Margin {
	
	private final Property<Integer> insetTop;
	private final Property<Integer> insetBottom;
	private final Property<Integer> insetLeft;
	private final Property<Integer> insetRight;
	
	public Margin(int insetTop, int insetBottom, int insetLeft, int insetRight) {
		this.insetTop = Properties.createIntegerProperty(insetTop);
		this.insetBottom = Properties.createIntegerProperty(insetBottom);
		this.insetLeft = Properties.createIntegerProperty(insetLeft);
		this.insetRight = Properties.createIntegerProperty(insetRight);
	}
	
	public Margin(int inset) {
		this(inset, inset, inset, inset);
	}
	
	public Margin(int insetV, int insetH) {
		this(insetV, insetV, insetH, insetH);
	}
	
	public Margin() {
		this(0);
	}
	
	public int getInsetTop() {
		return insetTop.get();
	}
	
	public void setInsetTop(int insetTop) {
		this.insetTop.set(insetTop);
	}
	
	public Property<Integer> insetTopProperty() {
		return insetTop;
	}
	
	public int getInsetBottom() {
		return insetBottom.get();
	}
	
	public void setInsetBottom(int insetBottom) {
		this.insetBottom.set(insetBottom);
	}
	
	public Property<Integer> insetBottomProperty() {
		return insetBottom;
	}
	
	public int getInsetLeft() {
		return insetLeft.get();
	}
	
	public void setInsetLeft(int insetLeft) {
		this.insetLeft.set(insetLeft);
	}
	
	public Property<Integer> insetLeftProperty() {
		return insetLeft;
	}
	
	public int getInsetRight() {
		return insetRight.get();
	}
	
	public void setInsetRight(int insetRight) {
		this.insetRight.set(insetRight);
	}
	
	public Property<Integer> insetRightProperty() {
		return insetRight;
	}
	
	public void update() {
		insetTop.update();
		insetBottom.update();
		insetLeft.update();
		insetRight.update();
	}
	
}
