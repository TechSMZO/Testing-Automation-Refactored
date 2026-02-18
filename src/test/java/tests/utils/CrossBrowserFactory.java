// Placeholder for CrossBrowserFactory.java content

// CrossBrowserFactory.java
package tests.utils;

import org.testng.annotations.Factory;
import tests.LoginTest;

public class CrossBrowserFactory {
	@Factory
	public Object[] createInstances() {
		return new Object[] {

				new LoginTest("chrome"),
				// new LoginTest("edge"),
				// new LoginTest("firefox")

		};
	}

}
