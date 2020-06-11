import org.junit.runner.RunWith;

import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.core.importer.ImportOption.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "org.mybatis.jpetstore", importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class, DoNotIncludeArchives.class})
public class ExtensionTest {
	@ArchTest
	public static final ArchRule c6 = SecArchUnit.doNotBleedSensitiveAssets();
}
