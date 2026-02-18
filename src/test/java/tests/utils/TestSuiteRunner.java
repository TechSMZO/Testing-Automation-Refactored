// Placeholder for ProgrammaticTestRunner.java content
package tests.utils;

import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.util.Collections;

public class TestSuiteRunner {
	
	
    public static void main(String[] args) {
    	
    	System.out.println("inside testrunner");
    	// Create TestNG instance
        TestNG testng = new TestNG();

        // Create suite
        XmlSuite suite = new XmlSuite();
        suite.setName("ParallelExecutionSuite");
//        suite.setParallel(XmlSuite.ParallelMode.CLASSES); // Run each class instance in parallel one by one
        suite.setParallel(XmlSuite.ParallelMode.INSTANCES); // Run each class instance simultaneously at once altogether
        suite.setThreadCount(10);

        // Create test
        XmlTest test = new XmlTest(suite);
        test.setName("CrossBrowserTests");
        test.setXmlClasses(Collections.singletonList(new XmlClass("tests.utils.CrossBrowserFactory")));

        // Set suite and run
        testng.setXmlSuites(Collections.singletonList(suite));
        testng.run();

    }
}

