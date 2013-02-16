package com.marklogic.client.test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.marklogic.client.admin.QueryOptionsManager;
import com.marklogic.client.alerting.RuleDefinition;
import com.marklogic.client.alerting.RuleDefinition.RuleMetadata;
import com.marklogic.client.alerting.RuleDefinitionList;
import com.marklogic.client.alerting.RuleManager;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import com.marklogic.client.query.StructuredQueryBuilder;

public class AlertingTest {

	private static RuleManager ruleManager;
	private static QueryOptionsManager queryOptionsManager;
	private static QueryManager queryManager;

	@AfterClass
	public static void teardown() {
		XMLDocumentManager docMgr = Common.client.newXMLDocumentManager();
		docMgr.delete("/alert/first.xml");
		docMgr.delete("/alert/second.xml");
		docMgr.delete("/alert/third.xml");
		teardownMatchRules();

		Common.release();
	}

	@BeforeClass
	public static void setup() throws FileNotFoundException {
		XMLUnit.setIgnoreWhitespace(true);
		Common.connectAdmin();

		queryOptionsManager = Common.client.newServerConfigManager()
				.newQueryOptionsManager();
		File options = new File("src/test/resources/alerting-options.xml");
		queryOptionsManager.writeOptions("alerts", new FileHandle(options));

		queryManager = Common.client.newQueryManager();

		Common.client.newServerConfigManager().setServerRequestLogging(true);
		Common.release();
		Common.connect();

		// write three files for alert tests.
		XMLDocumentManager docMgr = Common.client.newXMLDocumentManager();
		docMgr.write("/alert/first.xml", new FileHandle(new File(
				"src/test/resources/alertFirst.xml")));
		docMgr.write("/alert/second.xml", new FileHandle(new File(
				"src/test/resources/alertSecond.xml")));
		docMgr.write("/alert/third.xml", new FileHandle(new File(
				"src/test/resources/alertThird.xml")));

		ruleManager = Common.client.newRuleManager();
		setupMatchRules();
	}

