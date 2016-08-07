package org.mule.modules.burstsms;

import java.util.List;
import java.util.Map;

import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.display.Text;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.param.Email;
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
	
	public enum NumberFilter {
		OWNED, AVAILABLE
	}
	
	public enum MemberSelection {
		ACTIVE, INACTIVE, ALL, NONE
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
    		@Text String message,
    		@Optional String from,
    		@Optional String sendAt, // TODO: 
    		@Optional @Placement(order = 1, group = "Destination") List<String> to,
    		@Optional @Placement(order = 2, group = "Destination") CountryCode countryCode,
    		@Optional @Placement(order = 3, group = "Destination") Long listId,
    		@Optional @Placement(tab = "Advanced", group = "Callbacks")
    		@FriendlyName("Delivery Receipt Callback URL") String dlrCallback,
    		@Optional @Placement(tab = "Advanced", group = "Callbacks")
    		@FriendlyName("Reply Callback URL") String replyCallback,
    		@Optional @Placement(tab = "Advanced", group = "Other") Long validity,
    		@Optional @Placement(tab = "Advanced", group = "Other") String repliesToEmail,
    		@Optional @Placement(tab = "Advanced", group = "Other") Boolean fromShared) throws BurstSMSException {
    	
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
    		@Placement(order = 1) String number, 
    		@Placement(order = 2) CountryCode countryCode) throws BurstSMSException {
    	
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
    	return getBurstSMSClient().getSMS(messageId);
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
    	return getBurstSMSClient().getSMSStatus(messageId);
    }
    
    /**
	 * Pick up responses to messages you have sent. Filter by keyword or for just one phone number.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064243-get-sms-responses">get-sms-responses</a>
	 * @param messageId Message ID. Required if Keyword ID is not specified.
	 * @param keywordId Keyword ID. Required if Message ID is not specified.
	 * @param keyword Keyword
	 * @param responseNumber Filter results by response number. Required if keyword is set.
	 * @param mobileNumber Filter results by a particular mobile number
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @param includeOriginal Include text of original message
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-sms-responses", friendlyName = "Get SMS responses")
    public Map<?, ?> getSMSResponses(
    		@Optional @Placement(order = 1) String messageId,
    		@Optional @Placement(order = 2) String keywordId,
    		@Optional @Placement(order = 3) String keyword,
    		@Optional @Placement(order = 4) String responseNumber,
    		@Optional @Placement(order = 5) String mobileNumber,
    		@Optional @Placement(order = 6) Boolean includeOriginal,
    		@Optional @Placement(group = "Pagination", order = 1) Integer page,
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
		
		if (messageId == null && keywordId == null)
			throw new IllegalArgumentException("One of [messageId, keywordId] must be specified.");
		if (keyword != null && responseNumber == null)
			throw new IllegalArgumentException("responseNumber must be specified when keyword is.");
		
    	return getBurstSMSClient().getSMSResponses(messageId, keywordId, keyword, responseNumber, 
    			mobileNumber, page, max, includeOriginal);
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
    		@Optional @Placement(group = "Reporting Period", order = 1) String start, // TODO:
    		@Optional @Placement(group = "Reporting Period", order = 2) String end, // TODO:
    		@Optional @Placement(group = "Options", order = 1) OnlyOmitBoth keywords, 
    		@Optional @Placement(group = "Options", order = 2) Boolean includeOriginal, 
    		@Optional @Placement(group = "Pagination", order = 1) Integer page, 
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getUserSMSResponses(start, end, page, max, keywords, includeOriginal);
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
    		@Placement(order = 1) String messageId, 
    		@Optional @Placement(order = 2) OnlyOmitInclude optouts, 
    		@Optional @Placement(order = 3) DeliveryStatus delivery, 
    		@Optional @Placement(group = "Pagination", order = 1) Integer page, 
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getSMSSent(messageId, optouts, page, max, delivery);
    }
    
    /**
	 * Cancel a message you have scheduled to be sent in the future.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/200977465-cancel-sms">cancel-sms</a>
	 * @param messageId The message ID
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "cancel-sms", friendlyName = "Cancel scheduled SMS")
    public Map<?, ?> cancelSMS(String messageId)  throws BurstSMSException {
    	return getBurstSMSClient().cancelSMS(messageId);
    }
    
    /* *** Number API methods *** */
	
    /**
	 * Get detailed information about a response number you have leased
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064593-get-number">get-number</a>
	 * @param number The virtual number to retrieve
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-number", friendlyName = "Get leased number information")
    public Map<?, ?> getNumber(String number)  throws BurstSMSException {
    	return getBurstSMSClient().getNumber(number);
    }
    
    /**
	 * Get a list of numbers either leased by you or available to be leased
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202108026-get-numbers">get-numbers</a>
	 * @param filter Possible values are:
	 *               <ul>
	 *                 <li>OWNED: retrieve your own response numbers (default)
	 *                 <li>AVAILABLE: retrieve response numbers available for purchase
	 *               </ul>
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-numbers", friendlyName = "Get leased and available numbers")
    public Map<?, ?> getNumbers(
    		@Optional NumberFilter filter, 
    		@Optional @Placement(group = "Pagination", order = 1) Integer page,
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getNumbers(filter, page, max);
    }
    
    /**
	 * Lease a dedicated virtual number
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202108016-lease-number">lease-number</a>
	 * @param number The virtual number to lease. Omit this field to be given a random number. 
	 *               Use get-numbers to find out which numbers are currently available.
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "lease-number", friendlyName = "Lease a virtual number")
    public Map<?, ?> leaseNumber(@Optional String number)  throws BurstSMSException {
    	return getBurstSMSClient().leaseNumber(number);
    }
    
    /* *** Keyword API methods *** */
	
    /**
	 * Add a keyword to an existing virtual number.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064643-add-keyword">add-keyword</a>
	 * @param keyword The first word of a text message
	 * @param number The dedicated virtual number that the keyword belongs to
	 * @param reference Your own reference (up to 100 characters)
	 * @param listId ID of a list to add respondents to, list ID's can be found in the title of a 
	 *               list or in the list page URL
	 * @param welcomeMessage SMS message to send to new members
	 * @param membersMessage SMS message to existing members
	 * @param activate Whether to make the keyword active immediately.
	 * @param forwardURL Forward messages to a URL
	 * @param forwardEmail Forward messages to a set of email addresses
	 * @param forwardSMS Forward messages to a set of mobile numbers
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-keyword", friendlyName = "Add a keyword to a virtual number")
    public Map<?, ?> addKeyword(
    		@Placement(order = 1) String keyword,
    		@Placement(order = 2) String number,
    		@Optional @Placement(order = 3) String reference,
    		@Optional @Placement(order = 4) String listId,
    		@Optional @Text @Placement(order = 5) String welcomeMessage,
    		@Optional @Text @Placement(order = 6) String membersMessage,
    		@Optional @Placement(order = 7) Boolean activate,
    		@Optional @Placement(group = "Forwarding", order = 1) String forwardURL,
    		@Optional @Placement(group = "Forwarding", order = 2) List<String> forwardEmail,
    		@Optional @Placement(group = "Forwarding", order = 3) List<String> forwardSMS)  throws BurstSMSException {
		
		if (reference != null && reference.length() > 100)
				throw new IllegalArgumentException("Reference must be less than 100 characters.");
		
    	return getBurstSMSClient().addKeyword(keyword, number, reference, listId, welcomeMessage, 
    			membersMessage, activate, forwardURL, forwardEmail, forwardSMS);
    }
    
    /**
	 * Edit an existing keyword
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064623-edit-keyword">edit-keyword</a>
	 * @param keyword The first word of a text message
	 * @param number The dedicated virtual number that the keyword belongs to
	 * @param reference Your own reference (up to 100 characters)
	 * @param listId ID of a list to add respondents to, list ID's can be found in the title of a 
	 *               list or in the list page URL
	 * @param welcomeMessage SMS message to send to new members
	 * @param membersMessage SMS message to existing members
	 * @param activate Whether to make the keyword active immediately.
	 * @param forwardURL Forward messages to a URL
	 * @param forwardEmail Forward messages to a set of email addresses
	 * @param forwardSMS Forward messages to a set of mobile numbers
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "edit-keyword", friendlyName = "Edit an existing keyword")
    public Map<?, ?> editKeyword(
    		@Placement(order = 1) String keyword,
    		@Placement(order = 2) String number,
    		@Optional @Placement(order = 3) String reference,
    		@Optional @Placement(order = 4) String listId,
    		@Optional @Text @Placement(order = 5) String welcomeMessage,
    		@Optional @Text @Placement(order = 6) String membersMessage,
    		@Optional @Placement(order = 7) Boolean activate,
    		@Optional @Placement(group = "Forwarding", order = 1) String forwardURL,
    		@Optional @Placement(group = "Forwarding", order = 2) List<String> forwardEmail,
    		@Optional @Placement(group = "Forwarding", order = 3) List<String> forwardSMS)  throws BurstSMSException {

		if (reference != null && reference.length() > 100)
			throw new IllegalArgumentException("Reference must be less than 100 characters.");

		return getBurstSMSClient().editKeyword(keyword, number, reference, listId, welcomeMessage, 
				membersMessage, activate, forwardURL, forwardEmail, forwardSMS);
    }
    
    /**
	 * Get a list of existing keywords
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202501158-get-keywords">get-keywords</a>
	 * @param number Filter the list by virtual number
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-keywords", friendlyName = "Get a list of existing keywords")
    public Map<?, ?> getKeywords(
    		@Optional String number,
    		@Optional @Placement(group = "Pagination", order = 1) Integer page,
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getKeywords(number, page, max);
    }
    
    /* *** List API methods *** */
	
    /**
	 * Delete a list and its members.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/205046159-remove-list">remove-list</a>
	 * @param listId The ID of the list to remove
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "remove-list", friendlyName = "Delete a list and members")
    public Map<?, ?> removeList(String listId)  throws BurstSMSException {
    	return getBurstSMSClient().removeList(listId);
    }
    
    /**
	 * Get information about a list and its members
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202500838-get-list">get-list</a>
	 * @param listId The list to retrieve
	 * @param members Which types of members to return. Possible values:
	 *                <ul>
	 *                <li>ACTIVE: only get active members (default)
	 *                <li>INACTIVE: only get inactive members
	 *                <li>ALL: get active and inactive members
	 *                <li>NONE: do not get any members, just metadata
	 *                </ul>
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page	
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-list", friendlyName = "Get list and member information")
    public Map<?, ?> getList(
    		@Placement(order = 1) String listId,
    		@Optional @Placement(order = 2) MemberSelection members,
    		@Optional @Placement(group = "Pagination", order = 1) Integer page,
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getList(listId, members, page, max);
    }
    
    /**
	 * Get the metadata of all your lists.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064413-get-lists">get-lists</a>
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page	
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-lists", friendlyName = "Get metadata of all lists")
    public Map<?, ?> getLists(
    		@Optional @Placement(group = "Pagination", order = 1) Integer page,
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getLists(page, max);
    }
    
    /**
	 * Create a new list including the ability to add custom fields.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202102716-add-list">add-list</a>
	 * @param listName A unique name for the list
	 * @param fieldNames A list of up to 10 custom field names. 
	 *                   Once field names have been set they cannot be changed.
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-list", friendlyName = "Create a new list")
    public Map<?, ?> addList(
    		@Placement(order = 1) String listName,
    		@Optional @Placement(order = 2) List<String> fieldNames)  throws BurstSMSException {
		
		if (fieldNames != null && fieldNames.size() > 10)
			throw new IllegalArgumentException("A maximum of 10 custom fields may be specified.");
		
    	return getBurstSMSClient().addList(listName, fieldNames);
    }
    
    /**
	 * Add a member to a list.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/203139007-add-to-list">add-to-list</a>
	 * @param listId ID of the list to add to
	 * @param number Mobile number of the member
	 * @param firstName First name of the member
	 * @param lastName Last name of the member
	 * @param fields Custom fields to set. Key is either a number (1 to 10) or of the format 
	 *               <pre>field.name</pre> to use the names of the custom fields you have 
	 *               chosen for your list, e.g. field.birthday.
	 * @param countryCode Formats number for the given country code
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-to-list", friendlyName = "Add a member to a list")
    public Map<?, ?> addToList(
    		@Placement(order = 1) String listId,
    		@Placement(order = 2) String number,
    		@Optional @Placement(order = 3) CountryCode countryCode,
    		@Optional @Placement(order = 4) String firstName,
    		@Optional @Placement(order = 5) String lastName,
    		@Optional @Placement(order = 6) Map<String, String> fields)  throws BurstSMSException {
    	return getBurstSMSClient().addToList(listId, number, firstName, lastName, fields, countryCode);
    }
    
    /**
	 * Update or add custom fields to a list
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/206272695-add-field-to-list">add-field-to-list</a>
	 * @param listId ID of the list to add to
	 * @param fields Custom fields to set. Key is either a number (1 to 10) or of the format 
	 *               <pre>field.name</pre> to use the names of the custom fields you have 
	 *               chosen for your list, e.g. field.birthday.
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-field-to-list", friendlyName = "Update or add a custom field to a list")
    public Map<?, ?> addFieldToList(
    		@Placement(order = 1) String listId,
    		@Placement(order = 2) Map<String, String> fields)  throws BurstSMSException {
    	return getBurstSMSClient().addFieldToList(listId, fields);
    }
    
    /**
	 * Remove a member from one list or all lists
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064463-delete-from-list">delete-from-list</a>
	 * @param listId ID of the list to remove from. If set to 0 (zero) the member will be removed from all lists.
	 * @param number Mobile number of the member
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "delete-from-list", friendlyName = "Remove a member from one or all lists")
    public Map<?, ?> deleteFromList(
    		@Placement(order = 1) String listId, 
    		@Placement(order = 2) String number)  throws BurstSMSException {
    	return getBurstSMSClient().deleteFromList(listId, number);
    }
    
    /**
	 * Opt a user out of one list or all lists.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202501008-optout-list-member">optout-list-member</a>
	 * @param listId ID of the list to remove from. If set to 0 (zero) the member will be removed from all lists.
	 * @param number Mobile number of the member
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "optout-list-member", friendlyName = "Opt a user out of one or all lists")
    public Map<?, ?> optOutListMember(
    		@Placement(order = 1) String listId, 
    		@Placement(order = 2) String number)  throws BurstSMSException {
    	return getBurstSMSClient().optOutListMember(listId, number);
    }
    
    /**
	 * Edit a member of a list
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202107886-edit-list-member">edit-list-member</a>
	 * @param listId ID of the list the member belongs to
	 * @param number Mobile number of the member to edit
	 * @param firstName First name of the member
	 * @param lastName Last name of the member
	 * @param fields Custom fields to set. Key is either a number (1 to 10) or of the format 
	 *               <pre>field.name</pre> to use the names of the custom fields you have 
	 *               chosen for your list, e.g. field.birthday.
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "edit-list-member", friendlyName = "Edit a list member")
    public Map<?, ?> editListMember(
    		@Placement(order = 1) String listId, 
    		@Placement(order = 2) String number,
    		@Optional @Placement(order = 3) String firstName,
    		@Optional @Placement(order = 4) String lastName,
    		@Optional @Placement(order = 5) Map<String, String> fields) throws BurstSMSException {
    	return getBurstSMSClient().editListMember(listId, number, firstName, lastName, fields);
    }

    /* SMS Email API methods */
	
    /**
	 * Authorise an email address for sending Email to SMS
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/203139127-add-email">add-email</a>
	 * @param email Email address to register. You may also register a wild-card email which 
	 *              allows any user on the same domain to use Email to SMS.
	 *              <p>Wild-card format: <pre>*@example.com</pre>
	 * @param maxSMS The maximum number of SMS messages to send from one email message 
	 *               sent from this email address.
	 *               Possible values:
	 *               <ol>
	 *               <li>up to 160 characters (default)
	 *               <li>up to 306 characters
	 *               <li>up to 459 characters
	 *               <li>up to 612 characters
	 *               </ol>
	 * @param number Optional dedicated virtual number
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-email", friendlyName = "Authorise an email address for sending Email to SMS")
    public Map<?, ?> addEmail(
    		@Placement(order = 1) String email,
    		@Optional @Placement(order = 2) Integer maxSMS,
    		@Optional @Placement(order = 3) String number)  throws BurstSMSException {
		
		if (maxSMS != null && (maxSMS < 1 || maxSMS > 4))
			throw new IllegalArgumentException("maxSMS must be >= 1 and <= 4");
		
    	return getBurstSMSClient().addEmail(email, maxSMS, number);
    }
    
    /**
	 * Remove an email address from the Email to SMS authorised list.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202064603-delete-email">delete-email</a>
	 * @param email Email address to remove. You may also use a wild-card email which 
	 *              removes all emails on that domain.
	 *              <p>Wild-card format: <pre>*@example.com</pre>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "delete-email", friendlyName = "Remove an email address from the Email to SMS authorisation")
    public Map<?, ?> deleteEmail(String email)  throws BurstSMSException {
    	return getBurstSMSClient().deleteEmail(email);
    }
    

    /* *** Reseller API methods *** */
	
    /**
	 * Get detailed information about a client
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202091863-get-client">get-client</a>
	 * @param clientId The ID of the client
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-client", friendlyName = "Get client information")
    public Map<?, ?> getClient(String clientId)  throws BurstSMSException {
    	return getBurstSMSClient().getClient(clientId);
    }
    
    /**
	 * Get a list of all clients.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/203145737-get-clients">get-clients</a>
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-clients", friendlyName = "Get a list of all clients")
    public Map<?, ?> getClients(
    		@Optional @Placement(group = "Pagination", order = 1) Integer page, 
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getClients(page, max);
    }
    
    /**
	 * Add a new client.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202116806-add-client">add-client</a>
	 * @param companyName Client company name
	 * @param contactName Contact name
	 * @param clientEmail Client email address
	 * @param clientPassword Client password
	 * @param clentNumber Client phone number
	 * @param timezone A valid timezone, Australia/Sydney. Defaults to your own
	 * @param clientPays Set to true if the client will pay (the default) or false if you will pay
	 * @param smsMargin The number of cents to add to the base SMS price. A decimal value.
	 * @param numberMargin The number of cents to add to the base number price. A decimal value
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "add-client", friendlyName = "Add a new client")
    public Map<?, ?> addClient(
    		@Placement(order = 1, group = "Client Details") String companyName,
    		@Placement(order = 2, group = "Client Details") @Optional String contactName,
    		@Placement(order = 3, group = "Client Details") @Email String clientEmail,
    		@Placement(order = 4, group = "Client Details") String clientPassword,
    		@Placement(order = 5, group = "Client Details") String clentNumber,
    		@Placement(order = 6, group = "Client Details") @Optional String timezone,
    		@Placement(order = 1, group = "Payment Details") @Optional Boolean clientPays,
    		@Placement(order = 2, group = "Payment Details") @Optional Double smsMargin,
    		@Placement(order = 3, group = "Payment Details") @Optional Double numberMargin)  throws BurstSMSException {
    	return getBurstSMSClient().addClient(companyName, contactName, clientEmail, clientPassword, clentNumber, 
    			timezone, clientPays, smsMargin, numberMargin);
    }
    
    /**
	 * Edit an existing client
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202091853-edit-client">edit-client</a>
	 * @param clientId The ID of the client
	 * @param companyName Client company name. Must be unique
	 * @param contactName Contact name
	 * @param contactEmail Client email address
	 * @param clientPassword Client password
	 * @param clentNumber Client phone number
	 * @param timezone A valid timezone, Australia/Sydney. Defaults to your own
	 * @param clientPays Set to true if the client will pay (the default) or false if you will pay
	 * @param smsMargin The number of cents to add to the base SMS price. A decimal value.
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "edit-client", friendlyName = "Edit an existing client")
    public Map<?, ?> editClient(
    		@Placement(order = 1, group = "Client Details") String clientId,
    		@Placement(order = 2, group = "Client Details") String companyName,
    		@Placement(order = 3, group = "Client Details") @Optional String contactName,
    		@Placement(order = 4, group = "Client Details") @Email String contactEmail,
    		@Placement(order = 5, group = "Client Details") String clientPassword,
    		@Placement(order = 6, group = "Client Details") String clentNumber,
    		@Placement(order = 7, group = "Client Details") @Optional String timezone,
    		@Placement(order = 1, group = "Payment Details") @Optional Boolean clientPays,
    		@Placement(order = 2, group = "Payment Details") @Optional Double smsMargin)  throws BurstSMSException {

    	return getBurstSMSClient().editClient(clientId, companyName, contactName, contactEmail, clientPassword, 
    			clentNumber, timezone, clientPays, smsMargin);
    }
    
    /**
	 * Get a list of transactions for a client.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202510058-get-transactions">get-transactions</a>
	 * @param clientId Only retrieve records for a particular client
 	 * @param start A timestamp to start the report from
	 * @param end A timestamp to end the report at
	 * @param page Page number, for pagination
	 * @param max Maximum results returned per page
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-transactions", friendlyName = "Get a transaction list for a client")
    public Map<?, ?> getTransactions(
    		String clientId,
    		@Optional @Placement(group = "Reporting Period", order = 1) String start, // TODO:
    		@Optional @Placement(group = "Reporting Period", order = 2) String end, // TODO:
    		@Optional @Placement(group = "Pagination", order = 1) Integer page, 
    		@Optional @Placement(group = "Pagination", order = 2) Integer max)  throws BurstSMSException {
    	return getBurstSMSClient().getTransactions(clientId, start, end, page, max);
    }
    
    /**
	 * Get information about a transaction
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/202108046-get-transaction">get-transaction</a>
	 * @param transactionId The transaction id
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-transaction", friendlyName = "Get a information about a transaction")
    public Map<?, ?> getTransaction(String transactionId)  throws BurstSMSException {
    	return getBurstSMSClient().getTransaction(transactionId);
    }
	
	/* *** Account API methods *** */
	
    /**
	 * Get a summary of your account balance.
	 * @api.doc <a href="http://support.burstsms.com/hc/en-us/articles/200698319-get-balance">get-balance</a>
	 * @return The response, as a map
	 * @throws BurstSMSException If the API call fails for any reason
	 */
	@Processor(name = "get-balance", friendlyName = "Get a summary of your account balance")
    public Map<?, ?> getBalance()  throws BurstSMSException {
    	return getBurstSMSClient().getBalance();
    }
}