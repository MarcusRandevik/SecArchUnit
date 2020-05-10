import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import edu.ncsu.csc.itrust.beans.PatientBean;
import org.junit.runner.RunWith;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "edu.ncsu.csc.itrust")
public class Debug {
    @ArchTest
    public static ArchRule rule = SecArchUnit.dumpHints(
            PatientBean.class
    );
}
