package com.ucuenca.misctools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.di.core.exception.KettleException;

public final class LOVApiV2 {

	private static final String URL_VOCABULARY_SEARCH = "http://lov.okfn.org/dataset/lov/api/v2/vocabulary/search";
	
	private static Map<String, JSONArray> dataCache = new HashMap<String, JSONArray>();
	
	public static final List<String> vocabularySearch(String prefix) throws Exception {
		List<String> URIList = new ArrayList<String>();
		try {
			JSONArray rsJSON;
			if(dataCache.get(prefix) == null) {
				URL obj = new URL(URL_VOCABULARY_SEARCH + "?q=" + prefix);
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
				rsJSON = jsonRoot.getJSONArray("results");
				for(int i = 0; i<rsJSON.length();i++) {
					JSONObject vocab = rsJSON.getJSONObject(i);
					URIList.add( vocab.getString("_id") );
				}
				dataCache.put(prefix, rsJSON);
			} else {
				rsJSON = dataCache.get(prefix);
				for(int i = 0; i<rsJSON.length();i++) {
					JSONObject vocab = rsJSON.getJSONObject(i);
					String URI = vocab.getJSONObject("_source").getString("uri");
					URIList.add( URI.endsWith("/") ? URI:URI+"#" );
				}
				
			}

		}catch (IOException e) {
			throw new KettleException("PROBLEM TRYING TO CONNECT TO PREFIX.CC SERVICE", e);
		}catch (JSONException e) {
			throw new KettleException("ERROR PARSING JSON RESPONSE", e);
		}
		return URIList;
	}
	/*
	public static void main(String args[]) throws Exception {
		System.out.println(LOVApiV2.vocabularySearch("foaf").toString());
	}*/

}
