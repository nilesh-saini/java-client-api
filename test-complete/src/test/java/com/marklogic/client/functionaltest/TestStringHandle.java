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

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.StringHandle;

public class TestStringHandle extends BasicJavaClientREST {

	private static String dbName = "StringDB";
	private static String [] fNames = {"StringDB-1"};
	private static DatabaseClient client = null;

	@BeforeClass	
	public static void setUp() throws Exception
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		client = getDatabaseClientWithDigest("rest-writer", "x");
	}

	@Test	
	public void testXmlCRUD() throws KeyManagementException, NoSuchAlgorithmException, IOException,  SAXException, ParserConfigurationException
	{	
		String filename = "xml-original-test.xml";
		String uri = "/write-xml-string/";

		System.out.println("Running testXmlCRUD");

		// write docs
		writeDocumentUsingStringHandle(client, filename, uri, "XML");

		// read docs
		StringHandle contentHandle = readDocumentUsingStringHandle(client, uri + filename, "XML");

		String readContent = contentHandle.get();

		// get xml document for expected result
		Document expectedDoc = expectedXMLDocument(filename);

		// convert actual string to xml doc
		Document readDoc = convertStringToXMLDocument(readContent);

		assertXMLEqual("Write XML difference", expectedDoc, readDoc);

		// update the doc
		// acquire the content for update
		String updateFilename = "xml-updated-test.xml";
		updateDocumentUsingStringHandle(client, updateFilename, uri + filename, "XML");

		// read the document
		StringHandle updateHandle = readDocumentUsingStringHandle(client, uri + filename, "XML");

		// get the contents
		String readContentUpdate = updateHandle.get();

		// get xml document for expected result
		Document expectedDocUpdate = expectedXMLDocument(updateFilename);

		// convert actual string to xml doc
		Document readDocUpdate = convertStringToXMLDocument(readContentUpdate);

		assertXMLEqual("Write XML difference", expectedDocUpdate, readDocUpdate);

		// delete the document
		deleteDocument(client, uri + filename, "XML");

		// read the deleted document
		String exception = "";
		try {
			readDocumentUsingFileHandle(client, uri + filename, "XML");
		} 
		catch (Exception e) { exception = e.toString(); }

		String expectedException = "com.marklogic.client.ResourceNotFoundException: Local message: Could not read non-existent document. Server Message: RESTAPI-NODOCUMENT: (err:FOER0000) Resource or document does not exist:  category: content message: /write-xml-string/xml-original-test.xml";
		assertEquals("Document is not deleted", expectedException, exception);
	}

	@Test	
	public void testTextCRUD() throws KeyManagementException, NoSuchAlgorithmException, IOException
	{	
		String filename = "text-original.txt";
		String uri = "/write-text-stringhandle/";

		System.out.println("Running testTextCRUD");

		// write docs
		writeDocumentUsingStringHandle(client, filename, uri, "Text");

		// read docs
		StringHandle contentHandle = readDocumentUsingStringHandle(client, uri + filename, "Text");

		String readContent = contentHandle.get();

		String expectedContent = "hello world, welcome to java API";

		assertEquals("Write Text difference", expectedContent.trim(), readContent.trim());

		// update the doc
		// acquire the content for update
		String updateFilename = "text-updated.txt";
		updateDocumentUsingStringHandle(client, updateFilename, uri + filename, "Text");

		// read the document
		StringHandle updateHandle = readDocumentUsingStringHandle(client, uri + filename, "Text");

		String readContentUpdate = updateHandle.get();

		String expectedContentUpdate = "hello world, welcome to java API after new updates";

		assertEquals("Write Text difference", expectedContentUpdate.trim(), readContentUpdate.toString().trim());

		// delete the document
		deleteDocument(client, uri + filename, "Text");

		String exception = "";
		try {
			readDocumentUsingFileHandle(client, uri + filename, "Text");
		} 
		catch (Exception e) { exception = e.toString(); }

		String expectedException = "com.marklogic.client.ResourceNotFoundException: Local message: Could not read non-existent document. Server Message: RESTAPI-NODOCUMENT: (err:FOER0000) Resource or document does not exist:  category: content message: /write-text-stringhandle/text-original.txt";
		assertEquals("Document is not deleted", expectedException, exception);
	}

	@Test	
	public void testJsonCRUD() throws KeyManagementException, NoSuchAlgorithmException, IOException
	{	
		String filename = "json-original.json";
		String uri = "/write-json-stringhandle/";

		System.out.println("Running testJsonCRUD");

		ObjectMapper mapper = new ObjectMapper();

		// write docs
		writeDocumentUsingStringHandle(client, filename, uri, "JSON");

		// read docs
		StringHandle contentHandle = readDocumentUsingStringHandle(client, uri + filename, "JSON");

		// get the contents
		JsonNode readContent = mapper.readValue(contentHandle.get(),JsonNode.class);

		// get expected contents
		JsonNode expectedContent = expectedJSONDocument(filename);

		assertTrue("Write JSON document difference", readContent.equals(expectedContent));		

		// update the doc
		// acquire the content for update
		String updateFilename = "json-updated.json";
		updateDocumentUsingFileHandle(client, updateFilename, uri + filename, "JSON");

		// read the document
		FileHandle updateHandle = readDocumentUsingFileHandle(client, uri + filename, "JSON");

		// get the contents
		File fileReadUpdate = updateHandle.get();

		JsonNode readContentUpdate = mapper.readTree(fileReadUpdate);

		// get expected contents
		JsonNode expectedContentUpdate = expectedJSONDocument(updateFilename);

		assertTrue("Write JSON document difference", readContentUpdate.equals(expectedContentUpdate));		

		// delete the document
		deleteDocument(client, uri + filename, "JSON");

		String exception = "";
		try {
			readDocumentUsingFileHandle(client, uri + filename, "JSON");
		} 
		catch (Exception e) { exception = e.toString(); }

		String expectedException = "com.marklogic.client.ResourceNotFoundException: Local message: Could not read non-existent document. Server Message: RESTAPI-NODOCUMENT: (err:FOER0000) Resource or document does not exist:  category: content message: /write-json-stringhandle/json-original.json";
		assertEquals("Document is not deleted", expectedException, exception);
	}

	@Test	
	public void testBug22356() throws KeyManagementException, NoSuchAlgorithmException, IOException,  SAXException, ParserConfigurationException
	{	
		System.out.println("Running testBug22356");
		// read docs
		StringHandle contentHandle = null;
		try {
			// get the contents
			contentHandle.get();
		} 
		catch (NullPointerException e) { 
			System.out.println("Null pointer Exception is expected noy an Empty Value");
			e.toString(); 
		}
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
