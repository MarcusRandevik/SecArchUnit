package customrules.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.cfg.ControlFlowGraph;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Collections;
import java.util.List;

@Rule(
        key = "LimitThreadSpawns",
        name = "Thread spawns must be restricted",
        description = "Code that starts a thread or process must be marked as restricting resources",
        tags = "secarchunit",
        priority = Priority.MAJOR
)
public class LimitThreadSpawnRule extends IssuableSubscriptionVisitor {
    private static final String RESOURCE_RESTRICTION = "com.github.secarchunit.concepts.ResourceRestriction";

    private final MethodMatchers threadStartMethods = MethodMatchers.create()
            .ofSubTypes("java.lang.Thread").names("start").withAnyParameters().build();
    private final MethodMatchers processStartMethods = MethodMatchers.or(
            MethodMatchers.create().ofTypes("java.lang.ProcessBuilder").names("start").withAnyParameters().build(),
            MethodMatchers.create().ofTypes("java.lang.Runtime").names("exec").withAnyParameters().build()
    );

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodTree methodTree = (MethodTree) tree;

        if (methodTree.symbol().isAbstract()) {
            // Skip abstract methods, as they have no code block
            return;
        }

        if (methodStartsThreadOrProcess(methodTree)) {
            // Check for resource restriction marker...
            // ... on method
            if (methodTree.symbol().metadata().isAnnotatedWith(RESOURCE_RESTRICTION)) {
                return;
            }

            // ... or on class
            if (methodTree.symbol().enclosingClass().metadata().isAnnotatedWith(RESOURCE_RESTRICTION)) {
                return;
            }

            reportIssue(methodTree, "Thread spawns must be restricted");
        }
    }

    private boolean methodStartsThreadOrProcess(MethodTree methodTree) {
        for (ControlFlowGraph.Block block : methodTree.cfg().blocks()) {
            for (Tree blockTree : block.elements()) {
                if (blockTree.is(Tree.Kind.METHOD_INVOCATION)) {
                    MethodInvocationTree mit = (MethodInvocationTree) blockTree;
                    if (threadStartMethods.matches(mit) || processStartMethods.matches(mit)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
