package edu.ncsu.csc.itrust.action;

import java.util.Calendar;

import com.github.secarchunit.concepts.UserInput;

import edu.ncsu.csc.itrust.beans.SurveyBean;
import edu.ncsu.csc.itrust.dao.DAOFactory;
import edu.ncsu.csc.itrust.dao.mysql.OfficeVisitDAO;
import edu.ncsu.csc.itrust.dao.mysql.SurveyDAO;
import edu.ncsu.csc.itrust.exception.DBException;

/**
 * This class is used to add patient survey data to the database.  The office visit ID is linked with the survey ID.  Once the
 * survey is added, the transaction is logged
 *
 */
public class SurveyAction {
	private SurveyDAO surveyDAO;
	private OfficeVisitDAO ovDAO;
	
	/**
	 * Sets up defaults
	 * @param factory The DAOFactory used to create the DAOs used in this action.
	 */
	public SurveyAction(DAOFactory factory) {
		surveyDAO = factory.getSurveyDAO();
		ovDAO = factory.getOfficeVisitDAO();
	}


	/**
	 * Pass the OfficeVistBean along with SurveyBean
	 * @param surveyBean contains data to be added to database
	 * @param visitID The Office Visit ID corresponding to this Survey.
	 * @throws DBException
	 */
	@UserInput
	public void addSurvey(SurveyBean surveyBean, long visitID) throws DBException {
		
		surveyBean.setVisitID(visitID); //now set visit ID in the survey bean
		surveyDAO.addCompletedSurvey(surveyBean, Calendar.getInstance().getTime());
		
	}
	
	public long getPatientMID(long ovID) throws NumberFormatException, DBException
	{
		return ovDAO.getOfficeVisit(ovID).getPatientID();
	}
}
