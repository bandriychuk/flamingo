package org.flamingo.payloads.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthPayload {

	@JsonProperty("password")
	private String password;

	@JsonProperty("username")
	private String username;
}