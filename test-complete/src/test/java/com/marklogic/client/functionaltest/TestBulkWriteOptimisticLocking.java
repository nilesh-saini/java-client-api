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

import org.junit.BeforeClass;

import com.marklogic.client.DatabaseClient;

/*
 * This test is designed to to test optimistic locking simple bulk writes with different types of 
 * Managers and different content type like JSON,text,binary,XMl
 * 
 *  TextDocumentManager
 *  XMLDocumentManager
 *  BinaryDocumentManager
 *  JSONDocumentManager
 *  GenericDocumentManager
 */

public class TestBulkWriteOptimisticLocking extends BasicJavaClientREST {
	private static String dbName = "TestBulkWriteOptLockDB";
	private static String[] fNames = { "TestBulkWriteOptLockDB-1" };
	
	
	private DatabaseClient client;

	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
	}
/*
	@Before
	public void testSetup() throws KeyManagementException, NoSuchAlgorithmException, Exception {
		// create new connection for each test below
		client = getDatabaseClientWithDigest("rest-admin", "x");

		// create server configuration manager
		ServerConfigurationManager configMgr = client.newServerConfigManager();

		// read the server configuration from the database
		configMgr.readConfiguration();

		// require content versions for updates and deletes
		// use UpdatePolicy.VERSION_OPTIONAL to allow but not
		// require identifier use. Use UpdatePolicy.MERGE_METADATA
		// (the default) to deactive identifier use
//		configMgr.setUpdatePolicy(UpdatePolicy.VERSION_REQUIRED);

		// write the server configuration to the database
		configMgr.writeConfiguration();

		// release the client
		// client.release();

	}

	@After
	public void testCleanUp() throws KeyManagementException, NoSuchAlgorithmException, Exception {
		System.out.println("Running clear script");
		ServerConfigurationManager configMgr = client.newServerConfigManager();

		// read the server configuration from the database
		configMgr.readConfiguration();

		// require content versions for updates and deletes
		// use UpdatePolicy.VERSION_OPTIONAL to allow but not
		// require identifier use. Use UpdatePolicy.MERGE_METADATA
		// (the default) to deactive identifier use
		configMgr.setUpdatePolicy(UpdatePolicy.VERSION_OPTIONAL);

		// write the server configuration to the database
		configMgr.writeConfiguration();

		// release client
		client.release();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		System.out.println("In tear down");
		cleanupRESTServer(dbName, fNames);
	}
	
	public DocumentMetadataHandle setMetadata(){
		// create and initialize a handle on the metadata
		DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
		metadataHandle.getCollections().addAll("my-collection1","my-collection2");
		metadataHandle.getPermissions().add("app-user", Capability.UPDATE, Capability.READ);
		metadataHandle.getProperties().put("reviewed", true);
		metadataHandle.getProperties().put("myString", "foo");
		metadataHandle.getProperties().put("myInteger", 10);
		metadataHandle.getProperties().put("myDecimal", 34.56678);
		metadataHandle.getProperties().put("myCalendar", Calendar.getInstance().get(Calendar.YEAR));
		metadataHandle.setQuality(23);
		return	metadataHandle;
	}

	@Test
	public void testWriteSingleOptimisticLocking() throws KeyManagementException, NoSuchAlgorithmException, Exception {

		String bookFilename = "book.xml";
		String bookURI = "/book-xml-handle/";
		
		String docId = bookURI + bookFilename;
		GenericDocumentManager docMgr = client.newDocumentManager();		

		writeDocumentUsingInputStreamHandle(client, bookFilename, bookURI,
				"XML");

		// create a descriptor for versions of the document
		DocumentDescriptor desc = docMgr.newDescriptor(docId);

		// provide a handle for updating the content of the document
		FileHandle updateHandle = new FileHandle();

		// read the document, capturing the initial version with the descriptor
		docMgr.read(desc, updateHandle);

		long descriptorFirstVersion = desc.getVersion();
		System.out.println("created " + docId + " as version "
				+ descriptorFirstVersion);
		

		// modify the document
		Document document = expectedXMLDocument(bookFilename);
		document.getDocumentElement().setAttribute("modified", "true");

		// update the document, specifying the current version with the
		// descriptor
		// if the document changed after reading, write() throws KeyManagementException, NoSuchAlgorithmException, an exception
		docMgr.write(desc, updateHandle);

		// get the updated version without getting the content
		desc = docMgr.exists(docId);
		
		long descriptorSecondVersion = desc.getVersion();
		System.out.println("updated " + docId + " as version "
				+ descriptorSecondVersion);

		// delete the document, specifying the current version with the
		// descriptor
		// if the document changed after exists(), delete() throws KeyManagementException, NoSuchAlgorithmException, an exception
		docMgr.delete(desc);

		// release the client
		client.release();
		
		assertTrue("The document descriptors are equal. They need to be different.", (descriptorFirstVersion != descriptorSecondVersion));
	}
	
	@Test
	public void testWriteBulkOptimisticLocking() throws KeyManagementException, NoSuchAlgorithmException, Exception {

		String nameId[] = {"property1.xml","property2.xml","property3.xml"};
		String docId[] = {"/opt/lock/property1.xml","/opt/lock/property2.xml","/opt/lock/property3.xml"};
		
		TextDocumentManager docMgr = client.newTextDocumentManager();		

		// Write the documents.
		DocumentWriteSet writeset = docMgr.newWriteSet();
		// Set meta-data
		DocumentMetadataHandle mh = setMetadata();
		
		writeset.addDefault(mh); 
		writeset.add(docId[0], new StringHandle().with("This is so foo 1"));
		writeset.add(docId[1], new StringHandle().with("This is so foo 2"));
		writeset.add(docId[2], new StringHandle().with("This is so foo 3"));
		
		docMgr.write(writeset);

		DocumentPage page = docMgr.read(docId);
		
		DocumentDescriptor[] docDescriptor = new DocumentDescriptor[3];
		long[] descOrigLongArray = new long[10];
		long[] descNewLongArray = new long[10];		
		
		// provide a handle for updating the content of the document
		FileHandle updateHandle = null;
		
		    for(int i=0;i<3;i++) {
									
			// create a descriptor for versions of the document
			docDescriptor[i] = docMgr.newDescriptor(docId[i]);
			// provide a handle for updating the content of the document
			updateHandle = new FileHandle();
			// read the document, capturing the initial version with the descriptor
			docMgr.read(docDescriptor[i], updateHandle);
			descOrigLongArray[i] = docDescriptor[i].getVersion();
			
			// modify the document
			Document document = expectedXMLDocument(nameId[i]);
			document.getDocumentElement().setAttribute("modified", "true");
					
			
			// if the document changed after reading, write() throws an exception
			docMgr.write(docDescriptor[i], updateHandle);
			
			// get the updated version without getting the content
			docDescriptor[i] = docMgr.exists(docId[i]);
			descNewLongArray[i] = docDescriptor[i].getVersion();						
		}			
		//docMgr.delete(desc);
		assertFalse("The document descriptors are equal. They need to be different.",Arrays.equals(descOrigLongArray, descOrigLongArray));

		// release the client
		client.release();
	}
	*
	*
	* Use UpdatePolicy.VERSION_OPTIONAL and write test methods
	*
	*/
}