package com.rabo.service;

import java.util.List;

import com.rabo.controller.RaboException;
import com.rabo.dto.TransRecords;

public interface TransValidationService {
	
	List<TransRecords> findDuplicates(List<TransRecords> recordList) throws RaboException;
	
	List<TransRecords> findInvalidEntries(List<TransRecords> recordList) throws RaboException;
	
}
