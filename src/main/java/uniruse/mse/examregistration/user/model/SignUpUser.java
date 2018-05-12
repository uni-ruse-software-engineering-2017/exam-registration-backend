package uniruse.mse.examregistration.user.model;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Represents a new user used for signing-up.
 */
public class SignUpUser {

	private String username;
	private String password;

	@JsonGetter("username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonGetter("password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
