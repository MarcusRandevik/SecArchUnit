import org.junit.runner.RunWith;

import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;

import atm.physical.Log;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = {""}, importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class, DoNotIncludeArchives.class})
public class ExtensionTest {
	// -- EXTENSION CONSTRAINTS --
	
	@ArchTest
	public static final ArchRule c6 = SecArchUnit.doNotBleedSensitiveAssets();
	
	@ArchTest
	public static final ArchRule c7 = SecArchUnit.doNotLogSecrets(JavaClass.Predicates.type(Log.class));
}
