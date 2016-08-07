package org.mule.modules.burstsms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mule.modules.burstsms.BurstSMSConnector.CountryCode;
import org.mule.modules.burstsms.BurstSMSConnector.DeliveryStatus;
import org.mule.modules.burstsms.BurstSMSConnector.MemberSelection;
import org.mule.modules.burstsms.BurstSMSConnector.NumberFilter;
import org.mule.modules.burstsms.BurstSMSConnector.OnlyOmitBoth;
import org.mule.modules.burstsms.BurstSMSConnector.OnlyOmitInclude;
import org.mule.modules.burstsms.BurstSMSException.ResponseCode;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;

class BurstSMSClient {

	static Log logger = LogFactory.getLog(BurstSMSClient.class);
	
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
		apiResource.addFilter(new HTTPBasicAuthFilter(
				getConnector().getConfig().getUsername(), 
				getConnector().getConfig().getPassword()));
        return apiResource;
    }

    public void setApiResource(WebResource apiResource) {
        this.apiResource = apiResource;
    }
     
    public Map<?, ?> sendSMS(String message, List<String> to, String from, String sendAt, Long listId,
			String dlrCallback, String replyCallback, Long validity, String repliesToEmail, Boolean fromShared,
			CountryCode countryCode) throws BurstSMSException {
		return new RequestBuilder("send-sms.json")
				.param("message", message)
				.param("to", to)
				.param("from", from)
				.param("send_at", sendAt)
				.param("list_id", listId)
				.param("dlr_callback", dlrCallback)
				.param("reply_callback", replyCallback)
				.param("validity", validity)
				.param("replies_to_email", repliesToEmail)
				.param("from_shared", fromShared)
				.param("countrycode", countryCode)
				.execute();
    }

	public Map<?,?> formatNumber(String number, CountryCode countryCode) throws BurstSMSException {
		return new RequestBuilder("format-number.json")
				.param("msisdn", number)
				.param("countrycode", countryCode)
				.execute();
	}

	public Map<?, ?> getSMS(String messageId) throws BurstSMSException {
		return new RequestBuilder("get-sms.json").param("message_id", messageId).execute();
	}

	public Map<?, ?> getSMSStatus(String messageId) throws BurstSMSException {
		return new RequestBuilder("get-sms-stats.json").param("message_id", messageId).execute();
	}

	public Map<?, ?> getSMSResponses(String messageId, String keywordId, String keyword, String number, String msisdn,
			Integer page, Integer max, Boolean incluldeOriginal) throws BurstSMSException {
		return new RequestBuilder("get-sms-responses.json")
				.param("message_id", messageId)
				.param("keyword_id", keywordId)
				.param("keyword", keyword)
				.param("number", number)
				.param("msisdn", msisdn)
				.param("page", page)
				.param("max", max)
				.param("include_original", incluldeOriginal)
				.execute();
	}

	public Map<?, ?> getUserSMSResponses(String start, String end,
			Integer page, Integer max, OnlyOmitBoth keywords, Boolean includeOriginal) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getSMSSent(String messageId, OnlyOmitInclude optouts, Integer page, Integer max,
			DeliveryStatus delivery) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> cancelSMS(String messageId) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getNumber(String number) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getNumbers(NumberFilter filter, Integer page, Integer max) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> leaseNumber(String number) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> addKeyword(String keyword, String number, String reference, String listId, String welcomeMessage,
			String membersMessage, Boolean activate, String forwardURL, List<String> forwardEmail,
			List<String> forwardSMS) throws BurstSMSException {

		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> editKeyword(String keyword, String number, String reference, String listId, String welcomeMessage,
			String membersMessage, Boolean activate, String forwardURL, List<String> forwardEmail,
			List<String> forwardSMS) throws BurstSMSException {

		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getKeywords(String number, Integer page, Integer max) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> removeList(String listId) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getList(String listId, MemberSelection members, Integer page, Integer max)
			throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getLists(Integer page, Integer max) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> addList(String listName, List<String> fieldNames) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> addToList(String listId, String number, String firstName, String lastName,
			Map<String, String> fields, CountryCode countryCode) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> addFieldToList(String listId, Map<String, String> fields) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> deleteFromList(String listId, String number) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> optOutListMember(String listId, String number) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> editListMember(String listId, String number, String firstName, String lastName,
			Map<String, String> fields) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> addEmail(String email, Integer maxSMS, String number) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> deleteEmail(String email) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getClient(String clientId) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getClients(Integer page, Integer max) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> addClient(String listName, String contactName, String email, String password, String number,
			String timezone, Boolean clientPays, Double smsMargin, Double numberMargin) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> editClient(String clientId, String clientName, String contactName, String email, String password,
			String number, String timezone, Boolean clientPays, Double smsMargin) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getTransactions(String clientId, String start, String end, 
			Integer page, Integer max) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getTransaction(String transactionId) throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	public Map<?, ?> getBalance() throws BurstSMSException {
		throw new IllegalStateException("Not implemented");
	}

	//** helper classes **//
	
    private class RequestBuilder {
    	private WebResource webResource;
    	
    	public RequestBuilder(String path) {
    		webResource = getApiResource().path(path);
    	}
    	
    	public RequestBuilder param(String key, Object value) {
    		if (value != null) {
    			if (value instanceof List)
    				webResource = webResource.queryParam(key, listToString((List<?>) value));
    			else
    				webResource = webResource.queryParam(key, value.toString());
    		}
    		return this;
    	}
    	    	   
        @SuppressWarnings("unused")
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
        				str += "," + val.toString();
        		}
        	}
        	return str;
        }

        public Map<?, ?> execute() throws BurstSMSException {
        	return execute(Map.class);
        }
        
    	public <T> T execute(Class<T> responseClass) throws BurstSMSException {
    		logger.info("About to invoke: " + webResource.getURI());
    		
    		ClientResponse clientResponse = webResource
    				.accept(MediaType.APPLICATION_JSON)
    				.method("GET", ClientResponse.class);

    		if (clientResponse.getStatus() >= 200 && clientResponse.getStatus() < 300) {
    			return clientResponse.getEntity(responseClass);
    		} else {
    			clientResponse.bufferEntity();
    			try {
    				@SuppressWarnings("unchecked")
					Map<String,String> errorDetails = 
						(Map<String, String>) clientResponse.getEntity(Map.class).get("error");
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
    }
}
