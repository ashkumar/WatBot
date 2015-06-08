package com.liferay.chat.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;



public class CallWatson {
	
	public String WatsonAnswer(String question) {
		String result = null;
		try {
			CredentialsProvider provider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
					"a509e5db-db17-4d7e-b5f1-3bf23468dff1", "n7CGKCkzGcSU");
			
			provider.setCredentials(AuthScope.ANY, credentials);

			// assume a self signed certificate which doesn't have a host name that
			// matches
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build(),
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			CloseableHttpClient client = HttpClients.custom()
					.setSSLSocketFactory(sslsf)
					.setDefaultCredentialsProvider(provider).build();

			// create the HTTP Post operation
			HttpPost httpPost = new HttpPost(
					"https://gateway.watsonplatform.net/question-and-answer-beta/api/v1/question/healthcare");

			// create the HTTP Post Body information (How to build this comes from
			// the documentation)
			String template = "{ \"question\" : {\"questionText\" : \"%QQ%\"} }";
			String query = template.toString().replace("%QQ%", question);
			
			StringEntity ent = new StringEntity(query);
			
			ent.setContentType("application/json");
			httpPost.setEntity(ent);
			httpPost.setHeader("X-SyncTimeOut", "30");

			// execute
			HttpResponse response = client.execute(httpPost);
			
			result = EntityUtils.toString(response.getEntity());

			System.out.println(result);
			
			//JSONObject jsonobj = new JSONObject(response.getEntity());
			//JSONArray jsonarray = new JSONArray(response.getEntity().toString());
			


			
			
			
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	
	
	public static void main(String[] args) {
		//System.out.println(WatsonAnswer("What is hypertension?"));
		/*
		try {
			HttpResponse<JsonNode> jsonResponse = Unirest.post("https://gateway.watsonplatform.net/question-and-answer-beta/api/v1/question/healthcare")
					  .header("accept", "application/json")
					  .basicAuth("a509e5db-db17-4d7e-b5f1-3bf23468dff1", "n7CGKCkzGcSU")
					  .body("{\"question\":{\"questionText\":\"What is diabetes\"}}")
					  .asJson();
			
			System.out.println(jsonResponse.getBody().toString());
			
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

}
