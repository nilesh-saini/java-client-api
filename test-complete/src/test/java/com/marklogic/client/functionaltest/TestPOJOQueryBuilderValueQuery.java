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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.pojo.PojoPage;
import com.marklogic.client.pojo.PojoQueryBuilder;
import com.marklogic.client.pojo.PojoQueryDefinition;
import com.marklogic.client.pojo.PojoRepository;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;

public class TestPOJOQueryBuilderValueQuery extends BasicJavaClientREST {

	private static String dbName = "TestPOJOQueryBuilderValueSearchDB";
	private static String [] fNames = {"TestPOJOQueryBuilderValueSearchDB-1"};
	private static DatabaseClient client = null;

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

	public Artifact getArtifact(int counter) {

		Artifact cogs = new Artifact();
		cogs.setId(counter);
		if( counter % 5 == 0) {
			cogs.setName("Cogs special");
			if(counter % 2 ==0) {
				Company acme = new Company();
				acme.setName("Acme special, Inc.");
				acme.setWebsite("http://www.acme special.com");
				acme.setLatitude(41.998+counter);
				acme.setLongitude(-87.966+counter);
				cogs.setManufacturer(acme);

			} else {
				Company widgets = new Company();
				widgets.setName("Widgets counter Inc.");
				widgets.setWebsite("http://www.widgets counter.com");
				widgets.setLatitude(41.998+counter);
				widgets.setLongitude(-87.966+counter);
				cogs.setManufacturer(widgets);
			}
		} else {
			cogs.setName("Cogs "+counter);
			if(counter % 2 ==0) {
				Company acme = new Company();
				acme.setName("Acme "+counter+", Inc.");
				acme.setWebsite("http://www.acme"+counter+".com");
				acme.setLatitude(41.998+counter);
				acme.setLongitude(-87.966+counter);
				cogs.setManufacturer(acme);

			} else {
				Company widgets = new Company();
				widgets.setName("Widgets "+counter+", Inc.");
				widgets.setWebsite("http://www.widgets"+counter+".com");
				widgets.setLatitude(41.998+counter);
				widgets.setLongitude(-87.966+counter);
				cogs.setManufacturer(widgets);
			}
		}
		cogs.setInventory(1000+counter);
		return cogs;
	}

	public void validateArtifact(Artifact art) {
		assertNotNull("Artifact object should never be Null",art);
		assertNotNull("Id should never be Null",art.id);
		assertTrue("Inventry is always greater than 1000", art.getInventory()>1000);
	}

	public void loadSimplePojos(PojoRepository products) {
		for(int i=1;i<111;i++) {
			if(i%2==0) {
				products.write(this.getArtifact(i),"even","numbers");
			}
			else {
				products.write(this.getArtifact(i),"odd","numbers");
			}
		}
	}

	// Below scenario is to test the value query with numbers return correct results
	@Test
	public void testPOJOValueSearchWithNumbers() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;

