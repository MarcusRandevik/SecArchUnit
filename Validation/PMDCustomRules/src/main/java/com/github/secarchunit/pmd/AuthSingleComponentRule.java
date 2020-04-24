package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuthSingleComponentRule extends AbstractJavaRule {
    private static final PropertyDescriptor<List<String>> AUTH_POINTS_PROPERTY = PropertyFactory
            .stringListProperty("authPoints").emptyDefaultValue().build();

    private static final PropertyDescriptor<List<String>> AUTH_ENFORCER_PROPERTY = PropertyFactory
            .stringListProperty("authEnforcer").emptyDefaultValue().build();

    private final List<String> authPoints;
    private final List<String> authEnforcer;

    public AuthSingleComponentRule() {
        super();

        // Types of nodes to visit
        addRuleChainVisit(ASTClassOrInterfaceDeclaration.class);

        // Rule properties
        definePropertyDescriptor(AUTH_POINTS_PROPERTY);
        authPoints = getProperty(AUTH_POINTS_PROPERTY);

        definePropertyDescriptor(AUTH_ENFORCER_PROPERTY);
        authEnforcer = getProperty(AUTH_ENFORCER_PROPERTY);
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration clazz, Object data) {
        if (!authPoints.contains(clazz.getSimpleName())) {
            // This class is not an authpoint
            return data;
        }

        AtomicBoolean containsCallToEnforcer = new AtomicBoolean(false);

        //Search tree for method calls
        for (ASTPrimaryPrefix prefix : clazz.findDescendantsOfType(ASTPrimaryPrefix.class)) {
            prefix.getXPathAttributesIterator().forEachRemaining(attribute -> {
                //search the attributes to find the type
                if (attribute.getName().equals("typeIs()")) {
                    //See if type matches enforcer
                    if (authEnforcer.contains(attribute.getStringValue())) {
                        containsCallToEnforcer.set(true);
                    }
                }
            });
            
        }

        if (!(containsCallToEnforcer.get())) {
            addViolation(data, clazz);
        }

        return data;
    }
}
