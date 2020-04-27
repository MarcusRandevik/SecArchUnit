package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class ValidateInputRule extends AbstractJavaRule {
    private static final String USER_INPUT = "com.github.secarchunit.concepts.UserInput";
    private static final String INPUT_VALIDATOR = "com.github.secarchunit.concepts.InputValidator";

    public ValidateInputRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration method, Object data) {
        if (method.isAnnotationPresent(USER_INPUT)) {
            // Check for in-line validation
            if (method.isAnnotationPresent(INPUT_VALIDATOR)) {
                // Checks out
                return data;
            }

            // Check for calls to validator
            List<String> inputValidators = AnnotationHelper.getAnnotations(INPUT_VALIDATOR);
            boolean callsValidator = Util.getMethodCallsFrom(method).stream()
                    .anyMatch(call -> inputValidators.contains(call.targetOwner)
                            || inputValidators.contains(call.targetOwner + "." + call.target));

            if (callsValidator) {
                // Checks out
                return data;
            }

            // Check if all callers of this method are input validators
            // TODO Can't be done in PMD; assume no validation occurs

            addViolation(data, method);
        }

        return data;
    }
}
