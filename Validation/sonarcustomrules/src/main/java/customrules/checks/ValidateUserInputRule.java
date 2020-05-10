package customrules.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.cfg.ControlFlowGraph;
import org.sonar.plugins.java.api.tree.*;

import java.util.Collections;
import java.util.List;

@Rule(
        key = "ValidateUserInput",
        name = "All user input must be validated",
        description = "Methods that handle user input must validate it, directly or indirectly"
)
public class ValidateUserInputRule extends IssuableSubscriptionVisitor {
    private static final String USER_INPUT = "com.github.secarchunit.concepts.UserInput";
    private static final String INPUT_VALIDATOR = "com.github.secarchunit.concepts.InputValidator";

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodTree methodTree = (MethodTree) tree;

        // Check if this method deals with user input
        if (methodTree.symbol().metadata().isAnnotatedWith(USER_INPUT)) {
            // Check for in-line validation
            if (methodTree.symbol().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                return;
            }

            // Check for calls to validator
            for (ControlFlowGraph.Block block : methodTree.cfg().blocks()) {
                for (Tree blockTree : block.elements()) {
                    if (blockTree.is(Tree.Kind.METHOD_INVOCATION)) {
                        MethodInvocationTree mit = (MethodInvocationTree) blockTree;
                        if (mit.symbol().metadata().isAnnotatedWith(INPUT_VALIDATOR)
                                || mit.symbol().enclosingClass().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                            return;
                        }
                    }
                }
            }

            // Check if all callers are marked as validators
            boolean hasCallers = !methodTree.symbol().usages().isEmpty();
            if (hasCallers) {
                boolean validatedInAllCallers = true;

                for (IdentifierTree caller : methodTree.symbol().usages()) {
                    if (caller.symbol().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                        // Caller method is validator
                        continue;
                    }

                    if (caller.symbol().enclosingClass().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                        // Caller class is validator
                        continue;
                    }

                    validatedInAllCallers = false;
                    break;
                }

                if (validatedInAllCallers) {
                    return;
                }
            }

            reportIssue(methodTree, "User input must be validated");
        }
    }
}
