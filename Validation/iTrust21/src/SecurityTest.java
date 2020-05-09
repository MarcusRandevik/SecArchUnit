import org.junit.runner.RunWith;

import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;

import edu.ncsu.csc.itrust.action.EventLoggingAction;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "edu.ncsu.csc.itrust", importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class, DoNotIncludeArchives.class})
public class SecurityTest {
	@ArchTest
	ArchRule c1 = SecArchUnit.logSecurityEvents(JavaClass.Predicates.belongToAnyOf(
					
			), EventLoggingAction.class);
	
	@ArchTest
	ArchRule c3 = SecArchUnit.sendOutboundMessagesFromCentralPoint(null, JavaClass.Predicates.belongToAnyOf(
			
	));
	
	@ArchTest
	ArchRule c4 = SecArchUnit.validateUserInput();
	
	@ArchTest
	ArchRule c5 = SecArchUnit.limitResourceAllocation();
}
