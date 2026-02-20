package tests.utils;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Runs the cross-browser factory through TestNG's programmatic suite API.
 */
public class TestSuiteRunner {

    public static void main(String[] args) {
        System.out.println("inside testrunner");

        // Create TestNG engine.
        TestNG testng = new TestNG();

        // Create suite and configure parallel execution.
        XmlSuite suite = new XmlSuite();
        suite.setName("ParallelExecutionSuite");
        // suite.setParallel(XmlSuite.ParallelMode.CLASSES);
        suite.setParallel(XmlSuite.ParallelMode.INSTANCES);
        suite.setThreadCount(10);

        // Create one test block that points to the factory class.
        XmlTest test = new XmlTest(suite);
        test.setName("CrossBrowserTests");
        test.setXmlClasses(Collections.singletonList(new XmlClass("tests.utils.CrossBrowserFactory")));

        // Set suite and execute.
        testng.setXmlSuites(Collections.singletonList(suite));
        testng.run();
    }
}

