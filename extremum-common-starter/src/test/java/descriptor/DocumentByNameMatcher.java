package descriptor;

import org.bson.Document;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author rpuch
 */
public class DocumentByNameMatcher extends TypeSafeMatcher<Document> {
    private final String expectedName;

    public static Matcher<Document> havingName(String expectedName) {
        return hasName(expectedName);
    }

    public static Matcher<Document> hasName(String expectedName) {
        return new DocumentByNameMatcher(expectedName);
    }

    private DocumentByNameMatcher(String expectedName) {
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(Document document) {
        return expectedName.equals(document.get("name"));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Document containing name -> ").appendValue(expectedName);
    }
}
