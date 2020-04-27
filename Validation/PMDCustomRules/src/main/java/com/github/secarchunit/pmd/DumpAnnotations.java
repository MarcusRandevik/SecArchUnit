package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DumpAnnotations extends AbstractJavaRule {
    private static final List<String> ANNOTATIONS = Arrays.asList(
            "com.github.secarchunit.concepts.UserInput",
            "com.github.secarchunit.concepts.InputValidator",
            "com.github.secarchunit.concepts.ResourceRestriction"
    );

    public DumpAnnotations() {
        super();

        // Create annotation dump files
        AnnotationHelper.createFiles(ANNOTATIONS);

        // Types of nodes to visit
        addRuleChainVisit(ASTClassOrInterfaceDeclaration.class);
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration clazz, Object data) {
        for (String annotation : ANNOTATIONS) {
            if (clazz.isAnnotationPresent(annotation)) {
                AnnotationHelper.dumpAnnotation(annotation, clazz);
            }
        }

        for (ASTMethodDeclaration method : clazz.findDescendantsOfType(ASTMethodDeclaration.class)) {
            for (String annotation : ANNOTATIONS) {
                if (method.isAnnotationPresent(annotation)) {
                    AnnotationHelper.dumpAnnotation(annotation, method);
                }
            }
        }

        for (ASTConstructorDeclaration constructor : clazz.findDescendantsOfType(ASTConstructorDeclaration.class)) {
            for (String annotation : ANNOTATIONS) {
                if (constructor.isAnnotationPresent(annotation)) {
                    AnnotationHelper.dumpAnnotation(annotation, constructor);
                }
            }
        }

        return super.visit(clazz, data);
    }
}
