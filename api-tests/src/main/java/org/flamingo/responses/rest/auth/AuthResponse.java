package org.flamingo.responses.rest.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

	@JsonProperty("token")
	private String token;

	@JsonProperty("reason")
	private String reason;
}