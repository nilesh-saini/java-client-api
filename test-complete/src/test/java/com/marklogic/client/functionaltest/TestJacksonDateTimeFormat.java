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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.document.DocumentManager.Metadata;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.pojo.PojoRepository;
import com.marklogic.client.pojo.annotation.Id;

/*
 * Purpose : To test the date-time data type.
 * 
 */

public class TestJacksonDateTimeFormat extends BasicJavaClientREST {
	private static String dbName = "TestJacksonDateTimeFormatDB";
	private static String [] fNames = {"TestJacksonDateTimeFormatDB-1"};
	private static DatabaseClient client = null;
	private long artifactId = 1L;
	
	 /*
	  * This class is used to test writing and reading date-time
	  * Class member  expiryDate has been annotated with @Id.
	  */
	 public static class SpecialArtifactDateTime {	 
		 @Id
		 public Calendar expiryDate;

		 private String name;
		 private long id;
		 private Company manufacturer;
		 private int inventory;

		 public Calendar getExpiryDate() {
			 return expiryDate;
		 }
		 public SpecialArtifactDateTime setExpiryDate(Calendar expiryDate) {
			 this.expiryDate = expiryDate; return this;
		 }

		 public long getId() {
			 return id;
		 }
		 public SpecialArtifactDateTime setId(long id) {
			 this.id= id; return this;
		 }
		 public String getName() {
			 return name;
		 }
		 public SpecialArtifactDateTime setName(String name) {
			 this.name = name; return this;
		 }
		 public Company getManufacturer() {
			 return manufacturer;
		 }
		 public SpecialArtifactDateTime setManufacturer(Company manufacturer) {
			 this.manufacturer= manufacturer; return this;
		 }
		 public int getInventory() {
			 return inventory;
		 }
		 public SpecialArtifactDateTime setInventory(int inventory) {
			 this.inventory= inventory; return this;
		 }
	 }
	 
	 /*
	  * This class is used to test writing and reading date-time.
	  * ObjectMapper's support for date time is used for handle date time formats.
	  * Class member name has been annotated with @Id.
	  * 
	  */
	 public static class SpArtifactDateTimeObjMapper {	 
		 
		 @Id
		 public String name;
		 private Calendar expiryDate;
		 private long id;
		 private Company manufacturer;
		 private int inventory;

		 public Calendar getExpiryDate() {
			 return expiryDate;
		 }
		 public SpArtifactDateTimeObjMapper setExpiryDate(Calendar expiryDate) {
			 this.expiryDate = expiryDate; return this;
		 }

		 public long getId() {
			 return id;
		 }
		 public SpArtifactDateTimeObjMapper setId(long id) {
			 this.id= id; return this;
		 }
		 public String getName() {
			 return name;
		 }
		 public SpArtifactDateTimeObjMapper setName(String name) {
			 this.name = name; return this;
		 }
		 public Company getManufacturer() {
			 return manufacturer;
		 }
		 public SpArtifactDateTimeObjMapper setManufacturer(Company manufacturer) {
			 this.manufacturer= manufacturer; return this;
		 }
		 public int getInventory() {
			 return inventory;
		 }
		 public SpArtifactDateTimeObjMapper setInventory(int inventory) {
			 this.inventory= inventory; return this;
		 }
	 }
	 
	 public long getArtifactId() {
		 return artifactId;
	 }

