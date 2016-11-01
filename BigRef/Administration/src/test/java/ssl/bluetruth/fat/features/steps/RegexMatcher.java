package ssl.bluetruth.fat.features.steps;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 *
 * @author pwood
 */
public class RegexMatcher extends BaseMatcher {
    private final String regex;

    public RegexMatcher(String regex) {
        this.regex = regex;
    }

    public boolean matches(Object object) {
        return ((String)object).matches(regex);
    }

    public void describeTo(Description description) {
        description.appendText("matches regex="+regex);
    }
    
    public static RegexMatcher matches(String regex) {
        return new RegexMatcher(regex);
    }
}
