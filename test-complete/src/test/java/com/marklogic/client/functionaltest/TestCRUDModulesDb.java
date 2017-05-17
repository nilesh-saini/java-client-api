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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.admin.ExtensionLibrariesManager;
import com.marklogic.client.admin.ExtensionLibraryDescriptor;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
public class TestCRUDModulesDb extends BasicJavaClientREST {

	private static String dbName = "Modules";
	
@BeforeClass
	public static void setUp() throws Exception 
	{
	System.out.println("In setup");
	loadGradleProperties();
	assocRESTServer(getRestServerName(), dbName, getRestServerPort());
	}

@After
public  void testCleanUp() throws Exception
{
//	clearDB();
	System.out.println("Running clear script");
}
@Test
	public void testXQueryModuleCRUDDuplicateFile() throws KeyManagementException, NoSuchAlgorithmException, IOException
	{	
		DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
		
		// get a manager
		//ExtensionLibrariesManager libsMgr = Common.client.newServerConfigManager().newExtensionLibrariesManager();
		ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();

		String Path = "/ext/my/path/to/my/module.xqy";
		
		// write XQuery file to the modules database
		libsMgr.write(Path, new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/module.xqy")).withFormat(Format.TEXT));

		// read it back
		String xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		assertTrue("module read and read back", xqueryModuleAsString.startsWith("xquery version \"1.0-ml\";"));
		
		// write Duplicate XQuery file to the modules database with different content
		libsMgr.write(Path, new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/module.xqy")).withFormat(Format.TEXT));
		
		// read it back to check overwritten
		String xqueryModuleAsDuplicateString = libsMgr.read(Path, new StringHandle()).get();
		assertTrue("module read and read back", xqueryModuleAsDuplicateString.startsWith("xquery version \"1.0-ml\";"));
		
		// get the list of descriptors
		ExtensionLibraryDescriptor[] descriptors = libsMgr.list();
		assertEquals("number of modules installed", descriptors.length, 1);
		
		for (ExtensionLibraryDescriptor descriptor : descriptors) {
			assertEquals(descriptor.getPath(), Path);
		}

		// delete it
		libsMgr.delete(Path);
		
		try {
			// read deleted module
			xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		} catch (ResourceNotFoundException e) {
			// pass;
		}
	
	}


@Test	public void testXQueryModuleCRUDDifferentPath() throws KeyManagementException, NoSuchAlgorithmException, IOException {
		
		DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
		
		// get a manager
		ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();
		String firstPath = "/ext/my/path/to/my/module.xqy";
		String secondPath = "/ext/my/path/to/my/other/module.xqy" ;
		// write XQuery file to the modules database
		libsMgr.write(firstPath, new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/module.xqy")).withFormat(Format.TEXT));
		
		// write XQuery file to the modules database Different Path 
		libsMgr.write(secondPath, new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/module.xqy")).withFormat(Format.TEXT));
		
		// read 1st file back
		String xqueryModuleAsString = libsMgr.read(firstPath, new StringHandle()).get();
		assertTrue("module read and read back", xqueryModuleAsString.startsWith("xquery version \"1.0-ml\";"));
		
		// read 2nd file back
		String xqueryModuleAsString1 = libsMgr.read(secondPath, new StringHandle()).get();
		assertTrue("module read and read back", xqueryModuleAsString1.startsWith("xquery version \"1.0-ml\";"));
		
		// get the list of descriptors
		ExtensionLibraryDescriptor[] descriptors = libsMgr.list();
		assertEquals("number of modules installed", descriptors.length, 2);
		assertEquals(descriptors[0].getPath(), firstPath );
		assertEquals(descriptors[1].getPath(), secondPath);
		
		// delete it
		libsMgr.delete(firstPath );
		libsMgr.delete(secondPath);
		
		try {
			// read deleted module
			xqueryModuleAsString = libsMgr.read(firstPath, new StringHandle()).get();
			xqueryModuleAsString = libsMgr.read(secondPath, new StringHandle()).get();
		} catch (ResourceNotFoundException e) {
			// pass;
		}
		
	}


@Test
public void testXQueryModuleCRUDBinaryFile() throws KeyManagementException, NoSuchAlgorithmException, IOException {
		
		DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
		
		// get a manager
		ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();
		
		String Path = "/ext/my/path/to/my/module.xqy";
		
		// write XQuery file to the modules database
		libsMgr.write(Path, new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/binary.jpg")).withFormat(Format.BINARY));

		// read it back
		File f = new File("src/test/java/com/marklogic/client/functionaltest/data/binary.jpg");
		assertEquals(f.length(),libsMgr.read(Path, new StringHandle()).getByteLength());
		
		// get the list of descriptors
		ExtensionLibraryDescriptor[] descriptors = libsMgr.list();
		assertEquals("number of modules installed", descriptors.length, 1);
		
		for (ExtensionLibraryDescriptor descriptor : descriptors) {
			assertEquals(descriptor.getPath(), Path);
		}

		// delete it
		libsMgr.delete(Path);
		
		try {
			// read deleted module
		    libsMgr.read(Path, new StringHandle()).get();
		} catch (ResourceNotFoundException e) {
			// pass;
		}
		
	}


@Test	public void testXQueryModuleCRUDTextFile() throws KeyManagementException, NoSuchAlgorithmException, IOException {
		
		DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
		
		// get a manager
		ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();
		
		String Path = "/ext/my/path/to/my/module.xqy";
		
		// write XQuery file to the modules database
		libsMgr.write(Path, new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/readme.txt")).withFormat(Format.TEXT));

		// read it back
		String xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		assertTrue("module read and read back", xqueryModuleAsString.startsWith("Copyright 2017 MarkLogic Corporation"));
		
		// get the list of descriptors
		ExtensionLibraryDescriptor[] descriptors = libsMgr.list();
		assertEquals("number of modules installed", descriptors.length, 1);
		
		for (ExtensionLibraryDescriptor descriptor : descriptors) {
			assertEquals(descriptor.getPath(), Path);
		}

		// delete it
		libsMgr.delete(Path);

		try {
			// read deleted module
		 xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		} catch (ResourceNotFoundException e) {
			// pass;
		}
		
	}


@Test	public void testXQueryModuleCRUDXmlFile() throws KeyManagementException, NoSuchAlgorithmException, IOException {
		
		DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
		
		// get a manager
		ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();
		
		String Path = "/ext/my/path/to/my/module.xqy";
		FileHandle f = new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/all_well.xml")).withFormat(Format.XML);

		// write XQuery file to the modules database
		libsMgr.write(Path, f);

		// read it back
		assertEquals(209745, libsMgr.read(Path, new StringHandle()).getByteLength());
		
		// get the list of descriptors
		ExtensionLibraryDescriptor[] descriptors = libsMgr.list();
		assertEquals("number of modules installed", descriptors.length, 1);
		
		for (ExtensionLibraryDescriptor descriptor : descriptors) {
		assertEquals(descriptor.getPath(), Path);
		}

		// delete it
		libsMgr.delete(Path);
		
		try {
			// read deleted module
		    libsMgr.read(Path, new StringHandle()).get();
		} catch (ResourceNotFoundException e) {
			// pass;
		}
		
	}


@Test	public void testXQueryModuleReadModulesDb() throws KeyManagementException, NoSuchAlgorithmException, IOException {
		
		DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
		
		// get a manager
		ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();
		
		String Path = "/ext/my/path/to/my/module.xqy";
		FileHandle f = new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/module.xqy")).withFormat(Format.TEXT);

		// write XQuery file to the modules database
		libsMgr.write(Path, f);

		// read it back

		String xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		assertTrue("module read and read back", xqueryModuleAsString.contains("let $x := (1,2,3,4,5)"));
			
		// get the list of descriptors
		ExtensionLibraryDescriptor[] descriptors = libsMgr.list();	
		assertEquals("number of modules installed", descriptors.length, 1);
		
		for (ExtensionLibraryDescriptor descriptor : descriptors) {
			assertEquals(descriptor.getPath(), Path);
			System.out.println("Path returned by Descriptor "+ descriptor.getPath());
		}
		System.out.println("Path"+Path);
		// delete it
		libsMgr.delete(Path);
		try {
			// read deleted module
		xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		} catch (ResourceNotFoundException e) {
			System.out.println("Reading deleted file Failed");
			// pass;
		}
		try{
			libsMgr.delete(Path);
		}catch(Exception e){
			System.out.println("Attempt to Delete Non exsting file Failed");
			e.printStackTrace();
		}
		
	}

@Test
public void testXQueryModuleReadExtensionLibraryDescriptor () throws KeyManagementException, NoSuchAlgorithmException, IOException {
		System.out.println("testXQueryModuleReadExtensionLibraryDescriptor");
		
DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
		
		// get a manager
		ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();
		
		String Path = "/ext/my/path/to/my/module.xqy";
		FileHandle f = new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/module.xqy")).withFormat(Format.TEXT);

		// write XQuery file to the modules database
		libsMgr.write(Path, f);

		// read it back

		String xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		assertTrue("module read and read back", xqueryModuleAsString.contains("let $x := (1,2,3,4,5)"));
			
		// get the list of descriptors
		ExtensionLibraryDescriptor[] descriptors = libsMgr.list();	
		assertEquals("number of modules installed", descriptors.length, 1);
		
		for (ExtensionLibraryDescriptor descriptor : descriptors) {
			descriptor.setPath("/ext/my/path/to/my/new/module.xqy");
			String xqueryModuleAsStringNew = libsMgr.read(Path, new StringHandle()).get();		
			System.out.println("Path returned by Descriptor "+ descriptor.getPath()+"Document returned by Descriptor"+xqueryModuleAsStringNew);
			libsMgr.delete(descriptor.getPath());
		}
		
		// delete it
		libsMgr.delete(Path);

		try {
			// read deleted module
		xqueryModuleAsString = libsMgr.read(Path, new StringHandle()).get();
		} catch (ResourceNotFoundException e) {
			System.out.println("Reading deleted file Failed");
			// pass;
		}
		try{
			libsMgr.delete(Path);
		}catch(Exception e){
			System.out.println("Attempt to Delete Non exsting file Failed");
			e.printStackTrace();
		}
		
	}

@Test
public void testXQueryModuleCRUDXmlFileNegative() throws KeyManagementException, NoSuchAlgorithmException, IOException {
	
	DatabaseClient client = getDatabaseClient("rest-admin", "x", Authentication.DIGEST);
	
	// get a manager
	ExtensionLibrariesManager libsMgr = client.newServerConfigManager().newExtensionLibrariesManager();
	
	String Path = "/foo/my/path/to/my/module.xqy";
	FileHandle f = new FileHandle(new File("src/test/java/com/marklogic/client/functionaltest/data/all_well.xml")).withFormat(Format.XML);

	// write XQuery file to the modules database
	try{
		libsMgr.write(Path, f);
		Assert.assertTrue("Exception was not thrown",false);
	}catch(IllegalArgumentException e){
		//Issue 210 logged for meaningful error
		assertEquals("libraryPath (the modules database path under which you install an asset) must begin with /ext/" , e.getMessage());
	}
	// delete it
	try{
		libsMgr.delete(Path);
		Assert.assertTrue("Exception was not thrown",false);
	}catch(IllegalArgumentException e){
		assertEquals("libraryPath (the modules database path under which you install an asset) must begin with /ext/", e.getMessage());
	}
}

@AfterClass
	public static void tearDown() throws Exception
	{
		System.out.println("In tear down");
		try{
			deleteRESTServer(getRestServerName()); 
		}catch(Exception e){
			e.printStackTrace(); 
		}	
	}
}

