package celestial.util;

public final class KVEntry<K, V> implements java.io.Serializable {
	
	private static final long serialVersionUID = 552778916126269887L;
	
	private K key;
	private V value;
	
	public KVEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}
	
	public void setKey(K key) {
		this.key = key;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "(" + key + ", " + value + ")";
	}
	
}
