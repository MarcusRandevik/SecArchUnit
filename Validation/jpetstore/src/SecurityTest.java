import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.mybatis.jpetstore.service.AccountService;
import org.mybatis.jpetstore.service.OrderService;
import org.mybatis.jpetstore.web.actions.AbstractActionBean;
import org.mybatis.jpetstore.web.actions.AccountActionBean;
import org.mybatis.jpetstore.web.actions.OrderActionBean;

import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;

import net.sourceforge.stripes.action.ActionBeanContext;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "org.mybatis.jpetstore", importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class, DoNotIncludeArchives.class})
public class SecurityTest {
	@ArchTest
	public static final ArchRule c1 = SecArchUnit.logSecurityEvents(JavaClass.Predicates.belongToAnyOf(AccountService.class, OrderService.class), Logger.class);

	@ArchTest
	public static final ArchRule c2a = SecArchUnit.enforceAuthenticationAtCentralPoint(AccountActionBean.class, AccountActionBean.class);

	@ArchTest
	public static final ArchRule c2b = SecArchUnit.enforceAuthorizationAtCentralPoint(OrderActionBean.class, OrderActionBean.class);

	@ArchTest
	public static final ArchRule c3 = SecArchUnit.sendOutboundMessagesFromCentralPoint(AbstractActionBean.class, JavaClass.Predicates.type(ActionBeanContext.class));

	@ArchTest
	public static final ArchRule c4 = SecArchUnit.validateUserInput();

	@ArchTest
	public static final ArchRule c5 = SecArchUnit.limitResourceAllocation();
}
