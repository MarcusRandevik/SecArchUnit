package customrules;

import org.sonar.api.Plugin;

/**
 * Entry point of your plugin containing your custom rules
 */
// Copied class from example
public class SecurityRulesPlugin implements Plugin {

    @Override
    public void define(Context context) {
        // server extensions -> objects are instantiated during server startup
        context.addExtension(MyRulesDefinition.class);

        // batch extensions -> objects are instantiated during code analysis
        context.addExtension(SecurityRulesFileCheckRegistrar.class);
    }
}
