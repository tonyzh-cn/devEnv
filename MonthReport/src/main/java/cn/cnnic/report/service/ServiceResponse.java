package cn.cnnic.report.service;

public class ServiceResponse {
	public final static int EXISTS=30001;
	public final static int NOT_FOUND=30004;
	public final static int OK=200;
	private int code;
	private String data;
	public ServiceResponse(int code,String data) {
		this.code=code;
		this.data=data;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
}
