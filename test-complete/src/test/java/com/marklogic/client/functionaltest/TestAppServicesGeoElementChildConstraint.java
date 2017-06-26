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

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;

public class TestAppServicesGeoElementChildConstraint extends BasicJavaClientREST {

	private static String dbName = "AppServicesGeoElementChildConstraintDB";
	private static String [] fNames = {"AppServicesGeoElementChildConstraintDB-1"};
	private static DatabaseClient client = null;
	
@BeforeClass
	public static void setUp() throws Exception 
	{
	  System.out.println("In setup");
	  configureRESTServer(dbName, fNames);
	  setupAppServicesGeoConstraint(dbName);
	  client = getDatabaseClientWithDigest("rest-admin", "x");
	}
@After
public  void testCleanUp() throws Exception
{
	clearDB();
	System.out.println("Running clear script");
}

@Test
	public void testPointPositiveLangLat() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testPointPositiveLangLat");
		
		String queryOptionName = "geoConstraintOpt.xml";
		
		// write docs
		loadGeoData();
		
		setQueryOption(client, queryOptionName);
		
		QueryManager queryMgr = client.newQueryManager();
		
		// create query def
		StringQueryDefinition querydef = queryMgr.newStringDefinition(queryOptionName);
		querydef.setCriteria("geo-elem-child:\"12,5\"");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(querydef, resultsHandle);
		
		// get the result
		Document resultDoc = resultsHandle.get();
		
		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("karl_kara 12,5 12,5 12 5", "string(//*[local-name()='result'][1]//*[local-name()='match'])", resultDoc);	
	}

@Test
	public void testPointNegativeLangLat() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testPointNegativeLangLat");
		
		String queryOptionName = "geoConstraintOpt.xml";
		
		// write docs
		loadGeoData();
		
		setQueryOption(client, queryOptionName);
		
		QueryManager queryMgr = client.newQueryManager();
		
		// create query def
		StringQueryDefinition querydef = queryMgr.newStringDefinition(queryOptionName);
		querydef.setCriteria("geo-elem-child:\"-12,-5\"");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(querydef, resultsHandle);
		
		// get the result
		Document resultDoc = resultsHandle.get();
		
		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("karl_kara -12,-5 -12,-5 -12 -5", "string(//*[local-name()='result'][1]//*[local-name()='match'])", resultDoc);
	}

@Test
	public void testNegativePointInvalidValue() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testNegativePointInvalidValue");
		
		String queryOptionName = "geoConstraintOpt.xml";
		
		// write docs
		loadGeoData();
		
		setQueryOption(client, queryOptionName);
		
		QueryManager queryMgr = client.newQueryManager();
		
		// create query def
		StringQueryDefinition querydef = queryMgr.newStringDefinition(queryOptionName);
		querydef.setCriteria("geo-elem-child:\"12,A\"");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		
		String result = "";
		try
		{
			queryMgr.search(querydef, resultsHandle);
			Document resultDoc = resultsHandle.get();
			result = convertXMLDocumentToString(resultDoc).toString();
			System.out.println("Result : "+result);
			
		} catch (Exception e) { e.toString(); }
		
		assertTrue("Expected Warning message is not thrown", result.contains("<search:warning id=\"SEARCH-IGNOREDQTEXT\">[Invalid text, cannot parse geospatial point from '12,A'.]</search:warning>"));	
	}

@Test
	public void testCircleNegativeLangLat() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("testCircleNegativeLangLat");
		
		String queryOptionName = "geoConstraintOpt.xml";
		
		// write docs
		loadGeoData();
		
		setQueryOption(client, queryOptionName);
		
		QueryManager queryMgr = client.newQueryManager();
		
		// create query def
		StringQueryDefinition querydef = queryMgr.newStringDefinition(queryOptionName);
		querydef.setCriteria("geo-elem-child:\"@70 -12,-5\"");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(querydef, resultsHandle);
		
		// get the result
		Document resultDoc = resultsHandle.get();
		
		assertXpathEvaluatesTo("5", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("jack_kara -11,-5 -11,-5 -11 -5", "string(//*[local-name()='result'][1]//*[local-name()='match'])", resultDoc);
		assertXpathEvaluatesTo("karl_jill -12,-4 -12,-4 -12 -4", "string(//*[local-name()='result'][2]//*[local-name()='match'])", resultDoc);
		assertXpathEvaluatesTo("karl_kara -12,-5 -12,-5 -12 -5", "string(//*[local-name()='result'][3]//*[local-name()='match'])", resultDoc);
		assertXpathEvaluatesTo("bill_kara -13,-5 -13,-5 -13 -5", "string(//*[local-name()='result'][4]//*[local-name()='match'])", resultDoc);
		assertXpathEvaluatesTo("karl_gale -12,-6 -12,-6 -12 -6", "string(//*[local-name()='result'][5]//*[local-name()='match'])", resultDoc);	
	}

@Test
	public void testBoxNegativeLangLat() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("testBoxNegativeLangLat");
		
		String queryOptionName = "geoConstraintOpt.xml";
		
		// write docs
		loadGeoData();
		
		setQueryOption(client, queryOptionName);
		
		QueryManager queryMgr = client.newQueryManager();
		
		// create query def
		StringQueryDefinition querydef = queryMgr.newStringDefinition(queryOptionName);
		querydef.setCriteria("geo-elem-child:\"[-12,-5,-11,-4]\"");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(querydef, resultsHandle);
		
		// get the result
		Document resultDoc = resultsHandle.get();
		
		assertXpathEvaluatesTo("4", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("jack_kara -11,-5 -11,-5 -11 -5", "string(//*[local-name()='result'][1]//*[local-name()='match'])", resultDoc);
		assertXpathEvaluatesTo("karl_jill -12,-4 -12,-4 -12 -4", "string(//*[local-name()='result'][2]//*[local-name()='match'])", resultDoc);
		assertXpathEvaluatesTo("karl_kara -12,-5 -12,-5 -12 -5", "string(//*[local-name()='result'][3]//*[local-name()='match'])", resultDoc);
		assertXpathEvaluatesTo("jack_jill -11,-4 -11,-4 -11 -4", "string(//*[local-name()='result'][4]//*[local-name()='match'])", resultDoc);		
	}

@Test
	public void testBoxAndWord() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testPointAndWord");
		
		String queryOptionName = "geoConstraintOpt.xml";
		
		// write docs
		loadGeoData();
		
		setQueryOption(client, queryOptionName);
		
		QueryManager queryMgr = client.newQueryManager();
		
		// create query def
		StringQueryDefinition querydef = queryMgr.newStringDefinition(queryOptionName);
		querydef.setCriteria("geo-elem-child:\"[-12,-5,-11,-4]\" AND karl_kara");

		// create handle
		DOMHandle resultsHandle = new DOMHandle();
		queryMgr.search(querydef, resultsHandle);
		
		// get the result
		Document resultDoc = resultsHandle.get();
		
		assertXpathEvaluatesTo("1", "string(//*[local-name()='result'][last()]//@*[local-name()='index'])", resultDoc);
		assertXpathEvaluatesTo("/geo-constraint/geo-constraint2.xml", "string(//*[local-name()='result']//@*[local-name()='uri'])", resultDoc);	
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
