package com.rabo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class TransRecords implements Serializable {

	private static final long serialVersionUID = -5654061464959748983L;
	private Integer referenceNo;
	private String acountNumber;
	private String description;
	private BigDecimal startBalance;
	private BigDecimal mutation;
	private BigDecimal endBalance;
	private String validStatus;

	/**
	 * @return the referenceNo
	 */
	public Integer getReferenceNo() {
		return referenceNo;
	}

	/**
	 * @param referenceNo
	 *            the referenceNo to set
	 */
	public void setReferenceNo(Integer referenceNo) {
		this.referenceNo = referenceNo;
	}

	/**
	 * @return the acountNumber
	 */
	public String getAcountNumber() {
		return acountNumber;
	}

	/**
	 * @param acountNumber
	 *            the acountNumber to set
	 */
	public void setAcountNumber(String acountNumber) {
		this.acountNumber = acountNumber;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the startBalance
	 */
	public BigDecimal getStartBalance() {
		return startBalance;
	}

	/**
	 * @param startBalance
	 *            the startBalance to set
	 */
	public void setStartBalance(BigDecimal startBalance) {
		this.startBalance = startBalance;
	}

	/**
	 * @return the mutation
	 */
	public BigDecimal getMutation() {
		return mutation;
	}

	/**
	 * @param mutation
	 *            the mutation to set
	 */
	public void setMutation(BigDecimal mutation) {
		this.mutation = mutation;
	}

	/**
	 * @return the endBalance
	 */
	public BigDecimal getEndBalance() {
		return endBalance;
	}

	/**
	 * @param endBalance
	 *            the endBalance to set
	 */
	public void setEndBalance(BigDecimal endBalance) {
		this.endBalance = endBalance;
	}

	/**
	 * @return the validStatus
	 */
	public String getValidStatus() {
		return validStatus;
	}

	/**
	 * @param validStatus
	 *            the validStatus to set
	 */
	public void setValidStatus(String validStatus) {
		this.validStatus = validStatus;
	}

}
