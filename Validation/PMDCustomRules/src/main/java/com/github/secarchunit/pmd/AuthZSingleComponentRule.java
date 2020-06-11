package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.stream.Stream;

public class AuthZSingleComponentRule extends AbstractJavaRule {
    private static final String AUTH_POINT = "atm.transaction.Transaction";
    private static final String AUTH_ENFORCER = "atm.transaction.Transaction";

    public AuthZSingleComponentRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTClassOrInterfaceDeclaration.class);
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration clazz, Object data) {
        boolean isAuthPoint = AUTH_POINT.equals(clazz.getBinaryName());
        Stream<Util.MethodCall> methodCallsFromClass = clazz.findDescendantsOfType(ASTClassOrInterfaceBodyDeclaration.class).stream()
                .flatMap(body -> Util.getMethodCallsFrom(body).stream());

        if (isAuthPoint) {
            // Ensure at least one call to authentication enforcer
            boolean callsEnforcer = methodCallsFromClass
                    .anyMatch(call -> AUTH_ENFORCER.equals(call.targetOwner));

            if (!callsEnforcer) {
                addViolationWithMessage(data, clazz, "#2 Authorization point must call authorization enforcer");
            }
        } else {
            // Ensure no calls to authenticator
            methodCallsFromClass.filter(call -> AUTH_ENFORCER.equals(call.targetOwner))
                    .forEach(offendingCall -> {
                        String message = "#2 Method invocation to enforcer must be performed at authZ point";
                        addViolationWithMessage(data, offendingCall.source, message);
                    });
        }

        return data;
    }
}
