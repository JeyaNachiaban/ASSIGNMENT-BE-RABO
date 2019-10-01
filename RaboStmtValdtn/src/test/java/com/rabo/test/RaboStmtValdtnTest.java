package com.rabo.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.rabo.RaboTransValidationApp;
import com.rabo.constants.RaboConstants;
import com.rabo.dto.TransRecords;
import com.rabo.service.TransValidationService;

import org.junit.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RaboTransValidationApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RaboStmtValdtnTest {
	@Autowired
	private TestRestTemplate restTemplate;
	@LocalServerPort
	private int port;
	
	@Autowired
	TransValidationService transValidationService;
	
	
	private String getRootUrl() {
	return "http://localhost:" + port;
	}
	
	@Test
	public void testGetAllUsers() {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<String> response = restTemplate.exchange(getRootUrl() + "/rabo/transreport/validate",
		HttpMethod.GET, entity, String.class);
		Assert.assertNotNull(response.getBody());
	}
	
	@Test
	public void testDuplicateList() throws Exception {
		List<TransRecords> recordList = new ArrayList<TransRecords>();
		TransRecords tr = null;
		tr = new TransRecords(194261,"NL91RABO0315273637","Clothes from Jan Bakker",BigDecimal.valueOf(0),BigDecimal.valueOf(-41.83),BigDecimal.valueOf(-20.24),null);
		recordList.add(tr);
		tr = new TransRecords(112806,"NL27SNSB0917829871","Clothes for Willem Dekker",BigDecimal.valueOf(91.23),BigDecimal.valueOf(15.57),BigDecimal.valueOf(106.8),null);
		recordList.add(tr);
		tr = new TransRecords(194261,"NL91RABO0315273637","Clothes from Jan Bakker",BigDecimal.valueOf(0),BigDecimal.valueOf(-41.83),BigDecimal.valueOf(-20.24),null);
		recordList.add(tr);
		tr = new TransRecords(194261,"NL91RABO0315273637","Clothes from Jan Bakker",BigDecimal.valueOf(0),BigDecimal.valueOf(-41.83),BigDecimal.valueOf(-20.24),null);
		recordList.add(tr);
		List<TransRecords> duplicateList = transValidationService.findDuplicates(recordList);
		Assert.assertEquals(3, duplicateList.size());
	}
	
	@Test
	public void testInvalidEntries() throws Exception {
		List<TransRecords> recordList = new ArrayList<TransRecords>();
		TransRecords tr = null;
		tr = new TransRecords(194261,"NL91RABO0315273637","Clothes from Jan Bakker",BigDecimal.valueOf(0),BigDecimal.valueOf(-41.83),BigDecimal.valueOf(-20.24),RaboConstants.INVALID);
		recordList.add(tr);
		tr = new TransRecords(112806,"NL27SNSB0917829871","Clothes for Willem Dekker",BigDecimal.valueOf(91.23),BigDecimal.valueOf(15.57),BigDecimal.valueOf(106.8),RaboConstants.VALID);
		recordList.add(tr);
		tr = new TransRecords(112807,"NL27SSHB0917829871","Clothes for Willem ",BigDecimal.valueOf(94.25),BigDecimal.valueOf(41.6),BigDecimal.valueOf(135.85),RaboConstants.VALID);
		recordList.add(tr);
		List<TransRecords> invalidList = transValidationService.findInvalidEntries(recordList);
		Assert.assertEquals(1, invalidList.size());
	}

}
