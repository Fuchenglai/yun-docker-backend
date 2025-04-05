package com.lfc.clouddocker.exception;

public class YunException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public YunException(String message){
		super(message);
	}

	public YunException(Throwable cause)
	{
		super(cause);
	}

	public YunException(String message, Throwable cause)
	{
		super(message,cause);
	}
}
