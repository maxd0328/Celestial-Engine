package celestial.serialization;

public class ArrayAdapter {
	
	public static Boolean[] fromPrimitive(boolean[] arr) {
		Boolean[] objArr = new Boolean[arr.length];
		for(int i = 0 ; i < arr.length ; ++i) objArr[i] = arr[i];
		return objArr;
	}
	
	public static Byte[] fromPrimitive(byte[] arr) {
		Byte[] objArr = new Byte[arr.length];
		for(int i = 0 ; i < arr.length ; ++i) objArr[i] = arr[i];
		return objArr;
	}
	
	public static Short[] fromPrimitive(short[] arr) {
		Short[] objArr = new Short[arr.length];
		for(int i = 0 ; i < arr.length ; ++i) objArr[i] = arr[i];
		return objArr;
	}
	
	public static Integer[] fromPrimitive(int[] arr) {
		Integer[] objArr = new Integer[arr.length];
		for(int i = 0 ; i < arr.length ; ++i) objArr[i] = arr[i];
		return objArr;
	}
	
	public static Long[] fromPrimitive(long[] arr) {
		Long[] objArr = new Long[arr.length];
		for(int i = 0 ; i < arr.length ; ++i) objArr[i] = arr[i];
		return objArr;
	}
	
	public static Float[] fromPrimitive(float[] arr) {
		Float[] objArr = new Float[arr.length];
		for(int i = 0 ; i < arr.length ; ++i) objArr[i] = arr[i];
		return objArr;
	}
	
	public static Double[] fromPrimitive(double[] arr) {
		Double[] objArr = new Double[arr.length];
		for(int i = 0 ; i < arr.length ; ++i) objArr[i] = arr[i];
		return objArr;
	}
	
}
