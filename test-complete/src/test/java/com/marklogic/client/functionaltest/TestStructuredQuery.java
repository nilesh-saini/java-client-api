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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.admin.ServerConfigurationManager;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryBuilder.Operator;
import com.marklogic.client.query.StructuredQueryDefinition;
public class TestStructuredQuery extends BasicJavaClientREST {

	private static String dbName = "TestStructuredQueryDB";
	private static String [] fNames = {"TestStructuredQueryDB-1"};
	private static DatabaseClient client = null;
	
	@BeforeClass	
	public static void setUp() throws Exception {
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		setupAppServicesConstraint(dbName);
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}
	
	@After
	public  void testCleanUp() throws Exception {
		clearDB();
		System.out.println("Running clear script");
	}	

	@Test	
	public void testStructuredQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testStructuredQuery");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition t = qb.valueConstraint("id", "0026");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(t, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();

		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("0026", "string(//*[local-name()='result'][1]//*[local-name()='id'])", resultDoc);	
	}

	@Test	
	public void testStructuredQueryJSON() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testStructuredQueryJSON");

		String[] filenames = {"constraint1.json", "constraint2.json", "constraint3.json", "constraint4.json", "constraint5.json"};
		String queryOptionName = "valueConstraintWildCardOpt.json";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query/", "JSON");
		}
		
		setJSONQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create value query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition t = qb.value(qb.jsonProperty("popularity"),"4");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(t, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println(convertXMLDocumentToString(resultDoc));
		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("/structured-query/constraint2.json", "string(//*[local-name()='result'][1]//@*[local-name()='uri'])", resultDoc);

		//create new word query def
		StructuredQueryDefinition t1 = qb.word(qb.jsonProperty("id"), "0012");
		// create handle
		DOMHandle resultsHandle1 = new DOMHandle();
		queryMgr.search(t1, resultsHandle1);

		// get the result
		Document resultDoc1 = resultsHandle1.get();
		System.out.println(convertXMLDocumentToString(resultDoc1));
		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc1);
		assertXpathEvaluatesTo("/structured-query/constraint2.json", "string(//*[local-name()='result'][1]//@*[local-name()='uri'])", resultDoc1);

		//create new range word query def
		StructuredQueryDefinition t2 = qb.range(qb.jsonProperty("price"), "xs:integer", Operator.GE, "0.1");
		// create handle
		DOMHandle resultsHandle2 = new DOMHandle();
		queryMgr.search(t2, resultsHandle2);

		// get the result
		Document resultDoc2 = resultsHandle2.get();
		System.out.println(convertXMLDocumentToString(resultDoc2));
		assertXpathEvaluatesTo("5", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc2);	
	}

	@Test	
	public void testValueConstraintWildCard() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testValueConstraintWildCard");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition valueConstraintQuery1 = qb.valueConstraint("id", "00*2");
		StructuredQueryDefinition valueConstraintQuery2 = qb.valueConstraint("id", "0??6");
		StructuredQueryDefinition orFinalQuery = qb.or(valueConstraintQuery1, valueConstraintQuery2);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(orFinalQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();

		assertXpathEvaluatesTo("2", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("0012", "string(//*[local-name()='result'][1]//*[local-name()='id'])", resultDoc);
		assertXpathEvaluatesTo("0026", "string(//*[local-name()='result'][2]//*[local-name()='id'])", resultDoc);
	}

	@Test	
	public void testAndNotQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testAndNotQuery");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";
		
		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query-andnot/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition termQuery1 = qb.term("Atlantic");
		StructuredQueryDefinition termQuery2 = qb.term("Monthly");
		StructuredQueryDefinition termQuery3 = qb.term("Bush");
		StructuredQueryDefinition andQuery = qb.and(termQuery1, termQuery2);
		StructuredQueryDefinition andNotFinalQuery = qb.andNot(andQuery, termQuery3);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(andNotFinalQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println(convertXMLDocumentToString(resultDoc));

		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("0113", "string(//*[local-name()='result']//*[local-name()='id'])", resultDoc);
	}

	@Test	
	public void testNearQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testNearQuery");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query-near/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition termQuery1 = qb.term("Bush");
		StructuredQueryDefinition termQuery2 = qb.term("Atlantic");
		StructuredQueryDefinition nearQuery = qb.near(6, 1.0, StructuredQueryBuilder.Ordering.UNORDERED, termQuery1, termQuery2);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(nearQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println(convertXMLDocumentToString(resultDoc));

		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("0011", "string(//*[local-name()='result']//*[local-name()='id'])", resultDoc);
	}

	@Test	
	public void testDirectoryQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testDirectoryQuery");

		String[] filenames1 = {"constraint1.xml", "constraint2.xml", "constraint3.xml"};
		String[] filenames2 = {"constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename1 : filenames1) {
			writeDocumentUsingInputStreamHandle(client, filename1, "/dir1/dir2/", "XML");
		}

		// write docs
		for(String filename2 : filenames2) {
			writeDocumentUsingInputStreamHandle(client, filename2, "/dir3/dir4/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition termQuery = qb.term("Memex");
		StructuredQueryDefinition dirQuery = qb.directory(true, "/dir3/");
		StructuredQueryDefinition andFinalQuery = qb.and(termQuery, dirQuery);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(andFinalQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println(convertXMLDocumentToString(resultDoc));

		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("0026", "string(//*[local-name()='result']//*[local-name()='id'])", resultDoc);
	}

	@Test	
	public void testDocumentQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testDocumentQuery");

		String[] filenames1 = {"constraint1.xml", "constraint2.xml", "constraint3.xml"};
		String[] filenames2 = {"constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";
		
		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename1 : filenames1) {
			writeDocumentUsingInputStreamHandle(client, filename1, "/dir1/dir2/", "XML");
		}

		// write docs
		for(String filename2 : filenames2) {
			writeDocumentUsingInputStreamHandle(client, filename2, "/dir3/dir4/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition termQuery = qb.term("Memex");
		StructuredQueryDefinition docQuery = qb.or(qb.document("/dir1/dir2/constraint2.xml"), qb.document("/dir3/dir4/constraint4.xml"), qb.document("/dir3/dir4/constraint5.xml"));
		StructuredQueryDefinition andFinalQuery = qb.and(termQuery, docQuery);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(andFinalQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println(convertXMLDocumentToString(resultDoc));

		assertXpathEvaluatesTo("2", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("0012", "string(//*[local-name()='result'][1]//*[local-name()='id'])", resultDoc);
		assertXpathEvaluatesTo("0026", "string(//*[local-name()='result'][2]//*[local-name()='id'])", resultDoc);
	}

	@Test	
	public void testCollectionQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testCollectionQuery");

		String filename1 = "constraint1.xml";
		String filename2 = "constraint2.xml";
		String filename3 = "constraint3.xml";
		String filename4 = "constraint4.xml";
		String filename5 = "constraint5.xml";
		String queryOptionName = "valueConstraintWildCardOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// create and initialize a handle on the metadata
		DocumentMetadataHandle metadataHandle1 = new DocumentMetadataHandle();
		DocumentMetadataHandle metadataHandle2 = new DocumentMetadataHandle();
		DocumentMetadataHandle metadataHandle3 = new DocumentMetadataHandle();
		DocumentMetadataHandle metadataHandle4 = new DocumentMetadataHandle();
		DocumentMetadataHandle metadataHandle5 = new DocumentMetadataHandle();

		// set the metadata
		metadataHandle1.getCollections().addAll("http://test.com/set1");
		metadataHandle1.getCollections().addAll("http://test.com/set5");
		metadataHandle2.getCollections().addAll("http://test.com/set1");
		metadataHandle3.getCollections().addAll("http://test.com/set3");
		metadataHandle4.getCollections().addAll("http://test.com/set3/set3-1");
		metadataHandle5.getCollections().addAll("http://test.com/set1");
		metadataHandle5.getCollections().addAll("http://test.com/set5");

		// write docs
		writeDocumentUsingInputStreamHandle(client, filename1, "/collection-constraint/", metadataHandle1, "XML");
		writeDocumentUsingInputStreamHandle(client, filename2, "/collection-constraint/", metadataHandle2, "XML");
		writeDocumentUsingInputStreamHandle(client, filename3, "/collection-constraint/", metadataHandle3, "XML");
		writeDocumentUsingInputStreamHandle(client, filename4, "/collection-constraint/", metadataHandle4, "XML");
		writeDocumentUsingInputStreamHandle(client, filename5, "/collection-constraint/", metadataHandle5, "XML");

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition termQuery = qb.term("Memex");
		StructuredQueryDefinition collQuery = qb.or(qb.collection("http://test.com/set1"), qb.collection("http://test.com/set3"));
		StructuredQueryDefinition andFinalQuery = qb.and(termQuery, collQuery);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(andFinalQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println(convertXMLDocumentToString(resultDoc));

		assertXpathEvaluatesTo("2", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("0012", "string(//*[local-name()='result'][1]//*[local-name()='id'])", resultDoc);
		assertXpathEvaluatesTo("0026", "string(//*[local-name()='result'][2]//*[local-name()='id'])", resultDoc);	
	}

	@Test	
	public void testContainerConstraintQuery() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testContainerConstraintQuery");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "containerConstraintOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query-container/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition containerQuery = qb.containerConstraint("title-contain", qb.term("Bush"));

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(containerQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println(convertXMLDocumentToString(resultDoc));

		assertXpathEvaluatesTo("2", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);	
	}
	
	/*
	 * Create a StructuredQueryDefinition (using StructuredQueryBuilder) and add a string query by calling setCriteria and withCriteria
	 * Make sure a query using those query definitions selects only documents that match both the query definition and the string query
	 * 
	 * Uses setCriteria. Uses valueConstraintWildCardOpt.xml options file
	 * QD and string query (Memex) should return 1 URI in the response.
	 */
	@Test	
	public void testSetCriteriaOnStructdgQueryDef() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testSetCriteriaOnStructdgQueryDef");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition strutdDef = qb.valueConstraint("id", "0012");
		strutdDef.setCriteria("Memex");
		
		// create handle
		JacksonHandle strHandle = new JacksonHandle();
		JacksonHandle results = queryMgr.search(strutdDef, strHandle.withFormat(Format.JSON));
		
		JsonNode node = results.get();
		assertEquals("Number of results returned incorrect in response", "1", node.path("total").asText());
		assertEquals("Result returned incorrect in response", "/structured-query/constraint2.xml", node.path("results").get(0).path("uri").asText());
		
		// With multiple setCriteria - positive
		StructuredQueryDefinition strutdDefPos = qb.valueConstraint("id", "0012");
		strutdDefPos.setCriteria("Memex");
		strutdDefPos.setCriteria("described");
		
		// create handle
		JacksonHandle strHandlePos = new JacksonHandle();
		JacksonHandle resultsPos = queryMgr.search(strutdDefPos, strHandlePos.withFormat(Format.JSON));
		
		JsonNode nodePos = resultsPos.get();
		// Return 1 node - constraint2.xml
		assertEquals("Number of results returned incorrect in response", "1", nodePos.path("total").asText());
		assertEquals("Result returned incorrect in response", "/structured-query/constraint2.xml", nodePos.path("results").get(0).path("uri").asText());
		
		// With setCriteria AND - positive
		StructuredQueryDefinition strutdDefPosAnd = qb.valueConstraint("id", "0012");
		strutdDefPosAnd.setCriteria("Memex AND described");		

		// create handle
		JacksonHandle strHandlePosAnd = new JacksonHandle();
		JacksonHandle resultsPosAnd = queryMgr.search(strutdDefPosAnd, strHandlePosAnd.withFormat(Format.JSON));

		JsonNode nodePosAnd = resultsPosAnd.get();
		// Return 1 node - constraint2.xml
		assertEquals("Number of results returned incorrect in response", "1", nodePosAnd.path("total").asText());
		assertEquals("Result returned incorrect in response", "/structured-query/constraint2.xml", nodePosAnd.path("results").get(0).path("uri").asText());
		assertEquals("Get Criteria returned incorrect", "Memex AND described", strutdDefPosAnd.getCriteria());
			
		// With multiple setCriteria - negative
		StructuredQueryDefinition strutdDefNeg = qb.valueConstraint("id", "0012");
		strutdDefNeg.setCriteria("Memex");
		strutdDefNeg.setCriteria("Atlantic");

		// create handle
		JacksonHandle strHandleNeg = new JacksonHandle();
		JacksonHandle resultsNeg = queryMgr.search(strutdDefNeg, strHandleNeg.withFormat(Format.JSON));

		JsonNode nodeNeg = resultsNeg.get();
		// Return 0 nodes
		assertEquals("Number of results returned incorrect in response", "0", nodeNeg.path("total").asText());
	}
	
	/*
	 * Create a StructuredQueryDefinition (using StructuredQueryBuilder) and add a string query by calling setCriteria and withCriteria
	 * Make sure a query using those query definitions selects only documents that match both the query definition and the string query
	 * 
	 * Uses withCriteria. Uses valueConstraintPopularityOpt.xml options file
	 * QD and string query (Vannevar) should return 2 URIs in the response. constraint1.xml and constraint4.xml
	 */
	@Test	
	public void testWithCriteriaOnStructdgQueryDef() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testWithCriteriaOnStructdgQueryDef");

		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintPopularityOpt.xml";

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();

		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/structured-query/", "XML");
		}

		setQueryOption(client, queryOptionName);
		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition strutdDef = (qb.valueConstraint("popularity", "5")).withCriteria("Vannevar");
		
		// create handle
		JacksonHandle strHandle = new JacksonHandle();
		JacksonHandle results = queryMgr.search(strutdDef, strHandle.withFormat(Format.JSON));
		
		JsonNode node = results.get();
		assertEquals("Number of results returned incorrect in response", "2", node.path("total").asText());
		assertTrue("Results returned incorrect in response", node.path("results").get(0).path("uri").asText().contains("/structured-query/constraint1.xml")||
				                                             node.path("results").get(1).path("uri").asText().contains("/structured-query/constraint1.xml") );
		assertTrue("Results returned incorrect in response", node.path("results").get(0).path("uri").asText().contains("/structured-query/constraint4.xml")||
                                                             node.path("results").get(1).path("uri").asText().contains("/structured-query/constraint4.xml") );
		// With multiple withCriteria - positive
		StructuredQueryDefinition strutdDefPos = (qb.valueConstraint("popularity", "5")).withCriteria("Vannevar").withCriteria("Atlantic").withCriteria("intellectual");

		// create handle
		JacksonHandle strHandlePos = new JacksonHandle();
		JacksonHandle resultsPos = queryMgr.search(strutdDefPos, strHandlePos.withFormat(Format.JSON));

		JsonNode nodePos = results.get();
		// Return 2 nodes.
		assertEquals("Number of results returned incorrect in response", "2", nodePos.path("total").asText());
		assertTrue("Results returned incorrect in response", nodePos.path("results").get(0).path("uri").asText().contains("/structured-query/constraint1.xml")||
				nodePos.path("results").get(1).path("uri").asText().contains("/structured-query/constraint1.xml") );
		assertTrue("Results returned incorrect in response", nodePos.path("results").get(0).path("uri").asText().contains("/structured-query/constraint4.xml")||
				nodePos.path("results").get(1).path("uri").asText().contains("/structured-query/constraint4.xml") );
		
		// With multiple withCriteria - negative
		StructuredQueryDefinition strutdDefNeg = (qb.valueConstraint("popularity", "5")).withCriteria("Vannevar").withCriteria("England");

		// create handle
		JacksonHandle strHandleNeg = new JacksonHandle();
		JacksonHandle resultsNeg = queryMgr.search(strutdDefNeg, strHandleNeg.withFormat(Format.JSON));

		JsonNode nodeNeg = resultsNeg.get();
		// Return 0 nodes.
		assertEquals("Number of results returned incorrect in response", "0", nodeNeg.path("total").asText());
		assertEquals("Get Criteria returned incorrect in response", "England", strutdDefNeg.getCriteria());
		
		// create query def2 with both criteria methods and check fluent return
		
		StructuredQueryDefinition strutdDef2 = qb.valueConstraint("popularity", "5");
		strutdDef2.withCriteria("Vannevar").setCriteria("Bush");
		
		// create handle
		JacksonHandle strHandle2 = new JacksonHandle();
		JacksonHandle results2 = queryMgr.search(strutdDef2, strHandle2.withFormat(Format.JSON));

		JsonNode node2 = results2.get();
		// Returns 1 node. constraint1.xml
		assertEquals("Number of results returned incorrect in response", "1", node2.path("total").asText());
		assertEquals("Result returned incorrect in response", "/structured-query/constraint1.xml", node2.path("results").get(0).path("uri").asText());
		assertEquals("Get Criteria returned incorrect in response", "Bush", strutdDef2.getCriteria());
	}

	@AfterClass	
	public static void tearDown() throws Exception {
		System.out.println("In tear down");
		// release client
		client.release();
		cleanupRESTServer(dbName, fNames);
	}
}
