package com.github.secarchunit;

import com.github.secarchunit.concepts.*;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.lang.*;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tngtech.archunit.core.domain.AccessTarget.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.targetOwner;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

public class SecArchUnit {
    public static ArchRule logSecurityEvents(DescribedPredicate<? super JavaClass> securityServicesDescriptor, Class<?> logger) {
        return methods()
                .that().haveModifier(JavaModifier.PUBLIC)
                .and().areDeclaredInClassesThat(securityServicesDescriptor)
                .should(callTarget(declaredIn(logger)));
    }

    public static ArchRule enforceAuthenticationAtCentralPoint(Class<?> authenticationPoint, Class<?> authenticator) {
        String description = "Authentication should be enforced at authentication point {} by authenticator {}"
                .format(authenticationPoint.getName(), authenticator.getName());

        return CompositeArchRule.of(
                theClass(authenticator)
                        .should().onlyBeAccessed().byClassesThat().belongToAnyOf(authenticationPoint)
        ).and(
                theClass(authenticationPoint)
                        .should().accessClassesThat().belongToAnyOf(authenticator)
        ).as(description);
    }

    public static ArchRule enforceAuthorizationAtCentralPoint(Class<?> authorizationPoint, Class<?> authorizer) {
        String description = "Authorization should be enforced at authorization point {} by authorizer {}"
                .format(authorizationPoint.getName(), authorizer.getName());

        return enforceAuthenticationAtCentralPoint(authorizationPoint, authorizer).as(description);
    }

    public static ArchRule sendOutboundMessagesFromCentralPoint(Class<?> sendingPoint, DescribedPredicate<? super JavaClass> senderDescriptor) {
        return classes()
                .that(senderDescriptor)
                .should().onlyBeAccessed().byClassesThat().belongToAnyOf(sendingPoint);
    }

    public static ArchRule validateUserInput() {
        return codeUnits()
                .that().areAnnotatedWith(UserInput.class)
                .should(callTarget(codeUnitOrClassAnnotatedWith(InputValidator.class)));
    }

    public static ArchRule limitResourceAllocation() {
        return noClasses()
                .that().areNotAnnotatedWith(ResourceRestriction.class)
                .should().callMethodWhere(aThreadIsStarted())
                .orShould().callMethodWhere(aProcessIsStarted());
    }

    public static ArchRule doNotBleedAssetsBetweenComponents() {
        return fields()
                .that().areAnnotatedWith(Asset.class)
                .should(notBleedToInsecureComponents());
    }

    public static ArchRule doNotLogSecrets(DescribedPredicate<? super JavaClass> loggerDescriptor) {
        return noClasses()
                .should().callMethodWhere(targetOwner(loggerDescriptor).and(passSecretArgument()));
    }

    private static DescribedPredicate<AccessTarget> codeUnitOrClassAnnotatedWith(Class<? extends Annotation> annotation) {
        String description = "class or method annotated with @{}".format(annotation.getSimpleName());

        return new DescribedPredicate<AccessTarget>(description) {
            @Override
            public boolean apply(AccessTarget target) {
                return target.isAnnotatedWith(annotation) || target.getOwner().isAnnotatedWith(annotation);
            }
        };
    }

    private static DescribedPredicate<JavaMethodCall> aThreadIsStarted() {
        return new DescribedPredicate<>("a thread is started") {
            @Override
            public boolean apply(JavaMethodCall call) {
                AccessTarget.MethodCallTarget target = call.getTarget();

                boolean isRestricted = call.getOrigin().isAnnotatedWith(ResourceRestriction.class);
                boolean startsAThread = target.getOwner().isAssignableTo(Thread.class) && target.getName().equals("start");

                return !isRestricted && startsAThread;
            }
        };
    }

    private static DescribedPredicate<JavaMethodCall> aProcessIsStarted() {
        return new DescribedPredicate<>("a process is started") {
            @Override
            public boolean apply(JavaMethodCall call) {
                AccessTarget.MethodCallTarget target = call.getTarget();

                boolean isRestricted = call.getOrigin().isAnnotatedWith(ResourceRestriction.class);
                boolean startsAProcess =
                        target.getOwner().isEquivalentTo(ProcessBuilder.class) && target.getName().equals("start")
                                || target.getOwner().isEquivalentTo(Runtime.class) && target.getName().equals("exec");

                return !isRestricted && startsAProcess;
            }
        };
    }

