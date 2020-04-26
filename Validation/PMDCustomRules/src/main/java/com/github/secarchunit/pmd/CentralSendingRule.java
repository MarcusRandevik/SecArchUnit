package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.Arrays;
import java.util.Collection;

public class CentralSendingRule extends AbstractJavaRule {
    private static final String SENDING_POINT = "atm.transaction.Transaction";
    private static final Collection<String> SENDERS = Arrays.asList(
            "atm.physical.NetworkToBank"
    );

    public CentralSendingRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTClassOrInterfaceBodyDeclaration.class);
    }

    @Override
    public Object visit(ASTClassOrInterfaceBodyDeclaration body, Object data) {
        ASTClassOrInterfaceDeclaration owningClass = body.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        boolean isSendingPoint = owningClass != null && SENDING_POINT.equals(owningClass.getBinaryName());
        if (isSendingPoint) {
            // Skip
            return data;
        }

        Util.getMethodCallsFrom(body).stream()
                .filter(call -> SENDERS.contains(call.targetOwner) && call.argumentCount > 0)
                .forEach(call -> {
                    addViolation(data, call.source);
                });

        return data;
    }
}
