import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import edu.ncsu.csc.itrust.action.*;

import java.io.PrintStream;

import org.junit.runner.RunWith;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "edu.ncsu.csc.itrust")
public class ExtensionTest {
	@ArchTest
	ArchRule c6 = SecArchUnit.doNotBleedAssetsBetweenComponents();
	
	@ArchTest
	ArchRule c7 = SecArchUnit.doNotLogSecrets(JavaClass.Predicates.belongToAnyOf(
			EventLoggingAction.class,
			PrintStream.class
	));
}
