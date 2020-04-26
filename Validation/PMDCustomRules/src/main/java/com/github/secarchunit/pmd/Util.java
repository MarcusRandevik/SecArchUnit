package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.symboltable.NameFinder;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {
    public static class MethodCall {
        public final String targetOwner;
        public final String target;
        public final int argumentCount;
        public final Node source;

        public MethodCall(String targetOwner, JavaNameOccurrence occurrence) {
            this.targetOwner = targetOwner;
            this.target = occurrence.getImage();
            this.argumentCount = occurrence.getArgumentCount();
            this.source = occurrence.getLocation();
        }
    }

    public static List<MethodCall> getMethodCallsFrom(ScopedNode node) {
        List<MethodCall> methodCalls = new ArrayList<>();

        // Find expressions that contain at least one method call
        Set<List<JavaNameOccurrence>> invocationChains = node
                .findDescendantsOfType(ASTPrimaryExpression.class).stream()
                .map(expr -> new NameFinder(expr).getNames())
                .filter(names -> names.stream().anyMatch(name -> name.isMethodOrConstructorInvocation()))
                .collect(Collectors.toSet());

        for (List<JavaNameOccurrence> chain : invocationChains) {
            // Resolve type of first sub-expression, i.e. owner of target method
            String targetOwner = getType(chain.get(0), node.getScope());

            // Iterate over suffixes to resolve method calls
            for (Iterator<JavaNameOccurrence> it = chain.listIterator(1); it.hasNext();) {
                JavaNameOccurrence occurrence = it.next();

                if (occurrence.isMethodOrConstructorInvocation()) {
                    if (targetOwner == null) {
                        System.err.println(occurrence.getLocation().getRoot().getType().getCanonicalName()
                                + ": Call to unknown target owner (target=" + occurrence.getImage() + ")");
                    } else {
                        methodCalls.add(new MethodCall(targetOwner, occurrence));
                    }
                }

                // Return type becomes target owner for the next iteration
                targetOwner = null;
                if (occurrence.getLocation() instanceof TypeNode) {
                    TypeNode type = (TypeNode) occurrence.getLocation();
                    if (type.getType() != null) {
                        targetOwner = type.getType().getCanonicalName();
                    }
                }
            }
        }

        return methodCalls;
    }

    public static String getType(NameOccurrence nameOccurrence, Scope scope) {
        String type = null;

        // Search for name in local variables and class fields
        Optional<NameDeclaration> nameDeclaration = Stream
                .concat(scope.getDeclarations().keySet().stream(), scope.getParent().getDeclarations().keySet().stream())
                .filter(name -> name.getName() != null)
                .filter(name -> name.getName().equals(nameOccurrence.getImage()))
                .findFirst();
        if (nameDeclaration.isPresent()) {
            type = getType(nameDeclaration.get().getNode());
        }

        if (type == null) {
            // Try to extract type directly from node
            type = getType(nameOccurrence.getLocation());
        }

        return type;
    }

    public static String getType(ScopedNode node) {
        String canonicalName = null;
        Class type = null;

        try {
            if (node instanceof TypeNode) {
                type = ((TypeNode) node).getType();
            } else if (node instanceof ASTExpression) {
                type = node.getFirstChildOfType(ASTPrimaryExpression.class).getFirstChildOfType(ASTName.class).getType();
            } else if (node instanceof ASTPrimaryExpression) {
                ASTClassOrInterfaceType classType = node.getFirstChildOfType(ASTClassOrInterfaceType.class);
                if (classType != null) {
                    type = classType.getType();
                } else {
                    type = node.getFirstChildOfType(ASTName.class).getType();
                }
            } else if (node instanceof ASTName) {
                type = ((ASTName) node).getType();
            } else {
                return null;
            }

            canonicalName = type.getCanonicalName();
        } catch (Exception ex1) {
            //ex1.printStackTrace();
        }

        return canonicalName;
    }
}
