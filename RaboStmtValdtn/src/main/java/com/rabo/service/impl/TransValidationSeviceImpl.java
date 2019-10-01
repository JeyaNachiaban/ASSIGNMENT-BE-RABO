package com.rabo.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.rabo.constants.RaboConstants;
import com.rabo.controller.RaboException;
import com.rabo.dto.TransRecords;
import com.rabo.service.TransValidationService;

@Service
public class TransValidationSeviceImpl implements TransValidationService {

	Logger log = LogManager.getLogger(TransValidationSeviceImpl.class);

	/*
	 * This method is to find duplicate transactions
	 * based on file type
	 */
	@Override
	public List<TransRecords> findDuplicates(List<TransRecords> recordList) throws RaboException {
		List<TransRecords> duplicateTransRecList = null;
		try {
			duplicateTransRecList = recordList.stream().collect(Collectors.groupingBy(TransRecords::getReferenceNo))
			.entrySet().stream().filter(e -> e.getValue().size() > 1).flatMap(e -> e.getValue().stream())
			.collect(Collectors.toList());
			
		} catch (Exception e) {
			log.error("Exception in findDuplicates", e);
			throw new RaboException();
		}
		return duplicateTransRecList;
	}
	
	/*
	 * This method is to  get the invalid list of transactions
	 * based on file type
	 */
	@Override
	public List<TransRecords> findInvalidEntries(List<TransRecords> recordList) throws RaboException  {
		List<TransRecords> invalidTransRecList = null;
		try {
			invalidTransRecList = recordList.stream().filter(e -> e.getValidStatus().equals(RaboConstants.INVALID))
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Exception in invalidTransRecList", e);
			throw new RaboException();
		}
		return invalidTransRecList;
	}
}
