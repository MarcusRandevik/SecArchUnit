package customrules.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Rule(
        key = "AuthSingleCompnentEnforcer",
        name = "An authorization enforcer should only be called by auth points",
        description = "An authorization enforcer should only be called by auth points"
)
public class AuthSingleComponentEnforcerRule extends IssuableSubscriptionVisitor {

    public static String AUTHN_POINT_CLASS = "OrderActionBean".toLowerCase();
    public static List<String> AUTH_POINT_CLASSES = new ArrayList<>(Arrays.asList(AUTHN_POINT_CLASS));

    public static String AUTHN_CLASS = "org.mybatis.jpetstore.web.actions.OrderActionBean";

    MethodMatchers enforcerMethods = MethodMatchers.create().ofTypes(AUTHN_CLASS).anyName().withAnyParameters().build();

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

            System.out.println(enclosingClassOfCaller);
            if (AUTH_POINT_CLASSES.contains(enclosingClassOfCaller)) {
                return;
            } else {
                reportIssue(mit, "Method invocation to enforcer must be performed at auth points");
            }
        }
    }

}
