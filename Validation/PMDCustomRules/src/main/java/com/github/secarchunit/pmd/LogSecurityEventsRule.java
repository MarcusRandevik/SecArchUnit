package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.*;

public class LogSecurityEventsRule extends AbstractJavaRule {
    private static final String LOGGER = "atm.physical.Log";
    private static final Collection<String> SECURITY_SERVICES = Arrays.asList(
            "atm.physical.CardReader",
            "atm.physical.CashDispenser",
            "atm.physical.EnvelopeAcceptor",
            "atm.physical.NetworkToBank",
            "atm.transaction.Transaction"
    );

    public LogSecurityEventsRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration method, Object data) {
        ASTClassOrInterfaceDeclaration owningClass = method.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        if (!SECURITY_SERVICES.contains(owningClass.getBinaryName()) || !method.isPublic()) {
            // Not a security event; skip method
            return data;
        }

        boolean callsLogger = Util.getMethodCallsFrom(method).stream()
                .anyMatch(call -> LOGGER.equals(call.targetOwner));

        if (!callsLogger) {
            addViolation(data, method);
        }

        return data;
    }
}
