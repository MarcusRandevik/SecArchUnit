package customrules.checks;

import com.github.secarchunit.concepts.InputValidator;
import com.github.secarchunit.concepts.UserInput;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.cfg.ControlFlowGraph;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Collections;
import java.util.List;

public class ValidateUserInputRule extends IssuableSubscriptionVisitor {

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodTree methodTree = (MethodTree) tree;

        // Check for user input
        if (methodTree.symbol().metadata().isAnnotatedWith(UserInput.class.getName())) {
            // Check for in-line validation
            if (methodTree.symbol().metadata().isAnnotatedWith(InputValidator.class.getName())) {
                return;
            }

            // Check for calls to validator
            for (ControlFlowGraph.Block block : methodTree.cfg().blocks()) {
                for (Tree blockTree : block.elements()) {
                    if (blockTree.is(Tree.Kind.METHOD_INVOCATION)) {
                        MethodInvocationTree mit = (MethodInvocationTree) blockTree;
                        if (mit.symbol().metadata().isAnnotatedWith(InputValidator.class.getName())) {
                            return;
                        }
                    }
                }
            }

            // Check if validation occurs in all callers
            // There's no API for retrieving the callers of a method -> We can't test this case

            reportIssue(tree, "User input must be validated");
        }
    }
}
