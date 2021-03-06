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
        key = "AuthZSingleCompnentEnforcer",
        name = "An authorization enforcer should only be called by the authZ point",
        description = "An authorization enforcer should only be called by the authZ point",
        tags = "secarchunit",
        priority = Priority.MAJOR
)
public class AuthZSingleComponentEnforcerRule extends IssuableSubscriptionVisitor {

    private static final String AUTH_POINT_CLASS = "Transaction".toLowerCase();
    private static final String AUTH_ENFORCER_CLASS = "atm.transaction.Transaction";

    private final MethodMatchers enforcerMethods = MethodMatchers.create().ofTypes(AUTH_ENFORCER_CLASS).anyName().withAnyParameters().build();

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree mit = (MethodInvocationTree) tree;

        if (enforcerMethods.matches(mit)) {
            // Get the class of the calling method
            Tree parent = mit.parent();
            while (!parent.is(Tree.Kind.CLASS)) {
                parent = parent.parent();
            }
            ClassTree classTree = (ClassTree) parent;
            String enclosingClassOfCaller = classTree.symbol().name().toLowerCase();

            if (!AUTH_POINT_CLASS.equals(enclosingClassOfCaller)) {
                reportIssue(mit, "Method invocation to enforcer must be performed at auth points");
            }
        }
    }
}
