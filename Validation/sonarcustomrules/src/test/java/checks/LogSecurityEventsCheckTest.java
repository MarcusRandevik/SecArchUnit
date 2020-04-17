package checks;

import customrules.checks.LogSecurityEventsRule;
import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class LogSecurityEventsCheckTest {

    @Test
    public void test() {

        JavaCheckVerifier.newVerifier()
                .onFile("src/test/files/LogSecurityEventsCheck.java")
                .withCheck(new LogSecurityEventsRule())
                .verifyIssues();
    }
}
