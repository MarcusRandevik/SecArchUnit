package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.symboltable.NameFinder;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;

import java.util.*;
import java.util.stream.Collectors;

public class Util {
    public static class MethodCall {
        public final String targetOwner;
        public final Class<?> targetOwnerClass;
        public final String target;
        public final int argumentCount;
        public final Node source;

        public MethodCall(Class<?> targetOwner, JavaNameOccurrence occurrence) {
            this.targetOwner = targetOwner == null ? null : targetOwner.getCanonicalName();
            this.targetOwnerClass = targetOwner;
            this.target = occurrence.getImage();
            this.argumentCount = occurrence.getArgumentCount();
            this.source = occurrence.getLocation();
        }

        @Override
        public String toString() {
            return "MethodCall{" +
                    "targetOwner='" + targetOwner + '\'' +
                    ", targetOwnerClass='" + targetOwnerClass + '\'' +
                    ", target='" + target + '\'' +
                    ", argumentCount=" + argumentCount +
                    ", source=" + source +
                    '}';
        }
    }

    public static List<MethodCall> getMethodCallsFrom(JavaNode body) {
        List<MethodCall> methodCalls = new ArrayList<>();

        // Find expressions that contain at least one method call
        Set<List<JavaNameOccurrence>> invocationChains = body
                .findDescendantsOfType(ASTPrimaryExpression.class).stream()
                .map(expr -> new NameFinder(expr).getNames())
                .filter(names -> names.stream().anyMatch(name -> name.isMethodOrConstructorInvocation()))
                .collect(Collectors.toSet());

        for (List<JavaNameOccurrence> chain : invocationChains) {
            Class<?> targetOwner;
            if (chain.get(0).isMethodOrConstructorInvocation()) {
                // First name in chain is a method call

                // Look for previous sibling
                JavaNode node = chain.get(0).getLocation();
                if (node.getIndexInParent() > 0) {
                    // Get return type from sibling
                    Class<?> siblingType = getType(node.getParent().getChild(node.getIndexInParent() - 1));
                    if (siblingType != null) {
                        MethodCall call = new MethodCall(siblingType, chain.get(0));
                        methodCalls.add(call);
                    }
                } else {
                    // Should be local method, super method or static import
                    // TODO look for static imports?
                    Class<?> enclosingClass = body.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class).getType();
                    if (enclosingClass != null) {
                        MethodCall call = new MethodCall(enclosingClass, chain.get(0));
                        methodCalls.add(call);
                    }
                }

                /*
                System.err.println("First expression is method call");


                System.err.println(" + " + call);
                try {
                    boolean targetExists = Arrays.stream(Class.forName(call.targetOwner).getDeclaredMethods())
                            .anyMatch(method -> method.getName().equals(call.target));
                    if (!targetExists) {
                        System.err.println(" + Target does not exist");
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                */

            }

            // Resolve type of first sub-expression, i.e. owner of next target method
            targetOwner = getType(chain.get(0), body.getScope());

            // Iterate over suffixes to resolve method calls
            for (Iterator<JavaNameOccurrence> it = chain.listIterator(1); it.hasNext();) {
                JavaNameOccurrence occurrence = it.next();

                if (occurrence.isMethodOrConstructorInvocation()) {
                    if (targetOwner == null) {
                        System.err.println("Call to target with unknown owner (target=" + occurrence.getImage()
                                + ")" + describeLocation(occurrence.getLocation()));
                        System.err.println(" + " + chain.toString());
                    } else {
                        methodCalls.add(new MethodCall(targetOwner, occurrence));
                    }
                }

                // Return type becomes target owner for the next iteration
                targetOwner = getType(occurrence.getLocation());
            }
        }

        return methodCalls;
    }

    private static String describeLocation(JavaNode node) {
        Class<?> type = node.getRoot().getType();
        if (type != null) {
            return " in (" + type.getSimpleName() + ".java:" + node.getBeginLine() + ")";
        }

        return " in unknown class (" + node.toString() + ")";
    }

    public static Class<?> getType(NameOccurrence nameOccurrence, Scope scope) {
        Class<?> type = null;

        // Search for name in current and parent scopes
        while (scope != null) {
            Optional<NameDeclaration> nameDeclaration = scope.getDeclarations().keySet().stream()
                    .filter(name -> name.getName() != null)
                    .filter(name -> name.getName().equals(nameOccurrence.getImage()))
                    .findFirst();

            if (nameDeclaration.isPresent()) {
                type = getType(nameDeclaration.get().getNode());
                break;
            }

            scope = scope.getParent();
        }

        if (type == null) {
            // Try to extract type directly from node
            type = getType(nameOccurrence.getLocation());
        }

        return type;
    }

    public static Class<?> getType(ScopedNode node) {
        if (node instanceof TypeNode) {
            return ((TypeNode) node).getType();
        }

        return null;
    }
}
