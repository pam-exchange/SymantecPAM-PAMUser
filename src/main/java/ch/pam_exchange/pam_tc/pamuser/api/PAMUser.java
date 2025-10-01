/*******************************************************************************************************
 ****  Copyright (c) 2018 CA.  All rights reserved.  
 ****  This software and all information contained therein is confidential and proprietary and shall 
 ****  not be duplicated, used, disclosed or disseminated in any way except as authorized by the 
 ****  applicable license agreement, without the express written permission of CA. All authorized 
 ****  reproductions must be marked with this language.  
 ****  
 ****  EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE 
 ****  LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY 
 ****  IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA 
 ****  BE LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM 
 ****  THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION, 
 ****  GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 ********************************************************************************************************/

package ch.pam_exchange.pam_tc.pamuser.api;

import com.ca.pam.extensions.core.api.exception.ExtensionException;
import com.ca.pam.extensions.core.MasterAccount;
import com.ca.pam.extensions.core.model.LoggerWrapper;
import com.ca.pam.extensions.core.TargetAccount;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public class PAMUser {

	private static final Logger LOGGER = Logger.getLogger(PAMUser.class.getName());
	private static final boolean SHOW_PASWORD= false;
	
	/**
	 * 
	 * Constants
	 */
	private static final String FIELD_ACCOUNT_TYPE= "accountType";
	private static final String FIELD_ACCOUNT_TYPE_VALUE_APIKEY = "Apikey";
	private static final String FIELD_OTHER_ACCOUNT= "otherAccount";
	private static final String FIELD_CONNECTION_TIMEOUT= "connectionTimeout";
	private static final String FIELD_READ_TIMEOUT= "readTimeout";
	private static final String FIELD_PORT= "port";
	private static final String FIELD_IS_REMOTE= "isRemote";
	private static final String FIELD_APIKEY_HOSTNAME= "apikeyHostname";
	private static final String FIELD_APIKEY_APPNAME= "apikeyApplication";
	
	private static final int DEFAULT_PORT = 443;
	private static final long DEFAULT_CONNECT_TIMEOUT = 30000;
	private static final long DEFAULT_READ_TIMEOUT = 30000;
	private static final String DEFAULT_APIKEY_HOSTNAME= "apikey.xceedium.com";
	private static final String DEFAULT_APIKEY_APPNAME= "ApiKey";
	
	/**
	 * Instance variables used in the processCredentialsVerify and
	 * processCredentialsUpdate
	 */
	private String endpointHostname= "";
	private int endpointPort = DEFAULT_PORT;
	private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private long readTimeout = DEFAULT_READ_TIMEOUT;
	private String username = "";
	private String oldPassword = "";
	private String newPassword = "";
	private MasterAccount masterAccount = null;
	private String masterUsername = "";
	private String masterPassword = "";
	private String accountType= "";
	private boolean isApiKey = true;
	private boolean isRemote = false;
	private String apikeyHostname= "";
	private String apikeyAppName= "";
	
	public PAMUser(TargetAccount targetAccount) {

		/*
		 * Server attributes
		 * 
		 */
		this.endpointHostname = targetAccount.getTargetApplication().getTargetServer().getHostName();
		LOGGER.fine(LoggerWrapper.logMessage("endpointHostname= " + this.endpointHostname));

		/*
		 * Application attributes
		 * 
		 */
		try {
			this.endpointPort = Integer.parseUnsignedInt(targetAccount.getTargetApplication().getExtendedAttribute(FIELD_PORT));
		} catch (Exception e) {
			LOGGER.warning(LoggerWrapper.logMessage("Using default port"));
			this.endpointPort = DEFAULT_PORT;
		}
		LOGGER.fine(LoggerWrapper.logMessage("endpointPort= " + this.endpointPort));

		try {
			this.connectTimeout = Long.parseUnsignedLong(targetAccount.getTargetApplication().getExtendedAttribute(FIELD_CONNECTION_TIMEOUT));
		} catch (Exception e) {
			LOGGER.warning(LoggerWrapper.logMessage("Using default connectTimeout"));
			this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		}
		LOGGER.fine(LoggerWrapper.logMessage("connectTimeout= " + this.connectTimeout));

		try {
			this.readTimeout = Long.parseUnsignedLong(targetAccount.getTargetApplication().getExtendedAttribute(FIELD_READ_TIMEOUT));
		} catch (Exception e) {
			LOGGER.warning(LoggerWrapper.logMessage("Using default readTimeout"));
			this.readTimeout = DEFAULT_READ_TIMEOUT;
		}
		LOGGER.fine(LoggerWrapper.logMessage("readTimeout= " + this.readTimeout));

		this.isRemote= "true".equals(targetAccount.getTargetApplication().getExtendedAttribute(FIELD_IS_REMOTE));
		LOGGER.fine(LoggerWrapper.logMessage("isRemote= " + this.isRemote));
		
		this.apikeyHostname= targetAccount.getTargetApplication().getExtendedAttribute(FIELD_APIKEY_HOSTNAME);
		if (null==this.apikeyHostname || this.apikeyHostname.isBlank() || this.apikeyHostname.isEmpty()) {
			LOGGER.warning(LoggerWrapper.logMessage("apikeyHostname is empty, using default"));
			this.apikeyHostname= DEFAULT_APIKEY_HOSTNAME;
		}
		LOGGER.fine(LoggerWrapper.logMessage("apikeyHostname= " + this.apikeyHostname));
				
		this.apikeyAppName= targetAccount.getTargetApplication().getExtendedAttribute(FIELD_APIKEY_APPNAME);
		if (null==this.apikeyAppName || this.apikeyAppName.isBlank() || this.apikeyAppName.isEmpty()) {
			LOGGER.warning(LoggerWrapper.logMessage("apikeyAppName is empty, using default"));
			this.apikeyAppName= DEFAULT_APIKEY_APPNAME;
		}
		LOGGER.fine(LoggerWrapper.logMessage("apikeyApplication= " + this.apikeyAppName));
		
		/*
		 * Account attributes
		 * 
		 */
		this.username = targetAccount.getUserName();
		LOGGER.fine(LoggerWrapper.logMessage("username= " + this.username));

		this.newPassword = targetAccount.getPassword();
		if (SHOW_PASWORD) {
			LOGGER.fine(LoggerWrapper.logMessage("newPassword= " + this.newPassword));
		}

		this.oldPassword = targetAccount.getOldPassword();
		if (SHOW_PASWORD) {
			LOGGER.fine(LoggerWrapper.logMessage("oldPassword= " + this.oldPassword));
		}

		if (null == this.oldPassword || this.oldPassword.isEmpty()) {
			// oldPassword is empty if this is a new PAM target account
			LOGGER.fine(LoggerWrapper.logMessage("oldPassword is empty, set oldPassword to newPassword"));
			this.oldPassword = this.newPassword;
		}

		this.accountType= targetAccount.getExtendedAttribute(FIELD_ACCOUNT_TYPE);
		LOGGER.fine(LoggerWrapper.logMessage("accountType= " + this.accountType));
		this.isApiKey = FIELD_ACCOUNT_TYPE_VALUE_APIKEY.equals(this.accountType);

		if (this.isApiKey && !this.isRemote) {
			LOGGER.severe(LoggerWrapper.logMessage("AccountType=APIKEY and local PAM is not valid"));
			throw new ExtensionException(PAMUserMessageConstants.ERR_PAM_REMOTE, false);
		}
		
		this.masterAccount = targetAccount.getMasterAccount(FIELD_OTHER_ACCOUNT);
		if (null == this.masterAccount) {
			LOGGER.fine(LoggerWrapper.logMessage("No master account is provided, continue without masterAccount"));
			this.masterUsername= this.username;
			this.masterPassword= this.oldPassword;
		}
	    else {
	    	this.masterUsername = this.masterAccount.getUserName();
			if (null == this.masterUsername || this.masterUsername.isEmpty()) {
				LOGGER.severe(LoggerWrapper.logMessage("masterUsername is empty"));
			} else {
				LOGGER.fine(LoggerWrapper.logMessage("masterUsername= " + this.masterUsername));
			}

			this.masterPassword = this.masterAccount.getPassword();
			if (this.masterPassword == null || this.masterPassword.isEmpty()) {
				LOGGER.severe(LoggerWrapper.logMessage("masterPassword is empty"));
			} else {
				if (SHOW_PASWORD) {
					LOGGER.fine(LoggerWrapper.logMessage("masterPassword= " + this.masterPassword));
				}
			}
		}
	}

	/**
	 * Verifies credentials against target device. Stub method should be implemented
	 * by Target Connector Developer.
	 * 
	 * @param targetAccount object that contains details for the account for
	 *                      verification Refer to TargetAccount java docs for more
	 *                      details.
	 * @throws ExtensionException if there is any problem while verifying the
	 *                            credential
	 *
	 */
	public void credentialVerify(TargetAccount targetAccount) throws ExtensionException {
		try {
			if (this.isApiKey) {
				credentialVerifyApiKey(targetAccount);
			}
			else {
				credentialVerifyUser(targetAccount);
			}
		} 
		catch (ExtensionException e) {
			LOGGER.severe(LoggerWrapper.logMessage("PAMUser user '" + this.username + "' password verified - Not OK"));
			throw e;
		} 
	}

	public void credentialVerifyApiKey(TargetAccount targetAccount) throws ExtensionException {
		CloseableHttpClient httpClient= null;
		String resp= null;

		try {
			// Run restAPI command on remote system
			URI uri = new URIBuilder("https://" + this.endpointHostname + ":" + this.endpointPort + "/api.php/v1/devices.json")
					.addParameter("fields", "deviceId,domainName")
					.addParameter("domainName", "ServerIsNotExpected")
					.build();
			LOGGER.finer(LoggerWrapper.logMessage("uri= "+uri.toString()));
			
			httpClient= getHttpClient((int)(connectTimeout/1000), (int)(readTimeout/1000), username,oldPassword);
			resp= runHttpGet(httpClient,uri);
			if (resp.contains("{\"totalRows\":\"0\",\"devices\":[]}") == false) {
				LOGGER.warning(LoggerWrapper.logMessage("Error or unexpected response, resp= "+resp));
				throw new ExtensionException(PAMUserMessageConstants.ERR_AUTHENTICATION, false);
			}
		} 
		catch (ExtensionException e) {
			LOGGER.severe(LoggerWrapper.logMessage("PAMUser apikey '" + this.username + "' password verified - Not OK"));
			throw e;
		} 
		catch (Exception e) {
			// something else happened...
			LOGGER.severe(LoggerWrapper.logMessage("PAMUser apikey '" + this.username + "' password verified - Not OK"));
			LOGGER.log(Level.SEVERE, LoggerWrapper.logMessage("Extension Exception"), e);
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		} 
		finally {
			try {httpClient.close();} catch (Exception e) {}
		}
		LOGGER.info(LoggerWrapper.logMessage("PAMUser apikey '" + this.username + "' password verified - OK"));
	}

	public void credentialVerifyUser(TargetAccount targetAccount) throws ExtensionException {
		CloseableHttpClient httpClient= null;
		String resp= null;

		try {
			// Run CLI on remote system to verify password
			URI uri = new URIBuilder("https://" + this.endpointHostname + ":" + this.endpointPort + "/cspm/servlet/adminCLI")
					.addParameter("adminUserID", this.username)
					.addParameter("adminPassword", this.oldPassword)
					.addParameter("cmdName", "searchTargetServer")
					.addParameter("TargetServer.hostName", "ServerIsNotExpected")
					.build();

			if (SHOW_PASWORD) {
				LOGGER.finer(LoggerWrapper.logMessage("uri= " + uri));
			}
			else {
				String str = uri.toString()
					.replaceAll("adminPassword=.*?&", "adminPassword=*****&")
					.replaceAll("User.password=.*$", "User.password=*****");
				LOGGER.finer(LoggerWrapper.logMessage("uri= " + str));
			}

			httpClient= getHttpClient((int)(this.connectTimeout/1000), (int)(this.readTimeout/1000));
			resp= runHttpGet(httpClient,uri);
			if (resp.contains("<statusCode>400</statusCode>") == false) {
				LOGGER.warning(LoggerWrapper.logMessage("Error or unexpected response, resp= "+resp));
				throw new ExtensionException(PAMUserMessageConstants.ERR_AUTHENTICATION, false);
			}
		} 
		catch (ExtensionException e) {
			LOGGER.severe(LoggerWrapper.logMessage("PAMUser user '" + this.username + "' password verified - Not OK"));
			throw e;
		} 
		catch (Exception e) {
			// something else happened...
			LOGGER.severe(LoggerWrapper.logMessage("PAMeUser user '" + this.username + "' password verified - Not OK"));
			LOGGER.log(Level.SEVERE, LoggerWrapper.logMessage("Extension Exception"), e);
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		} 
		finally {
			try {httpClient.close();} catch (Exception e) {}
		}
		LOGGER.info(LoggerWrapper.logMessage("PAMUser user '" + this.username + "' password verified - OK"));
	}
	
	/**
	 * Updates credentials against target device. Stub method should be implemented
	 * by Target Connector Developer.
	 * 
	 * @param targetAccount object that contains details for the account for
	 *                      verification Refer to TargetAccount java docs for more
	 *                      details.
	 * @throws ExtensionException if there is any problem while update the
	 *                            credential
	 */
	public void credentialUpdate(TargetAccount targetAccount) throws ExtensionException {
		if (this.isApiKey) {
			credentialUpdateApiKey(targetAccount);
		}
		else {
			credentialUpdateUser(targetAccount);
		}
	}

	/* 
	 * TargetAccount is an ApiKey account
	 */
	public void credentialUpdateApiKey(TargetAccount targetAccount) throws ExtensionException {
		CloseableHttpClient httpClient = null;

		try {
			LOGGER.finer(LoggerWrapper.logMessage("Find ID for ApiKey= " + this.username));
			
			httpClient = getHttpClient((int)(connectTimeout/1000), (int)(this.readTimeout/1000), this.masterUsername, this.masterPassword);
			String devId= apiFindDevice(httpClient, this.apikeyHostname);
			String appId= apiFindApplication(httpClient, devId, this.apikeyAppName);
			String accId= apiFindAccount(httpClient, devId, appId, this.username);

			LOGGER.fine(LoggerWrapper.logMessage("Found - devId= "+devId+", appId= "+appId+", accId= "+accId));
			
			/*
			 * Update password
			 */
			String json = null;
			json = "{";
			json += "\"accountId\":"+ accId + ",";
			json += "\"password\":\"" + newPassword + "\"";
			json += "}";
			
			if (SHOW_PASWORD) {
				LOGGER.finer(LoggerWrapper.logMessage("Update json= " + json));
			}
			else {
				String str = json.toString().replaceAll("\"password\":\".*?\"", "\"password\":\"******\"");
				LOGGER.finer(LoggerWrapper.logMessage("Update json= " + str));
			}

			HttpPut httpPut = new HttpPut("https://" + this.endpointHostname + ":" + this.endpointPort + "/api.php/v1/devices.json/"+devId+"/targetApplications/"+appId+"/targetAccounts");
			httpPut.setHeader("Accept", "application/json");
			httpPut.setHeader("Content-type", "application/json");
			StringEntity entity = new StringEntity(json);
			httpPut.setEntity(entity);

			LOGGER.finer(LoggerWrapper.logMessage("Update ApiKey, URI= "+httpPut.getURI().toString()));
			
			CloseableHttpResponse respPut = httpClient.execute(httpPut);
			LOGGER.finer(LoggerWrapper.logMessage("Update ApiKey, respPut= " + respPut.getStatusLine().getStatusCode()));
			if (respPut.getStatusLine().getStatusCode() != 200) {
				LOGGER.severe(LoggerWrapper.logMessage( "Error updating apiKey, respPut= " + respPut.getStatusLine().getStatusCode()));
				throw new ExtensionException(PAMUserMessageConstants.ERR_STATUSCODE_NOT_OK, false);
			}
		} 
		catch (ExtensionException e) {
			throw e;
		} 
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, LoggerWrapper.logMessage("Extension Exception"), e);
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		} 
		finally {
			try { httpClient.close(); } catch (Exception e) {}
		}
		LOGGER.info(LoggerWrapper.logMessage("PAMUser apikey '" + this.username + "' password updated - OK"));
	}

	public void credentialUpdateUser(TargetAccount targetAccount) throws ExtensionException {
		CloseableHttpClient httpClient = null;
		URI uri;

		try {
			/* TargetAccount is a login user
			 *  
			 *  1) Find userId
			 *  2) Update user password 
			 */
			LOGGER.finer(LoggerWrapper.logMessage("Find userId for user= " + username));

			httpClient = getHttpClient((int)(connectTimeout/1000), (int)(this.readTimeout/1000), this.masterUsername, this.masterPassword);
			uri = new URIBuilder("https://" + endpointHostname + ":" + this.endpointPort + "/api.php/v1/users.json")
					.addParameter("matchType", "exact")
					.addParameter("userName", username)
					.addParameter("fields", "userId,userName")
					.build();

			LOGGER.finer(LoggerWrapper.logMessage("uri= " + uri.toString()));

			String resp = runHttpGet(httpClient, uri);
			LOGGER.finer(LoggerWrapper.logMessage("resp= " + resp));

			Pattern userIdPattern = Pattern.compile("\"userId\":\"(\\d+)\"");
			Matcher match = userIdPattern.matcher(resp);
			if (!match.find()) {
				// userId not found
				LOGGER.severe(LoggerWrapper.logMessage("userIdPattern - userId not found. resp= " + resp));
				throw new ExtensionException(PAMUserMessageConstants.ERR_USER_NOT_FOUND, false, this.username);
			}
				
			// user found, update password
			String userId= match.group(1);
			LOGGER.finer(LoggerWrapper.logMessage("userIdPattern - userId= " + userId));

			String json = null;
			json = "{";
			json += "\"userId\":" + userId + ",";
			json += "\"password\":\"" + newPassword + "\",";
			json += "\"resetPasswordFlag\":\"f\"";
			json += "}";

			if (SHOW_PASWORD) {
				LOGGER.finer(LoggerWrapper.logMessage("Update json= " + json));
			} else {
				String str = json.toString().replaceAll("\"password\":\".*?\"", "\"password\":\"******\"");
				LOGGER.finer(LoggerWrapper.logMessage("Update json= " + str));
			}

			HttpPut httpPut = new HttpPut("https://" + this.endpointHostname + ":" + this.endpointPort + "/api.php/v1/users.json");
			httpPut.setHeader("Accept", "application/json");
			httpPut.setHeader("Content-type", "application/json");
			StringEntity entity = new StringEntity(json);
			httpPut.setEntity(entity);

			LOGGER.finer(LoggerWrapper.logMessage("Update User, URI= "+httpPut.getURI().toString()));
			
			CloseableHttpResponse respPut = httpClient.execute(httpPut);
			LOGGER.finer(LoggerWrapper.logMessage("UpdateUser, respPut= " + respPut.getStatusLine().getStatusCode()));
			if (respPut.getStatusLine().getStatusCode() != 204) {
				LOGGER.severe(LoggerWrapper.logMessage("Error updating user password, respPut= " + respPut.getStatusLine().getStatusCode()));
				throw new ExtensionException(PAMUserMessageConstants.ERR_STATUSCODE_NOT_OK, false);
			}
		} 
		catch (ExtensionException e) {
			throw e;
		} 
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, LoggerWrapper.logMessage("Extension Exception"), e);
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		} 
		finally {
			try { httpClient.close(); } catch (Exception e) {}
		}
		LOGGER.info(LoggerWrapper.logMessage("PAMUser user '" + this.username + "' password updated - OK"));
	}
	
	/**
	 * run a HTTP GET command
	 *
	 * @return the string content
	 */
	private String runHttpGet(CloseableHttpClient httpClient, URI uri) throws ExtensionException {
		CloseableHttpResponse httpResp = null;
		HttpGet httpGet = new HttpGet(uri);
		String resp = null;

		try {
			if (LOGGER.isLoggable(Level.FINER)) {
				if (SHOW_PASWORD) {
					LOGGER.finer(LoggerWrapper.logMessage("URI= " + uri));
				}
				else {
					String str = uri.toString()
							.replaceAll("adminPassword=.*?&", "adminPassword=*****&")
							.replaceAll("User.password=.*$", "User.password=*****");
					LOGGER.finer(LoggerWrapper.logMessage("URI= " + str));
				}
			}

			httpResp = httpClient.execute(httpGet);

			// Log HttpResponse Status
			LOGGER.finer(LoggerWrapper.logMessage("httpResp= " + httpResp.getStatusLine().toString())); // HTTP/1.1 200

			HttpEntity entity = httpResp.getEntity();
			if (entity != null) {
				resp = EntityUtils.toString(entity);
				LOGGER.finer(LoggerWrapper.logMessage("PAM response= " + resp));
			} else {
				LOGGER.severe(LoggerWrapper.logMessage("No response returned"));
			}
			EntityUtils.consume(entity);
			return resp;
		} 
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, LoggerWrapper.logMessage("Extension Exception"), e);
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		} 
		finally {
			try { httpResp.close(); } catch (Exception e) {}
		}
	}

	/*
	 * Escape regex special chars
	 */
	private String escapeMetaCharacters(String inputString) {
		final String[] metaCharacters = { "\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<", ">", "-", "&", "%" };

		for (int i = 0; i < metaCharacters.length; i++) {
			if (inputString.contains(metaCharacters[i])) {
				inputString = inputString.replace(metaCharacters[i], "\\" + metaCharacters[i]);
			}
		}
		return inputString;
	}

	/**
	 * Verify <statusCode>XX</statusCode> is 400.
	 * 
	 * @param result is the response from PAM
	 */
	static final Pattern statusCodePattern = Pattern.compile("<statusCode>(.*?)</statusCode>");

	/**
	 * Gets the http client.
	 *
	 * @return the http client
	 */
	private CloseableHttpClient getHttpClient(int connectTimeout, int readTimeout) throws ExtensionException {
		CloseableHttpClient httpClient = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout * 1000)
					.setConnectionRequestTimeout(readTimeout * 1000)
					.setSocketTimeout(connectTimeout * 1000).build();

			httpClient = HttpClientBuilder.create()
					.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
						@Override
						public boolean isTrusted(X509Certificate[] arg0, String arg1) {
							return true;
						}
					}).build())
					.setDefaultRequestConfig(config)
					.build();
		} 
		catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			LOGGER.log(Level.SEVERE, LoggerWrapper.logMessage("Error getting getHttpClient"), e);
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		}
		return httpClient;
	}

	private CloseableHttpClient getHttpClient(int connectTimeout, int readTimeout, String username, String password) throws ExtensionException {
		CloseableHttpClient httpClient = null;

		try {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(connectTimeout * 1000)
					.setConnectionRequestTimeout(readTimeout * 1000)
					.setSocketTimeout(connectTimeout * 1000).build();

			CredentialsProvider provider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			provider.setCredentials(AuthScope.ANY, credentials);

			httpClient = HttpClientBuilder.create().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
						@Override
						public boolean isTrusted(X509Certificate[] arg0, String arg1) {
							return true;
						}
					}).build()).setDefaultRequestConfig(config).setDefaultCredentialsProvider(provider).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			LOGGER.log(Level.SEVERE, LoggerWrapper.logMessage("Error getting getHttpClient"), e);
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		}
		return httpClient;
	}

	/*
	 * Use RestAPI to find accountID from hostname, applicationName and accountName
	 */
	private String apiFindDevice(CloseableHttpClient httpClient, String domainName) throws ExtensionException {
		try {
			/*
			 * Find deviceId
			 */
			URI uri= new URIBuilder("https://" + this.endpointHostname + ":" + this.endpointPort + "/api.php/v1/devices.json")
					.addParameter("fields", "deviceId,domainName")
					.addParameter("domainName", domainName)
					.build();
			LOGGER.finer(LoggerWrapper.logMessage("uri= " + uri.toString()));

			String resp = runHttpGet(httpClient, uri);
			LOGGER.finer(LoggerWrapper.logMessage("resp= " + resp));

			Pattern devIdPattern = Pattern.compile("\"deviceId\":\"(\\d+)\",\"domainName\":\"" + escapeMetaCharacters(domainName) + "\"");
			LOGGER.finer(LoggerWrapper.logMessage("deviceIdPattern= " + devIdPattern.toString()));
			
			Matcher match = devIdPattern.matcher(resp);
			if (!match.find()) {
				LOGGER.log(Level.SEVERE,LoggerWrapper.logMessage("Device not found"));
				throw new ExtensionException(PAMUserMessageConstants.ERR_DEVICE_NOT_FOUND, false, domainName);
			}
			
			String devId = match.group(1);
			LOGGER.finer(LoggerWrapper.logMessage("found TargetServer, deviceId= " + devId));
			return devId;
		}
		catch (ExtensionException e) {
			throw e;
		} 
		catch (Exception e) {
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		}
	}
	
	/*
	 * Use RestAPI to find accountID from hostname, applicationName and accountName
	 */
	private String apiFindApplication(CloseableHttpClient httpClient, String devId, String appName) throws ExtensionException {

		try {
			URI uri= new URIBuilder("https://" + this.endpointHostname + ":" + this.endpointPort + "/api.php/v1/devices.json/" + devId + "/targetApplications")
					.addParameter("fields", "id,applicationName")
					.addParameter("applicationName", appName)
					.build();
			LOGGER.finer(LoggerWrapper.logMessage("uri= " + uri.toString()));

			String resp = runHttpGet(httpClient, uri);
			LOGGER.finer(LoggerWrapper.logMessage("resp= " + resp));

			Pattern appIdPattern = Pattern.compile("\"id\":\"(\\d+)\",\"applicationName\":\"" + escapeMetaCharacters(appName) + "\"");
			LOGGER.finer(LoggerWrapper.logMessage("appIdPattern= " + appIdPattern.toString()));
			
			Matcher match= appIdPattern.matcher(resp);
			if (!match.find()) {
				LOGGER.log(Level.SEVERE,LoggerWrapper.logMessage("Application not found"));
				throw new ExtensionException(PAMUserMessageConstants.ERR_APPLICATION_NOT_FOUND, false, appName);
			}
			
			String appId= match.group(1);
			LOGGER.finer(LoggerWrapper.logMessage("found TargetAppliction, appId= " + appId));
			return appId;
			
		} catch (ExtensionException e) {
			throw e;
		} catch (Exception e) {
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		}
	}
	
	/*
	 * Use RestAPI to find accountID from hostname, applicationName and accountName
	 */
	private String apiFindAccount(CloseableHttpClient httpClient, String devId, String appId, String accName) throws ExtensionException {

		try {
			URI uri= new URIBuilder("https://" + this.endpointHostname + ":" + this.endpointPort + "/api.php/v1/devices.json/" + devId + "/targetApplications/" + appId + "/targetAccounts")
					.addParameter("accountName", accName)
					.build();
			LOGGER.finer(LoggerWrapper.logMessage("uri= " + uri.toString()));

			String resp = runHttpGet(httpClient, uri);
			LOGGER.finer(LoggerWrapper.logMessage("resp= " + resp));

			Pattern accIdPattern = Pattern.compile("\"accountId\":\"(\\d+)\",\"accountName\":\"" + escapeMetaCharacters(accName) + "\"");
			LOGGER.finer(LoggerWrapper.logMessage("accIdPattern= " + accIdPattern.toString()));
			Matcher match= accIdPattern.matcher(resp);
			if (!match.find()) {
				LOGGER.log(Level.SEVERE,LoggerWrapper.logMessage("Account not found"));
				throw new ExtensionException(PAMUserMessageConstants.ERR_ACCOUNT_NOT_FOUND, false, accName);
			}

			String accId= match.group(1);
			LOGGER.finer(LoggerWrapper.logMessage("found TargetAccount, accId= " + accId));
			return accId;
			
		} catch (ExtensionException e) {
			throw e;
		} catch (Exception e) {
			throw new ExtensionException(PAMUserMessageConstants.ERR_EXCEPTION, false);
		}
	}
}
