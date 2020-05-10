package edu.ncsu.csc.itrust.risk.factors;

import edu.ncsu.csc.itrust.beans.PatientBean;

/**
 * The risk factor associated with checking if a person is over a particular age
 * See {@link PatientRiskFactor} for details on what each method typically does.
 *  
 */
public class AgeFactor extends PatientRiskFactor {
	private PatientBean patient;
	private int age;

	public AgeFactor(PatientBean patient, int age) {
		this.age = age;
		this.patient = patient;
	}

	@Override
	public String getDescription() {
		return "Patient is over " + age;
	}

	@Override
	public boolean hasFactor() {
		return patient.getAge() > age;
	}
}
