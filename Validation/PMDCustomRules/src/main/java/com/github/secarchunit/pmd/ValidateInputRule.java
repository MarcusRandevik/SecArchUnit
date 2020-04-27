package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class ValidateInputRule extends AbstractJavaRule {
    private static final String USER_INPUT = "com.github.secarchunit.concepts.UserInput";

    public ValidateInputRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration method, Object data) {
        if (method.isAnnotationPresent(USER_INPUT)) {
            //System.out.println("Found @UserInput on " + method);
        }

        /*
        boolean callsValidator = Util.getMethodCallsFrom(method).stream()
                .anyMatch(call -> LOGGER.equals(call.targetOwner));

        if (!callsLogger) {
            addViolation(data, method);
        }
        */

        return data;
    }
}
