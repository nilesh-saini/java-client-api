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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.client.util.EditableNamespaceContext;
public class TestStructuredSearchGeo extends BasicJavaClientREST {

	private static String dbName = "TestStructuredSearchGeoDB";
	private static String [] fNames = {"TestStructuredSearchGeoDB-1"};
	private static DatabaseClient client = null;

	@BeforeClass 
	public static void setUp() throws Exception 
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		setupAppServicesGeoConstraint(dbName);
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@Test	
	public void testTestStructuredSearchGeo() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testTestStructuredSearchGeo");
		String queryOptionName = "geoConstraintOpt.xml";

		// write docs
		for(int i = 1; i <= 7; i++) {
			writeDocumentUsingInputStreamHandle(client, "geo-constraint" + i + ".xml", "/geo-constraint/", "XML");
		}

		setQueryOption(client, queryOptionName);

		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition geoElementConstraintQuery = qb.geospatialConstraint("geo-elem", qb.point(12, 5));
		StructuredQueryDefinition termQuery = qb.term("bill_kara");
		StructuredQueryDefinition finalOrQuery = qb.or(geoElementConstraintQuery, termQuery);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(finalOrQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();

		assertXpathEvaluatesTo("2", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);		
	}

	@Test	
	public void testTestStructuredSearchGeoBox() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testTestStructuredSearchGeoBox");
		String queryOptionName = "geoConstraintOpt.xml";

		// write docs
		loadGeoData();

		setQueryOption(client, queryOptionName);

		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);

		StructuredQueryDefinition geoElementConstraintQuery = qb.geospatialConstraint("geo-elem-child", qb.box(-12,-5,-11,-4));
		StructuredQueryDefinition termQuery = qb.term("karl_kara");
		StructuredQueryDefinition finalAndQuery = qb.and(geoElementConstraintQuery, termQuery);

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(finalAndQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println("Output : " + convertXMLDocumentToString(resultDoc));
		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("/geo-constraint/geo-constraint2.xml", "string(//*[local-name()='result']//@*[local-name()='uri'])", resultDoc);
	}

	@Test	
	public void testTestStructuredSearchGeoBoxAndPath() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testTestStructuredSearchGeoBoxAndPath" + "This test is for Bug : 22071 & 22136");
		String queryOptionName = "geoConstraintOpt.xml";

		// write docs
		loadGeoData();

		setQueryOption(client, queryOptionName);

		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);

		StructuredQueryDefinition geoQuery = qb.geospatial(qb.geoPath(qb.pathIndex("/doc/g-elem-point")), qb.box(-12,-5,-11,-4));
		Collection<String> nameSpaceCollection = qb.getNamespaces().getAllPrefixes();
		assertEquals("getNamespace failed ",false, nameSpaceCollection.isEmpty());
		for(String prefix : nameSpaceCollection){
			System.out.println("Prefixes : "+prefix);
			System.out.println(qb.getNamespaces().getNamespaceURI(prefix));
			if (qb.getNamespaces().getNamespaceURI(prefix).contains("http://www.w3.org/2001/XMLSchema"))
			{
				EditableNamespaceContext namespaces = new EditableNamespaceContext();
				namespaces.put("new", "http://www.marklogic.com");
				qb.setNamespaces(namespaces);
				System.out.println(qb.getNamespaces().getNamespaceURI("new"));
			}
		}

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(geoQuery, resultsHandle);

		// get the result
		Document resultDoc = resultsHandle.get();
		System.out.println("Output : " + convertXMLDocumentToString(resultDoc));
		assertXpathEvaluatesTo("4", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);		
	}

	@AfterClass	
	public static void tearDown() throws Exception
	{
		System.out.println("In tear down");
		// release client
		client.release();
		cleanupRESTServer(dbName, fNames);
	}
}