	 public void setArtifactId(long artifactId) {
		 this.artifactId = artifactId;
	 }

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("In tear down");
		// release client
		client.release();
		cleanupRESTServer(dbName, fNames);
	}
	
	// Increment and return the current Id.
	public  long getOneLongId() {
		long lTemp = getArtifactId();
		setArtifactId(lTemp + 1);
		return lTemp == 1 ? 1: lTemp;
	}
	
	/*
	 * This method is used to test POJO Repository read / search with date time.
	 * Test should pass in the date-time field and POJORepository read should handle this request just fine.
	 */
	public SpecialArtifactDateTime setSpecialArtifactWithDateTime(long counter, String specialArtifactName, Calendar datetime) {
		Company acme = new Company();
		acme.setName(specialArtifactName);
		acme.setWebsite("http://www.acme.com");
		acme.setLatitude(41.998);
		acme.setLongitude(-87.966);
		SpecialArtifactDateTime cogs = new SpecialArtifactDateTime();
		cogs.setId(counter);
		cogs.setName(specialArtifactName);
		cogs.setManufacturer(acme);
		cogs.setInventory(1000);
		cogs.setExpiryDate(datetime);
		
		return cogs;
	}
	
	
	/*
	 * This method is used to test POJO Repository read / search with date time in the SpArtifactDateTimeObjMapper name.
	 * Test should pass in the Name field and POJORepository read should handle this request just fine.
	 * ObjectMapper should handle the date time formats.
	 */
	public SpArtifactDateTimeObjMapper setSpArtifactDateTimeObjMapper(long counter, String specialArtifactName, Calendar datetime) {
		Company acme = new Company();
		acme.setName(specialArtifactName);
		acme.setWebsite("http://www.acme.com");
		acme.setLatitude(41.998);
		acme.setLongitude(-87.966);
		SpArtifactDateTimeObjMapper cogs = new SpArtifactDateTimeObjMapper();
		cogs.setId(counter);
		cogs.setName(specialArtifactName);
		cogs.setManufacturer(acme);
		cogs.setInventory(1000);
		cogs.setExpiryDate(datetime);
		
		return cogs;
	}
		
	/*
	 * This method is used when there is a need to validate one read and search with 
	 * date-time in SpecialArtifactDateTime class
	 * 
	 */
	public void validateSpecialArtifactDateTime(SpecialArtifactDateTime artifact, String artifactName, long longId, Calendar datetime) {
		System.out.println("Argumnt : " + datetime.toString());
		System.out.println("Jackson POJO : " + artifact.getExpiryDate().toString());
		
		assertNotNull("Artifact object should never be Null", artifact);
		assertNotNull("Id should never be Null",artifact.id);
		assertEquals("Id of the object is ",longId, artifact.getId());
		assertEquals("Name of the object is ",artifactName, artifact.getName());
		assertEquals("Inventory of the object is ",1000, artifact.getInventory());
		assertEquals("Company name of the object is ",artifactName, artifact.getManufacturer().getName());
		assertEquals("Web site of the object is ","http://www.acme.com", artifact.getManufacturer().getWebsite());
		//Validate the calendar object's field, instead of object or string comparisions.
		assertEquals("Expiry date: MONTH ",datetime.get(Calendar.MONTH), artifact.getExpiryDate().get(Calendar.MONTH));
		assertEquals("Expiry date: DAY_OF_MONTH ",datetime.get(Calendar.DAY_OF_MONTH), artifact.getExpiryDate().get(Calendar.DAY_OF_MONTH));
		assertEquals("Expiry date: YEAR ",datetime.get(Calendar.YEAR), artifact.getExpiryDate().get(Calendar.YEAR));
		assertEquals("Expiry date: HOUR ",datetime.get(Calendar.HOUR), artifact.getExpiryDate().get(Calendar.HOUR));
		assertEquals("Expiry date: MINUTE ",datetime.get(Calendar.MINUTE), artifact.getExpiryDate().get(Calendar.MINUTE));
		assertEquals("Expiry date: SECOND ",datetime.get(Calendar.SECOND), artifact.getExpiryDate().get(Calendar.SECOND));
		assertEquals(-87.966, artifact.getManufacturer().getLongitude(), 0.00);
		assertEquals(41.998, artifact.getManufacturer().getLatitude(), 0.00);
	}
		
	/*
	 * This method is used when there is a need to validate one read and search with 
	 * date-time in SpArtifactDateTimeObjMapper class
	 * 
	 */
	public void validateSpArtifactDateTimeObjMapper(SpArtifactDateTimeObjMapper artifact, String artifactName, long longId, Calendar datetime) {
		System.out.println("Argumnt : " + datetime.toString());
		System.out.println("Jackson POJO : " + artifact.getExpiryDate().toString());
		
		assertNotNull("Artifact object should never be Null", artifact);
		assertNotNull("Id should never be Null",artifact.id);
		assertEquals("Id of the object is ",longId, artifact.getId());
		assertEquals("Name of the object is ",artifactName, artifact.getName());
		assertEquals("Inventory of the object is ",1000, artifact.getInventory());
		assertEquals("Company name of the object is ",artifactName, artifact.getManufacturer().getName());
		assertEquals("Web site of the object is ","http://www.acme.com", artifact.getManufacturer().getWebsite());
		//Validate the calendar object's field, instead of object or string comparisions.
		assertEquals("Expiry date: MONTH ",datetime.get(Calendar.MONTH), artifact.getExpiryDate().get(Calendar.MONTH));
		assertEquals("Expiry date: DAY_OF_MONTH ",datetime.get(Calendar.DAY_OF_MONTH), artifact.getExpiryDate().get(Calendar.DAY_OF_MONTH));
		assertEquals("Expiry date: YEAR ",datetime.get(Calendar.YEAR), artifact.getExpiryDate().get(Calendar.YEAR));
		assertEquals("Expiry date: HOUR ",datetime.get(Calendar.HOUR), artifact.getExpiryDate().get(Calendar.HOUR));
		assertEquals("Expiry date: MINUTE ",datetime.get(Calendar.MINUTE), artifact.getExpiryDate().get(Calendar.MINUTE));
		assertEquals("Expiry date: SECOND ",datetime.get(Calendar.SECOND), artifact.getExpiryDate().get(Calendar.SECOND));
		assertEquals(-87.966, artifact.getManufacturer().getLongitude(), 0.00);
		assertEquals(41.998, artifact.getManufacturer().getLatitude(), 0.00);
	}
	
	/*
	 * To verify that a JSON String with datetime string can be written and read back using Jackson.
	 */
	@Test
	public void testWriteJSONDocsWithDateTimeAsString() throws KeyManagementException, NoSuchAlgorithmException, Exception 
	{
		String docId[] = { "/datetime.json" };
		String jsonDate = new String("{\"expiryDate\": {\"java.util.GregorianCalendar\": \"2014-11-06,13:00\"}}");
		
		JSONDocumentManager docMgr = client.newJSONDocumentManager();
		docMgr.setMetadataCategories(Metadata.ALL);
		
		DocumentWriteSet writeset = docMgr.newWriteSet();
		ObjectMapper mapper = new ObjectMapper();

		JacksonHandle jacksonHandle1 = new JacksonHandle();

		JsonNode dateNode = mapper.readTree(jsonDate);
		jacksonHandle1.set(dateNode);
		jacksonHandle1.withFormat(Format.JSON);

		writeset.add(docId[0], jacksonHandle1);
		docMgr.write(writeset);

		JacksonHandle jacksonhandle = new JacksonHandle();
		docMgr.read(docId[0], jacksonhandle);
		JSONAssert.assertEquals(jsonDate, jacksonhandle.toString(), true);
	}
	
	/*
     * Purpose : This test is to validate read documents using read(Id)
     * POJO instance @Id field value with date-time.
     */
    
	@Test
	public void testPOJORepoReadWithDateTime() {
		PojoRepository<SpecialArtifactDateTime,Calendar> pojoReposProducts = client.newPojoRepository(SpecialArtifactDateTime.class, Calendar.class);		
		
		//Load object into database
		long longId = this.getOneLongId();
		// Create a calendar object with GMT Time Zone, since Jackson by default uses it.
		// You can set TZ in ObjectMapper also.
		Calendar calTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		String artifactName = new String("Acme");
		pojoReposProducts.write(this.setSpecialArtifactWithDateTime(longId, artifactName, calTime),"odd","numbers");		
			
		// Validate the artifact read back.
		SpecialArtifactDateTime artifact = pojoReposProducts.read(calTime);
		validateSpecialArtifactDateTime(artifact, artifactName, longId, calTime);
	}
	
	/*
	 * Delete using date-time
	 */
	@Test
	public void testPOJORepoDeleteDateTime() {
		PojoRepository<SpecialArtifactDateTime,Calendar> pojoReposProducts = client.newPojoRepository(SpecialArtifactDateTime.class, Calendar.class);		
		
		//Load object into database
		long longId = this.getOneLongId();
		// Create a calendar object with GMT Time Zone, since Jackson by default uses it.
		// You can set TZ in ObjectMapper also.
		Calendar calTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		String artifactName = new String("Acme");
		pojoReposProducts.write(this.setSpecialArtifactWithDateTime(longId, artifactName, calTime),"odd","numbers");		
			
		// Validate the artifact read back.
		SpecialArtifactDateTime artifact = pojoReposProducts.read(calTime);
		validateSpecialArtifactDateTime(artifact, artifactName, longId, calTime);
		
		pojoReposProducts.delete(calTime);
		// Introduce a wait for the document to be deleted.
		try {
			artifact = pojoReposProducts.read(calTime);
			assertFalse("Test Expecting exception",true);
		} catch (ResourceNotFoundException e) {
			assertTrue("expected exception",true);
		}catch (Exception e){
			assertFalse("Test got unexpected",true);
		}
		// Validate the artifact read back.
//		long count = pojoReposProducts.count();
//		
//		assertEquals("Artifact with calendar as Id found - Delete did not work",0, count);		
	}
	
	@Test
	public void testReadWriteDateTimeObjectWithMapper() {
		PojoRepository<SpArtifactDateTimeObjMapper,String> pojoReposProducts = client.newPojoRepository(SpArtifactDateTimeObjMapper.class, String.class);		
		
		//Load object into database
		long longId = this.getOneLongId();
		// Create a calendar object with GMT Time Zone, since Jackson by default uses it.
		// You can set TZ in ObjectMapper also.
		Calendar calTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		String artifactName = new String("Acme");
		pojoReposProducts.write(this.setSpArtifactDateTimeObjMapper(longId, artifactName, calTime),"odd","numbers");		
			
		// Validate the artifact read back.
		SpArtifactDateTimeObjMapper artifact = pojoReposProducts.read(artifactName);
		validateSpArtifactDateTimeObjMapper(artifact, artifactName, longId, calTime);
	}
	
	// Additional methods usingObjectmapper and File Streams with jacksonDataBind need to be here.artifactDel
}
