package com.liferay.chat.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;


public class ChatPortlet extends MVCPortlet {
	
	/**
	 * Format answers from a json string to a key-value list
	 *
	 * @param resultJson the answers in json
	 * @return the key-value answer list
	 */
	/*
	private List<Map<String,String>> formatAnswers(String resultJson) {
		List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
		try {
			JSONArray pipelines = JSONArray.parse(resultJson);
			// the response has two pipelines, lets use the first one
			JSONObject answersJson = (JSONObject) pipelines.get(0);
			JSONArray answers = (JSONArray) ((JSONObject) answersJson.get("question")).get("evidencelist");

			for(int i = 0; i < answers.size();i++) {
				JSONObject answer = (JSONObject) answers.get(i);
				Map<String, String> map = new HashMap<String, String>();

				double p = Double.parseDouble((String)answer.get("value"));
				p = Math.floor(p * 100);
				map.put("confidence",  Double.toString(p) + "%");
				map.put("text", (String)answer.get("text"));

				ret.add(map);
			}
		} catch (IOException e) {

       }
		return ret;
	}*/
	
	@ProcessAction(name = "serveAnswer")
	public void serveResource(ResourceRequest request, ResourceResponse response)
            throws PortletException, IOException {
		//request.setCharacterEncoding("application/text");
		HttpServletRequest httpReq = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request));
		String uiQuestion = httpReq.getParameter("question");
		System.out.println("UI Question " + uiQuestion);
		
		
		CallWatson watson = new CallWatson();
		//String answer = watson.WatsonAnswer(sb.toString());
		String answer = watson.WatsonAnswer(uiQuestion);
		
		JSONParser parser = new JSONParser();
		String toDisplay = null;
		
			Object obj = null;
			try {
				obj = parser.parse(answer);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray pipelines = (JSONArray) obj;

			JSONObject answersJson = (JSONObject) pipelines.get(0);
			JSONArray answers = (JSONArray) ((JSONObject) answersJson
					.get("question")).get("evidencelist");
			for (int i = 0; i < 1; i++) {
				JSONObject answerJSON = (JSONObject) answers.get(i);
				Map<String, String> map = new HashMap<String, String>();

				double p = Double.parseDouble((String) answerJSON.get("value"));
				p = Math.floor(p * 100);
				map.put("confidence", Double.toString(p) + "%");
				map.put("text", (String) answerJSON.get("text"));
				
				toDisplay = map.get("text");
				// ret.add(map);
			}
		
		
		response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.print(toDisplay);
        writer.flush();
        writer.close();
        super.serveResource(request, response);
    }
}
