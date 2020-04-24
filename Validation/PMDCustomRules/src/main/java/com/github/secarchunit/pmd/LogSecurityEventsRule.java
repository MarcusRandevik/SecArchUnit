package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;

public class LogSecurityEventsRule extends AbstractJavaRule {
    private static final PropertyDescriptor<List<String>> SECURITY_SERVICES_PROPERTY = PropertyFactory
            .stringListProperty("securityServices").emptyDefaultValue().build();

    private final List<String> securityServices;

    public LogSecurityEventsRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTMethodDeclaration.class);

        // Rule properties
        definePropertyDescriptor(SECURITY_SERVICES_PROPERTY);
        securityServices = getProperty(SECURITY_SERVICES_PROPERTY);
    }

    @Override
    public Object visit(ASTMethodDeclaration method, Object data) {
        ASTClassOrInterfaceDeclaration owningClass = method.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class);
        if (!securityServices.contains(owningClass.getSimpleName())) {
            // Not a security service; skip class
            return data;
        }

        if (method.isPublic()) {
            // Security event; check for call to logger

        }

        return data;
    }
}
