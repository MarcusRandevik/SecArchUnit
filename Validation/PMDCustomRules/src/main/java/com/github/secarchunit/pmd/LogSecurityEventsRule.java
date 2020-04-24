package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.util.Arrays;
import java.util.Collection;

public class LogSecurityEventsRule extends AbstractJavaRule {
    private static final String LOGGER = "atm.physical.Log";
    private static final Collection<String> SECURITY_SERVICES = Arrays.asList(
            "physical.CardReader",
            "physical.CashDispenser",
            "physical.EnvelopeAcceptor",
            "physical.NetworkToBank",
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
        if (!SECURITY_SERVICES.contains(owningClass.getBinaryName())) {
            // Not a security service; skip method
            return data;
        }

        System.out.println("LogSecurityEventsRule visit " + owningClass.getBinaryName() + " " + owningClass.getSimpleName() + " " + method.getName());

        if (method.isPublic()) {
            // Security event; check for call to logger
            method.getBody().findDescendantsOfType(ASTPrimaryPrefix.class).stream()
                    .forEach(expr -> {
                        expr.getXPathAttributesIterator().forEachRemaining(attr -> {
                            System.out.println(attr.getName() + " -> " + attr.getStringValue());
                        });
                    });
        }

        return data;
    }
}
