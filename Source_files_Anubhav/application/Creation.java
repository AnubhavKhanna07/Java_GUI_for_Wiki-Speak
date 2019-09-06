package application;

public class Creation {
	
	String _creationName;
	Integer _creationNumber;
	
	public Creation(String creationName, Integer creationNumber) {
		_creationName = creationName;
		_creationNumber= creationNumber;
	}
	
	public String get_creationName() {
		return _creationName;
	}
	
	public int get_creationNumber() {
		return _creationNumber;
	}
}
