package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.List;

public class RestrictThreadSpawningRule extends AbstractJavaRule {
    private static final String RESOURCE_RESTRICTION = "com.github.secarchunit.concepts.ResourceRestriction";

    public RestrictThreadSpawningRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTMethodDeclaration.class);
        addRuleChainVisit(ASTConstructorDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration method, Object data) {
        if (containsViolation(method)) {
            addViolation(data, method);
        }

        return super.visit(method, data);
    }

    @Override
    public Object visit(ASTConstructorDeclaration constructor, Object data) {
        if (containsViolation(constructor)) {
            addViolation(data, constructor);
        }

        return super.visit(constructor, data);
    }

    private boolean containsViolation(Annotatable annotatable) {
        boolean startsThreadOrProcess = Util.getMethodCallsFrom(annotatable).stream()
                .anyMatch(call -> Thread.class.isAssignableFrom(call.targetOwnerClass) && "start".equals(call.target)
                        || ProcessBuilder.class.equals(call.targetOwnerClass) && "start".equals(call.target)
                        || Runtime.class.equals(call.targetOwnerClass) && "exec".equals(call.target)
                );
        if (startsThreadOrProcess) {
            // Ensure there is resource restriction
            if (annotatable.isAnnotationPresent(RESOURCE_RESTRICTION)) {
                // Checks out
                return false;
            }

            if (annotatable.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class).isAnnotationPresent(RESOURCE_RESTRICTION)) {
                // Checks out
                return false;
            }

            // Violation
            return true;
        }

        return false;
    }
}
