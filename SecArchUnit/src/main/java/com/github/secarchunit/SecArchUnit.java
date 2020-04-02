package com.github.secarchunit;

import com.github.secarchunit.concepts.*;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.lang.*;

import java.lang.annotation.Annotation;

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
        // TODO
        return null;
    }

    public static ArchRule doNotLogSecrets(DescribedPredicate<? super JavaClass> loggerDescriptor) {
        return noClasses()
                .should().callMethodWhere(targetOwner(loggerDescriptor).and(passArgument(annotatedWith(Secret.class))));
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

    private static DescribedPredicate<JavaAccess<?>> passArgument(DescribedPredicate<? super JavaClass> argumentDescriptor) {
        return new DescribedPredicate<>("pass argument whose class is " + argumentDescriptor.getDescription()) {
            @Override
            public boolean apply(JavaAccess<?> access) {
                boolean passesArgument = access.getArgumentHints().stream()
                        .anyMatch(hint -> argumentDescriptor.apply(hint.getJavaClass()));

                return passesArgument;
            }
        };
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
}
