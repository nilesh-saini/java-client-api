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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.xml.sax.SAXException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.admin.ServerConfigurationManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
public class TestBug18990 extends BasicJavaClientREST {

	private static String dbName = "TestBug18990DB";
	private static String [] fNames = {"TestBug18990DB-1"};
	private static DatabaseClient client = null;
	
@BeforeClass
	public static void setUp() throws Exception 
	{
	  System.out.println("In setup");
	  configureRESTServer(dbName, fNames);
	  setupAppServicesConstraint(dbName);
	  client = getDatabaseClientWithDigest("rest-admin", "x");
	}

@Test
	public void testBug18990() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException, JSONException
	{	
		System.out.println("Running testBug18990");
		
		String[] filenames = {"constraint1.xml", "constraint2.xml", "constraint3.xml", "constraint4.xml", "constraint5.xml"};
		String queryOptionName = "valueConstraintWildCardOpt.xml";

		// set query option validation to true and server logger to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setServerRequestLogging(true);
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();
				
		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/bug18990/", "XML");
		}
		
		setQueryOption(client, queryOptionName);
		
		QueryManager queryMgr = client.newQueryManager();
		
		// create query def
		StructuredQueryBuilder qb = queryMgr.newStructuredQueryBuilder(queryOptionName);
		StructuredQueryDefinition valueConstraintQuery1 = qb.valueConstraint("id", "00*2");
		StructuredQueryDefinition valueConstraintQuery2 = qb.valueConstraint("id", "0??6");
		StructuredQueryDefinition orFinalQuery = qb.or(valueConstraintQuery1, valueConstraintQuery2);
		
		// create handle
		StringHandle resultsHandle = new StringHandle().withFormat(Format.JSON);
		queryMgr.search(orFinalQuery, resultsHandle);
		
		// get the result
		String resultDoc = resultsHandle.get();
		
		System.out.println(resultDoc);
		JSONAssert.assertEquals("{\"snippet-format\":\"raw\", \"total\":2, \"start\":1, \"page-length\":10, \"results\":[{\"index\":1, \"uri\":\"/bug18990/constraint5.xml\", \"path\":\"fn:doc(\\\"/bug18990/constraint5.xml\\\")\", \"score\":0, \"confidence\":0, \"fitness\":0, \"href\":\"/v1/documents?uri=%2Fbug18990%2Fconstraint5.xml\", \"mimetype\":\"application/xml\", \"format\":\"xml\", \"content\":\"<root xmlns:search=\\\"http://marklogic.com/appservices/search\\\">\\n  <title>The memex</title>\\n  <popularity>5</popularity>\\n  <id>0026</id>\\n  <date xmlns=\\\"http://purl.org/dc/elements/1.1/\\\">2009-05-05</date>\\n  <price amt=\\\"123.45\\\" xmlns=\\\"http://cloudbank.com\\\"/>\\n  <p>The Memex, unfortunately, had no automated search feature.</p>\\n</root>\"}, {\"index\":2, \"uri\":\"/bug18990/constraint2.xml\", \"path\":\"fn:doc(\\\"/bug18990/constraint2.xml\\\")\", \"score\":0, \"confidence\":0, \"fitness\":0, \"href\":\"/v1/documents?uri=%2Fbug18990%2Fconstraint2.xml\", \"mimetype\":\"application/xml\", \"format\":\"xml\", \"content\":\"<root xmlns:search=\\\"http://marklogic.com/appservices/search\\\">\\n  <title>The Bush article</title>\\n  <popularity>4</popularity>\\n  <id>0012</id>\\n  <date xmlns=\\\"http://purl.org/dc/elements/1.1/\\\">2006-02-02</date>\\n  <price amt=\\\"0.12\\\" xmlns=\\\"http://cloudbank.com\\\"/>\\n  <p>The Bush article described a device called a Memex.</p>\\n</root>\"}], \"report\":\"(cts:search(fn:collection(), cts:or-query((cts:element-value-query(fn:QName(\\\"\\\",\\\"id\\\"), \\\"00*2\\\", (\\\"lang=en\\\"), 1), cts:element-value-query(fn:QName(\\\"\\\",\\\"id\\\"), \\\"0??6\\\", (\\\"lang=en\\\"), 1)), ()), (\\\"score-logtfidf\\\",cts:score-order(\\\"descending\\\")), 1))[1 to 10]\"}",resultDoc , false);
		
	    // turn off server logger
	    srvMgr.setServerRequestLogging(false);
	    srvMgr.writeConfiguration();
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
