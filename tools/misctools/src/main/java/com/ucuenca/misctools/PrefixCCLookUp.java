package com.ucuenca.misctools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.di.core.exception.KettleException;

public final class PrefixCCLookUp {
	
	private static final String URL_SERVICE = "http://prefix.cc/context";
	
	private static JSONObject dataCache = null;
	
	public static final String queryService(String prefix) throws Exception {
		String URI = "";
		try {
			if(dataCache == null) {
				URL obj = new URL(URL_SERVICE);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				//con.setRequestProperty("User-Agent", USER_AGENT);
				int responseCode = con.getResponseCode();
	
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				JSONObject jsonRoot = new JSONObject(response.toString());
				dataCache = jsonRoot.getJSONObject("@context");
			}
			URI = dataCache.getString(prefix);

		}catch (IOException e) {
			throw new KettleException("PROBLEM TRYING TO CONNECT TO PREFIX.CC SERVICE", e);
		}catch (JSONException e) {
			throw new KettleException("ERROR PARSING JSON RESPONSE", e);
		}
		return URI;
	}
	/*
	public static void main(String args[]) throws Exception {
		System.out.println(PrefixCCLookUp.queryService("foaf"));
	}*/

}