    private static DescribedPredicate<JavaAccess<?>> passSecretArgument() {
        return new DescribedPredicate<>("pass a secret as argument") {
            @Override
            public boolean apply(JavaAccess<?> access) {
                boolean passesSecretArgument = recurseOnHints(access.getArgumentHints())
                        .anyMatch(hint -> hint.getJavaClass().isAnnotatedWith(Secret.class)
                                || hint.getMemberOrigin() != null && hint.getMemberOrigin().isAnnotatedWith(Secret.class));

                return passesSecretArgument;
            }
        };
    }

    private static Stream<Hint> recurseOnHints(Collection<Hint> hints) {
        return recurseOnHints(hints, 5).distinct();
    }

    private static Stream<Hint> recurseOnHints(Collection<Hint> hints, int depth) {
        if (depth == 0) {
            return hints.stream();
        }

        // Hints with an originating member
        Set<JavaMember> hintOrigins = hints.stream()
                .filter(hint -> hint.getMemberOrigin() != null)
                .map(hint -> hint.getMemberOrigin())
                .collect(Collectors.toSet());

        // Hints flowing into a member
        Stream<Hint> hintsFlowingIntoMembers = hintOrigins.stream()
                .flatMap(hint -> hint.getAccessesToSelf().stream())
                .flatMap(access -> access.getArgumentHints().stream());
        // ^ This assumes that all arguments that flowed into a method also flow into the method's return value...

        // Hints flowing out of a method
        Stream<Hint> hintsFlowingOutOfMethods = hintOrigins.stream()
                .filter(member -> member instanceof JavaMethod)
                .map(member -> (JavaMethod) member)
                .flatMap(method -> method.getReturnValueHints().stream());

        // Collect hints from this level
        Set<Hint> recursedHints = Stream.concat(hintsFlowingIntoMembers, hintsFlowingOutOfMethods)
                .collect(Collectors.toSet());

        // Concatenate this level of hints with the next recursion level
        return Stream.concat(hints.stream(), recurseOnHints(recursedHints, depth - 1));
    }

    private static ArchCondition<JavaCodeUnit> callTarget(DescribedPredicate<AccessTarget> targetDescriptor) {
        return new ArchCondition<>("contain method call to {}", targetDescriptor.getDescription()) {
            @Override
            public void check(JavaCodeUnit codeUnit, ConditionEvents events) {
                boolean containsCallToTarget = codeUnit.getAccessesFromSelf().stream()
                        .anyMatch(access ->
                                access.getTarget() instanceof AccessTarget.MethodCallTarget
                                        && targetDescriptor.apply(access.getTarget())
                        );

                if (!containsCallToTarget) {
                    String message = String.format("{} does not contain method call to {}",
                            codeUnit.getFullName(), targetDescriptor.getDescription());
                    events.add(SimpleConditionEvent.violated(codeUnit, message));
                }
            }
        };
    }

    private static ArchCondition<JavaField> notBleedToInsecureComponents() {
        return new ArchCondition<>("not bleed to insecure components") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                // Direct access
                field.getAccessesToSelf().stream()
                        .filter(access -> !access.getOriginOwner().isAnnotatedWith(AssetHandler.class))
                        .forEach(offendingFieldAccess -> {
                            String message = offendingFieldAccess + ": access to asset " + field.getName();
                            events.add(SimpleConditionEvent.violated(offendingFieldAccess, message));
                        });

                // Access via getter method
                field.getAccessesToSelf().stream()
                        .filter(access -> access.getOrigin() instanceof JavaMethod)
                        .map(access -> (JavaMethod) access.getOrigin())
                        .filter(method -> method.getReturnValueHints().stream().anyMatch(hint -> field.equals(hint.getMemberOrigin())))
                        .map(method -> method.getCallsOfSelf())
                        .flatMap(calls -> calls.stream())
                        .filter(call -> !call.getOriginOwner().isAnnotatedWith(AssetHandler.class))
                        .forEach(offendingMethodCall -> {
                            String message = offendingMethodCall + ": access to asset " + field.getName() + " (via getter method)";
                            events.add(SimpleConditionEvent.violated(offendingMethodCall, message));
                        });
            }
        };
    }
}