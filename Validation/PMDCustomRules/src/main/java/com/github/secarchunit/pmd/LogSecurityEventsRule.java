package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.symboltable.NameFinder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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

        //System.out.println("LogSecurityEventsRule visit " + owningClass.getBinaryName() + "." + method.getName());
        boolean callsLogger = false;

        // Find expressions that contain at least one method call
        Set<List<JavaNameOccurrence>> invocationChains = method.getBody()
                .findDescendantsOfType(ASTPrimaryExpression.class).stream()
                .map(expr -> new NameFinder(expr).getNames())
                .filter(names -> names.stream().anyMatch(name -> name.isMethodOrConstructorInvocation()))
                .collect(Collectors.toSet());

        for (List<JavaNameOccurrence> chain : invocationChains) {
            // Resolve type of first sub-expression, i.e. owner of target method
            String targetOwner = Util.getType(chain.get(0), method.getScope());

            // Iterate over suffixes to resolve method calls
            for (Iterator<JavaNameOccurrence> it = chain.listIterator(1); it.hasNext();) {
                JavaNameOccurrence occurrence = it.next();

                if (occurrence.isMethodOrConstructorInvocation() && LOGGER.equals(targetOwner)) {
                    callsLogger = true;
                    // TODO refactor
                }

                // Return type becomes target owner for the next iteration
                targetOwner = Util.resolveReturnType(targetOwner, occurrence.getImage());
            }
        }

        if (!callsLogger) {
            addViolation(data, method);
        }

        return data;
    }
}
