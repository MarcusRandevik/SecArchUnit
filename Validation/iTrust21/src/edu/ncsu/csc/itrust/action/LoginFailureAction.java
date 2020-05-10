package edu.ncsu.csc.itrust.action;

import java.sql.SQLException;
import edu.ncsu.csc.itrust.dao.DAOFactory;
import edu.ncsu.csc.itrust.dao.mysql.AuthDAO;
import edu.ncsu.csc.itrust.exception.DBException;

/**
 * Please note that this is not the best mitigation for Denial of Service attacks. The better way would be to
 * keep track of password failure attempts per user account, NOT with easily spoofable ip addresses. The
 * reason this feature is implemented with ip addresses is a limitation in Tomcat authentication (actually,
 * it's technically JSP's fault for not specifying a form of account lockout). <br />
 * <br />
 * All authentication in this application is done by the container (Tomcat), which doesn't support account
 * lockout. So our options would be (a) to implement our own authentication (yuck!), or (2) to extend the
 * JDBCRealm class in the Tomcat source code and add the logic. I've looked into this and it's actually pretty
 * easy. The ONLY reason it's not implemented here is that the code would be buried in a jar in your Tomcat
 * installation - not very educational for those who want to learn about authentication in webapps. Feel free
 * to change this; extending this would be perfectly acceptable.
 * 
 * 
 */
public class LoginFailureAction {
	private AuthDAO authDAO;
	private String ipAddr;
	private boolean validCaptcha;
	private boolean hasAttempts;

	/**
	 * Set up defaults
	 * @param factory The DAOFactory used to create the DAOs used in this action.
	 * @param ipAddr The IP address of the user making the login attempt.
	 */
	public LoginFailureAction(DAOFactory factory, String ipAddr) {
		this.authDAO = factory.getAuthDAO();
		this.ipAddr = ipAddr;
		validCaptcha = false;
		hasAttempts = false;
	}

	/**
	 * Calls authDAO to record the login failure in the database
	 * 
	 * @return How many login failure attempts or a DBException message
	 */
	public String recordLoginFailure() {
		try {
			authDAO.recordLoginFailure(ipAddr);
			int loginFailures = authDAO.getLoginFailures(ipAddr);
			hasAttempts = true;
			return "Login failed, attempt " + loginFailures;
		} catch (DBException e) {
			
			return e.getMessage();
		}
	}

	/**
	 * Checks to see if the current user can login (#failures<3)
	 * 
	 * @return true if the user is valid to login
	 */
	public boolean isValidForLogin() {
		try {
			return authDAO.getLoginFailures(ipAddr) < 3 || validCaptcha;
		} catch (DBException e) {
			return false;
		}
	}
	
	public boolean needsCaptcha() {
		try {
			return authDAO.getLoginFailures(ipAddr) >= 3;
		} catch (DBException e) {
			System.err.println("Denying access due to DBException");
			return false;
		}
	}
	
	/**
	 * resetFailure
	 * @throws DBException
	 * @throws SQLException
	 */
	public void resetFailures() throws DBException, SQLException{
		if(hasAttempts) {
			authDAO.resetLoginFailuresToZero(ipAddr);
			hasAttempts = false;
		}
	}
	
	/**
	 * setCaptcha
	 * @param val val
	 */
	public void setCaptcha(boolean val) {
		validCaptcha = val;
	}
	
	/**
	 * getFailureCount
	 * @return 0
	 */
	public int getFailureCount() {
		int loginFailures = 0;
		try {
			loginFailures = authDAO.getLoginFailures(ipAddr);
			if (loginFailures > 0) {
				hasAttempts = true;
			}
			return loginFailures;
		} catch (DBException e) {
			return 0;
		}
	}
}
