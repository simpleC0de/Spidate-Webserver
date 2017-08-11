package webserver.main;

/**
 * Created by root on 11.08.2017.
 */
public class StatusResponse {

	private int status;
	private String message;
	private String version;
	
	/**
	 * @param status
	 * @param message
	 * @param version
	 */
	public StatusResponse(int status, String message, String version) {
		this.status = status;
		this.message = message;
		this.version = version;
	}

	public int getStatus() {
		return this.status;
	}

	public String getMessage() {
		return this.message;
	}

	public String getVersion() {
		return this.version;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
