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
		return new RequestBuilder("get-user-sms-responses.json")
				.param("start", start)
				.param("end", end)
				.param("page", page)
				.param("max", max)
				.param("keywords", keywords)
				.param("include_original", includeOriginal)
				.execute();
	}

	public Map<?, ?> getSMSSent(String messageId, OnlyOmitInclude optouts, Integer page, Integer max,
			DeliveryStatus delivery) throws BurstSMSException {
		return new RequestBuilder("get-sms-sent.json")
				.param("message_id", messageId)
				.param("optouts", optouts)
				.param("page", page)
				.param("max", max)
				.param("delivery", delivery)
				.execute();
	}

	public Map<?, ?> cancelSMS(String messageId) throws BurstSMSException {
		return new RequestBuilder("cancel-sms.json")
				.param("message_id", messageId).execute();
	}

	public Map<?, ?> getNumber(String number) throws BurstSMSException {
		return new RequestBuilder("get-number.json")
				.param("number", number).execute();
	}

	public Map<?, ?> getNumbers(NumberFilter filter, Integer page, Integer max) throws BurstSMSException {
		return new RequestBuilder("get-numbers.json")
				.param("filter", filter)
				.param("page", page)
				.param("max", max)
				.execute();
	}

	public Map<?, ?> leaseNumber(String number) throws BurstSMSException {
		return new RequestBuilder("lease-number.json")
				.param("number", number).execute();
	}

	public Map<?, ?> addKeyword(String keyword, String number, String reference, String listId, String welcomeMessage,
			String membersMessage, Boolean activate, String forwardURL, List<String> forwardEmail,
			List<String> forwardSMS) throws BurstSMSException {

		return new RequestBuilder("add-keyword.json")
				.param("keyword", keyword)
				.param("number", number)
				.param("reference", reference)
				.param("list_id", listId)
				.param("welcome_message", welcomeMessage)
				.param("members_message", membersMessage)
				.param("activate", activate)
				.param("forward_url", forwardURL)
				.param("forward_email", forwardEmail)
				.param("forward_sms", forwardSMS)
				.execute();
	}

	public Map<?, ?> editKeyword(String keyword, String number, String reference, String listId, String welcomeMessage,
			String membersMessage, Boolean activate, String forwardURL, List<String> forwardEmail,
			List<String> forwardSMS) throws BurstSMSException {

		return new RequestBuilder("edit-keyword.json")
				.param("keyword", keyword)
				.param("number", number)
				.param("reference", reference)
				.param("list_id", listId)
				.param("welcome_message", welcomeMessage)
				.param("members_message", membersMessage)
				.param("activate", activate)
				.param("forward_url", forwardURL)
				.param("forward_email", forwardEmail)
				.param("forward_sms", forwardSMS)
				.execute();
	}

	public Map<?, ?> getKeywords(String number, Integer page, Integer max) throws BurstSMSException {
		return new RequestBuilder("get-keywords.json")
				.param("number", number)
				.param("page", page)
				.param("max", max)
				.execute();
	}

	public Map<?, ?> removeList(String listId) throws BurstSMSException {
		return new RequestBuilder("remove-list.json")
				.param("list_id", listId).execute();
	}

	public Map<?, ?> getList(String listId, MemberSelection members, Integer page, Integer max)
			throws BurstSMSException {
		return new RequestBuilder("get-list.json")
				.param("list_id", listId)
				.param("members", members)
				.param("page", page)
				.param("max", max)
				.execute();
	}

	public Map<?, ?> getLists(Integer page, Integer max) throws BurstSMSException {
		return new RequestBuilder("get-lists.json")
				.param("page", page)
				.param("max", max)
				.execute();
	}

	public Map<?, ?> addList(String listName, List<String> fieldNames) throws BurstSMSException {
		RequestBuilder builder = new RequestBuilder("add-list.json")
				.param("name", listName);
		
		for (int i = 0; (fieldNames != null) && (i < fieldNames.size()); i++)
			builder.param("field_" + (i + 1), fieldNames.get(i));
		
		return builder.execute();
	}

	public Map<?, ?> addToList(String listId, String number, String firstName, String lastName,
			Map<String, String> fields, CountryCode countryCode) throws BurstSMSException {

		RequestBuilder builder = new RequestBuilder("add-to-list.json")
				.param("list_id", listId)
				.param("msisdn", number)
				.param("first_name", firstName)
				.param("last_name", lastName)
				.param("countrycode", countryCode);
		
		if (fields != null) {
			for (String key: fields.keySet()) {
				String name;
				if (key.matches("\\d{1,2}"))
					name = "field_" + key;
				else
					name = "field." + key;
				builder.param(name, fields.get(key));
			}
		}
		
		return builder.execute();
	}

	public Map<?, ?> addFieldToList(String listId, Map<String, String> fields) throws BurstSMSException {
		RequestBuilder builder = new RequestBuilder("add-field-to-list.json")
				.param("list_id", listId);
		
		if (fields != null) {
			for (String key: fields.keySet()) {
				String name;
				if (key.matches("\\d{1,2}"))
					name = "field_" + key;
				else
					name = "field." + key;
				builder.param(name, fields.get(key));
			}
		}
		
		return builder.execute();
	}

	public Map<?, ?> deleteFromList(String listId, String number) throws BurstSMSException {
		return new RequestBuilder("delete-from-list.json")
				.param("list_id", listId)
				.param("msisdn", number)
				.execute();
	}

	public Map<?, ?> optOutListMember(String listId, String number) throws BurstSMSException {
		return new RequestBuilder("optout-list-member.json")
				.param("list_id", listId)
				.param("msisdn", number)
				.execute();				
	}

	public Map<?, ?> editListMember(String listId, String number, String firstName, String lastName,
			Map<String, String> fields) throws BurstSMSException {

		RequestBuilder builder = new RequestBuilder("add-to-list.json")
				.param("list_id", listId)
				.param("msisdn", number)
				.param("first_name", firstName)
				.param("last_name", lastName);
		
		if (fields != null) {
			for (String key: fields.keySet()) {
				String name;
				if (key.matches("\\d{1,2}"))
					name = "field_" + key;
				else
					name = "field." + key;
				builder.param(name, fields.get(key));
			}
		}
		
		return builder.execute();
	}

	public Map<?, ?> addEmail(String email, Integer maxSMS, String number) throws BurstSMSException {
		return new RequestBuilder("add-email.json")
				.param("email", email)
				.param("max-sms", maxSMS)
				.param("number", number)
				.execute();
	}

	public Map<?, ?> deleteEmail(String email) throws BurstSMSException {
		return new RequestBuilder("delete-email.json")
				.param("email", email).execute();
	}

	public Map<?, ?> getClient(String clientId) throws BurstSMSException {
		return new RequestBuilder("get-client.json")
				.param("client_id", clientId).execute();
	}

	public Map<?, ?> getClients(Integer page, Integer max) throws BurstSMSException {
		return new RequestBuilder("get-clients.json")
				.param("page", page)
				.param("max", max)
				.execute();
	}

	public Map<?, ?> addClient(String clientName, String contactName, String email, 
			String password, String number, String timezone, Boolean clientPays, 
			Double smsMargin, Double numberMargin) throws BurstSMSException {

		return new RequestBuilder("add-client.json")
				.param("name", clientName)
				.param("contact", contactName)
				.param("email", email)
				.param("password", password)
				.param("msisdn", number)
				.param("timezone", timezone)
				.param("client_pays", clientPays)
				.param("sms_margin", smsMargin)
				.param("number_margin", numberMargin)
				.execute();
	}

	public Map<?, ?> editClient(String clientId, String clientName, String contactName, 
			String email, String password, String number, String timezone, 
			Boolean clientPays, Double smsMargin) throws BurstSMSException {

		return new RequestBuilder("edit-client.json")
				.param("name", clientName)
				.param("contact", contactName)
				.param("email", email)
				.param("password", password)
				.param("msisdn", number)
				.param("timezone", timezone)
				.param("client_pays", clientPays)
				.param("sms_margin", smsMargin)
				.execute();
	}

	public Map<?, ?> getTransactions(String clientId, String start, String end, 
			Integer page, Integer max) throws BurstSMSException {
		return new RequestBuilder("get-transactions.json")
				.param("client_id", clientId)
				.param("start", start)
				.param("end", end)
				.param("page", page)
				.param("max", max)
				.execute();
	}

	public Map<?, ?> getTransaction(String transactionId) throws BurstSMSException {
		return new RequestBuilder("get-transaction.json")
				.param("transaction_id", transactionId)
				.execute();
	}

	public Map<?, ?> getBalance() throws BurstSMSException {
		return new RequestBuilder("get-balance.json").execute();
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
