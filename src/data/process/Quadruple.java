package data.process;


public class Quadruple {

	private String subject;
	private String predicate;
	private String object;
	public  QuadrupleType quadrupleType;
	
	public Quadruple() {
		subject = predicate = object = "";
	}
	
	public Quadruple(String subject, String predicate, String object, QuadrupleType quadrupleType) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.quadrupleType = quadrupleType;
	}
	
	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}


	public String getPredicate() {
		return predicate;
	}


	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}


	public String getObject() {
		return object;
	}


	public void setObject(String object) {
		this.object = object;
	}


	public QuadrupleType getQuadrupleType() {
		return quadrupleType;
	}


	public void setQuadrupleType(QuadrupleType quadrupleType) {
		this.quadrupleType = quadrupleType;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void print(){
		System.out.println(this.subject + "\t" + this.predicate+"\t" + this.object + "\t\t" + "type:" + this.quadrupleType);
	}

}
