// Placeholder for CrossBrowserFactory.java content

// CrossBrowserFactory.java
package tests;
import org.testng.annotations.Factory;
public class CrossBrowserFactory {
	@Factory
	public Object[] createInstances() {
	    return new Object[] {
	    		
//	        new BluedartHubAddress("chrome", "src/test/Pincode4.csv", 1),   
//	        new BluedartHubAddress("chrome", "src/test/Pincode2.csv", 1), 
//	        new BluedartHubAddress("chrome", "src/test/Pincode3.csv", 1),
//	        new BluedartHubAddress("chrome", "src/test/Pincode4.csv", 1),
//	        new BluedartHubAddress("chrome", "src/test/Pincode5.csv", 15001),

	         		  
	         		 new tests.LoginTest("chrome"),
//	                 new tests.LoginTest("edge"),
//	                 new tests.LoginTest("firefox")

	    };
	}

}


