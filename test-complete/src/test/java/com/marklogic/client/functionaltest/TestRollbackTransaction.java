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
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.Transaction;
import com.marklogic.client.document.DocumentManager;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.Format;

public class TestRollbackTransaction extends BasicJavaClientREST {

	private static String dbName = "TestRollbackTransactionDB";
	private static String [] fNames = {"TestRollbackTransactionDB-1"};
	private static DatabaseClient clientA = null;
	private static DatabaseClient clientW = null;

	@BeforeClass	
	public static void setUp() throws Exception 
	{
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		setupAppServicesConstraint(dbName);
		clientA = getDatabaseClientWithDigest("rest-admin", "x");
		clientW = getDatabaseClientWithDigest("rest-writer", "x");
	}

	@Test	
	public void testRollbackDeleteDocument() throws KeyManagementException, NoSuchAlgorithmException, ParserConfigurationException, SAXException, IOException
	{	
		System.out.println("testRollbackDeleteDocument");

		String filename = "bbq1.xml";
		String uri = "/tx-rollback/";
		
		File file = new File("src/test/java/com/marklogic/client/functionaltest/data/" + filename);

		// create transaction 1
		Transaction transaction1 = clientA.openTransaction();

		// create a manager for document
		DocumentManager docMgr = clientA.newDocumentManager();

		// create an identifier for the document
		String docId = uri + filename;

		// create a handle on the content
		FileHandle handle = new FileHandle(file);
		handle.set(file);
		handle.setFormat(Format.XML);

		// write the document content
		docMgr.write(docId, handle, transaction1);

		// commit transaction
		transaction1.commit();		

		// create transaction 2
		Transaction transaction2 = clientA.openTransaction();

		// delete document
		docMgr.delete(docId, transaction2);

		transaction2.rollback();

		// read document
		FileHandle readHandle = new FileHandle();
		docMgr.read(docId, readHandle);
		File fileRead = readHandle.get();
		String readContent = convertFileToString(fileRead);

		// get xml document for expected result
		Document expectedDoc = expectedXMLDocument(filename);

		// convert actual string to xml doc
		Document readDoc = convertStringToXMLDocument(readContent);

		assertXMLEqual("Rollback on document delete failed", expectedDoc, readDoc);	
	}

	@Test	
	public void testRollbackUpdateDocument() throws KeyManagementException, NoSuchAlgorithmException, ParserConfigurationException, SAXException, IOException
	{	
		System.out.println("testRollbackUpdateDocument");

		String filename = "json-original.json";
		String updateFilename = "json-updated.json";
		String uri = "/tx-rollback/";

		File file = new File("src/test/java/com/marklogic/client/functionaltest/data/" + filename);

		// create transaction 1
		Transaction transaction1 = clientW.openTransaction();

		// create a manager for document
		DocumentManager docMgr = clientW.newDocumentManager();

		// create an identifier for the document
		String docId = uri + filename;

		// create a handle on the content
		FileHandle handle = new FileHandle(file);
		handle.set(file);
		handle.setFormat(Format.JSON);

		// write the document content
		docMgr.write(docId, handle, transaction1);

		// commit transaction
		transaction1.commit();		

		// create transaction 2
		Transaction transaction2 = clientW.openTransaction();

		// update document
		File updateFile = new File("src/test/java/com/marklogic/client/functionaltest/data/" + updateFilename);
		FileHandle updateHandle = new FileHandle(updateFile);
		updateHandle.set(updateFile);
		updateHandle.setFormat(Format.JSON);
		docMgr.write(docId, updateHandle, transaction2);

		transaction2.rollback();

		ObjectMapper mapper = new ObjectMapper();

		// read document
		FileHandle readHandle = new FileHandle();
		docMgr.read(docId, readHandle);
		File fileRead = readHandle.get();
		JsonNode readContent = mapper.readTree(fileRead);

		// get expected contents
		JsonNode expectedContent = expectedJSONDocument(filename);

		assertTrue("Rollback on document update failed", readContent.equals(expectedContent));		
	}

