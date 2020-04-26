package com.github.secarchunit.pmd;

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

        public MethodCall(String targetOwner, String target) {
            this.targetOwner = targetOwner;
            this.target = target;
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
                    methodCalls.add(new MethodCall(targetOwner, occurrence.getImage()));
                }

                // Return type becomes target owner for the next iteration
                targetOwner = resolveReturnType(targetOwner, occurrence.getImage());
            }
        }

        return methodCalls;
    }

    public static String resolveReturnType(String targetOwner, String target) {
        try {
            Class<?> targetClass = Class.forName(targetOwner);
            Optional<Method> targetMethods = Arrays.stream(targetClass.getDeclaredMethods())
                    .filter(targetMethod -> targetMethod.getName().equals(target))
                    .findFirst();
            // ^ ignores overloaded methods, can be wrong target

            if (targetMethods.isPresent()) {
                return targetMethods.get().getReturnType().getCanonicalName();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getType(NameOccurrence nameOccurrence, Scope scope) {
        String type = null;

        // Search for name in local variables and class fields
        Optional<NameDeclaration> nameDeclaration = Stream
                .concat(scope.getDeclarations().keySet().stream(), scope.getParent().getDeclarations().keySet().stream())
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