	@Test
	public void testRuleDefinitions() throws ParserConfigurationException,
			SAXException, IOException {
		RuleDefinition definition = new RuleDefinition("javatestrule",
				"Rule for testing java");

		String head = "<search:search xmlns:search=\"http://marklogic.com/appservices/search\">";
		String tail = "</search:search>";

		String qtext1 = "<search:qtext>favorited:true</search:qtext>";

		StructuredQueryBuilder qb = queryManager.newStructuredQueryBuilder();

		String structuredString = qb.valueConstraint("name", "one").serialize();

		String ruleOptions = "<search:options >"
				+ "<search:constraint name=\"favorited\">" + "<search:value>"
				+ "<search:element name=\"favorited\" ns=\"\"/>"
				+ "</search:value>" + "</search:constraint>"
				+ "</search:options>";

		StringHandle textQuery = new StringHandle(head + qtext1 + tail);
		definition.importQueryDefinition(textQuery);

		StringHandle qdefCheck = definition
				.exportQueryDefinition(new StringHandle());
		assertEquals(head + qtext1 + tail, qdefCheck.get());

		RuleMetadata metadata = definition.getMetadata();
		metadata.put(new QName("dataelem1"), "Here's a value in metadata");
		metadata.put(new QName("dataelem2"), 10.2);

		// one. no options, string query.
		ruleManager.writeRule(definition);

		// fetch the rule
		RuleDefinition roundTripped = ruleManager.readRule("javatestrule",
				new RuleDefinition());
		assertEquals("javatestrule", roundTripped.getName());
		assertEquals("Rule for testing java", roundTripped.getDescription());

		BytesHandle x = roundTripped.exportQueryDefinition(new BytesHandle());

		assertXMLEqual(
				"Search element round-tripped",
				"<search:search xmlns:search=\"http://marklogic.com/appservices/search\"><search:qtext>favorited:true</search:qtext></search:search>",
				new String(x.get()));

		RuleMetadata metadataReturned = roundTripped.getMetadata();
		assertEquals(metadata.get(new QName("dataelem1")),
				metadataReturned.get(new QName("dataelem1")));
		assertEquals(metadata.get(new QName("dataelem2")),
				metadataReturned.get(new QName("dataelem2")));

		// two. with options string query.
		StringHandle rawDefWithOptions = new StringHandle(head + qtext1
				+ ruleOptions + tail);
		definition.importQueryDefinition(rawDefWithOptions);
		ruleManager.writeRule(definition);
		roundTripped = ruleManager.readRule("javatestrule",
				new RuleDefinition());
		assertEquals("javatestrule", roundTripped.getName());
		assertEquals("Rule for testing java", roundTripped.getDescription());

		assertXMLEqual(
				"Search element round-tripped - string query and options",
				"<search:search xmlns:search=\"http://marklogic.com/appservices/search\"><search:qtext>favorited:true</search:qtext></search:search>",
				new String(x.get()));

		// three. structured query with options.

		StringHandle structuredWithOptions = new StringHandle(head
				+ structuredString + ruleOptions + tail);
		definition.importQueryDefinition(structuredWithOptions);
		ruleManager.writeRule(definition);
		roundTripped = ruleManager.readRule("javatestrule",
				new RuleDefinition());
		assertEquals("javatestrule", roundTripped.getName());
		assertEquals("Rule for testing java", roundTripped.getDescription());

		assertXMLEqual(
				"Search element round-tripped - structured query and options",
				"<search:search xmlns:search=\"http://marklogic.com/appservices/search\"><search:qtext>favorited:true</search:qtext></search:search>",
				new String(x.get()));
		ruleManager.delete("javatestrule");
	}
	private static void setupMatchRules() {
		RuleDefinition definition = new RuleDefinition("favorites",
				"Rule for testing favorited:true");
		String head = "<search:search xmlns:search=\"http://marklogic.com/appservices/search\">";
		String qtext1 = "<search:qtext>favorited:true</search:qtext>";
		String qtext2 = "<search:qtext>favorited:false</search:qtext>";
		String tail = "</search:search>";
		String ruleOptions = "<search:options >"
				+ "<search:constraint name=\"favorited\">" + "<search:value>"
				+ "<search:element name=\"favorited\" ns=\"\"/>"
				+ "</search:value>" + "</search:constraint>"
				+ "</search:options>";
		StringHandle textQuery = new StringHandle(head + qtext1 + ruleOptions
				+ tail);
		definition.importQueryDefinition(textQuery);
		ruleManager.writeRule(definition);

		textQuery = new StringHandle(head + qtext2 + ruleOptions + tail);
		definition.importQueryDefinition(textQuery);
		definition.setName("notfavorited");
		definition.setDescription("Rule for testing favorited:false");
		ruleManager.writeRule(definition);

	}
	
	private static void teardownMatchRules() {
		ruleManager.delete("notfavorited");
		ruleManager.delete("favorites");
	}

	@Test
	public void testMatchDocument() {
		
		String[] docs = new String[] { "/alert/second.xml" };
		String[] candidates = new String[] { "notfavorited" };

		RuleDefinitionList answer = ruleManager.match(docs);
		
		assertEquals("One answer for first match scenario, favorite against all rules",
				1, answer.size());

		RuleDefinition ruleMatch = answer.iterator().next();
		assertEquals("favorites", ruleMatch.getName());

		answer = ruleManager.match(docs, candidates);
		assertEquals("Zero answers for second match scenario, favorites against false rule ",
				0, answer.size());

		docs = new String[] { "/alert/first.xml", "/alert/third.xml" };

		answer = ruleManager.match(docs);
		assertEquals("One answer for first match scenario",
				1, answer.size());
		
		RuleDefinition match = answer.iterator().next();
		assertEquals("notfavorited", match.getName());

	}

	@Test
	public void testMatchQuery() {

		StringQueryDefinition qtext = queryManager.newStringDefinition("alerts");
		qtext.setCriteria("favorited:true");

		RuleDefinitionList answer = ruleManager.match(qtext);
		assertEquals("One answer", 1, answer.size());

		qtext.setCriteria("favorited:false");
		answer = ruleManager.match(qtext, 1L, new String[] { "favorites",
		"notfavorited" });
		assertEquals("One answer", answer.size(), 1);
		assertEquals("Right answer", "notfavorited",
				answer.iterator().next().getName());

	}

	@Test
	public void testMatchPostQuery() {
		//    public <T extends RuleListReadHandle> T match(StructureWriteHandle document, T RuleListDefinition);
	//	    public <T extends RuleListReadHandle> T match(StructureWriteHandle document, String[] candidateRules, T RuleListDefinition);
	}
}
