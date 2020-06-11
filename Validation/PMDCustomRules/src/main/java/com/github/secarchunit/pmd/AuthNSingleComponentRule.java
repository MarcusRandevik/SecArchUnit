package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.stream.Stream;

public class AuthNSingleComponentRule extends AbstractJavaRule {
    private static final String AUTH_POINT = "org.mybatis.jpetstore.web.actions.AccountActionBean";
    private static final String AUTH_ENFORCER = "org.mybatis.jpetstore.web.actions.AccountActionBean";

    public AuthNSingleComponentRule() {
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
                addViolationWithMessage(data, clazz, "#2 Authentication point must call authentication enforcer");
            }
        } else {
            // Ensure no calls to authenticator
            methodCallsFromClass.filter(call -> AUTH_ENFORCER.equals(call.targetOwner))
                    .forEach(offendingCall -> {
                        String message = "#2 Method invocation to enforcer must be performed at authN point";
                        addViolationWithMessage(data, offendingCall.source, message);
                    });
        }

        return data;
    }
}
