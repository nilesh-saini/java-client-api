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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.pojo.PojoPage;
import com.marklogic.client.pojo.PojoRepository;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;

public class TestPOJOWithStringQD extends BasicJavaClientREST {
	private static String dbName = "TestPOJOStringQDSearchDB";
	private static String [] fNames = {"TestPOJOStringQDSearchDB-1"};
	private static DatabaseClient client = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		addRangePathIndex(dbName, "string", "com.marklogic.client.functionaltest.Artifact/name", "http://marklogic.com/collation/", "ignore");
		addRangePathIndex(dbName, "string", "com.marklogic.client.functionaltest.Artifact/manufacturer/com.marklogic.client.functionaltest.Company/name", "http://marklogic.com/collation/", "ignore");
		client = getDatabaseClientWithDigest("rest-admin", "x");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("In tear down");
		// release client
		client.release();
		cleanupRESTServer(dbName, fNames);
	}

	public Artifact getArtifact(int counter){

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
	
	public void validateArtifact(Artifact art)
	{
		assertNotNull("Artifact object should never be Null",art);
		assertNotNull("Id should never be Null",art.id);
		assertTrue("Inventry is always greater than 1000", art.getInventory()>1000);
	}
	
	public void loadSimplePojos(PojoRepository products)
	{
		for(int i=1;i<111;i++) {
			if(i%2==0) {
				products.write(this.getArtifact(i),"even","numbers");
			}
			else {
				products.write(this.getArtifact(i),"odd","numbers");
			}
		}
	}
	
	@Test
	public void testPOJOSearchWithoutSearchHandle() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		QueryManager queryMgr = client.newQueryManager();
		StringQueryDefinition qd = queryMgr.newStringDefinition();
		qd.setCriteria("Widgets");

		products.setPageLength(11);
		p = products.search(qd, 1);
		assertEquals("total no of pages",5,p.getTotalPages());
		System.out.println(p.getTotalPages());
		long pageNo=1,count=0;
		do {
			count =0;

			p = products.search(qd,pageNo);

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				assertTrue("Artifact Id is odd", a.getId()%2!=0);
				assertTrue("Company name contains widgets",a.getManufacturer().getName().contains("Widgets"));
				count++;
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertEquals("page number after the loop",5,p.getPageNumber());
		assertEquals("total no of pages",5,p.getTotalPages());
	}
	
	@Test
	public void testPOJOSearchWithSearchHandle() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		QueryManager queryMgr = client.newQueryManager();
		StringQueryDefinition qd = queryMgr.newStringDefinition();
		qd.setCriteria("Acme");
		SearchHandle results = new SearchHandle();
		products.setPageLength(11);
		p = products.search(qd, 1,results);
		assertEquals("total no of pages",5,p.getTotalPages());
		System.out.println(p.getTotalPages());
		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo,results);

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				assertTrue("Artifact Id is even", a.getId()%2==0);
				assertTrue("Company name contains Acme",a.getManufacturer().getName().contains("Acme"));
				count++;
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
			MatchDocumentSummary[] mds =results.getMatchResults();
			assertEquals("Size of the results summary",11,mds.length);
			for(MatchDocumentSummary md:mds){
				assertTrue("every uri should contain the class name",md.getUri().contains("Artifact"));
			}
			String[] facetNames = results.getFacetNames();
			for(String fname:facetNames) {
				System.out.println(fname);
			}
			assertEquals("Total resulr from search handle ",55,results.getTotalResults());
			
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertEquals("Page start check",45,p.getStart());
		assertEquals("page number after the loop",5,p.getPageNumber());
		assertEquals("total no of pages",5,p.getTotalPages());
	}
	
	@Test
	public void testPOJOSearchWithJacksonHandle() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		QueryManager queryMgr = client.newQueryManager();
		StringQueryDefinition qd = queryMgr.newStringDefinition();
		qd.setCriteria("cogs");
		JacksonHandle results = new JacksonHandle();
		p = products.search(qd, 1,results);
		products.setPageLength(11);
		assertEquals("total no of pages",3,p.getTotalPages());

		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo,results);

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				count++;
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();

			assertEquals("Page start from search handls vs page methods",results.get().get("start").asLong(),p.getStart() );
			assertEquals("Format in the search handle","json",results.get().withArray("results").get(1).path("format").asText());
			assertTrue("Uri in search handle contains Artifact",results.get().withArray("results").get(1).path("uri").asText().contains("Artifact"));

		} while(!p.isLastPage() && pageNo<p.getTotalSize());

		assertEquals("Search text is","cogs",results.get().path("qtext").asText());
		assertEquals("Total from search handle",110,results.get().get("total").asInt());
		assertEquals("page number after the loop",10,p.getPageNumber());
		assertEquals("total no of pages",10,p.getTotalPages());
	}
	
	//Searching for Id as Number in JSON using string should not return any results 
	@Test
	public void testPOJOSearchWithStringHandle() throws KeyManagementException, NoSuchAlgorithmException, JsonProcessingException, IOException {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		QueryManager queryMgr = client.newQueryManager();
		StringQueryDefinition qd = queryMgr.newStringDefinition();
		qd.setCriteria("5");
		StringHandle results = new StringHandle();
		JacksonHandle jh = new JacksonHandle();
		p = products.search(qd, 1,jh);
		
		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo,results.withFormat(Format.JSON));

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				count++;
			}
			assertEquals("Page total results",0,p.getTotalSize());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertFalse("String handle is not empty",results.get().isEmpty());
		assertTrue("String handle contains results",results.get().contains("results"));
		assertFalse("String handle contains format",results.get().contains("\"format\":\"json\""));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actNode = mapper.readTree(results.get()).get("total");
		
		assertEquals("Total search results resulted are ",actNode.asInt(),0 );
	}
}
