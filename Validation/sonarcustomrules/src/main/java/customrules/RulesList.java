package customrules;

import customrules.checks.*;
import org.sonar.plugins.java.api.JavaCheck;

import java.util.*;


public final class RulesList {

    private RulesList() {

    }

    public static List<Class<? extends JavaCheck>> getChecks() {
        List<Class<? extends JavaCheck>> checks = new ArrayList<>();
        checks.addAll(getJavaChecks());
        checks.addAll(getJavaTestChecks());
        return Collections.unmodifiableList(checks);
    }


    public static List<Class<? extends JavaCheck>> getJavaChecks() {
        return Collections.unmodifiableList(Arrays.asList(
                LogSecurityEventsRule.class,
                ValidateUserInputRule.class,
                LimitThreadSpawnRule.class,
                CentralMessageRule.class
        ));
    }

    public static List<Class<? extends JavaCheck>> getJavaTestChecks() {
        return Collections.emptyList();
    }
}
