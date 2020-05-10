package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.Arrays;
import java.util.Collection;

public class LogSecurityEventsRule extends AbstractJavaRule {
    private static final String LOGGER = "edu.ncsu.csc.itrust.action.EventLoggingAction";
    private static final Collection<String> SECURITY_SERVICES = Arrays.asList(
            "edu.ncsu.csc.itrust.action.ActivityFeedAction",
            "edu.ncsu.csc.itrust.action.AddApptAction",
            "edu.ncsu.csc.itrust.action.AddDrugListAction",
            "edu.ncsu.csc.itrust.action.AddERespAction",
            "edu.ncsu.csc.itrust.action.AddHCPAction",
            "edu.ncsu.csc.itrust.action.AddLTAction",
            "edu.ncsu.csc.itrust.action.AddObstetricsAction",
            "edu.ncsu.csc.itrust.action.AddOphthalmologyOVAction",
            "edu.ncsu.csc.itrust.action.AddOphthalmologyScheduleOVAction",
            "edu.ncsu.csc.itrust.action.AddOphthalmologySurgeryAction",
            "edu.ncsu.csc.itrust.action.AddPatientAction",
            "edu.ncsu.csc.itrust.action.AddPatientFileAction",
            "edu.ncsu.csc.itrust.action.AddPHAAction",
            "edu.ncsu.csc.itrust.action.AddUAPAction",
            "edu.ncsu.csc.itrust.action.ChangePasswordAction",
            "edu.ncsu.csc.itrust.action.ChangeSessionTimeoutAction",
            "edu.ncsu.csc.itrust.action.DeclareHCPAction",
            "edu.ncsu.csc.itrust.action.DesignateNutritionistAction",
            "edu.ncsu.csc.itrust.action.EditDiagnosesAction",
            "edu.ncsu.csc.itrust.action.EditHealthHistoryAction",
            "edu.ncsu.csc.itrust.action.EditOPDiagnosesAction",
            "edu.ncsu.csc.itrust.action.EditPatientAction",
            "edu.ncsu.csc.itrust.action.EditPersonnelAction",
            "edu.ncsu.csc.itrust.action.EditPHRAction",
            "edu.ncsu.csc.itrust.action.EditPrescriptionsAction",
            "edu.ncsu.csc.itrust.action.EditRepresentativesAction",
            "edu.ncsu.csc.itrust.action.EmergencyReportAction",
            "edu.ncsu.csc.itrust.action.GroupReportAction",
            "edu.ncsu.csc.itrust.action.LoginFailureAction",
            "edu.ncsu.csc.itrust.action.ManageHospitalAssignmentsAction",
            "edu.ncsu.csc.itrust.action.MonitorAdverseEventAction",
            "edu.ncsu.csc.itrust.action.MyDiagnosisAction",
            "edu.ncsu.csc.itrust.action.PayBillAction",
            "edu.ncsu.csc.itrust.action.PrescriptionReportAction",
            "edu.ncsu.csc.itrust.action.ReportAdverseEventAction",
            "edu.ncsu.csc.itrust.action.RequestRecordsReleaseAction",
            "edu.ncsu.csc.itrust.action.ResetPasswordAction",
            "edu.ncsu.csc.itrust.action.SetSecurityQuestionAction",
            "edu.ncsu.csc.itrust.action.UpdateCPTCodeListAction",
            "edu.ncsu.csc.itrust.action.UpdateHospitalListAction",
            "edu.ncsu.csc.itrust.action.UpdateICDCodeListAction",
            "edu.ncsu.csc.itrust.action.UpdateLOINCListAction",
            "edu.ncsu.csc.itrust.action.UpdateNDCodeListAction",
            "edu.ncsu.csc.itrust.action.UpdateReasonCodeListAction",
            "edu.ncsu.csc.itrust.action.VerifyClaimAction",
            "edu.ncsu.csc.itrust.action.ViewHealthRecordsHistoryAction",
            "edu.ncsu.csc.itrust.action.ViewObstetricsAction",
            "edu.ncsu.csc.itrust.action.ViewOphthalmologyOVAction",
            "edu.ncsu.csc.itrust.action.ViewOphthalmologySurgeryAction"
    );

    public LogSecurityEventsRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration method, Object data) {
        ASTClassOrInterfaceDeclaration owningClass = method.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        if (owningClass == null) {
            return data;
        }

        if (!SECURITY_SERVICES.contains(owningClass.getBinaryName()) || !method.isPublic()) {
            // Not a security event; skip method
            return data;
        }

        boolean callsLogger = Util.getMethodCallsFrom(method).stream()
                .anyMatch(call -> LOGGER.equals(call.targetOwner));

        if (!callsLogger) {
            addViolation(data, method);
        }

        return data;
    }
}
