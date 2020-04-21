import org.junit.runner.RunWith;

import com.github.secarchunit.SecArchUnit;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;

import atm.physical.CardReader;
import atm.physical.CashDispenser;
import atm.physical.EnvelopeAcceptor;
import atm.physical.Log;
import atm.physical.NetworkToBank;
import atm.transaction.Transaction;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = {""}, importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class, DoNotIncludeArchives.class})
public class SecurityTest {
	// -- BASIC CONSTRAINTS --
	
	@ArchTest
    public static final ArchRule logSecurityEvents = SecArchUnit.logSecurityEvents(
    		JavaClass.Predicates.belongToAnyOf(
	    		CardReader.class,
	    		CashDispenser.class,
	    		EnvelopeAcceptor.class,
	    		NetworkToBank.class,
	    		Transaction.class
	    	), Log.class);
	
	@ArchTest
    public static final ArchRule enforceAuthenticationAtCentralPoint =
    		SecArchUnit.enforceAuthenticationAtCentralPoint(
    				Transaction.class,
    				Transaction.class);
	
	@ArchTest
    public static final ArchRule enforceAuthorizationAtCentralPoint =
    		SecArchUnit.enforceAuthorizationAtCentralPoint(
    				Transaction.class,
    				Transaction.class);
	
	@ArchTest
    public static final ArchRule sendMessagesFromCentralPoint =
    		SecArchUnit.sendOutboundMessagesFromCentralPoint(
    				Transaction.class,
		    		JavaClass.Predicates.belongToAnyOf(NetworkToBank.class));
    
	// -- ANNOTATION CONSTRAINTS --

    @ArchTest
    public static final ArchRule validateUserInput =
    		SecArchUnit.validateUserInput();

    @ArchTest
    public static final ArchRule limitResourceAllocation =
    		SecArchUnit.limitResourceAllocation();
}
