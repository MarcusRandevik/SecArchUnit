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

    public static String AUTHN_POINT_CLASS_TEST = "AuthSingleComponentEnforcerCheck".toLowerCase();
    public static String AUTHN_POINT_CLASS = "Transaction".toLowerCase();
    public static List<String> AUTH_POINT_CLASSES = new ArrayList<>(Arrays.asList(AUTHN_POINT_CLASS, AUTHN_POINT_CLASS_TEST));

    public static String AUTHN_CLASS_TEST = "java.math.BigDecimal";
    public static String AUTHN_CLASS = "atm.transaction.Transaction";

    MethodMatchers enforcerMethodsTest = MethodMatchers.create().ofTypes(AUTHN_CLASS_TEST).anyName().withAnyParameters().build();
    MethodMatchers enforcerMethods = MethodMatchers.create().ofTypes(AUTHN_CLASS).anyName().withAnyParameters().build();

    MethodMatchers mitBoth = MethodMatchers.or(enforcerMethodsTest, enforcerMethods);


    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree mit = (MethodInvocationTree) tree;

        if (mitBoth.matches(mit)) {
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
