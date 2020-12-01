package pipe.test;

public class Test2 {
	
	

	public static void main(String[] args) {
		
		Address address = new Address();
		address.setId("Yueyang");
		
		Person person = new Person();
		person.setName("moqi");
		//存储的是引用
		person.setAddress(address);
		
		address.setId("Changsha");
		
		System.out.println("name: " + person.getName() + ", add: " + person.getAddress().getId());
		

	}

}
