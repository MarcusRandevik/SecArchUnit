package customrules.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.cfg.ControlFlowGraph;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.*;

@Rule(
        key = "AuthSingleComponentRule",
        name = "AuthN/AuthZ are each enforced in a single component",
        description = "Authentication and authorization must each be enforced in a single component."
)
public class AuthSingleComponentRule extends IssuableSubscriptionVisitor {

    public static String AUTH_POINT_CLASS = "OrderActionBean".toLowerCase();
    public static String AUTH_ENFORCER_CLASS = "org.mybatis.jpetstore.web.actions.OrderActionBean";

    MethodMatchers authNMethods = MethodMatchers.create().ofTypes(AUTH_ENFORCER_CLASS).anyName().withAnyParameters().build();

    private static int INITIAL_AMOUNT_OF_METHODS = -1;
    int methodsInAuthPoint = INITIAL_AMOUNT_OF_METHODS;
    int methodsVisited = 0;
    boolean containsCallToEnforcer = false;

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodTree method = (MethodTree) tree;

        String enclosingClassName = method.symbol().enclosingClass().name().toLowerCase();
        // Check if we're in the auth point class
        if (!AUTH_POINT_CLASS.equals(enclosingClassName)) {
            return;
        }

        methodsVisited++;

        // If this is the first time we're in the auth point class, calculate the number of methods
        if (methodsInAuthPoint == INITIAL_AMOUNT_OF_METHODS) {
            Collection<Symbol> symbols = method.symbol().enclosingClass().memberSymbols();
            setMethodsInAuthPoint(symbols);
        }

        // From the cgf, see if the methods contains a call to enforcer class
        ControlFlowGraph cfg = method.cfg();

        for (ControlFlowGraph.Block block : cfg.blocks()) {
            for (Tree blockTree : block.elements()) {
                if (blockTree.is(Tree.Kind.METHOD_INVOCATION)) {
                    MethodInvocationTree mit = (MethodInvocationTree) blockTree;
                    if (authNMethods.matches(mit)) {
                        containsCallToEnforcer = true;
                    }
                }
            }
        }

        if (methodsVisited >= methodsInAuthPoint && !containsCallToEnforcer) {
            reportIssue(method.symbol().enclosingClass().declaration(), "Authpoint must contain call to enforcer");
        }
    }

    /**
     * Calculate the amount of methods in the authpoint class
     * @param symbols the collection of symbols defined within the authpoint class
     */
    private void setMethodsInAuthPoint(Collection<Symbol> symbols) {
        for (Symbol symbol : symbols) {
            if (symbol.isMethodSymbol() && (!symbol.name().equals("<init>"))) {
                if (methodsInAuthPoint == INITIAL_AMOUNT_OF_METHODS) {
                    methodsInAuthPoint = 1;
                } else {
                    methodsInAuthPoint++;
                }
            }
        }

        if (methodsInAuthPoint == INITIAL_AMOUNT_OF_METHODS) {
            methodsInAuthPoint = 0;
        }
    }
}
