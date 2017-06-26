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

import static org.junit.Assert.assertTrue;

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
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.XMLStreamReaderHandle;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
public class TestLinkResultDocuments extends BasicJavaClientREST {

	private static String dbName = "TestLinkResultDocuments";
	private static String [] fNames = {"TestLinkResultDocuments-1"};
	private static DatabaseClient client = null;

	@BeforeClass
	public static void setUp() throws Exception 
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);;
		setupAppServicesConstraint(dbName);
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@Test
	public void testMimeType() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running TestLinkResultDocuments");

		String[] filenames = {"constraint4.xml", "binary.jpg", "constraint4.json"};

		// write docs
		for(String filename : filenames) {
			if (filename.contains("xml")) {
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "XML");
			}
			else if (filename.contains("json")) {
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "JSON");
			}
			else if (filename.contains("jpg")) { 
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "Binary");
			}
		}

		// set query option
		setQueryOption(client,"LinkResultDocumentsOpt.xml");

		QueryManager queryMgr = client.newQueryManager();

		// create query def
		StringQueryDefinition querydef = queryMgr.newStringDefinition("LinkResultDocumentsOpt.xml");
		querydef.setCriteria("5");

		// create result handle
		SearchHandle resultsHandle = queryMgr.search(querydef, new SearchHandle()); 

		// get the result
		for (MatchDocumentSummary result : resultsHandle.getMatchResults()) {
			System.out.println(result.getMimeType()+ ": Mime Type");
			System.out.println(result.getPath()+ ": Path");
			System.out.println(result.getFormat()+ ": Format");
			System.out.println(result.getUri()+ ": Uri");
			assertTrue("Uri is Wrong", result.getPath().contains("/mime-type/constraint4.json")||result.getPath().contains("/mime-type/constraint4.xml"));
		} 

		XMLStreamReaderHandle shandle = queryMgr.search(querydef, new XMLStreamReaderHandle());
		String resultDoc2 = shandle.toString();
		System.out.println("Statics : \n"+resultDoc2);
	}

	@Test	
	public void testResultDecorator() throws KeyManagementException, NoSuchAlgorithmException, IOException {

		System.out.println("Running testResultDecorator");

		String[] filenames = {"constraint4.xml", "binary.jpg", "constraint3.json"};

		// write docs
		for(String filename : filenames) {
			if (filename.contains("xml")) {
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "XML");
			}
			else if (filename.contains("json")) {
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "JSON");
			}
			else if (filename.contains("jpg")) { 
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "Binary");
			}
		}
		try {
			String OS = System.getProperty("os.name");
			System.out.println("OS name : "+ OS);
			File source = null;
			File target = null;
			if (OS.contains("Windows 7")) {
				source = new File("C:/builds/winnt/HEAD/xcc/api_tests/src/test/java/com/marklogic/client/functionaltest/data/result-decorator-test.xqy");
				target = new File("C:/Program Files/MarkLogic/Modules/MarkLogic/appservices/search/result-decorator-test.xqy");
			}
			else if (OS.contains("Mac OS X")) {
				source = new File("/space/builder/builds/macosx-64/HEAD/xcc/api_tests/src/test/java/com/marklogic/client/functionaltest/data/result-decorator-test.xqy");
				target = new File("/Users/buildermac/Library/MarkLogic/Modules/MarkLogic/appservices/search/result-decorator-test.xqy");
			}
			else if (OS.contains("Linux")) {
				source = new File("/space/builder/builds/macosx/HEAD/xcc/api_tests/src/test/java/com/marklogic/client/functionaltest/data/result-decorator-test.xqy");
				target = new File("/opt/MarkLogic/Modules/MarkLogic/appservices/search/result-decorator-test.xqy");
			}

			System.out.println(source.exists());
			System.out.println(target.exists());
			if (target.exists()) {
				target.delete();
			}
			copyWithChannels(source, target, true);
			// set query option
			setQueryOption(client,"LinkResultDocumentsOptDecorator.xml");

			QueryManager queryMgr = client.newQueryManager();

			// create query def
			StringQueryDefinition querydef = queryMgr.newStringDefinition("LinkResultDocumentsOptDecorator.xml");
			querydef.setCriteria("5");

			// create result handle
			SearchHandle resultsHandle = queryMgr.search(querydef, new SearchHandle()); 

			// get the result
			for (MatchDocumentSummary result : resultsHandle.getMatchResults()) {
				System.out.println(result.getMimeType()+ ": Mime Type");
				System.out.println(result.getPath()+ ": Path");
				System.out.println(result.getFormat()+ ": Format");
				System.out.println(result.getUri()+ ": Uri");
			}
			XMLStreamReaderHandle shandle = queryMgr.search(querydef, new XMLStreamReaderHandle());
			String resultDoc2 = shandle.toString();
			System.out.println("Statics : \n"+resultDoc2);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test	
	public void testResultDecoratorNoMimeType() throws KeyManagementException, NoSuchAlgorithmException, IOException {

		System.out.println("Running testResultDecoratorNoMimeType");

		String[] filenames = {"constraint4.xml", "binary.jpg", "constraint4.json"};

		// write docs
		for(String filename : filenames) {
			if (filename.contains("xml")) {
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "XML");
			}
			else if (filename.contains("json")) {
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "JSON");
			}
			else if (filename.contains("jpg")) { 
				writeDocumentUsingInputStreamHandle(client, filename, "/mime-type/", "Binary");
			}
		}
		try {	
			String OS = System.getProperty("os.name");
			System.out.println("OS name : "+ OS);
			File source = null;
			File target = null;
			if (OS.contains("Windows 7")) {
				source = new File("C:/builds/winnt/HEAD/xcc/api_tests/src/test/java/com/marklogic/client/functionaltest/data/result-decorator-test.xqy");
				target = new File("C:/Program Files/MarkLogic/Modules/MarkLogic/appservices/search/result-decorator-test.xqy");
			}
			else if (OS.contains("Mac OS X")) {
				source = new File("/space/builder/builds/macosx-64/HEAD/xcc/api_tests/src/test/java/com/marklogic/client/functionaltest/data/result-decorator-test.xqy");
				target = new File("/Users/buildermac/Library/MarkLogic/Modules/MarkLogic/appservices/search/result-decorator-test.xqy");
			}
			else if (OS.contains("Linux")) {
				source = new File("/space/builder/builds/macosx/HEAD/xcc/api_tests/src/test/java/com/marklogic/client/functionaltest/data/result-decorator-test.xqy");
				target = new File("/opt/MarkLogic/Modules/MarkLogic/appservices/search/result-decorator-test.xqy");
			}

			System.out.println(source.exists());
			System.out.println(target.exists());
			if (target.exists()) {
				target.delete();
			}
			copyWithChannels(source, target, true);
			// set query option
			setQueryOption(client,"LinkResultDocumentsOptDecorator1.xml");

			QueryManager queryMgr = client.newQueryManager();

			// create query def
			StringQueryDefinition querydef = queryMgr.newStringDefinition("LinkResultDocumentsOptDecorator1.xml");
			querydef.setCriteria("5");

			// create result handle
			SearchHandle resultsHandle = queryMgr.search(querydef, new SearchHandle()); 

			// get the result
			for (MatchDocumentSummary result : resultsHandle.getMatchResults()) {
				System.out.println(result.getMimeType()+ ": Mime Type");
				System.out.println(result.getPath()+ ": Path");
				System.out.println(result.getFormat()+ ": Format");
				System.out.println(result.getUri()+ ": Uri");
			}
			XMLStreamReaderHandle shandle = queryMgr.search(querydef, new XMLStreamReaderHandle());
			String resultDoc2 = shandle.toString();
			System.out.println("Statics : \n"+resultDoc2);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void tearDown() throws Exception
	{
		// release client
		client.release();	
		cleanupRESTServer(dbName, fNames);
	}
}