package ssl.bluetruth.fat;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 *
 * @author pwood
 */
@RunWith(Cucumber.class)
@CucumberOptions(
//        tags={"@pete"},
        format = {
            "pretty",
            "junit:build/fat-report.xml",
            "json:build/fat-report.json",
            "html:build/fat-report-html"
        },
        features = "classpath:ssl/bluetruth/fat/features/",
        glue="classpath:ssl/bluetruth/fat/features/steps/",
        strict = true)
public class FatTest {
}
