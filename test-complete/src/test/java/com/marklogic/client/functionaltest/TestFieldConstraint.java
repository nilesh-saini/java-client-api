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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.SearchHandle;
public class TestFieldConstraint extends BasicJavaClientREST {
	static String filenames[] = {"bbq1.xml", "bbq2.xml", "bbq3.xml", "bbq4.xml", "bbq5.xml"};
	static String queryOptionName = "fieldConstraintOpt.xml";
	private static String dbName = "FieldConstraintDB";
	private static String [] fNames = {"FieldConstraintDB-1"};
	private static DatabaseClient client = null;
	private static int restPort=8011;
	
	@BeforeClass
	public static void setUp() throws Exception
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		addField(dbName, "bbqtext");
		includeElementField(dbName, "bbqtext", "http://example.com", "title");
		includeElementField(dbName, "bbqtext", "http://example.com", "abstract");
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}
	 
	 @After
	public  void testCleanUp() throws Exception
	{
		clearDB();
		System.out.println("Running clear script");
	}

	@Test
	public void testFieldConstraint() throws KeyManagementException, NoSuchAlgorithmException, IOException
	{
		// write docs
		for(String filename:filenames) {
			writeDocumentReaderHandle(client, filename, "/field-constraint/", "XML");
		}
							
		// write the query options to the database
		setQueryOption(client, queryOptionName);
							
		// run the search
		SearchHandle resultsHandle = runSearch(client, queryOptionName, "summary:Louisiana AND summary:sweet");
		
		// search result
		String matchResult = "Matched "+resultsHandle.getTotalResults();
		String expectedMatchResult = "Matched 1";
		assertEquals("Match results difference", expectedMatchResult, matchResult);
		
		String result = returnSearchResult(resultsHandle);
		String expectedResult = "|Matched 3 locations in /field-constraint/bbq3.xml";
		
		assertEquals("Results difference", expectedResult, result);	
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
