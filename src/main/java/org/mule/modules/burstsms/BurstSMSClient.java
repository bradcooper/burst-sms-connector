package org.mule.modules.burstsms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mule.modules.burstsms.BurstSMSConnector.CountryCode;
import org.mule.modules.burstsms.BurstSMSException.ResponseCode;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;

public class BurstSMSClient {

	private Client client; /* a Jersey client instance */
	private WebResource apiResource;
	private BurstSMSConnector connector;

	public BurstSMSClient(BurstSMSConnector connector) {
		setConnector(connector);
		ClientConfig clientConfig = new DefaultClientConfig();
        
		/* Enable support for JSON to POJO entity mapping in Jersey */
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

		this.client = Client.create(clientConfig);		
		this.apiResource = this.client.resource(getConnector().getConfig().getApiUrl());
	}

	public void setConnector(BurstSMSConnector connector) {
		this.connector = connector;
	}

	public BurstSMSConnector getConnector() {
		return connector;
	}
	
	public WebResource getApiResource() {
		apiResource.addFilter(new HTTPBasicAuthFilter(getConnector().getConfig().getUsername(), getConnector().getConfig().getPassword()));
        return apiResource;
    }

    public void setApiResource(WebResource apiResource) {
        this.apiResource = apiResource;
    }
    
    @SuppressWarnings("unchecked")
	public <T> T execute(String path, Class<T> responseClass, QueryParam... params) throws BurstSMSException {
    	WebResource webResource = getApiResource().path(path);
    	
    	if (params != null) {
	    	for (QueryParam param: params)
	    		webResource = param.apply(webResource);
    	}
    	
		ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).method("GET", ClientResponse.class);

		if (clientResponse.getStatus() >= 200 && clientResponse.getStatus() < 300) {
			return clientResponse.getEntity(responseClass);
		} else {
			clientResponse.bufferEntity();
			try {
				Map<String,String> errorDetails = (Map<String, String>) clientResponse.getEntity(Map.class).get("error");
				throw new BurstSMSException(
						ResponseCode.valueOf(errorDetails.get("code")),
						errorDetails.get("description"),
						clientResponse.getStatus());
			} catch (BurstSMSException ex) {
				throw ex;
			} catch (Exception ex) {
				String message = "An unexpected error occurred";
				try {
					clientResponse.getEntityInputStream().reset();
					message = clientResponse.getEntity(String.class);
				} catch (Exception ignore) {}
				
				throw new BurstSMSException(
						ResponseCode.UNKNOWN,
						message,
						clientResponse.getStatus());				
			}
		}
    }
    
    private static class QueryParam {
    	private String key;
    	private Object value;
    	public QueryParam(String key, Object value) {
    		this.key = key;
    		this.value = value;
    	}
    	public WebResource apply(WebResource webResource) {
    		return value == null ? webResource : webResource.queryParam(key, value.toString());
    	}
    }
    
    private String dateToString(Date date) {
    	String str = null;
    	if (date != null) {
    		DateTime dt = new DateTime(date.getTime(), DateTimeZone.UTC);
    		str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt.toDate());
    	}
    	return str;
    }
    
    private String listToString(List<?> list) {
    	String str = null;
    	if (list != null) {
    		for (Object val: list) {
    			if (str == null)
    				str = val.toString();
    			else
    				str = "," + val.toString();
    		}
    	}
    	return str;
    }
    
    @SuppressWarnings("unchecked")
	public Map<?, ?> sendSMS(String message, List<String> to, String from, String sendAt, Long listId,
			String dlrCallback, String replyCallback, Long validity, String repliesToEmail, Boolean fromShared,
			CountryCode countryCode) throws BurstSMSException {
		return execute(
				"send-sms.json",
				Map.class,
				new QueryParam("message", message),
				new QueryParam("to", listToString(to)),
				new QueryParam("from", from),
				new QueryParam("send_at", sendAt),
				new QueryParam("list_id", listId),
				new QueryParam("dlr_callback", dlrCallback),
				new QueryParam("reply_callback", replyCallback),
				new QueryParam("validity", validity),
				new QueryParam("replies_to_email", repliesToEmail),
				new QueryParam("from_shared", fromShared),
				new QueryParam("countrycode", countryCode));
    }

	public Map<?,?> formatNumber(String number, CountryCode countryCode) throws BurstSMSException {
		return execute("format-number.json",
				Map.class,
				new QueryParam("msisdn", number),
				new QueryParam("countrycode", countryCode));
	}
}
