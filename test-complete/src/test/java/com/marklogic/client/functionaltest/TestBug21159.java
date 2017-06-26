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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.admin.ServerConfigurationManager;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.TuplesHandle;
import com.marklogic.client.io.ValuesHandle;
import com.marklogic.client.query.CountedDistinctValue;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.RawCombinedQueryDefinition;
import com.marklogic.client.query.Tuple;
import com.marklogic.client.query.ValuesDefinition;
public class TestBug21159 extends BasicJavaClientREST {

	private static String dbName = "TestRawCombinedQueryDB";
	private static String [] fNames = {"TestRawCombinedQueryDB-1"};
	private static DatabaseClient client = null;
	
@BeforeClass
	public static void setUp() throws Exception 
	{
	    System.out.println("In setup");
	    configureRESTServer(dbName, fNames);
	    setupAppServicesConstraint(dbName);
	  
    	addRangeElementIndex(dbName, "string", "", "grandchild", "http://marklogic.com/collation/");
    	addRangeElementIndex(dbName, "double", "", "double");
    	addRangeElementIndex(dbName, "int", "", "int");
    	addRangeElementIndex(dbName, "string", "", "string", "http://marklogic.com/collation/");
    	client = getDatabaseClientWithDigest("rest-admin", "x");
	}

@Test
public void testBug21159Tuples() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testBug21159Tuples");
		
		String[] filenames = {"tuples-test1.xml", "tuples-test2.xml", "tuples-test3.xml", "tuples-test4.xml", "lexicon-test1.xml","lexicon-test2.xml"};

		// set query option validation to true
		ServerConfigurationManager srvMgr = client.newServerConfigManager();
		srvMgr.readConfiguration();
		srvMgr.setQueryOptionValidation(true);
		srvMgr.writeConfiguration();
				
		// write docs
		for(String filename : filenames) {
			writeDocumentUsingInputStreamHandle(client, filename, "/raw-combined-query/", "XML");
		}
		
		// get the combined query
        File file = new File("src/test/java/com/marklogic/client/functionaltest/combined/LexiconOptions.xml");
		
		String combinedQuery = convertFileToString(file);
				
		RawCombinedQueryDefinition rawCombinedQueryDefinition;
		QueryManager queryMgr = client.newQueryManager();
		rawCombinedQueryDefinition = queryMgr.newRawCombinedQueryDefinition(new StringHandle(combinedQuery).withMimetype("application/xml"));
		
		StringHandle stringResults = null;
		ValuesDefinition vdef = queryMgr.newValuesDefinition("grandchild");
		
		vdef.setQueryDefinition(rawCombinedQueryDefinition);
		
		stringResults = queryMgr.tuples(vdef, new StringHandle());
		System.out.println(stringResults.get());
		
		ValuesHandle valuesResults = queryMgr.values(vdef,new ValuesHandle());
		
		assertFalse(valuesResults.getMetrics().getTotalTime() == -1);

		CountedDistinctValue[] values = valuesResults.getValues();
		
		assertNotNull(values);	
	}

@Test
public void testBug21159Values() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
{	
	System.out.println("Running testBug21159Values");
	
	String[] filenames = {"tuples-test1.xml", "tuples-test2.xml", "tuples-test3.xml", "tuples-test4.xml", "lexicon-test1.xml","lexicon-test2.xml"};

	// set query option validation to true
	ServerConfigurationManager srvMgr = client.newServerConfigManager();
	srvMgr.readConfiguration();
	srvMgr.setQueryOptionValidation(true);
	srvMgr.writeConfiguration();
			
	// write docs
	for(String filename : filenames) {
		writeDocumentUsingInputStreamHandle(client, filename, "/raw-combined-query/", "XML");
	}
	
	// get the combined query
    File file = new File("src/test/java/com/marklogic/client/functionaltest/combined/LexiconOptions.xml");
	
	String combinedQuery = convertFileToString(file);
			
	RawCombinedQueryDefinition rawCombinedQueryDefinition;
	QueryManager queryMgr = client.newQueryManager();
	rawCombinedQueryDefinition = queryMgr.newRawCombinedQueryDefinition(new StringHandle(combinedQuery).withMimetype("application/xml"));
	
	StringHandle stringResults = null;
	ValuesDefinition vdef = queryMgr.newValuesDefinition("n-way");
	
	vdef.setQueryDefinition(rawCombinedQueryDefinition);
	
	stringResults = queryMgr.tuples(vdef, new StringHandle());
	System.out.println(stringResults.get());
	
	TuplesHandle tuplesResults = queryMgr.tuples(vdef,
			new TuplesHandle());
	Tuple[] tuples = tuplesResults.getTuples();
	assertNotNull(tuples);
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