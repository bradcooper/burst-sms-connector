package org.mule.modules.burstsms.config;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;

/**
 * Configurable items for the BurstSMS connector
 * @author Brad Cooper
 */
@Configuration(friendlyName = "Config")
public class ConnectorConfig {

	/**
	 * The HTTP endpoint for the BurstSMS API
	 */
	@Configurable
	@Optional
	@Default("https://api.transmitsms.com")
	@Placement(order = 1)
	private String apiUrl;

	/**
	 * The user name to use when invoking API methods
	 */
	@Configurable
	@Placement(order = 2)
	private String username;

	/**
	 * The password used to authenticate API method calls
	 */
	@Configurable
	@Password
	@Placement(order = 3)
	private String password;

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
