import java.math.BigDecimal;

class LogSecurityEventsCheck {
    public void foo1()  // Noncompliant
    {

    }

    public void foo2()
    {
        BigDecimal.valueOf(0);
    }
}