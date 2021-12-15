package org.springframework.samples.petclinic.server;

import java.io.Serializable;

public class PetClinicResponse<T> implements Serializable {

	public static final long serialVersionUID = 42L;

	public static final int SUCCESS = 200;

	public static final int FAIL = 200;

	private int code;

	private String message;

	private T response;

	public PetClinicResponse() {

	}

	public PetClinicResponse(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public PetClinicResponse(T response) {
		this.code = SUCCESS;
		this.response = response;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getResponse() {
		return response;
	}

	public void setResponse(T response) {
		this.response = response;
	}

}
