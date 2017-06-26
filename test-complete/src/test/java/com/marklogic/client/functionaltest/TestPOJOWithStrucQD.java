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
import static org.junit.Assert.assertNull;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.pojo.PojoPage;
import com.marklogic.client.pojo.PojoQueryDefinition;
import com.marklogic.client.pojo.PojoRepository;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryBuilder.Operator;
import com.marklogic.client.query.StructuredQueryDefinition;

public class TestPOJOWithStrucQD extends BasicJavaClientREST {
	private static String dbName = "TestPOJOStrucQDSearchDB";
	private static String [] fNames = {"TestPOJOStrucQDSearchDB-1"};
	private static DatabaseClient client = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		System.out.println("In setup");
		configureRESTServer(dbName, fNames);
		addRangePathIndex(dbName, "long", "com.marklogic.client.functionaltest.Artifact/inventory", "", "ignore");
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
		for(int i=1;i<111;i++){
			if(i%2==0){
				products.write(this.getArtifact(i),"even","numbers");
			}
			else {
				products.write(this.getArtifact(i),"odd","numbers");
			}
		}
	}
	
	public ArtifactIndexedOnString getArtifactIndexedOnString(int counter){

		ArtifactIndexedOnString cogs = new ArtifactIndexedOnString();
		cogs.setId(counter);
		if( counter % 5 == 0) {
			cogs.setName("Cogs special");
			if(counter % 2 ==0) {
				Company acme = new Company();
				acme.setName("Acme special, Inc.");
				cogs.setManufacturer(acme);

			} else {
				Company widgets = new Company();
				widgets.setName("Widgets counter Inc.");
				cogs.setManufacturer(widgets);
			}
		} else {
			cogs.setName("Cogs " + counter);
			if(counter % 2 ==0) {
				Company acme = new Company();
				acme.setName("Acme "+counter+", Inc.");
				cogs.setManufacturer(acme);

			} else {
				Company widgets = new Company();
				widgets.setName("Widgets "+counter+", Inc.");
				cogs.setManufacturer(widgets);
			}
		}
		cogs.setInventory(1000 + counter);
		return cogs;
	}
	
	@Test
	public void testPOJOSearchWithoutSearchHandle() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);

		StructuredQueryBuilder qb = new StructuredQueryBuilder();
		StructuredQueryDefinition q1 =qb.andNot(qb.term("cogs"),qb.term("special"));
		StructuredQueryDefinition qd = qb.and(q1,qb.collection("odd"));
		products.setPageLength(11);
		p = products.search(qd, 1);
		assertEquals("total no of pages",4,p.getTotalPages());
		
		long pageNo=1,count=0;
		do {
			count =0;

			p = products.search(qd,pageNo);

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				assertFalse("Verifying document with special is not there",a.getId()%5==0);
				assertTrue("Artifact Id is odd", a.getId()%2!=0);
				assertTrue("Company name contains widgets",a.getManufacturer().getName().contains("Widgets"));
				count++;			
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertEquals("page number after the loop",4,p.getPageNumber());
		assertEquals("total no of pages",4,p.getTotalPages());
	}
	
	@Test
	public void testPOJOSearchWithSearchHandle() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);

		StructuredQueryBuilder qb = new StructuredQueryBuilder();
		StructuredQueryDefinition q1 =qb.range(qb.pathIndex("com.marklogic.client.functionaltest.Artifact/inventory"), "xs:long",Operator.GT, 1010);
		PojoQueryDefinition qd = qb.and(q1,qb.range(qb.pathIndex("com.marklogic.client.functionaltest.Artifact/inventory"), "xs:long",Operator.LE, 1110),qb.collection("even"));
		SearchHandle results = new SearchHandle();
		products.setPageLength(10);
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
				assertTrue("Enventory lies between 1010 to 1110", a.getInventory()>1010 && a.getInventory()<=1110);
				assertTrue("Artifact Id is even", a.getId()%2==0);
				assertTrue("Company name contains Acme",a.getManufacturer().getName().contains("Acme"));
				count++;
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();
			MatchDocumentSummary[] mds =results.getMatchResults();
			assertEquals("Size of the results summary",10,mds.length);
			for(MatchDocumentSummary md:mds) {
				assertTrue("every uri should contain the class name",md.getUri().contains("Artifact"));
			}
			String[] facetNames = results.getFacetNames();
			for(String fname:facetNames) {
				System.out.println(fname);
			}
			assertEquals("search handle has facets ",0,results.getFacetNames().length);
			assertEquals("Total resulr from search handle ",50,results.getTotalResults());
			assertNull("Search Handle metric results ",results.getMetrics());
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertEquals("Page start check",41,p.getStart());
		assertEquals("page number after the loop",5,p.getPageNumber());
		assertEquals("total no of pages",5,p.getTotalPages());
	}
	
	@Test
	public void testPOJOSearchWithJacksonHandle() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		StructuredQueryBuilder qb = new StructuredQueryBuilder();
		StructuredQueryDefinition q1 =qb.containerQuery(qb.jsonProperty("name"),qb.term("special") );
		PojoQueryDefinition qd = qb.and(q1,qb.word(qb.jsonProperty("name"), "acme"));
		JacksonHandle results = new JacksonHandle();
		p = products.search(qd, 1,results);
		products.setPageLength(11);
		assertEquals("total no of pages",1,p.getTotalPages());
		System.out.println(p.getTotalPages()+results.get().toString());
		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo,results);

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				count++;
				assertTrue("Manufacture name starts with acme",a.getManufacturer().getName().contains("Acme"));
				assertTrue("Artifact name contains",a.getName().contains("special"));
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();

			assertEquals("Page start from search handls vs page methods",results.get().get("start").asLong(),p.getStart() );
			assertEquals("Format in the search handle","json",results.get().withArray("results").get(1).path("format").asText());
			assertTrue("Uri in search handle contains Artifact",results.get().withArray("results").get(1).path("uri").asText().contains("Artifact"));
			
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertFalse("search handle has metrics",results.get().has("metrics"));
		assertEquals("Total from search handle",11,results.get().get("total").asInt());
		assertEquals("page number after the loop",1,p.getPageNumber());
		assertEquals("total no of pages",1,p.getTotalPages());
	}
	
	//Searching for Id as Number in JSON using value query 
	@Test
	public void testPOJOSearchWithStringHandle() throws KeyManagementException, NoSuchAlgorithmException, JsonProcessingException, IOException {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);
		StructuredQueryBuilder qb = new StructuredQueryBuilder();
		PojoQueryDefinition qd =qb.value(qb.jsonProperty("id"), 5,10,15,20,25,30);

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
			assertEquals("Page total results",count,p.getTotalSize());
			pageNo=pageNo+p.getPageSize();
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertFalse("String handle is not empty",results.get().isEmpty());
		assertTrue("String handle contains results",results.get().contains("results"));
		assertTrue("String handle contains format",results.get().contains("\"format\":\"json\""));
		
		ObjectMapper mapper = new ObjectMapper();		
		JsonNode actNode = mapper.readTree(results.get()).get("total");
		assertEquals("Total search results resulted are ",6,actNode.asInt() );
	}
	
	/* Searching for string in JSON using value query.
	 * Purpose: To validate QueryBuilder's new value methods.
	 * We need to use PathIndexProperty on String for StructuredQueryBuilder.JSONProperty.Therefore  
	 * use ArtifactIndexedOnString.class here.
	 * Method used : value(StructuredQueryBuilder.TextIndex index, String... values)
	 */
	
		@Test
		public void testQueryBuilderValueWithString() throws KeyManagementException, NoSuchAlgorithmException, JsonProcessingException, IOException {
			
			PojoRepository<ArtifactIndexedOnString,String> products = client.newPojoRepository(ArtifactIndexedOnString.class, String.class);
			PojoPage<ArtifactIndexedOnString> p;
			StructuredQueryBuilder qb = new StructuredQueryBuilder();
			
			for(int i=1;i<111;i++) {
				if(i%2==0) {
					products.write(this.getArtifactIndexedOnString(i),"even","numbers");
				}
				else {
					products.write(this.getArtifactIndexedOnString(i),"odd","numbers");
				}
			}
			
			PojoQueryDefinition qd = qb.value(qb.jsonProperty("name"), "Cogs 11","Cogs 22", "Cogs 33","Cogs 44", "Cogs 55", "Cogs 66", "Cogs 77");

			StringHandle results = new StringHandle();
			JacksonHandle jh = new JacksonHandle();
			p = products.search(qd, 1,jh);

			long pageNo=1,count=0;
			
			do {
				count = 0;
				p = products.search(qd,pageNo,results.withFormat(Format.JSON));

				while(p.iterator().hasNext()) {
					ArtifactIndexedOnString a = p.iterator().next();					
					count++;
				}
				assertEquals("Page total results",count,p.getTotalSize());
				pageNo=pageNo+p.getPageSize();
				
			} while(!p.isLastPage() && pageNo<p.getTotalSize());
			assertFalse("String handle is not empty",results.get().isEmpty());
			assertTrue("String handle contains results",results.get().contains("results"));
			assertTrue("String handle contains format",results.get().contains("\"format\":\"json\""));
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actNode = mapper.readTree(results.get()).get("total");
			assertEquals("Total search results resulted are ",6,actNode.asInt() );
		}
		
	@Test 
	public void testPOJOSearchWithRawXMLStructQD() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);

		QueryManager queryMgr = client.newQueryManager();
		
		StringQueryDefinition qd = queryMgr.newStringDefinition();
		qd.setCriteria("special AND Acme");
		JacksonHandle results = new JacksonHandle();
		p = products.search(qd, 1,results);
		products.setPageLength(11);
		assertEquals("total no of pages",1,p.getTotalPages());
		System.out.println(p.getTotalPages()+results.get().toString());
		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo,results);

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				count++;
				assertTrue("Manufacture name starts with acme",a.getManufacturer().getName().contains("Acme"));
				assertTrue("Artifact name contains",a.getName().contains("special"));
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();

			assertEquals("Page start from search handls vs page methods",results.get().get("start").asLong(),p.getStart() );
			assertEquals("Format in the search handle","json",results.get().withArray("results").get(1).path("format").asText());
			assertTrue("Uri in search handle contains Artifact",results.get().withArray("results").get(1).path("uri").asText().contains("Artifact"));
			//			System.out.println(results.get().toString());
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertFalse("search handle has metrics",results.get().has("metrics"));
		assertEquals("Total from search handle",11,results.get().get("total").asInt());
		assertEquals("page number after the loop",1,p.getPageNumber());
		assertEquals("total no of pages",1,p.getTotalPages());
	}
	
	@Test
	public void testPOJOSearchWithRawJSONStructQD() {
		PojoRepository<Artifact,Long> products = client.newPojoRepository(Artifact.class, Long.class);
		PojoPage<Artifact> p;
		this.loadSimplePojos(products);

		QueryManager queryMgr = client.newQueryManager();
		JacksonHandle jh = new JacksonHandle();
		ObjectMapper mapper = new ObjectMapper();
		//	constructing JSON representation of Raw JSON Structured Query

		ObjectNode txtNode = mapper.createObjectNode();
		txtNode.putArray("text").add("special");
		ObjectNode termQNode = mapper.createObjectNode();
		termQNode.set("term-query", txtNode);
		ObjectNode queriesArrayNode = mapper.createObjectNode();
		queriesArrayNode.putArray("queries").add(termQNode);

		ObjectNode txtNode2 = mapper.createObjectNode();
		txtNode2.putArray("text").add("Widgets");
		ObjectNode termQNode2 = mapper.createObjectNode();
		termQNode2.set("term-query", txtNode2);
		queriesArrayNode.withArray("queries").add(termQNode2);

		ObjectNode orQueryNode = mapper.createObjectNode();
		orQueryNode.set("and-query",queriesArrayNode );

		ObjectNode queryArrayNode = mapper.createObjectNode();
		queryArrayNode.putArray("queries").add(orQueryNode);
		ObjectNode mainNode = mapper.createObjectNode();
		mainNode.set("query", queryArrayNode);
		jh.set(mainNode);
		
		StringQueryDefinition qd = queryMgr.newStringDefinition();
		qd.setCriteria("special AND Widgets");

		JacksonHandle results = new JacksonHandle();
		p = products.search(qd, 1,results);
		products.setPageLength(11);
		assertEquals("total no of pages",1,p.getTotalPages());
		System.out.println(p.getTotalPages()+results.get().toString());
		long pageNo=1,count=0;
		do {
			count =0;
			p = products.search(qd,pageNo,results);

			while(p.iterator().hasNext()) {
				Artifact a =p.iterator().next();
				validateArtifact(a);
				count++;
				assertTrue("Manufacture name starts with acme",a.getManufacturer().getName().contains("Widgets"));
				assertTrue("Artifact name contains",a.getName().contains("special"));
			}
			assertEquals("Page size",count,p.size());
			pageNo=pageNo+p.getPageSize();

			assertEquals("Page start from search handls vs page methods",results.get().get("start").asLong(),p.getStart() );
			assertEquals("Format in the search handle","json",results.get().withArray("results").get(1).path("format").asText());
			assertTrue("Uri in search handle contains Artifact",results.get().withArray("results").get(1).path("uri").asText().contains("Artifact"));
			System.out.println(results.get().toString());
		} while(!p.isLastPage() && pageNo<p.getTotalSize());
		assertFalse("search handle has metrics",results.get().has("metrics"));
		assertEquals("Total from search handle",11,results.get().get("total").asInt());
		assertEquals("page number after the loop",1,p.getPageNumber());
		assertEquals("total no of pages",1,p.getTotalPages());
	}
}
