package checks;

import customrules.checks.AuthSingleComponentEnforcerRule;
import customrules.checks.AuthSingleComponentRule;
import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;

public class AuthSingleComponentCheckTest {

    @Test
    public void firstTest() {
        JavaCheckVerifier.newVerifier()
                .onFile("src/test/files/AuthSingleComponentCheck.java")
                .withCheck(new AuthSingleComponentRule())
                .verifyIssues();
    }

    @Test
    public void secondTest() {
        JavaCheckVerifier.newVerifier()
                .onFile("src/test/files/AuthSingleComponentFailCheck.java")
                .withCheck(new AuthSingleComponentRule())
                .verifyNoIssues();
    }
}