		this.loadSimplePojos(products);
		String[] searchOptions ={"case-sensitive","wildcarded","min-occurs=2"};
		PojoQueryBuilder qb = products.getQueryBuilder();
		Number[] searchIds = {5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100,105,110,115,120,121,122,123,124,125,126};
		PojoQueryDefinition qd = qb.value("id",searchOptions,-1.0,searchIds);

		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd, 1,jh);
		assertEquals("total no of pages",5,p.getTotalPages());
		System.out.println(jh.get().toString());
		long pageNo=1,count=0;
		do {
			count = 0;
			p = products.search(qd,pageNo);
			while(p.hasNext()) {
				Artifact a =p.next();
				validateArtifact(a);
				assertTrue("Verifying document id is part of the search ids",a.getId()%5==0);
				count++;
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertEquals("page number after the loop",5,p.getPageNumber());
		assertEquals("total no of pages",5,p.getTotalPages());
		assertEquals("page length from search handle",5,jh.get().path("page-length").asInt());
		assertEquals("Total results from search handle",22,jh.get().path("total").asInt());
	}

	//Below scenario is to test value query with wild cards in strings
	@Test
	public void testPOJOValueSearchWithStrings() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		String[] searchOptions ={"case-sensitive","wildcarded","min-occurs=1"};
		PojoQueryBuilder qb = products.getQueryBuilder();
		String[] searchNames = {"Acme spe* *","Widgets spe* *"};
		PojoQueryDefinition qd =qb.filteredQuery(qb.value("name",searchOptions,100.0,searchNames));
		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd, 1,jh);

		System.out.println(jh.get().toString());
		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo);
			while(p.hasNext()) {
				Artifact a =p.next();
				validateArtifact(a);
				assertTrue("Verifying document id is part of the search ids",a.getId()%5==0);
				assertTrue("Verifying document name has Acme/Wigets special",a.getManufacturer().getName().contains("Acme special")||a.getManufacturer().getName().contains("Widgets special"));
				count++;
				System.out.println(a.getId());
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<=p.getTotalSize());
		assertEquals("page length from search handle",5,jh.get().path("results").size());
	}

	//Below scenario is verifying value query from PojoBuilder that matches to no document
	//Issue 127 is logged for the below scenario
	@Test
	public void testPOJOValueSearchWithNoResults() throws KeyManagementException, NoSuchAlgorithmException, Exception {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		setupServerRequestLogging(client,true);
		String[] searchOptions ={"case-sensitive","wildcarded","min-occurs=2"};
		PojoQueryBuilder qb = products.getQueryBuilder();
		String[] searchNames = {"acme*"};
		PojoQueryDefinition qd =qb.filteredQuery( qb.value("name",searchOptions,100.0,searchNames));
		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd, 1,jh);
		setupServerRequestLogging(client,false);
		System.out.println(jh.get().toString());
		assertEquals("total no of pages",0,p.getTotalPages());

		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo);
			while(p.hasNext()) {
				Artifact a =p.next();
				validateArtifact(a);
				assertTrue("Verifying document id is part of the search ids",a.getId()%5==0);
				count++;
				System.out.println(a.getId());
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<=p.getTotalSize());
		assertEquals("page number after the loop",1,p.getPageNumber());
		assertEquals("total no of pages",0,p.getTotalPages());
		assertEquals("page length from search handle",5,jh.get().path("page-length").asInt());
	}

	//Below scenario is to test word query without options
	@Test
	public void testPOJOWordQuerySearchWithoutOptions() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		PojoQueryBuilder qb = products.getQueryBuilder();
		String[] searchNames = {"counter","special"};
		PojoQueryDefinition qd = qb.word("name",searchNames);
		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd, 1,jh);
		System.out.println(jh.get().toString());
		assertEquals("total no of pages",5,p.getTotalPages());

		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo);
			while(p.hasNext()) {
				Artifact a =p.next();
				validateArtifact(a);
				assertTrue("Verifying document id is part of the search ids",a.getId()%5==0);
				assertTrue("Verifying document has word counter",a.getManufacturer().getName().contains("counter")||a.getManufacturer().getName().contains("special"));
				count++;
				System.out.println(a.getId());
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<=p.getTotalSize());
		assertEquals("page number after the loop",5,p.getPageNumber());
		assertEquals("total no of pages",5,p.getTotalPages());
		assertEquals("page length from search handle",5,jh.get().path("page-length").asInt());
		assertEquals("Total results from search handle",22,jh.get().path("total").asInt());
	}

	//Below scenario is verifying word query from PojoBuilder with options

	@Test
	public void testPOJOWordSearchWithOptions() throws KeyManagementException, NoSuchAlgorithmException, Exception {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		setupServerRequestLogging(client,true);
		String[] searchOptions ={"case-sensitive","wildcarded","min-occurs=1"};
		PojoQueryBuilder qb = products.getQueryBuilder();
		String[] searchNames = {"count*"};
		PojoQueryDefinition qd = qb.filteredQuery(qb.word("name",searchOptions,0.0,searchNames));
		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd, 1,jh);
		setupServerRequestLogging(client,false);
		System.out.println(jh.get().toString()+ p.getTotalPages());

		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo,jh);
			while(p.hasNext()){
				Artifact a =p.next();
				validateArtifact(a);
				assertTrue("Verifying document id is part of the search ids",a.getId()%5==0);
				assertTrue("Verifying document has word counter",a.getManufacturer().getName().contains("counter"));
				count++;
			}
			System.out.println(jh.get().toString());
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<=p.getTotalSize());
		System.out.println( p.getPageNumber());
		assertEquals("page length from search handle",5,jh.get().path("page-length").asInt());
	}

	/* Verify PojoRepository.count(query) does not scope count with the query  Git Issue #486*/
	@Test
	public void testPOJORepoCountWithQuery() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		String[] searchOptions ={"case-sensitive","wildcarded","min-occurs=1"};
		PojoQueryBuilder qb = products.getQueryBuilder();
		String[] searchNames = {"Acme spe* *","Widgets spe* *"};

		PojoQueryDefinition qd1 = qb.value("name",searchOptions,100.0,searchNames);
		long cnt1 = products.count(qd1);
		System.out.println("Count returned from PojoRepository is " + cnt1);
		assertEquals("Number of results returned incorrect in response", 110, cnt1);
		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd1, 1,jh);

		JsonNode nodePos = jh.get();
		// Return 1 node - constraint2.xml
		assertEquals("Number of results returned incorrect in response", "110", nodePos.path("total").asText());

		PojoQueryDefinition qd2 = qb.value("name",searchOptions,100.0,searchNames).withCriteria("Cogs 101");
		long cnt2 = products.count(qd2);
		System.out.println("Count returned from PojoRepository is " + cnt2);
		assertEquals("Number of results returned incorrect in response", 1, cnt2);

		String[] searchNames3 = {"Cogs 101", "Cogs 110", "Cogs 3"};
		PojoQueryDefinition qd3 = qb.value("name",searchOptions,100.0,searchNames3);
		long cnt3 = products.count(qd3);
		System.out.println("Count returned from PojoRepository is " + cnt3);
		assertEquals("Number of results returned incorrect in response", 2, cnt3);

		String[] searchNames4 = {"Cogs 3"};
		PojoQueryDefinition qd4 = qb.value("name",searchOptions,100.0,searchNames4);
		long cnt4 = products.count(qd4);
		System.out.println("Count returned from PojoRepository is " + cnt4);
		assertEquals("Number of results returned incorrect in response", 1, cnt4);

		String[] searchNames5 = {"12345"};
		PojoQueryDefinition qd5 = qb.value("name",searchOptions,100.0,searchNames5);
		long cnt5 = products.count(qd5);
		System.out.println("Count returned from PojoRepository is " + cnt5);
		assertEquals("Number of results returned incorrect in response", 0, cnt5);

		String[] searchNames6 = {"*"};
		PojoQueryDefinition qd6 = qb.value("name",searchOptions,100.0,searchNames6);
		long cnt6 = products.count(qd6);
		System.out.println("Count returned from PojoRepository is " + cnt6);
		assertEquals("Number of results returned incorrect in response", 110, cnt6);

		String[] searchNames7 = {" "};
		PojoQueryDefinition qd7 = qb.value("name",searchOptions,100.0,searchNames7);
		long cnt7 = products.count(qd7);
		System.out.println("Count returned from PojoRepository is " + cnt7);
		assertEquals("Number of results returned incorrect in response", 0, cnt7);

		String[] searchNames8 = {""};
		PojoQueryDefinition qd8 = qb.value("name",searchOptions,100.0,searchNames8);
		long cnt8 = products.count(qd8);
		System.out.println("Count returned from PojoRepository is " + cnt8);
		assertEquals("Number of results returned incorrect in response", 0, cnt8);

		String[] searchNames9 = {" ", "Cogs 3"};
		PojoQueryDefinition qd9 = qb.value("name",searchOptions,100.0,searchNames9);
		long cnt9 = products.count(qd9);
		System.out.println("Count returned from PojoRepository is " + cnt9);
		assertEquals("Number of results returned incorrect in response", 1, cnt9);
	}

	/* Below scenarios are to test setCriteria and withCriteris on query returned from a PojoQueryBuilder.
	 * Query with wild cards in strings. Same as testPOJOValueSearchWithStrings() method.
	 */
	@Test
	public void testPOJOValueQueryWithCriteria() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		String[] searchOptions ={"case-sensitive","wildcarded","min-occurs=1"};
		PojoQueryBuilder qb = products.getQueryBuilder();
		String[] searchNames = {"Acme spe* *","Widgets spe* *"};
		PojoQueryDefinition qd = qb.value("name",searchOptions,100.0,searchNames).withCriteria("Cogs 101");

		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd, 1,jh);

		JsonNode nodePos = jh.get();
		// Return 1 node - constraint2.xml
		assertEquals("Number of results returned incorrect in response", "1", nodePos.path("total").asText());
		assertEquals("Result returned incorrect in response", "com.marklogic.client.functionaltest.Artifact/101.json", nodePos.path("results").get(0).path("uri").asText());
	}

	@Test
	public void testPOJOValueQuerySetCriteria() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		String[] searchOptions ={"case-sensitive","wildcarded","min-occurs=1"};
		PojoQueryBuilder qb = products.getQueryBuilder();
		String[] searchNames = {"Adme spe* *","Wedgets spe* *"};
		QueryManager queryMgr = client.newQueryManager();
		StructuredQueryBuilder strdqb = queryMgr.newStructuredQueryBuilder();

		StructuredQueryDefinition strutdDef =  qb.word("name", "Widgets 101");
		strutdDef.setCriteria("Cogs 101");
		PojoQueryDefinition qd = qb.or(strutdDef, qb.value("name",searchOptions,100.0,searchNames));

		JacksonHandle jh = new JacksonHandle();
		products.setPageLength(5);
		p = products.search(qd, 1,jh);

		JsonNode nodePos = jh.get();
		// Return 1 node - constraint2.xml
		assertEquals("Number of results returned incorrect in response", "1", nodePos.path("total").asText());
		assertEquals("Result returned incorrect in response", "com.marklogic.client.functionaltest.Artifact/101.json", nodePos.path("results").get(0).path("uri").asText());
	}
}
