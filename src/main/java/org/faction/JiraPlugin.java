package org.faction;
import java.util.Base64;

import java.util.List;

import com.faction.elements.Assessment;
import com.faction.elements.BaseExtension;
import com.faction.elements.CustomField;
import com.faction.elements.Vulnerability;
import com.faction.elements.results.AssessmentManagerResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class JiraPlugin extends BaseExtension implements com.faction.extender.AssessmentManager{

	@Override
	public AssessmentManagerResult assessmentChange(Assessment assessment, List<Vulnerability> vulns, Operation opcode) {
		
		System.out.println("Running Assessment Manager");
		String project ="KAN";  //Default Jira Project Name.
		
		
		List<CustomField> fields = assessment.getCustomFields(); // Custom Fields override the default Jira Project Name
		if(fields != null) {
			for(CustomField field : fields) {
				System.out.println(field.getType().getKey());
				// 'Jira Project' is the Name of the Custom Field. You could name it anything as long as it matches here
				if(field.getType().getKey().equals("Jira Project")) { 
					project = field.getValue();
					break;
				}
			}
			
		}
		System.out.println("Configured for " + project);
		
		if(opcode == Operation.Finalize) { // We only want to run when an assessment is finalized
			for(Vulnerability vuln : vulns) {
				// This can update Faction with the Jira Tracking Id
				String issueId = sendVulnerbilityToJira(vuln, project);
				if(issueId != null) {
					vuln.setTracking(issueId);
				}
			}
		}
		//return the assessment and updated vulns back to Faction;
		AssessmentManagerResult result = new AssessmentManagerResult();
		result.setAssessment(assessment);
		result.setVulnerabilities(vulns);
		return result;

	}
	/*
	 * This Utility function handles creating the Jira Issue and sending 
	 * the correct JSON to the Jira Server. 
	 */
	public String sendVulnerbilityToJira(Vulnerability vuln, String projectName) {
		try {
		
			JSONObject issueType = new JSONObject();
			issueType.put("name", "Bug");
		
			JSONObject project = new JSONObject();
			project.put("key", projectName);
			
			JSONObject fields = new JSONObject();
			fields.put("summary", vuln.getName());
			fields.put("description", vuln.getDescription());
			fields.put("project", project);
			fields.put("issuetype", issueType);
			
			JSONObject issue = new JSONObject();
			issue.put("fields", fields);
			// Get Extension Configs
			String jiraHost = this.getConfigs().get("Jira Host");
			String jiraURL = String.format("%s%s", jiraHost, "rest/api/2/issue/");
			return httpPost(jiraURL, issue);
		}catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		
		
		
	}
	
	private String base64(String data) {
		return "Basic " +Base64.getEncoder().encodeToString(data.getBytes());
	}
	
	private String httpPost(String url, JSONObject payload) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		
		// Get Extension Configs.
		String apiKey = this.getConfigs().get("Jira API Key");
		String email = this.getConfigs().get("Jira Email");
		
		try {
		    HttpPost request = new HttpPost(url);
		    StringEntity params = new StringEntity(payload.toJSONString());
		    request.addHeader("content-type", "application/json");
		    request.addHeader("Authorization", base64(email + ":" + apiKey));
		    request.setEntity(params);
		    HttpResponse response = httpClient.execute(request);
		    if( response.getStatusLine().getStatusCode() == 201) {
		    	String json = EntityUtils.toString(response.getEntity());
		    	JSONParser parser = new JSONParser();
		    	JSONObject jsonObj = (JSONObject) parser.parse(json);
		    	return (String) jsonObj.get("id");
		    }else {
		    	System.out.println(response.getStatusLine().getStatusCode());
		    	System.out.println(EntityUtils.toString(response.getEntity()));
		    	return null;
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		} 
		
	}
	

}
