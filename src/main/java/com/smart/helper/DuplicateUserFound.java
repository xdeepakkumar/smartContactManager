package com.smart.helper;

public class DuplicateUserFound extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DuplicateUserFound(){
		
	}
	public DuplicateUserFound(String msg) {
		super(msg);
	}

}
