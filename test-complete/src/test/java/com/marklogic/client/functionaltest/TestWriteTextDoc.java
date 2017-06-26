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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;
import com.marklogic.client.document.TextDocumentManager;
import com.marklogic.client.io.StringHandle;
public class TestWriteTextDoc extends BasicJavaClientREST
{
	private static String hostname = null;

	@BeforeClass 
	public static void setUp() throws Exception 
	{
		System.out.println("In setup");
		loadGradleProperties();
		setupJavaRESTServerWithDB( "REST-Java-Client-API-Server-withDB", 8015);
		hostname = getRestServerHostName();
	}

	@Test  
	public void testWriteTextDoc()  
	{
		DatabaseClient client = DatabaseClientFactory.newClient(hostname, 8015, new DigestAuthContext("admin", "admin"));

		String docId = "/foo/test/myFoo.txt";
		TextDocumentManager docMgr = client.newTextDocumentManager();
		docMgr.write(docId, new StringHandle().with("This is so foo"));
		assertEquals("Text document write difference", "This is so foo", docMgr.read(docId, new StringHandle()).get());
		// release the client
		client.release();
	}
	
	@AfterClass	
	public static void tearDown() throws Exception
	{
		System.out.println("In tear down");
		deleteRESTServerWithDB("REST-Java-Client-API-Server-withDB");
	}
}
