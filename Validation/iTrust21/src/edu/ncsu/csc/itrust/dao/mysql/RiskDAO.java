package edu.ncsu.csc.itrust.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import edu.ncsu.csc.itrust.DBUtil;
import edu.ncsu.csc.itrust.beans.FamilyMemberBean;
import edu.ncsu.csc.itrust.dao.DAOFactory;
import edu.ncsu.csc.itrust.exception.DBException;

/**
 * Used for finding risk factors for a given patient.
 * 
 * DAO stands for Database Access Object. 
 * All DAOs are intended to be reflections of the database, that is,
 * one DAO per table in the database (most of the time). 
 * For more complex sets of queries, extra DAOs are added. 
 * DAOs can assume that all data has been validated and is correct.
 * 
 * DAOs should never have setters or any other parameter 
 * to the constructor than a factory. All DAOs should be
 * accessed by DAOFactory (@see {@link DAOFactory}) 
 * and every DAO should have a factory - for obtaining JDBC
 * connections and/or accessing other DAOs.
 */
@SuppressWarnings({})
public class RiskDAO {
	private transient final DAOFactory factory;

	/**
	 * The typical constructor.
	 * @param factory The {@link DAOFactory} associated with this DAO, 
	 * 		which is used for obtaining SQL connections, etc.
	 */
	public RiskDAO(final DAOFactory factory) {
		this.factory = factory;
	}

	/**
	 * This method is implemented using {@link FamilyDAO} for 2 reasons: 
	 * (a) definitions of family members might change, 
	 * so it's better to centralize that code and 
	 * (2) to make this code a little bit "nicer"
	 * even though it executes one query per family member.
	 * If this method gets slow, then you will need to
	 * refactor. Otherwise, let's just keep it simple...
	 * 
	 * @param patientID The MID of the patient in question.
	 * @param icdLower A double of the lower bound for the codes.
	 * @param icdUpper A double of the upper bound for the codes.
	 * @return A boolean indicating whether a family member had a match in this range.
	 * @throws DBException
	 */
	public boolean hasFamilyHistory(final long patientID, final double icdLower, final double icdUpper) throws DBException {
		boolean hasHistory = false;
		final List<FamilyMemberBean> familyMembers = getFamilyMembers(patientID);
		for (final FamilyMemberBean famMember : familyMembers) {
			if (hadPriorDiagnoses(famMember.getMid(), icdLower, icdUpper)) {
				hasHistory = true;
			}
		}
		return hasHistory;
	}

	private List<FamilyMemberBean> getFamilyMembers(final long patientID) throws DBException {
		final FamilyDAO famDAO = factory.getFamilyDAO();
		final List<FamilyMemberBean> familyMembers = famDAO.getParents(patientID);
		familyMembers.addAll(famDAO.getSiblings(patientID));
		return familyMembers;
	}

	/**
	 * Returns whether or not a patient had a childhood infection for the exact, given ICD codes.
	 * 
	 * @param patientID The MID of the patient in question.
	 * @param icdCodes A parameter list of the ICD codes to match.
	 * @return A boolean indicating whether this patient had all the listed ICD codes.
	 * @throws DBException
	 */
	public boolean hadChildhoodInfection(final long patientID, final double... icdCodes) throws DBException {
		// Note the datediff call - this is a MySQL function 
		// that takes the difference between two dates 
		// and returns that value in terms of days. 
		// 6570 days is 18 years (not counting leap years)
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = factory.getConnection();
			pstmt = conn.prepareStatement("SELECT * FROM ovdiagnosis ovd, officevisits ov, patients p "
					+ "WHERE ovd.visitID=ov.id AND ov.patientid=p.mid AND p.mid=? "
					+ "AND datediff(ov.visitdate,p.dateofbirth) < 6570 AND ovd.icdcode IN ("
					+ createPrepared(icdCodes.length) + ")");
			pstmt.setLong(1, patientID);
			setICDs(2, pstmt, icdCodes);
			final boolean returnVal = pstmt.executeQuery().next();
			pstmt.close();
			return returnVal; // if this query has ANY rows, then yes
		} catch (SQLException e) {
			
			throw new DBException(e);
		} finally {
			DBUtil.closeConnection(conn, pstmt);
		}
	}

	private String createPrepared(final int length) {
		String str;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buf.append("?,");
		}
		str = buf.toString();
		return str.substring(0, str.length() - 1);
	}

	private void setICDs(final int start, final PreparedStatement pstmt, final double[] icdCodes) throws SQLException {
		int idx = start;
		for (final double icdCode : icdCodes) {
			pstmt.setDouble(idx++, icdCode);
		}
	}

	/**
	 * Returns if the patient has ever smoked in their life
	 * 
	 * @param patientID The MID of the patient in question.
	 * @return A boolean indicating whether the patient smoked.
	 * @throws DBException
	 */
	public boolean hasSmoked(final long patientID) throws DBException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = factory.getConnection();
			pstmt = conn
					.prepareStatement("SELECT * FROM personalhealthinformation WHERE PatientID=? AND Smoker=1");
			pstmt.setLong(1, patientID);
			final boolean returnVal = pstmt.executeQuery().next();
			pstmt.close();
			return returnVal; // if this query has ANY rows, then yes
		} catch (SQLException e) {
			
			throw new DBException(e);
		} finally {
			DBUtil.closeConnection(conn, pstmt);
		}
	}

	/**
	 * Returns if a patient has ever been diagnosed 
	 * with the given ICD code, in the range [lower,upper)
	 * 
	 * @param patientID The MID of the patient in question.
	 * @param lowerICDCode A double of the lower ICD code.
	 * @param upperICDCode A double of the upper ICD code.
	 * @return A boolean indicating whether there was a match in the given range.
	 * @throws DBException
	 */
	public boolean hadPriorDiagnoses(final long patientID, final double lowerICDCode, final double upperICDCode)
			throws DBException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = factory.getConnection();
			pstmt = conn.prepareStatement("SELECT * FROM ovdiagnosis ovd, officevisits ov, patients p "
					+ "WHERE ovd.visitID=ov.id AND ov.patientid=p.mid AND p.mid=? "
					+ "AND ovd.icdcode>=? AND ovd.icdcode<?");
			pstmt.setLong(1, patientID);
			pstmt.setDouble(2, lowerICDCode);
			pstmt.setDouble(3, upperICDCode);
			final boolean returnVal = pstmt.executeQuery().next();
			pstmt.close();
			return returnVal; // if this query has ANY rows, then yes
		} catch (SQLException e) {
			
			throw new DBException(e);
		} finally {
			DBUtil.closeConnection(conn, pstmt);
		}
	}
}
