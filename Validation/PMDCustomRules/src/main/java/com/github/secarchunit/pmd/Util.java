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

        @Override
        public String toString() {
            return "MethodCall{" +
                    "targetOwner='" + targetOwner + '\'' +
                    ", target='" + target + '\'' +
                    ", argumentCount=" + argumentCount +
                    ", source=" + source +
                    '}';
        }
    }

    public static List<MethodCall> getMethodCallsFrom(JavaNode node) {
        List<MethodCall> methodCalls = new ArrayList<>();

        // Find expressions that contain at least one method call
        Set<List<JavaNameOccurrence>> invocationChains = node
                .findDescendantsOfType(ASTPrimaryExpression.class).stream()
                .map(expr -> new NameFinder(expr).getNames())
                .filter(names -> names.stream().anyMatch(name -> name.isMethodOrConstructorInvocation()))
                .collect(Collectors.toSet());

        for (List<JavaNameOccurrence> chain : invocationChains) {
            String targetOwner;
            if (chain.get(0).isMethodOrConstructorInvocation()) {
                // First sub-expression is method call -> local method, super method or static import
                // TODO look for static imports?
                MethodCall call = new MethodCall(node.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class).getBinaryName(), chain.get(0));
                methodCalls.add(call);

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
            targetOwner = getType(chain.get(0), node.getScope());

            // Iterate over suffixes to resolve method calls
            for (Iterator<JavaNameOccurrence> it = chain.listIterator(1); it.hasNext();) {
                JavaNameOccurrence occurrence = it.next();

                if (occurrence.isMethodOrConstructorInvocation()) {
                    if (targetOwner == null) {
                        /*
                        System.err.println("Call to target with unknown owner (target=" + occurrence.getImage()
                                + ")" + describeLocation(occurrence.getLocation()));
                        System.err.println(" + " + chain.toString());
                        */
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

    private static String describeLocation(JavaNode node) {
        return " in (" + node.getRoot().getType().getSimpleName()
                + ".java:" + node.getBeginLine()
                + ")";
    }

    public static String getType(NameOccurrence nameOccurrence, Scope scope) {
        String type = null;

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
