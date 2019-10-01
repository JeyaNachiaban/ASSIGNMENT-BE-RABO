package com.rabo.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rabo.constants.RaboConstants;
import com.rabo.dto.TransRecords;
import com.rabo.service.TransValidationService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class is to process the RABO Bank Transaction reports
 *
 */
@RestController
@RequestMapping("/rabo/transreport")
public class TransValidationController {

	@Autowired
	TransValidationService transValidationService;
	
	@Value("${report.file.path}")
	private String reportFilePath;

	Logger log = LogManager.getLogger(TransValidationController.class);

	/*
	 * This method is to validate the list of transactions
	 */
	@GetMapping("/validate")
	@ResponseBody
	private ResponseEntity<String> validateTransReport() {
		List<TransRecords> recordList = null;
		List<TransRecords> duplicateEntries = null;
		List<TransRecords> inValidEntries = null;
		String failureRecords = null;
		try {
			recordList = processInputFile();
			duplicateEntries = transValidationService.findDuplicates(recordList);
			inValidEntries = transValidationService.findInvalidEntries(recordList);
			failureRecords = getOutputRecords(duplicateEntries, inValidEntries);
		} catch (RaboException e) {
			log.error("Exception in validateTransReport", e);
			throw new RaboException();
		}
		return ResponseEntity.ok().body(failureRecords);
	}

	/*
	 * This method is to process the input file and get the list of transactions
	 * based on file type
	 */
	private List<TransRecords> processInputFile() throws RaboException {
		String fileType = null;
		List<TransRecords> transRecordsList = null;
		try {
			fileType = getFileType(reportFilePath);
			if (RaboConstants.CSV_TYPE.equalsIgnoreCase(fileType)) {
				transRecordsList = processCsvFile(reportFilePath);
			} else if (RaboConstants.XML_TYPE.equalsIgnoreCase(fileType)) {
				transRecordsList = processXMLFile(reportFilePath);
			}
		} catch (RaboException e) {
			log.error("Exception in processInputFile", e);
			throw new RaboException();
		}
		return transRecordsList;
	}

	/*
	 * This method is to process the get the file type from the specified path
	 */
	private String getFileType(String filePath) {
		String extension = null;
		try {
			if (filePath != null) {
				extension = FilenameUtils.getExtension(filePath);
			}
		} catch (RaboException e) {
			log.error("Exception in getFileType", e);
						throw new RaboException();
		}
		return extension;
	}

	/*
	 * This method is to process the get the process the transactions from CSV
	 * File
	 */
	private List<TransRecords> processCsvFile(String filePath) throws RaboException {
		List<TransRecords> inputList = new ArrayList<TransRecords>();
		InputStream inputFS = null;
		BufferedReader br = null;
		try {
			File inputFile = new File(filePath);
			inputFS = new FileInputStream(inputFile);
			br = new BufferedReader(new InputStreamReader(inputFS));
			inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Exception in processCsvFile", e);
						throw new RaboException();
		} finally {
			try {
				inputFS.close();
				br.close();
			} catch (IOException e) {
				log.error("Exception in processCsvFile", e);
			}
		}
		return inputList;
	}

	/*
	 * This method is to process the get the transactions from CSV File
	 */
	private Function<String, TransRecords> mapToItem = (line) -> {
		String[] rec = line.split(",");
		TransRecords transRecord = new TransRecords();
		transRecord.setReferenceNo(Integer.valueOf(rec[0]));
		transRecord.setAcountNumber(rec[1]);
		transRecord.setDescription(rec[2]);
		if (null != rec[3] && !"".equals(rec[3])) {
			transRecord.setStartBalance(BigDecimal.valueOf(Double.valueOf(rec[3])));
		} else {
			transRecord.setStartBalance(BigDecimal.valueOf(0));
		}
		if (null != rec[4] && !"".equals(rec[4])) {
			transRecord.setMutation(BigDecimal.valueOf(Double.valueOf(rec[4])));
		} else {
			transRecord.setMutation(BigDecimal.valueOf(0));
		}
		if (null != rec[5] && !"".equals(rec[5])) {
			transRecord.setEndBalance(BigDecimal.valueOf(Double.valueOf(rec[5])));
		} else {
			transRecord.setEndBalance(BigDecimal.valueOf(0));
		}
		if (transRecord.getEndBalance()
				.equals(transRecord.getStartBalance().add(transRecord.getMutation()).stripTrailingZeros())) {
			transRecord.setValidStatus(RaboConstants.VALID);
		} else {
			transRecord.setValidStatus(RaboConstants.INVALID);
		}
		return transRecord;
	};

