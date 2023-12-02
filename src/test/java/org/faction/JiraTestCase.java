package org.faction;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.faction.elements.Vulnerability;

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
