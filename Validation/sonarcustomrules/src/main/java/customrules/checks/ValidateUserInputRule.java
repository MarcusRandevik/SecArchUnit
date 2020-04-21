package customrules.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.cfg.ControlFlowGraph;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

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
        return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;

        if (methodInvocation.symbol().metadata().isAnnotatedWith(USER_INPUT)) {
            // Target method handles user input

            // Check for in-line validation in target method
            if (methodInvocation.symbol().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                return;
            }

            // Check for calls to validator in target method
            MethodTree targetMethod = (MethodTree) methodInvocation.symbol().declaration();
            for (ControlFlowGraph.Block block : targetMethod.cfg().blocks()) {
                for (Tree blockTree : block.elements()) {
                    if (blockTree.is(Tree.Kind.METHOD_INVOCATION)) {
                        MethodInvocationTree mit = (MethodInvocationTree) blockTree;
                        if (mit.symbol().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                            return;
                        }
                    }
                }
            }

            // Traverse tree to source method
            Tree parent = methodInvocation.parent();
            while (!(parent instanceof MethodTree)) {
                parent = parent.parent();
            }

            // Check if source method is validator
            MethodTree sourceMethod = (MethodTree) parent;
            if (sourceMethod.symbol().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                // Source method is validator
                return;
            }

            // Traverse tree to source class
            while (!(parent instanceof ClassTree)) {
                parent = parent.parent();
            }

            ClassTree sourceClass = (ClassTree) parent;
            if (sourceClass.symbol().metadata().isAnnotatedWith(INPUT_VALIDATOR)) {
                // Source class is validator
                return;
            }

            // There is no validation
            reportIssue(targetMethod, "User input must be validated");
        }
    }
}
