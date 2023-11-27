package org.faction;
import java.util.Base64;
import java.util.List;

import com.fuse.elements.Assessment;
import com.fuse.elements.Vulnerability;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JiraPlugin implements com.fuse.extender.AssessmentManager{

	@Override
	public Object[] assessmentChange(Assessment assessment, List<Vulnerability> vulns, Operation opcode) {
		
		if(opcode == Operation.Finalize) {
			//Integration into vulnerability management system
			for(Vulnerability vuln : vulns) {
				//this can update vulns and send the updated values back into Faction
				String issueId = sendVulnerbilityToJira(vuln);
				if(issueId != null) {
					vuln.setTracking(issueId);
				}
			}
		}
		return new Object [] {assessment, vulns};
	}
	
	public String sendVulnerbilityToJira(Vulnerability vuln) {
		
		JSONObject issueType = new JSONObject();
		issueType.put("name", "Bug");
		
		JSONObject project = new JSONObject();
		project.put("key", "KAN");
		
		JSONObject fields = new JSONObject();
		fields.put("summary", vuln.getName());
		fields.put("description", vuln.getDescription());
		fields.put("project", project);
		fields.put("issuetype", issueType);
		
		JSONObject issue = new JSONObject();
		issue.put("fields", fields);
		String jiraHost = System.getenv("JIRA_HOST");
		String jiraURL = String.format("%s%s", jiraHost, "rest/api/2/issue/");
		return httpPost(jiraURL, issue);
		
	}
	
	private String base64(String data) {
		return "Basic " +Base64.getEncoder().encodeToString(data.getBytes());
	}
	
	private String httpPost(String url, JSONObject payload) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		String apiKey = System.getenv("JIRA_API_KEY");
		try {
		    HttpPost request = new HttpPost(url);
		    StringEntity params = new StringEntity(payload.toJSONString());
		    request.addHeader("content-type", "application/json");
		    request.addHeader("Authorization", base64(apiKey));
		    request.setEntity(params);
		    HttpResponse response = httpClient.execute(request);
		    if( response.getStatusLine().getStatusCode() == 201) {
		    	String json = EntityUtils.toString(response.getEntity());
		    	JSONParser parser = new JSONParser();
		    	JSONObject jsonObj = (JSONObject) parser.parse(json);
		    	return (String) jsonObj.get("id");
		    }else {
		    	return null;
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		} 
		
	}
	

}
