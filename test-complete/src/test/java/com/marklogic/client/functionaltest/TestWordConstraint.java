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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.SearchHandle;
public class TestWordConstraint extends BasicJavaClientREST {

	static String filenames[] = {"word-constraint-doc1.xml", "word-constraint-doc2.xml"};
	static String queryOptionName = "wordConstraintOpt.xml"; 
	private static String dbName = "WordConstraintDB";
	private static String [] fNames = {"WordConstraintDB-1"};
	private static DatabaseClient client = null;

	@BeforeClass	
	public static void setUp() throws Exception
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@Test	
	public void testElementWordConstraint() throws KeyManagementException, NoSuchAlgorithmException, IOException
	{		
		// write docs
		for(String filename:filenames) {
			writeDocumentReaderHandle(client, filename, "/word-constraint/", "XML");
		}

		// write the query options to the database
		setQueryOption(client, queryOptionName);

		// run the search
		SearchHandle resultsHandle = runSearch(client, queryOptionName, "my-element-word:paris");

		// search result
		String searchResult = returnSearchResult(resultsHandle);

		String expectedSearchResult = "|Matched 1 locations in /word-constraint/word-constraint-doc1.xml";

		System.out.println(searchResult);

		assertEquals("Search result difference", expectedSearchResult, searchResult);	
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

