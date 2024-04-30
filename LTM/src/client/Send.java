package client;

import java.io.Serializable;

public class Send implements Serializable{
	private int id,soketnoi;
	private byte [] a;
	public Send(int id, byte[] a,int soketnoi) {
		super();
		this.id = id;
		this.a = a;
		this.soketnoi = soketnoi;
	}
	
	
	
	public int getSoketnoi() {
		return soketnoi;
	}



	public void setSoketnoi(int soketnoi) {
		this.soketnoi = soketnoi;
	}



	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public byte[] getA() {
		return a;
	}
	public void setA(byte[] a) {
		this.a = a;
	}
	
	
}