	/*
	 * This method is to process the XML file to get the list of transactions
	 */
	private List<TransRecords> processXMLFile(String filePath) throws RaboException {
		List<TransRecords> transRecordsList = new ArrayList<TransRecords>();
		TransRecords transRecord = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(filePath));
			document.getDocumentElement().normalize();
			NodeList nList = document.getElementsByTagName("record");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node node = nList.item(temp);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					transRecord = new TransRecords();
					transRecord.setReferenceNo(Integer.parseInt(eElement.getAttribute("reference")));
					transRecord
							.setAcountNumber(eElement.getElementsByTagName("accountNumber").item(0).getTextContent());
					transRecord.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());
					if (null != eElement.getElementsByTagName("startBalance").item(0).getTextContent()
							&& !"".equals(eElement.getElementsByTagName("startBalance").item(0).getTextContent())) {
						transRecord.setStartBalance(BigDecimal.valueOf(Double
								.valueOf(eElement.getElementsByTagName("startBalance").item(0).getTextContent())));
					} else {
						transRecord.setStartBalance(BigDecimal.valueOf(0));
					}
					if (null != eElement.getElementsByTagName("mutation").item(0).getTextContent()
							&& !"".equals(eElement.getElementsByTagName("mutation").item(0).getTextContent())) {
						transRecord.setMutation(BigDecimal.valueOf(
								Double.valueOf(eElement.getElementsByTagName("mutation").item(0).getTextContent())));
					} else {
						transRecord.setMutation(BigDecimal.valueOf(0));
					}
					if (null != eElement.getElementsByTagName("endBalance").item(0).getTextContent()
							&& !"".equals(eElement.getElementsByTagName("endBalance").item(0).getTextContent())) {
						transRecord.setEndBalance(BigDecimal.valueOf(
								Double.valueOf(eElement.getElementsByTagName("endBalance").item(0).getTextContent())));
					} else {
						transRecord.setEndBalance(BigDecimal.valueOf(0));
					}
					if (transRecord.getEndBalance().equals(
							transRecord.getStartBalance().add(transRecord.getMutation()).stripTrailingZeros())) {
						transRecord.setValidStatus(RaboConstants.VALID);
					} else {
						transRecord.setValidStatus(RaboConstants.INVALID);
					}
					transRecordsList.add(transRecord);
				}
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			log.error("Exception in processXMLFile", e);
			throw new RaboException();
		}
		return transRecordsList;
	}

	/*
	 * Since UI is not developed for this action,this method is used to process
	 * and generate the output.
	 */
	private String getOutputRecords(List<TransRecords> duplicateEntries, List<TransRecords> inValidEntries) {
		StringBuffer failureRecords = new StringBuffer();
		try {
			failureRecords.append("<Center><H3>RABO BANK</H3></Center>");
			failureRecords.append("<BR><BR>");
			failureRecords.append("<B>Duplicate Reference Number Entries:</B>");
			failureRecords.append("<BR><BR>");
			duplicateEntries.stream().forEach(x -> {
				failureRecords.append(x.getReferenceNo());
				failureRecords.append("\t-\t");
				failureRecords.append(x.getDescription());
				failureRecords.append("<BR>");
			});
			failureRecords.append("<BR>");
			failureRecords.append("<B>Wrong End Balance Entries :</B>");
			failureRecords.append("<BR><BR>");
			inValidEntries.stream().forEach(x -> {
				failureRecords.append(x.getReferenceNo());
				failureRecords.append("\t-\t");
				failureRecords.append(x.getDescription());
				failureRecords.append("<BR>");
			});
		} catch (RaboException e) {
			log.error("Exception in getOutputRecords", e);
			throw new RaboException();
		}
		return null != failureRecords ? failureRecords.toString() : null;
	}

}