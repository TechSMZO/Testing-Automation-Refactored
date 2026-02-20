package tests.utils;

import org.testng.annotations.Factory;
import tests.LoginSanityTest;

/**
 * Creates browser-specific test class instances for factory-based runs.
 */
public class CrossBrowserFactory {
    @Factory
    public Object[] createInstances() {
        return new Object[] {

                new LoginSanityTest("chrome"),
                // new LoginSanityTest("edge"),
                // new LoginSanityTest("firefox")

        };
    }

}
