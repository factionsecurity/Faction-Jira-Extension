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


/*
 * This is an example Jira Extension to be used with Faction. 
 * This Extension has 3 requirements:
 * 
 * 1. Must ensure your pom.xml includes the following directives:
 * 			<manifestEntries>
 *	 			<Title>Your Title</Title>
 *				<Version>${project.version}</Version>
 *				<Author>Your Name</Author>
 *				<URL>Your URL</URL>
 *			</manifestEntries> 
 *    
 * 2. Set your extension defualt configs. These are settings that can 
 *    be configured in Faction's AppStore Dashboard. The default settings 
 *    are initialized here (src/main/resources/configs.json) 
 * 
 * 3. Set the Jira Project Name using a Custom Field in Faction. 
 *    This is added in admin settings. (Faction->admin->settings)
 *    The name should be "Jira Project", 'variable' can be what ever
 *    you want. Variable names are only used for report generation.   
 */
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
		
		
		
	}
	
	private String base64(String data) {
		return "Basic " +Base64.getEncoder().encodeToString(data.getBytes());
	}
	
	private String httpPost(String url, JSONObject payload) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		
		// Get Extension Configs.
		String apiKey = this.getConfigs().get("Jira API Key");
		
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
