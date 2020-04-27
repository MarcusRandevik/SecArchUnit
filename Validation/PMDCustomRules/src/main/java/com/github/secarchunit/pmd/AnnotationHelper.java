package com.github.secarchunit.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnnotationHelper {
    public static void createFiles(List<String> annotations) {
        for (String annotation : annotations) {
            try {
                Files.write(Paths.get("pmd_" + annotation + ".txt"), new byte[0],
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void dumpAnnotation(String annotation, JavaNode node) {
        try {
            String content = getLocation(node) + "\n";

            Files.write(getPath(annotation), content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAnnotations(String annotation) {
        try {
            String dump = new String(Files.readAllBytes(getPath(annotation)));
            return Arrays.asList(dump.strip().split("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private static Path getPath(String annotation) {
        return Paths.get("pmd_" + annotation + ".txt");
    }

    private static String getLocation(JavaNode node) {
        String location = node.getFirstParentOfType(ASTClassOrInterfaceDeclaration.class).getBinaryName();

        if (node instanceof ASTMethodDeclaration) {
            ASTMethodDeclaration method = (ASTMethodDeclaration) node;
            location += "." + method.getName();
        } else if (node instanceof ASTConstructorDeclaration) {
            location += "." + node.getImage();
        }

        return location;
    }
}
