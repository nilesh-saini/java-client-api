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
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.QueryOptionsListHandle;

public class TestQueryOptionsListHandle extends BasicJavaClientREST {

	private static String dbName = "QueryOptionsListHandleDB";
	private static String [] fNames = {"QueryOptionsListHandleDB-1"};
	private static DatabaseClient client = null;

	@BeforeClass	
	public static void setUp() throws Exception
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@Test	
	public void testNPE() throws KeyManagementException, NoSuchAlgorithmException, IOException,  SAXException, ParserConfigurationException
	{		
		System.out.println("Running testNPE");
		QueryOptionsListHandle handle = new QueryOptionsListHandle();

		Map map = handle.getValuesMap();		
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