	@Test	
	public void testRollbackMetadata() throws KeyManagementException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, XpathException
	{
		System.out.println("Running testRollbackMetadata");

		String filename = "Simple_ScanTe.png";
		String uri = "/tx-rollback/";

		File file = new File("src/test/java/com/marklogic/client/functionaltest/data/" + filename);

		// create transaction 1
		Transaction transaction1 = clientW.openTransaction();

		// create doc manager
		DocumentManager docMgr = clientW.newDocumentManager();

		// create an identifier for the document
		String docId = uri + filename;

		// create a handle on the content
		FileHandle handle = new FileHandle(file);
		handle.set(file);
		handle.setFormat(Format.BINARY);

		// write the document content
		docMgr.write(docId, handle, transaction1);

		// get the original metadata
		Document docMetadata = getXMLMetadata("metadata-original.xml");

		// create handle to write metadata
		DOMHandle writeMetadataHandle = new DOMHandle();
		writeMetadataHandle.set(docMetadata);

		// write original metadata
		docMgr.writeMetadata(docId, writeMetadataHandle, transaction1);

		// commit transaction
		transaction1.commit();		

		// create transaction 2
		Transaction transaction2 = clientW.openTransaction();

		// get the update metadata
		Document docMetadataUpdate = getXMLMetadata("metadata-updated.xml");

		// create handle for metadata update
		DOMHandle writeMetadataHandleUpdate = new DOMHandle();
		writeMetadataHandleUpdate.set(docMetadataUpdate);

		// write updated metadata
		docMgr.writeMetadata(docId, writeMetadataHandleUpdate, transaction2);

		// rollback transaction2
		transaction2.rollback();

		// create handle to read updated metadata
		DOMHandle readMetadataHandleUpdate = new DOMHandle();

		// read updated metadata
		docMgr.readMetadata(docId, readMetadataHandleUpdate);
		Document docReadMetadataUpdate = readMetadataHandleUpdate.get();

		assertXpathEvaluatesTo("coll1", "string(//*[local-name()='collection'][1])", docReadMetadataUpdate);
		assertXpathEvaluatesTo("coll2", "string(//*[local-name()='collection'][2])", docReadMetadataUpdate);
		assertXpathEvaluatesTo("MarkLogic", "string(//*[local-name()='Author'])", docReadMetadataUpdate);
	}	

	@Test	
	public void testNegative() throws KeyManagementException, NoSuchAlgorithmException, ParserConfigurationException, SAXException, IOException
	{	
		System.out.println("testNegative");

		String filename = "bbq1.xml";
		String uri = "/tx-rollback/";

		File file = new File("src/test/java/com/marklogic/client/functionaltest/data/" + filename);

		// create transaction 1
		Transaction transaction1 = clientA.openTransaction();

		// create a manager for document
		DocumentManager docMgr = clientA.newDocumentManager();

		// create an identifier for the document
		String docId = uri + filename;

		// create a handle on the content
		FileHandle handle = new FileHandle(file);
		handle.set(file);
		handle.setFormat(Format.XML);

		// write the document content
		docMgr.write(docId, handle, transaction1);

		// commit transaction
		transaction1.commit();		

		// create transaction 2
		Transaction transaction2 = clientA.openTransaction();

		// delete document
		docMgr.delete(docId, transaction2);

		// commit transaction
		transaction2.commit();

		String expectedException = "com.marklogic.client.FailedRequestException: Local message: transaction rollback failed: Bad Request. Server Message: XDMP-NOTXN";
		String exception = "";

		// rollback transaction
		try {
			transaction2.rollback();
		} catch(Exception e) { exception = e.toString(); };

		assertTrue("Exception is not thrown", exception.contains(expectedException));
	}

	@AfterClass	
	public static void tearDown() throws Exception
	{
		System.out.println("In tear down");
		// release client
		clientA.release();
		clientW.release();
		cleanupRESTServer(dbName, fNames);
	}
}
