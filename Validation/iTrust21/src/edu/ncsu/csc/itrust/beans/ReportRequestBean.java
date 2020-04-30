package edu.ncsu.csc.itrust.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import edu.ncsu.csc.itrust.Messages;

/**
 * A bean for storing data about a report request.
 * 
 * A bean's purpose is to store data. Period. Little or no functionality is to be added to a bean 
 * (with the exception of minor formatting such as concatenating phone numbers together). 
 * A bean must only have Getters and Setters (Eclipse Hint: Use Source > Generate Getters and Setters.
 * to create these easily)
 */
public class ReportRequestBean {
	private long ID = 0L;
	private long requesterMID = 0L;
	private long patientMID = 0L;
	private Date requestedDate;
	private Date viewedDate;
	private String status = ""; //$NON-NLS-1$

	public final static String dateFormat = "MM/dd/yyyy HH:mm"; //$NON-NLS-1$

	public final static String Requested = Messages.getString("ReportRequestBean.requested"); //$NON-NLS-1$
	public final static String Viewed = Messages.getString("ReportRequestBean.viewed"); //$NON-NLS-1$

	public ReportRequestBean() {
	}

	public void setID(long iD) {
		ID = iD;
	}

	public long getID() {
		return ID;
	}

	public void setRequesterMID(long requesterMID) {
		this.requesterMID = requesterMID;
	}

	public long getRequesterMID() {
		return requesterMID;
	}

	public void setPatientMID(long patientMID) {
		this.patientMID = patientMID;
	}

	public long getPatientMID() {
		return patientMID;
	}

	public void setRequestedDate(Date requestedDate) {
		this.requestedDate = (requestedDate == null ? null : (Date) requestedDate.clone());
	}

	public void setRequestedDateString(String s) {
		try {
			setRequestedDate(new SimpleDateFormat(dateFormat).parse(s));
		} catch (ParseException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public Date getRequestedDate() {
		return (requestedDate == null ? null : (Date) requestedDate.clone());
	}

	public String getRequestedDateString() {
		if (requestedDate == null)
			return ""; //$NON-NLS-1$
		return new SimpleDateFormat(dateFormat).format(requestedDate);
	}

	public void setViewedDate(Date viewedDate) {
		this.viewedDate = (viewedDate == null ? null : (Date) viewedDate.clone());
	}

	public void setViewedDateString(String s) {
		try {
			setViewedDate(new SimpleDateFormat(dateFormat).parse(s));
		} catch (ParseException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public Date getViewedDate() {
		return (viewedDate == null ? null : (Date) viewedDate.clone());
	}

	public String getViewedDateString() {
		if (viewedDate == null)
			return ""; //$NON-NLS-1$
		return new SimpleDateFormat(dateFormat).format(viewedDate);
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
}
