package customrules.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Collections;
import java.util.List;

@Rule(
        key = "CentralMessageSend",
        name = "Outbound messages must be sent from central point",
        description = "Outbound messages must be sent from central point",
        tags = "secarchunit",
        priority = Priority.MAJOR
)
public class CentralMessageRule extends IssuableSubscriptionVisitor {
    private static final String SENDING_POINT = "EmailUtil";
    private static final MethodMatchers SENDERS = MethodMatchers.create()
            .ofTypes("edu.ncsu.csc.itrust.dao.mysql.FakeEmailDAO")
            .anyName()
            .addParametersMatcher(parameters -> !parameters.isEmpty())
            .build();

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;

        if (SENDERS.matches(methodInvocation)) {
            // Get class where method invocation takes place
            Tree parent = methodInvocation.parent();
            while (!parent.is(Tree.Kind.CLASS)) {
                parent = parent.parent();
            }
            ClassTree classTree = (ClassTree) parent;

            // Compare class with sending point
            String sendingClassName = classTree.symbol().name();
            if (!SENDING_POINT.equals(sendingClassName)) {
                reportIssue(methodInvocation, "Messages must only be sent from the sending point");
            }
        }
    }
}
