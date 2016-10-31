package ssl.bluetruth.sat;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 *
 * @author pwood
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        format = {
            "pretty",
            "junit:build/sat-report.xml",
            "json:build/sat-report.json"
        }, 
        features = "classpath:ssl/bluetruth/sat/features/",
        glue = "classpath:ssl/bluetruth/sat/features/steps/", 
        strict = true)
public class SatTest {
}
