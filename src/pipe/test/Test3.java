package pipe.test;

public class Test3 {
	
	public static void main(String[] args) {
		
		int[] Arr={1,2,3,83,5,6,7,34,3};
		int ECCIndex = 0;
		for (int i = 1; i < 9; i++) {
			if (Arr[i] > Arr[ECCIndex]) {
				ECCIndex = i;
			}
		}
		
		System.out.println("ECCIndex: " + ECCIndex);
		
	}

}
