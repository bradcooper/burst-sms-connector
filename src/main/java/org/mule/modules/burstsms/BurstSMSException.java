package org.mule.modules.burstsms;

/**
 * Thrown if a call to a BurstSMS API method fails
 * @author Brad Cooper
 */
public class BurstSMSException extends Exception {

	private static final long serialVersionUID = -1L;

	/**
	 * Response codes returned from BurstSMS
	 */
	public static enum ResponseCode {
		AUTH_FAILED_NO_DATA,
		AUTH_FAILED,
		NOT_IMPLEMENTED,
		OVER_LIMIT,
		FIELD_EMPTY,
		FIELD_INVALID,
		NO_ACCESS,
		KEY_EXISTS,
		NOT_FOUND,
		UNKNOWN;
	}
	
	private ResponseCode code;
	private int httpStatus;

	public BurstSMSException(ResponseCode code, String message, int httpStatus) {
		super(message);
		setCode(code);
		setHttpStatus(httpStatus);
	}

	public BurstSMSException(ResponseCode code, String message, int httpStatus, Throwable cause) {
		super(message, cause);
		setCode(code);
		setHttpStatus(httpStatus);
	}
	
	public ResponseCode getCode() {
		return code;
	}

	public void setCode(ResponseCode code) {
		this.code = code;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
}
