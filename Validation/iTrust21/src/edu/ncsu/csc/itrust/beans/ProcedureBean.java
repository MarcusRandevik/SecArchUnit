package edu.ncsu.csc.itrust.beans;

import java.sql.Date;

/**
 * A bean for storing data about a procedure.
 * 
 * A bean's purpose is to store data. Period. Little or no functionality is to be added to a bean 
 * (with the exception of minor formatting such as concatenating phone numbers together). 
 * A bean must only have Getters and Setters (Eclipse Hint: Use Source > Generate Getters and Setters
 * to create these easily)
 */
public class ProcedureBean {

	private long visitID;
	private long ovProcedureID = 0L;
	private String CPTCode;
	private String description;
	private String attribute;
	private Date date;
	private String hcpid;

	public ProcedureBean() {
	}

	public ProcedureBean(String code) {
		CPTCode = code;
	}

	public ProcedureBean(String code, String description) {
		CPTCode = code;
		this.description = description;
	}

	public ProcedureBean(String code, String description, String attribute) {
		CPTCode = code;
		this.description = description;
		this.attribute = attribute;
	}
	
	public ProcedureBean(String code, String description, String attribute, String hcpid) {
		CPTCode = code;
		this.description = description;
		this.attribute = attribute;
		this.hcpid = hcpid;
	}
	
	public long getID() {
		return ovProcedureID;
	}

	public void setID(long id) {
		this.ovProcedureID = id;
	}
	
	/**
	 * Gets the CPT Code for this procedure
	 * 
	 * @return The CPT Code for this procedure
	 */
	public String getCPTCode() {
		return CPTCode;
	}

	public void setCPTCode(String code) {
		CPTCode = code;
	}
	
	public long getVisitID() {
		return visitID;
	}
	public void setVisitID(long visitID) {
		this.visitID = visitID;
	}
	
	/**
	 * Gets the CPT Description for this procedure
	 * 
	 * @return The CPT Description for this procedure
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * setDescription
	 * @param description description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the HCP ID of the HCP administering the immunization
	 * @return String
	 */
	public String getHcpid() {
		return hcpid;
	}

	/**
	 * setHcpid
	 * @param hcpid hcpid
	 */
	public void setHcpid(String hcpid) {
		this.hcpid = hcpid;
	}
		
	/**
	 * Gets the CPT attribute, used to determine if this is an immunization
	 * @return String
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * Sets the CPT attribute, used to determine if this is an immunization
	 * @param attrib attrib
	 */
	public void setAttribute(String attrib) {
		attribute = attrib;
	}
	
	/**
	 * getOvProcedureID
	 * @return ovProcedureID
	 */
	public long getOvProcedureID() {
		return ovProcedureID;
	}

	/**
	 * setOvProcedureID
	 * @param ovProcedureID ovProcedureID
	 */
	public void setOvProcedureID(long ovProcedureID) {
		this.ovProcedureID = ovProcedureID;
	}
	
	/**
	 * getDate
	 * @return date
	 */
	public Date getDate() {
		return (Date) date.clone();
	}
	
	/**
	 * setDate
	 * @param d date
	 */
	public void setDate(Date d) {
		date = (Date) d.clone();
	}

}
