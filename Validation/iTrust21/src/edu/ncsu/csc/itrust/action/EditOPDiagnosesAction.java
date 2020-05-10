/**
 * 
 */
package edu.ncsu.csc.itrust.action;

import java.util.List;

import com.github.secarchunit.concepts.UserInput;

import edu.ncsu.csc.itrust.beans.OphthalmologyDiagnosisBean;
import edu.ncsu.csc.itrust.dao.DAOFactory;
import edu.ncsu.csc.itrust.dao.mysql.OphthalmologyDiagnosisDAO;
import edu.ncsu.csc.itrust.exception.DBException;
import edu.ncsu.csc.itrust.exception.ITrustException;

/**
 * Handle patients Diagnosis
 * Edit Diagnosis
 * Add Diagnosis
 * Remove Diagnosis
 *  
 *  
 */
public class EditOPDiagnosesAction {
	
	private OphthalmologyDiagnosisDAO diagnosesDAO;
	private String officeVisitID;
	
	public EditOPDiagnosesAction(DAOFactory factory, String ovIDString ) 
		throws ITrustException {
		diagnosesDAO = factory.getOPDiagnosisDAO();
		officeVisitID=ovIDString;
		
	}
	
	public List<OphthalmologyDiagnosisBean> getDiagnoses() throws DBException {
			return diagnosesDAO.getList(Integer.parseInt(officeVisitID));
	}
	
	@UserInput
	public void addDiagnosis(OphthalmologyDiagnosisBean bean) throws ITrustException {
		diagnosesDAO.add(bean);
	}
	
	@UserInput
	public void editDiagnosis(OphthalmologyDiagnosisBean bean) throws ITrustException {
		diagnosesDAO.edit(bean);
		
	}
	
	@UserInput
	public void deleteDiagnosis(OphthalmologyDiagnosisBean bean) throws ITrustException {
		diagnosesDAO.remove(bean.getOpDiagnosisID());
		
	}
	
	public List<OphthalmologyDiagnosisBean> getDiagnosisCodes() throws DBException {
		return diagnosesDAO.getOpICDCodes();
	}
	
}
