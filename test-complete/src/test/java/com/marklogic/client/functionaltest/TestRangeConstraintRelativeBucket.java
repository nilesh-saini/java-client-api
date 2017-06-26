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

public class TestRangeConstraintRelativeBucket extends BasicJavaClientREST {
	static String filenames[] = {"bbq1.xml", "bbq2.xml", "bbq3.xml", "bbq4.xml", "bbq5.xml"};
	static String queryOptionName = "rangeRelativeBucketConstraintOpt.xml"; 
	private static String dbName = "RangeConstraintRelBucketDB";
	private static String [] fNames = {"RangeConstraintRelBucketDB-1"};
	private static DatabaseClient client = null;

	@BeforeClass	
	public static void setUp() throws Exception
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		addRangeElementAttributeIndex(dbName, "dateTime", "http://example.com", "entry", "", "date");
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@Test	
	public void testRangeConstraintRelativeBucket() throws KeyManagementException, NoSuchAlgorithmException, IOException
	{
		// write docs
		for(String filename:filenames) {
			writeDocumentReaderHandle(client, filename, "/range-constraint-rel-bucket/", "XML");
		}

		// write the query options to the database
		setQueryOption(client, queryOptionName);

		// run the search
		SearchHandle resultsHandle = runSearch(client, queryOptionName, "date:older");

		// search result
		String result = "Matched "+resultsHandle.getTotalResults();
		String expectedResult = "Matched 5";
		assertEquals("Document match difference", expectedResult, result);
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
