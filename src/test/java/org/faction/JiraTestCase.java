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
	
	@Test
	void loadExtension() throws MalformedURLException {
		Vulnerability vuln = new Vulnerability();
		vuln.setDescription("test issue");
		vuln.setName("Faction Vulnerability");
		List<Vulnerability>vulns = new ArrayList<>();
		vulns.add(vuln);
		Assessment asmt = new Assessment();
		asmt.setName("Test Assessment");
		File f = new File("/opt/faction/modules/JiraPlugin-0.0.1-jar-with-dependencies.jar");
		URL [] urls = {f.toURL()};
		URLClassLoader extensionClassLoader = new URLClassLoader(urls);
		
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
		      Thread.currentThread().setContextClassLoader(extensionClassLoader);
		      for (AssessmentManager extension : ServiceLoader.load(AssessmentManager.class,extensionClassLoader)) {
		    	  Object [] output = extension.assessmentChange(asmt, vulns, Operation.Finalize);
		    	  assertTrue(output != null);
		    	  assertTrue(output.length == 2);
		      }
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
		  Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
		
	}

}
