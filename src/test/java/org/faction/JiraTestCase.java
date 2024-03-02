package org.faction;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;

import com.faction.elements.Assessment;
import com.faction.elements.Vulnerability;
import com.faction.extender.AssessmentManager;
import com.faction.extender.AssessmentManager.Operation;

class JiraTestCase {

	@Test
	void test() {
		JiraPlugin plugin = new JiraPlugin();
		Vulnerability vuln = new Vulnerability();
		vuln.setDescription("test issue");
		vuln.setName("Faction Vulnerability");
		String jiraIssueId = plugin.sendVulnerbilityToJira(vuln, "KAN");
		assertTrue(jiraIssueId != null);
		assertTrue(jiraIssueId.matches("[0-9]+"));
		
	}

}
