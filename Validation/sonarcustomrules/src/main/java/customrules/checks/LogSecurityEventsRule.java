package customrules.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.cfg.ControlFlowGraph;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Rule(
        key = "LogSecurityEvents",
        name = "All security events must be logged",
        description = "Public methods within a security service class must call a logger",
        tags = "secarchunit",
        priority = Priority.MAJOR
)

public class LogSecurityEventsRule extends IssuableSubscriptionVisitor {

    public static String LOGGER_CLASS = "edu.ncsu.csc.itrust.action.EventLoggingAction";
    public static List<String> SECURITY_CLASSES = new ArrayList<String>(Arrays.asList(
            "ActivityFeedAction".toLowerCase(),
            "AddApptAction".toLowerCase(),
            "AddDrugListAction".toLowerCase(),
            "AddERespAction".toLowerCase(),
            "AddHCPAction".toLowerCase(),
            "AddLTAction".toLowerCase(),
            "AddObstetricsAction".toLowerCase(),
            "AddOphthalmologyOVAction".toLowerCase(),
            "AddOphthalmologyScheduleOVAction".toLowerCase(),
            "AddOphthalmologySurgeryAction".toLowerCase(),
            "AddPatientAction".toLowerCase(),
            "AddPatientFileAction".toLowerCase(),
            "AddPHAAction".toLowerCase(),
            "AddUAPAction".toLowerCase(),
            "ChangePasswordAction".toLowerCase(),
            "ChangeSessionTimeoutAction".toLowerCase(),
            "DeclareHCPAction".toLowerCase(),
            "DesignateNutritionistAction".toLowerCase(),
            "EditDiagnosesAction".toLowerCase(),
            "EditHealthHistoryAction".toLowerCase(),
            "EditOPDiagnosesAction".toLowerCase(),
            "EditPatientAction".toLowerCase(),
            "EditPersonnelAction".toLowerCase(),
            "EditPHRAction".toLowerCase(),
            "EditPrescriptionsAction".toLowerCase(),
            "EditRepresentativesAction".toLowerCase(),
            "EmergencyReportAction".toLowerCase(),
            "GroupReportAction".toLowerCase(),
            "LoginFailureAction".toLowerCase(),
            "ManageHospitalAssignmentsAction".toLowerCase(),
            "MonitorAdverseEventAction".toLowerCase(),
            "MyDiagnosisAction".toLowerCase(),
            "PayBillAction".toLowerCase(),
            "PrescriptionReportAction".toLowerCase(),
            "ReportAdverseEventAction".toLowerCase(),
            "RequestRecordsReleaseAction".toLowerCase(),
            "ResetPasswordAction".toLowerCase(),
            "SetSecurityQuestionAction".toLowerCase(),
            "UpdateCPTCodeListAction".toLowerCase(),
            "UpdateHospitalListAction".toLowerCase(),
            "UpdateICDCodeListAction".toLowerCase(),
            "UpdateLOINCListAction".toLowerCase(),
            "UpdateNDCodeListAction".toLowerCase(),
            "UpdateReasonCodeListAction".toLowerCase(),
            "VerifyClaimAction".toLowerCase(),
            "ViewHealthRecordsHistoryAction".toLowerCase(),
            "ViewObstetricsAction".toLowerCase(),
            "ViewOphthalmologyOVAction".toLowerCase(),
            "ViewOphthalmologySurgeryAction".toLowerCase()
    ));

    MethodMatchers loggerMethods = MethodMatchers.create().ofTypes(LOGGER_CLASS).anyName().withAnyParameters().build();

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodTree method = (MethodTree) tree;

        String methodEnclosingClass = method.symbol().enclosingClass().name().toLowerCase();
        if (!SECURITY_CLASSES.contains(methodEnclosingClass)) return;

        boolean publicMethod = false;
        for (ModifierKeywordTree keywordTree : method.modifiers().modifiers()) {
            if (keywordTree.modifier() == Modifier.PUBLIC) {
                publicMethod = true;
                break;
            }
        }

        if (!publicMethod) {
            return;
        }

        boolean containsCallToLogger = false;

        ControlFlowGraph cfg = method.cfg();

        for (ControlFlowGraph.Block block : cfg.blocks()) {
            System.out.println(block.elements().size());
            for (Tree blockTree : block.elements()) {
                if (blockTree.is(Tree.Kind.METHOD_INVOCATION)) {
                    MethodInvocationTree mit = (MethodInvocationTree) blockTree;
                    if (loggerMethods.matches(mit)) {
                        containsCallToLogger = true;
                    }
                }
            }
        }

        if (!containsCallToLogger) {
            reportIssue(method.simpleName(), "Secure classes must contain call to logger");
        }
    }
}
