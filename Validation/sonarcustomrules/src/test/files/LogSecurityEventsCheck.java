class LogSecurityEventsCheck {
    int     foo1() { return 0; }
    void    foo2(int value) { }
    int     foo3(int value) { return 0; } // Noncompliant
    Object  foo4(int value) { return null; }

    int     foo6(int value, String name) { return 0; }
    int     foo7(int ... values) { return 0;}
}