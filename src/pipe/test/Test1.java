package pipe.test;

import java.util.ArrayList;
import java.util.List;

import toolkits.utils.com.MathUtils;

public class Test1 {
	
	public void test1() {
		
		int num = 0;
		
		test2(num);
		
		System.out.println("num: " + num);
		
	}
	
	

	public void test2(int num) {
	  
		num ++;
		num ++;
		
	}



	public static void main(String[] args) {
		
		Test1 test = new Test1();
		test.test1();
		
		/*List l1 = new ArrayList();
		l1.add(1);  
        l1.add(2);
        
        List l2 = new ArrayList();
		l2.add(3);  
        l2.add(4);
        l2.add(1);
        
        List<List<Integer>> sets = new ArrayList<List<Integer>>();
        sets.add(l1);
        sets.add(l2);
        
        List<List<Integer>> result = MathUtils.descInt(sets);
        for (List<Integer> list : result) {
			System.out.println("Product: " + list);
		}*/
		

	}

}
