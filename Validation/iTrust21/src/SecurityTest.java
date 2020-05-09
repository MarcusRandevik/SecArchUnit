import org.junit.runner.RunWith;

import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;

import edu.ncsu.csc.itrust.EmailUtil;
import edu.ncsu.csc.itrust.action.ActivityFeedAction;
import edu.ncsu.csc.itrust.action.AddApptAction;
import edu.ncsu.csc.itrust.action.AddDrugListAction;
import edu.ncsu.csc.itrust.action.AddERespAction;
import edu.ncsu.csc.itrust.action.AddHCPAction;
import edu.ncsu.csc.itrust.action.AddLTAction;
import edu.ncsu.csc.itrust.action.AddObstetricsAction;
import edu.ncsu.csc.itrust.action.AddOphthalmologyOVAction;
import edu.ncsu.csc.itrust.action.AddOphthalmologyScheduleOVAction;
import edu.ncsu.csc.itrust.action.AddOphthalmologySurgeryAction;
import edu.ncsu.csc.itrust.action.AddPHAAction;
import edu.ncsu.csc.itrust.action.AddPatientAction;
import edu.ncsu.csc.itrust.action.AddPatientFileAction;
import edu.ncsu.csc.itrust.action.AddUAPAction;
import edu.ncsu.csc.itrust.action.ChangePasswordAction;
import edu.ncsu.csc.itrust.action.ChangeSessionTimeoutAction;
import edu.ncsu.csc.itrust.action.DeclareHCPAction;
import edu.ncsu.csc.itrust.action.DesignateNutritionistAction;
import edu.ncsu.csc.itrust.action.EditDiagnosesAction;
import edu.ncsu.csc.itrust.action.EditHealthHistoryAction;
import edu.ncsu.csc.itrust.action.EditOPDiagnosesAction;
import edu.ncsu.csc.itrust.action.EditPHRAction;
import edu.ncsu.csc.itrust.action.EditPatientAction;
import edu.ncsu.csc.itrust.action.EditPersonnelAction;
import edu.ncsu.csc.itrust.action.EditPrescriptionsAction;
import edu.ncsu.csc.itrust.action.EditRepresentativesAction;
import edu.ncsu.csc.itrust.action.EmergencyReportAction;
import edu.ncsu.csc.itrust.action.EventLoggingAction;
import edu.ncsu.csc.itrust.action.GroupReportAction;
import edu.ncsu.csc.itrust.action.LoginFailureAction;
import edu.ncsu.csc.itrust.action.ManageHospitalAssignmentsAction;
import edu.ncsu.csc.itrust.action.MonitorAdverseEventAction;
import edu.ncsu.csc.itrust.action.MyDiagnosisAction;
import edu.ncsu.csc.itrust.action.PayBillAction;
import edu.ncsu.csc.itrust.action.PrescriptionReportAction;
import edu.ncsu.csc.itrust.action.ReportAdverseEventAction;
import edu.ncsu.csc.itrust.action.RequestRecordsReleaseAction;
import edu.ncsu.csc.itrust.action.ResetPasswordAction;
import edu.ncsu.csc.itrust.action.SendMessageAction;
import edu.ncsu.csc.itrust.action.SetSecurityQuestionAction;
import edu.ncsu.csc.itrust.action.UpdateCPTCodeListAction;
import edu.ncsu.csc.itrust.action.UpdateHospitalListAction;
import edu.ncsu.csc.itrust.action.UpdateICDCodeListAction;
import edu.ncsu.csc.itrust.action.UpdateLOINCListAction;
import edu.ncsu.csc.itrust.action.UpdateNDCodeListAction;
import edu.ncsu.csc.itrust.action.UpdateReasonCodeListAction;
import edu.ncsu.csc.itrust.action.VerifyClaimAction;
import edu.ncsu.csc.itrust.action.ViewHealthRecordsHistoryAction;
import edu.ncsu.csc.itrust.action.ViewObstetricsAction;
import edu.ncsu.csc.itrust.action.ViewOphthalmologyOVAction;
import edu.ncsu.csc.itrust.action.ViewOphthalmologySurgeryAction;
import edu.ncsu.csc.itrust.dao.mysql.FakeEmailDAO;
import edu.ncsu.csc.itrust.dao.mysql.MessageDAO;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "edu.ncsu.csc.itrust")
public class SecurityTest {
	@ArchTest
	ArchRule c1 = SecArchUnit.logSecurityEvents(JavaClass.Predicates.belongToAnyOf(
					ActivityFeedAction.class,
					AddApptAction.class,
					AddDrugListAction.class,
					AddERespAction.class,
					AddHCPAction.class,
					AddLTAction.class,
					AddObstetricsAction.class,
					AddOphthalmologyOVAction.class,
					AddOphthalmologyScheduleOVAction.class,
					AddOphthalmologySurgeryAction.class,
					AddPatientAction.class,
					AddPatientFileAction.class,
					AddPHAAction.class,
					AddUAPAction.class,
					ChangePasswordAction.class,
					ChangeSessionTimeoutAction.class,
					DeclareHCPAction.class,
					DesignateNutritionistAction.class,
					EditDiagnosesAction.class,
					EditHealthHistoryAction.class,
					EditOPDiagnosesAction.class,
					EditPatientAction.class,
					EditPersonnelAction.class,
					EditPHRAction.class,
					EditPrescriptionsAction.class,
					EditRepresentativesAction.class,
					EmergencyReportAction.class,
					GroupReportAction.class,
					LoginFailureAction.class,
					ManageHospitalAssignmentsAction.class,
					MonitorAdverseEventAction.class,
					MyDiagnosisAction.class,
					PayBillAction.class,
					PrescriptionReportAction.class,
					ReportAdverseEventAction.class,
					RequestRecordsReleaseAction.class,
					ResetPasswordAction.class,
					SetSecurityQuestionAction.class,
					UpdateCPTCodeListAction.class,
					UpdateHospitalListAction.class,
					UpdateICDCodeListAction.class,
					UpdateLOINCListAction.class,
					UpdateNDCodeListAction.class,
					UpdateReasonCodeListAction.class,
					VerifyClaimAction.class,
					ViewHealthRecordsHistoryAction.class,
					ViewObstetricsAction.class,
					ViewOphthalmologyOVAction.class,
					ViewOphthalmologySurgeryAction.class
			), EventLoggingAction.class);
	
	@ArchTest
	ArchRule c3a = SecArchUnit.sendOutboundMessagesFromCentralPoint(SendMessageAction.class, JavaClass.Predicates.belongToAnyOf(
			MessageDAO.class
	));
	
	@ArchTest
	ArchRule c3b = SecArchUnit.sendOutboundMessagesFromCentralPoint(EmailUtil.class, JavaClass.Predicates.belongToAnyOf(
			FakeEmailDAO.class
	));
	
	@ArchTest
	ArchRule c4 = SecArchUnit.validateUserInput();
	
	@ArchTest
	ArchRule c5 = SecArchUnit.limitResourceAllocation();
	
	@ArchTest
	ArchRule c6 = SecArchUnit.doNotBleedAssetsBetweenComponents();
	
	@ArchTest
	ArchRule c7 = SecArchUnit.doNotLogSecrets(JavaClass.Predicates.belongToAnyOf(EventLoggingAction.class));
}
