package com.github.secarchunit;

import com.github.secarchunit.concepts.*;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.lang.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tngtech.archunit.core.domain.AccessTarget.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.targetOwner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

public class SecArchUnit {
    public static ArchRule logSecurityEvents(DescribedPredicate<? super JavaClass> securityServicesDescriptor, Class<?> logger) {
        return methods()
                .that().haveModifier(JavaModifier.PUBLIC)
                .and().areDeclaredInClassesThat(securityServicesDescriptor)
                .should(CodeUnitConditions.callMethod(declaredIn(logger)));
    }

    public static ArchRule enforceAuthenticationAtCentralPoint(Class<?> authenticationPoint, Class<?> authenticator) {
        String description = "Authentication should be enforced at authentication point "
                + authenticationPoint.getName() + " by authenticator " + authenticator.getName();

        return CompositeArchRule.of(
                theClass(authenticationPoint)
                        .should(ClassConditions.callMethod(declaredIn(authenticator)))
        ).and(
                methods()
                        .that().areDeclaredIn(authenticator)
                        .should(MemberConditions.onlyBeAccessedBy(authenticationPoint))
        ).as(description);
    }

    public static ArchRule enforceAuthorizationAtCentralPoint(Class<?> authorizationPoint, Class<?> authorizer) {
        String description = "Authorization should be enforced at authorization point "
                + authorizationPoint.getName() + " by authorizer " + authorizer.getName();

        return enforceAuthenticationAtCentralPoint(authorizationPoint, authorizer).as(description);
    }

    public static ArchRule sendOutboundMessagesFromCentralPoint(Class<?> sendingPoint, DescribedPredicate<? super JavaClass> senderDescriptor) {
        String description = "Outbound messages should be sent from the central sending point " + sendingPoint.getName();

        return methods()
                .that().areDeclaredInClassesThat(senderDescriptor)
                .and(MethodPredicates.haveAtLeastOneParameter)
                .should(MemberConditions.onlyBeAccessedBy(sendingPoint))
                .as(description);
    }

    public static ArchRule validateUserInput() {
        return codeUnits()
                .that().areAnnotatedWith(UserInput.class)
                .should(CodeUnitConditions.performDirectOrIndirectValidation);
    }

    public static ArchRule limitResourceAllocation() {
        return noClasses()
                .that().areNotAnnotatedWith(ResourceRestriction.class)
                .should().callMethodWhere(MethodCallPredicates.aThreadIsStartedWithoutRestriction)
                .orShould().callMethodWhere(MethodCallPredicates.aProcessIsStartedWithoutRestriction);
    }

    public static ArchRule doNotBleedAssetsBetweenComponents() {
        return fields()
                .that().areAnnotatedWith(Asset.class)
                .should(FieldConditions.notBleedToInsecureComponents);
    }

    public static ArchRule doNotLogSecrets(DescribedPredicate<? super JavaClass> loggerDescriptor) {
        return noClasses()
                .should().callMethodWhere(targetOwner(loggerDescriptor).and(AccessPredicates.passSecretArgument));
    }

    private static class AccessPredicates {
        private static DescribedPredicate<JavaAccess<?>> passSecretArgument =
                new DescribedPredicate<>("pass a secret as argument") {
                    @Override
                    public boolean apply(JavaAccess<?> access) {
                        boolean passesSecretArgument = InformationFlow.recurseOnHints(access.getArgumentHints())
                                .anyMatch(hint -> hint.getType().isAnnotatedWith(Secret.class)
                                        || hint.getMemberOrigin() != null && (
                                                hint.getMemberOrigin().isAnnotatedWith(Secret.class)
                                                || hint.getMemberOrigin().getOwner().isAnnotatedWith(Secret.class))
                                );

                        return passesSecretArgument;
                    }
                };
    }

    private static class MethodCallPredicates {
        private static DescribedPredicate<JavaMethodCall> aThreadIsStartedWithoutRestriction =
                new DescribedPredicate<>("a thread is started") {
                    @Override
                    public boolean apply(JavaMethodCall call) {
                        AccessTarget.MethodCallTarget target = call.getTarget();

                        boolean isRestricted = call.getOrigin().isAnnotatedWith(ResourceRestriction.class);
                        boolean startsAThread = target.getOwner().isAssignableTo(Thread.class) && target.getName().equals("start");

                        return !isRestricted && startsAThread;
                    }
                };

