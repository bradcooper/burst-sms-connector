package org.mule.modules.burstsms;

import java.util.List;
import java.util.Map;

import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.display.Summary;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.param.Optional;
import org.mule.modules.burstsms.config.ConnectorConfig;

/**
 * Provides access to the Burst SMS REST API from within a Mule application
 * 
 * @see <a href="http://www.burstsms.com.au/">Burst SMS</a>
 * @see <a href="http://support.burstsms.com/hc/en-us/categories/200154016-API-Documentation">API Documentation</a>
 * 
 * @author Brad Cooper
 */
@Connector(
		name="burst-sms", 
		friendlyName="BurstSMS", 
		description = "Access the Burst SMS API to send and manage SMS messages",
		keywords="burstsms, burst, sms")
public class BurstSMSConnector {

	public enum CountryCode {
		AU, NZ, SG, GB, US
	}

	public enum OnlyOmitBoth {
		ONLY, OMIT, BOTH
	}
	
	public enum OnlyOmitInclude {
		ONLY, OMIT, INCLUDE
	}

	public enum DeliveryStatus {
		DELIVERED, FAILED, PENDING
	}
	
	private BurstSMSClient burstSMSClient;

    @Config
    private ConnectorConfig config;
    
    public ConnectorConfig getConfig() {
		return config;
	}
    
    public void setConfig(ConnectorConfig config) {
		this.config = config;
	}
    
    public BurstSMSClient getBurstSMSClient() {
		return burstSMSClient;
	}
    
    public void setBurstSMSClient(BurstSMSClient client) {
		this.burstSMSClient = client;
	}

    @Start
    public void init() {
        setBurstSMSClient(new BurstSMSClient(this));
    }

    /* *** SMS API methods *** */
    
    /**
     * The Send-SMS call is the primary method of sending SMS.
     * <p>
     * You can elect to pass the recipient numbers from your database each time you make a call, 
     * or you can elect to store recipient data in a contact list and submit only the list_id to 
     * trigger the send. This is best for large databases. To add a list please refer to the add-list call.
     * <p>
     * Cost data is returned in the major unit of your account currency, e.g. dollars or pounds.
     * <p>
     * NOTE: If you do not pass the from option the messages will be sent from the shared number pool, 
     * unless you have a leased number on your account in which case it will be set as the Caller ID
     * <p>
     * When you set a alphanumeric for Caller ID, messages cannot be replied to
     * 
     * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202500828-send-sms">send-sms</a>
     * @param to List of up to 10,000 numbers to send the SMS to. 
     *           If your number set has some invalid numbers, they won’t cause validation error, 
     *           but will be returned as ‘fails’ parameter of the response.
     *           Number must be defined in international format, for example: 
     *           AU: 61491570156, NZ: 64212670129, SG: 6598654321, UK: 44750017696, US: 1213811413
     * @param countryCode Formats numbers given to international format for this 2 letter country code. 
     *           i.e. 0422222222 will become 6142222222 when countrycode is AU.
     * @param listId This ID is the numerical reference to one of your recipient lists
     * @param message Message text
     * @param from Set the alphanumeric Caller ID
     * @param sendAt A time in the future to send the message
     * @param dlrCallback A URL on your system which we can call to notify you of Delivery Receipts. 
     *           If required, this Parameter can be different for each message sent and will take precedence 
     *           over the DLR Callback URL supplied by you in the API Settings.
     * @param replyCallback A URL on your system which we can call to notify you of incoming messages. 
     *           If required, this parameter can be different and will take precedence over the Reply 
     *           Callback URL supplied by you on the API Settings.
     * @param validity Specify the maximum time to attempt to deliver. In minutes, 0 (zero) implies no limit.
     * @param repliesToEmail Specify an email address to send responses to this message. NOTE: specified email 
     *           must be authorised to send messages via add-email or in your account under the 'Email SMS' section.
     * @param fromShared Forces sending via the shared number when you have virtual numbers
     * @return The response, as a map
     * @throws BurstSMSException If the API call fails for any reason
     */
    @Processor(name = "send-sms", friendlyName = "Send SMS")
    public Map<?,?> sendSMS(
    		@Optional
    		@Placement(order = 1, group = "Destination")
    		List<String> to,
    		@Optional
    		@Placement(order = 2, group = "Destination")
    		CountryCode countryCode,
    		@Optional
    		@Placement(order = 3, group = "Destination")
    		Long listId,
    		String message,
    		@Optional
    		String from,
    		@Optional 
    		String sendAt, // TODO: 
    		@Optional
    		@Placement(tab = "Advanced", group = "Callbacks")
    		@FriendlyName("Delivery Receipt Callback URL")
    		String dlrCallback,
    		@Optional
    		@Placement(tab = "Advanced", group = "Callbacks")
    		@FriendlyName("Reply Callback URL")
    		String replyCallback,
    		@Optional
    		@Placement(tab = "Advanced", group = "Other")
    		Long validity,
    		@Optional
    		@Placement(tab = "Advanced", group = "Other")
    		String repliesToEmail,
    		@Optional
    		@Placement(tab = "Advanced", group = "Other")
    		Boolean fromShared) throws BurstSMSException {
    	
    	return getBurstSMSClient().sendSMS(message, to, from, sendAt, listId, dlrCallback, replyCallback,
    			validity, repliesToEmail, fromShared, countryCode);
    }

