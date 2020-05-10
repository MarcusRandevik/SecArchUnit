package edu.ncsu.csc.itrust.action;

import java.util.ArrayList;
import java.util.List;

import com.github.secarchunit.concepts.UserInput;

import edu.ncsu.csc.itrust.action.base.EditOfficeVisitBaseAction;
import edu.ncsu.csc.itrust.beans.ProcedureBean;
import edu.ncsu.csc.itrust.dao.DAOFactory;
import edu.ncsu.csc.itrust.dao.mysql.CPTCodesDAO;
import edu.ncsu.csc.itrust.dao.mysql.ProceduresDAO;
import edu.ncsu.csc.itrust.exception.DBException;
import edu.ncsu.csc.itrust.exception.ITrustException;

/**
 * Edit patiens procedure action
 *  
 * @aurhor student
 */
public class EditProceduresAction extends EditOfficeVisitBaseAction {

	private ProceduresDAO proceduresDAO;
	private CPTCodesDAO cptDAO;
	
	/**
	 * @param factory
	 * @param hcpid
	 * @param pidString
	 * @param ovIDString
	 * @throws ITrustException
	 */
	public EditProceduresAction(DAOFactory factory, long hcpid, 
			   	String pidString, String ovIDString) 
		throws ITrustException {
		super(factory, hcpid, pidString, ovIDString);
		proceduresDAO = factory.getProceduresDAO();
		cptDAO = factory.getCPTCodesDAO();
	}
	
	public EditProceduresAction(DAOFactory factory, long hcpid, 
				String pidString) 
		throws ITrustException {
		super(factory, hcpid, pidString);
		proceduresDAO = factory.getProceduresDAO();
		cptDAO = factory.getCPTCodesDAO();
		
	}
	
	public List<ProcedureBean> getProcedures() throws DBException {
		if (isUnsaved()) {
			return new ArrayList<ProcedureBean>();
		} else {
			return proceduresDAO.getMedProceduresList(getOvID());
		}
	}
	
	@UserInput
	public void addProcedure(ProcedureBean bean) throws ITrustException {
		verifySaved();
		proceduresDAO.add(bean);
	}
	
	@UserInput
	public void editProcedure(ProcedureBean bean) throws ITrustException {
		verifySaved();
		proceduresDAO.edit(bean);
		
	}
	
	@UserInput
	public void deleteProcedure(ProcedureBean bean) throws ITrustException {
		verifySaved();
		proceduresDAO.remove(bean.getOvProcedureID());
		
	}
	
	public List<ProcedureBean> getProcedureCodes() throws DBException {
		return cptDAO.getProcedureCPTCodes();
	}

	
}
