package customrules.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.cfg.ControlFlowGraph;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Rule(
        key = "LogSecurityEvents",
        name = "All security events must be logged",
        description = "Public methods within a security service class must call a logger"
)

public class LogSecurityEventsRule extends IssuableSubscriptionVisitor {

    public static String LOGGER_CLASS_TEST = "java.math.BigDecimal";
    public static String LOGGER_CLASS = "java.math.BigDecimal";
    public static List<String> SECURITY_CLASSES = new ArrayList<String>(Arrays.asList("LogSecurityEventsCheck".toLowerCase(),
                                                                                    "CardReader".toLowerCase()));

    MethodMatchers loggerMethodsTest = MethodMatchers.create().ofTypes(LOGGER_CLASS_TEST).anyName().withAnyParameters().build();
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

        boolean containsCallToLogger = false;

        ControlFlowGraph cfg = method.cfg();

        for (ControlFlowGraph.Block block : cfg.blocks()) {
            System.out.println(block.elements().size());
            for (Tree blockTree : block.elements()) {
                if (blockTree.is(Tree.Kind.METHOD_INVOCATION)) {
                    MethodInvocationTree mit = (MethodInvocationTree) blockTree;
                    if (loggerMethodsTest.matches(mit) || loggerMethods.matches(mit)) {
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