    /**
     * Format and validate a given number.
     * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/203098949-format-number">format-number</a>
     * @param number The number to check
     * @param countryCode Country code to validate number against
     * @return The response, as a map
     * @throws BurstSMSException If the API call fails for any reason
     */
    @Processor(name = "format-number", friendlyName = "Format and validate number")
    public Map<?, ?> formatNumber(
    		@Placement(order = 1)
    		@Summary("The number to check")
    		String number, 
    		@Placement(order = 2)
    		@Summary("2 Letter countrycode to validate number against")
    		CountryCode countryCode) throws BurstSMSException {
    	
    	return getBurstSMSClient().formatNumber(number, countryCode);
    }
    
    /**
     * Get information about a message you have sent.
     * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202102676-get-sms">get-sms</a>
     * @param messageId Message ID
     * @return The response, as a map
     * @throws BurstSMSException  If the API call fails for any reason
     */
    @Processor(name = "get-sms", friendlyName = "Get SMS information")
    public Map<?, ?> getSMS(String messageId) throws BurstSMSException {
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
     * Get the status about a message you have sent.
     * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/205576699-get-sms-stats">get-sms-stats</a>
     * @param messageId Message ID
     * @return The response, as a map
     * @throws BurstSMSException  If the API call fails for any reason
     */
    @Processor(name = "get-sms-stats", friendlyName = "Get SMS status")
    public Map<?, ?> getSMSStatus(String messageId) throws BurstSMSException {
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * Pick up responses to messages you have sent. Filter by keyword or for just one phone number.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064243-get-sms-responses">get-sms-responses</a>
	 * @param messageId Message ID. Required if Keyword ID is not specified.
	 * @param keywordId Keyword ID. Required if Message ID is not specified.
	 * @param keyword Keyword
	 * @param number Filter results by response number. Required if keyword is set.
	 * @param msisdn Filter results by a particular mobile number
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @param incluldeOriginal Include text of original message
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-sms-responses", friendlyName = "Get SMS responses")
    public Map<?, ?> getSMSResponses(
    		@Optional
    		String messageId,
    		@Optional
    		String keywordId,
    		@Optional
    		String keyword,
    		@Optional
    		String number,
    		@Optional
    		String msisdn,
    		@Optional
    		Integer page,
    		@Optional
    		Integer max,
    		@Optional
    		Boolean incluldeOriginal)  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * Pick up responses to messages you have sent. 
	 * Instead of setting message ID, you should provide a time frame.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202494938-get-user-sms-responses">get-user-sms-responses</a>
	 * @param start A timestamp to start the report from
	 * @param end A timestamp to end the report at
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @param keywords Filter if keyword responses should be included. Can be:
	 *                 <ul>
	 *                   <li>ONLY: only keyword responses will be included
	 *                   <li>OMIT: only regular campaign responses will be included
	 *                   <li>BOTH: both keyword and campaign responses will be included (default)
	 *                 </ul>
	 * @param includeOriginal include text of original message
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-user-sms-responses", friendlyName = "Get user SMS responses")
    public Map<?, ?> getUserSMSResponses(
    		@Optional
    		String start, // TODO:
    		@Optional
    		String end, // TODO:
    		@Optional
    		Integer page, 
    		@Optional
    		Integer max, 
    		@Optional
    		OnlyOmitBoth keywords, 
    		@Optional
    		Boolean includeOriginal)  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * Get a list of recipients from a message send. Get up to date information such as opt-out status and delivery status.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202494888-get-sms-sent">get-sms-sent</a>
	 * @param messageId The message ID
	 * @param optouts Whether to include optouts. Valid options are:
	 *                <ul>
	 *                  <li>ONLY: only get optouts
	 *                  <li>OMIT: do not get optouts
	 *                  <li>INCLUDE: get all recipients including optouts (default)
	 *                </ul>
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @param delivery Only show messages with requested delivery status. Valid options are:
	 *                <ul>
	 *                  <li>DELIVERED: only show delivered messages
	 *                  <li>FAILED: only show failed messages
	 *                  <li>PENDING: only show pending messages
	 *                </ul>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-sms-sent", friendlyName = "Get SMS recipient information")
    public Map<?, ?> getSMSSent(
    		String messageId, 
    		@Optional
    		OnlyOmitInclude optouts, 
    		@Optional
    		Integer page, 
    		@Optional
    		Integer max, 
    		@Optional
    		DeliveryStatus delivery)  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * Cancel a message you have scheduled to be sent in the future.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/200977465-cancel-sms">cancel-sms</a>
	 * @param messageId The message ID
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "cancel-sms", friendlyName = "Cancel scheduled SMS")
    public Map<?, ?> cancelSMS(String messageId)  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /* *** Number API methods *** */
	
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-number", friendlyName = "Get leased number information")
    public Map<?, ?> getNumber()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-numbers", friendlyName = "Get leased and available numbers")
    public Map<?, ?> getNumbers()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "lease-number", friendlyName = "Least a virtual number")
    public Map<?, ?> leaseNumber()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /* Keyword API methods */
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-keyword", friendlyName = "Add a keyword to a virtual number")
    public Map<?, ?> addKeyword()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "edit-keyword", friendlyName = "Edit an existing keyword")
    public Map<?, ?> editKeyword()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-keywords", friendlyName = "Get a list of existing keywords")
    public Map<?, ?> getKeywords()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /* List API methods */
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "remove-list", friendlyName = "Delete a list and members")
    public Map<?, ?> removeList()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-list", friendlyName = "Get list and member information")
    public Map<?, ?> getList()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-lists", friendlyName = "Get metadata of all lists")
    public Map<?, ?> getLists()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-list", friendlyName = "Create a new list")
    public Map<?, ?> addList()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-to-list", friendlyName = "Add a member to a list")
    public Map<?, ?> addToList()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-field-to-list", friendlyName = "Update or add a custom field to a list")
    public Map<?, ?> addFieldToList()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "delete-from-list", friendlyName = "Remove a member from one or all lists")
    public Map<?, ?> deleteFromList()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "optout-list-member", friendlyName = "Opt a user out of one or all lists")
    public Map<?, ?> optOutListMember()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "edit-list-member", friendlyName = "Edit a list member")
    public Map<?, ?> editListMember()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }

    /* SMS Email API methods */
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-email", friendlyName = "Authorise email address for EMail -> SMS")
    public Map<?, ?> addEmail()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "delete-email", friendlyName = "Remove email address from EMail -> SMS")
    public Map<?, ?> deleteEmail()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    

    /* Reseller API methods */
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-client", friendlyName = "Get client information")
    public Map<?, ?> getClient()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-clients", friendlyName = "Get a list of all clients")
    public Map<?, ?> getClients()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-client", friendlyName = "Add a new client")
    public Map<?, ?> addClient()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "edit-client", friendlyName = "Edit an existing client")
    public Map<?, ?> editClient()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-transactions", friendlyName = "Get a transaction list for a client")
    public Map<?, ?> getTransactions()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
    
    /**
	 * 
	 * @api.doc <a href=""></a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-transaction", friendlyName = "Get a information about a transaction")
    public Map<?, ?> getTransaction()  throws BurstSMSException{
    	throw new IllegalStateException("Not implemented");
    }
}