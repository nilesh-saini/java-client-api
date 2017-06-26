/*
 * Copyright 2014-2017 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marklogic.client.functionaltest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.admin.TransformExtensionsManager;
import com.marklogic.client.alerting.RuleDefinition;
import com.marklogic.client.alerting.RuleDefinitionList;
import com.marklogic.client.alerting.RuleManager;
import com.marklogic.client.document.ServerTransform;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;

public class TestRawAlert extends BasicJavaClientREST {
	private static String dbName = "TestRawAlertDB";
	private static String [] fNames = {"TestRawAlertDB-1"};
	private static DatabaseClient client = null;
	
	@BeforeClass	
	public static void setUp() throws Exception 
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		setupAppServicesConstraint(dbName);
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@After
	public void testCleanUp() throws Exception
	{
		clearDB();
		System.out.println("Running clear script");
	}

	@Test	
	public void testRawAlert() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlert");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// get the rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");

		// create a handle for the rule
		FileHandle writeHandle = new FileHandle(file);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", writeHandle);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		String criteria = "atlantic";
		StringQueryDefinition querydef = queryMgr.newStringDefinition();
		querydef.setCriteria(criteria);

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(querydef, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		String expected = "";

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria "+criteria+" matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);

			expected = expected + rule.getName() + " - " + rule.getMetadata() + " | ";
		}

		System.out.println(expected);

		assertTrue("incorrect rule", expected.contains("RULE-TEST-1 - {rule-number=one} |"));		
	}

	@Test	
	public void testRawAlertUnmatched() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertUnmatched");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// get the rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");

		// create a handle for the rule
		FileHandle writeHandle = new FileHandle(file);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", writeHandle);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		String criteria = "Memex"; // test case for unmatched rule
		StringQueryDefinition querydef = queryMgr.newStringDefinition();
		querydef.setCriteria(criteria);

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(querydef, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		assertEquals("incorrect matching rule", 0, matchedRules.size());	
	}

	@Test	
	public void testRawAlertMultipleRules() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertMultipleRules");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// create handle
		InputStreamHandle ruleHandle1 = new InputStreamHandle();
		InputStreamHandle ruleHandle2 = new InputStreamHandle();

		// get the rule file
		InputStream inputStream1 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");
		InputStream inputStream2 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule2.xml");

		ruleHandle1.set(inputStream1);
		ruleHandle2.set(inputStream2);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", ruleHandle1);
		ruleMgr.writeRule("RULE-TEST-2", ruleHandle2);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		String criteria = "atlantic";
		StringQueryDefinition querydef = queryMgr.newStringDefinition();
		querydef.setCriteria(criteria);

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(querydef, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		String expected = "";

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria "+criteria+" matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);
			expected = expected + rule.getName() + " - " + rule.getMetadata() + " | ";
		}

		System.out.println(expected);

		assertTrue("incorrect rules", expected.contains("RULE-TEST-1 - {rule-number=one}")&& expected.contains("RULE-TEST-2 - {rule-number=two}"));		
	}

	@Test	
	public void testRawAlertUnmatchingRuleName() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertUnmatchingRuleName");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// create handle
		InputStreamHandle ruleHandle1 = new InputStreamHandle();

		// get the rule file
		InputStream inputStream1 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");

		ruleHandle1.set(inputStream1);

		String exception = "";

		// write the rule to the database
		try {
			ruleMgr.writeRule("RULE-TEST-A", ruleHandle1); // test case for non-matching rule name
		} catch(Exception e) {
			exception = e.toString();
		}

		String expectedException = "Invalid content: If provided, rule name in payload must match rule name in URL";

		assertTrue("Exception is not thrown", exception.contains(expectedException));	
	}

	@Test	
	public void testRawAlertJSON() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertJSON");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// get the rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.json");

		String ruleInJson = convertFileToString(file);

		// create a handle for the rule
		StringHandle ruleHandle = new StringHandle(ruleInJson);
		ruleHandle.setFormat(Format.JSON);
		
		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1-JSON", ruleHandle);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		String criteria = "atlantic";
		StringQueryDefinition querydef = queryMgr.newStringDefinition(); // test case with string def
		querydef.setCriteria(criteria);

		// create query def
		//StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(); // tes case with structured query
		//StructuredQueryDefinition termQuery1 = qb.term("Atlantic");

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(querydef, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		String expected= "";

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);
			expected = expected + rule.getName() + " - " + rule.getMetadata() + " | ";
		}

		System.out.println(expected);

		assertTrue("rule is not correct", expected.contains("RULE-TEST-1-JSON - {{http://marklogic.com/rest-api}rule-number=one json}"));		
	}

	@Test	
	public void testRawAlertStructuredQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertStructuredQuery");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// get the rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");

		// create a handle for the rule
		FileHandle writeHandle = new FileHandle(file);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", writeHandle);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder();
		StructuredQueryDefinition termQuery1 = qb.term("Atlantic");

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(termQuery1, new RuleDefinitionList());

		System.out.println(matchedRules.size()); // bug, should return 1

		assertEquals("result count is not correct", 1, matchedRules.size());

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);
		}	
	}

	@Test	
	public void testRawAlertStructuredQueryTransform() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertStructuredQueryTransform");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// get the rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");

		// create a handle for the rule
		FileHandle writeHandle = new FileHandle(file);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", writeHandle);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder();
		StructuredQueryDefinition termQuery1 = qb.term("Atlantic");

		// Write the rule in Modules database of Server
		TransformExtensionsManager transformManager= client.newServerConfigManager().newTransformExtensionsManager();

		File ruleTransform = new File("src/test/java/com/marklogic/client/functionaltest/rules/rule-transform.xqy");
		transformManager.writeXQueryTransform("ruleTransform", new FileHandle(ruleTransform));

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();	

		ServerTransform transform = new ServerTransform("ruleTransform");
		RuleDefinitionList matchedRules = ruleMatchMgr.match(termQuery1, 0L, QueryManager.DEFAULT_PAGE_LENGTH, new String[] {}, new RuleDefinitionList(), transform);

		System.out.println(matchedRules.size()); // bug, should return 1

		assertEquals("result count is not correct", 1, matchedRules.size());

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);
		}	
	}

	@Test	
	public void testRawAlertCandidateRules() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertCandidateRules");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// create handle
		InputStreamHandle ruleHandle1 = new InputStreamHandle();
		InputStreamHandle ruleHandle2 = new InputStreamHandle();
		InputStreamHandle ruleHandle3 = new InputStreamHandle();

		// get the rule file
		InputStream inputStream1 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");
		InputStream inputStream2 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule2.xml");
		InputStream inputStream3 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule3.xml");

		ruleHandle1.set(inputStream1);
		ruleHandle2.set(inputStream2);
		ruleHandle3.set(inputStream3);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", ruleHandle1);
		ruleMgr.writeRule("RULE-TEST-2", ruleHandle2);
		ruleMgr.writeRule("RULE-TEST-3", ruleHandle3);

		// get the json rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule3.json");

		String ruleInJson = convertFileToString(file);

		// create a handle for the rule
		StringHandle ruleHandle4 = new StringHandle(ruleInJson);
		ruleHandle4.setFormat(Format.JSON);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-3-JSON", ruleHandle4);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		String criteria = "memex";
		StringQueryDefinition querydef = queryMgr.newStringDefinition();
		querydef.setCriteria(criteria);

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		String[] candidateRules = {"RULE-TEST-1", "RULE-TEST-2", "RULE-TEST-3", "RULE-TEST-3-JSON"};

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(querydef, 1,2, candidateRules, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		String expected = "";

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria "+criteria+" matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);
			expected = expected + rule.getName() + " - " + rule.getMetadata() + " | ";
		}

		System.out.println(expected);

		if(expected.equals("RULE-TEST-3 - {rule-number=three} | RULE-TEST-3-JSON - {{http://marklogic.com/rest-api}rule-number=three json} | ")) {
			assertTrue("rule is incorrect", expected.contains("RULE-TEST-3 - {rule-number=three} | RULE-TEST-3-JSON - {{http://marklogic.com/rest-api}rule-number=three json}"));
		}
		else if(expected.equals("RULE-TEST-3-JSON - {{http://marklogic.com/rest-api}rule-number=three json} | RULE-TEST-3 - {rule-number=three} | ")) {
			assertTrue("rule is incorrect", expected.contains("RULE-TEST-3-JSON - {{http://marklogic.com/rest-api}rule-number=three json} | RULE-TEST-3 - {rule-number=three}"));
		}
		else {
			assertTrue("there is no matching rule", false);
		}		
	}

	@Test	
	public void testRawAlertCandidateRulesUnmatched() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertCandidateRulesUnmatched");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// create handle
		InputStreamHandle ruleHandle1 = new InputStreamHandle();
		InputStreamHandle ruleHandle2 = new InputStreamHandle();
		InputStreamHandle ruleHandle3 = new InputStreamHandle();

		// get the rule file
		InputStream inputStream1 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");
		InputStream inputStream2 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule2.xml");
		InputStream inputStream3 = new FileInputStream("src/test/java/com/marklogic/client/functionaltest/rules/alertRule3.xml");

		ruleHandle1.set(inputStream1);
		ruleHandle2.set(inputStream2);
		ruleHandle3.set(inputStream3);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", ruleHandle1);
		ruleMgr.writeRule("RULE-TEST-2", ruleHandle2);
		ruleMgr.writeRule("RULE-TEST-3", ruleHandle3);

		// get the json rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule3.json");

		String ruleInJson = convertFileToString(file);

		// create a handle for the rule
		StringHandle ruleHandle4 = new StringHandle(ruleInJson);
		ruleHandle4.setFormat(Format.JSON);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-3-JSON", ruleHandle4);

		// create a manager for document search criteria
		QueryManager queryMgr = client.newQueryManager();

		// specify the search criteria for the documents
		String criteria = "atlantic";
		StringQueryDefinition querydef = queryMgr.newStringDefinition();
		querydef.setCriteria(criteria);

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		String[] candidateRules = {"gar", "bar", "foo"};

		// match the rules against the documents qualified by the criteria

		RuleDefinitionList matchedRules = ruleMatchMgr.match(querydef, 1, 2, candidateRules, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		assertEquals("match rule is incorrect", 0, matchedRules.size());	
	}

	@Test	
	public void testRawAlertDocUris() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertDocUris");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		
		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// get the rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule1.xml");

		// create a handle for the rule		
		FileHandle writeHandle = new FileHandle(file);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-1", writeHandle);

		// specify the search criteria for the documents
		String[] docUris = {"/raw-alert/constraint1.xml", 
				"/raw-alert/constraint2.xml", 
				"/raw-alert/constraint3.xml", 
				"/raw-alert/constraint4.xml", 
		"/raw-alert/constraint5.xml"};

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(docUris, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		String expected = "";

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);
			expected = expected + rule.getName() + " - " + rule.getMetadata() + " | ";
		}

		System.out.println(expected);

		assertTrue("rule is incorrect", expected.contains("RULE-TEST-1 - {rule-number=one}"));	
	}

	@Test	
	public void testRawAlertDocPayload() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testRawAlertDocPayload");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-alert/", "XML");
		}

		// create a manager for configuring rules
		RuleManager ruleMgr = client.newRuleManager();

		// get the rule
		File file = new File("src/test/java/com/marklogic/client/functionaltest/rules/alertRule2.xml");

		// create a handle for the rule
		FileHandle writeHandle = new FileHandle(file);

		// write the rule to the database
		ruleMgr.writeRule("RULE-TEST-2", writeHandle);

		String filename = "constraint1.xml";

		// get the file
		File doc = new File("src/test/java/com/marklogic/client/functionaltest/data/" + filename);
		String docContent = convertFileToString(doc);
		StringHandle handle = new StringHandle(docContent);

		// create a manager for matching rules
		RuleManager ruleMatchMgr = client.newRuleManager();

		// match the rules against the documents qualified by the criteria
		RuleDefinitionList matchedRules = ruleMatchMgr.match(handle, new RuleDefinitionList());

		System.out.println(matchedRules.size());

		String expected = "";

		// iterate over the matched rules
		Iterator<RuleDefinition> ruleItr = matchedRules.iterator();
		while (ruleItr.hasNext()) {
			RuleDefinition rule = ruleItr.next();
			System.out.println(
					"document criteria matched rule "+
							rule.getName()+" with metadata "+rule.getMetadata()
					);
			expected = expected + rule.getName() + " - " + rule.getMetadata() + " | ";
		}

		System.out.println(expected);

		assertTrue("rule is incorrect", expected.contains("RULE-TEST-2 - {rule-number=two}"));	
	}

	@AfterClass	
	public static  void tearDown() throws Exception
	{
		System.out.println("In tear down");
		// release client
		client.release();	
		cleanupRESTServer(dbName, fNames);
	}
}
