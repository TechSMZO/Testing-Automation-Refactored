// Placeholder for CrossBrowserFactory.java content

// CrossBrowserFactory.java
package tests;

import org.testng.annotations.Factory;

public class CrossBrowserFactory {
	@Factory
	public Object[] createInstances() {
		return new Object[] {

				new tests.LoginTest("chrome"),
				// new tests.LoginTest("edge"),
				// new tests.LoginTest("firefox")

		};
	}

}