        private static DescribedPredicate<JavaMethodCall> aProcessIsStartedWithoutRestriction =
                new DescribedPredicate<>("a process is started") {
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

    private static class MethodPredicates {
        private static DescribedPredicate<JavaMethod> haveAtLeastOneParameter =
                new DescribedPredicate<>("have at least one parameter") {
                    @Override
                    public boolean apply(JavaMethod method) {
                        return !method.getRawParameterTypes().isEmpty();
                    }
                };
    }

    private static class MemberConditions {
        private static ArchCondition<JavaMember> onlyBeAccessedBy(Class<?> originOwner) {
            return new ArchCondition<>("only be accessed by " + originOwner.getName()) {
                @Override
                public void check(JavaMember codeUnit, ConditionEvents events) {
                    codeUnit.getAccessesToSelf().stream()
                            .filter(access -> {
                                boolean interClassAccess = !access.getOriginOwner().equals(codeUnit.getOwner());
                                boolean allowedOrigin = access.getOriginOwner().isEquivalentTo(originOwner);
                                return interClassAccess && !allowedOrigin;
                            })
                            .forEach(offendingAccess -> {
                                events.add(SimpleConditionEvent.violated(offendingAccess, offendingAccess.getDescription()));
                            });
                }
            };
        }
    }

    private static class CodeUnitConditions {
        private static ArchCondition<JavaCodeUnit> callMethod(DescribedPredicate<AccessTarget> targetDescriptor) {
            return new ArchCondition<>("contain method call to target " + targetDescriptor.getDescription()) {
                @Override
                public void check(JavaCodeUnit codeUnit, ConditionEvents events) {
                    boolean containsCallToTarget = codeUnit.getMethodCallsFromSelf().stream()
                            .anyMatch(access -> targetDescriptor.apply(access.getTarget()));

                    if (!containsCallToTarget) {
                        String message = codeUnit.getFullName() + " does not contain method call to target";
                        events.add(SimpleConditionEvent.violated(codeUnit, message));
                    }
                }
            };
        }

        private static ArchCondition<JavaCodeUnit> performDirectOrIndirectValidation =
                new ArchCondition<>("perform direct or indirect validation") {
                    @Override
                    public void check(JavaCodeUnit codeUnit, ConditionEvents events) {
                        if (codeUnit.isAnnotatedWith(InputValidator.class)) {
                            // Validation performed by same code unit => condition passed
                            return;
                        }

                        boolean callsValidator = codeUnit.getCallsFromSelf().stream()
                                .map(call -> call.getTarget())
                                .anyMatch(target -> target.isAnnotatedWith(InputValidator.class)
                                        || target.getOwner().isAnnotatedWith(InputValidator.class));
                        if (callsValidator) {
                            // Contains at least one call to a validator => condition passed
                            return;
                        }

                        boolean calledAtLeastOnce = !codeUnit.getAccessesToSelf().isEmpty();
                        boolean onlyCalledByValidators = codeUnit.getAccessesToSelf().stream()
                                .map(call -> call.getOrigin())
                                .allMatch(origin -> origin.isAnnotatedWith(InputValidator.class)
                                        || origin.getOwner().isAnnotatedWith(InputValidator.class));
                        if (calledAtLeastOnce && onlyCalledByValidators) {
                            // Is only called by validators => condition passed
                            return;
                        }

                        String message = codeUnit.getFullName() + " takes user input that is never validated";
                        events.add(SimpleConditionEvent.violated(codeUnit, message));
                    }
                };
    }

    private static class ClassConditions {
        private static ArchCondition<JavaClass> callMethod(DescribedPredicate<AccessTarget> targetDescriptor) {
            return new ArchCondition<>("contain method call to target " + targetDescriptor.getDescription()) {
                @Override
                public void check(JavaClass codeUnit, ConditionEvents events) {
                    boolean containsCallToTarget = codeUnit.getMethodCallsFromSelf().stream()
                            .anyMatch(access -> targetDescriptor.apply(access.getTarget()));

                    if (!containsCallToTarget) {
                        String message = codeUnit.getFullName() + " does not contain method call to target";
                        events.add(SimpleConditionEvent.violated(codeUnit, message));
                    } else {
                        String message = codeUnit.getFullName() + " contains method call to target";
                        events.add(SimpleConditionEvent.satisfied(codeUnit, message));
                    }
                }
            };
        }
    }

    private static class FieldConditions {
        private static ArchCondition<JavaField> notBleedToInsecureComponents =
                new ArchCondition<>("not bleed to insecure components") {
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
                                .flatMap(method -> method.getCallsOfSelf().stream())
                                .filter(call -> !call.getOriginOwner().isAnnotatedWith(AssetHandler.class))
                                .forEach(offendingMethodCall -> {
                                    String message = offendingMethodCall + ": access to asset " + field.getName() + " (via getter method)";
                                    events.add(SimpleConditionEvent.violated(offendingMethodCall, message));
                                });
                    }
                };
    }

    private static class InformationFlow {
        private static Stream<Hint> recurseOnHints(Set<Hint> hints) {
            return recurseOnHints(hints, 5).distinct();
        }

        private static Stream<Hint> recurseOnHints(Set<Hint> hints, int depth) {
            if (depth == 0 || hints.isEmpty()) {
                return hints.stream();
            }

            // Hints with an originating member
            Set<JavaMember> hintOrigins = hints.stream()
                    .filter(hint -> hint.getMemberOrigin() != null)
                    .map(hint -> hint.getMemberOrigin())
                    .collect(Collectors.toSet());

            // Hints flowing into a field
            Stream<Hint> hintsFlowingIntoMembers = hintOrigins.stream()
                    .filter(member -> member instanceof JavaField)
                    .map(member -> (JavaField) member)
                    .flatMap(hint -> hint.getAccessesToSelf().stream())
                    .flatMap(access -> access.getArgumentHints().stream());

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
    }
}
