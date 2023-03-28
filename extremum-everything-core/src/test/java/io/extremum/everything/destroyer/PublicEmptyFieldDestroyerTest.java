package io.extremum.everything.destroyer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;

class PublicEmptyFieldDestroyerTest {
    @Test
    public void notEmptyFieldsNotDestroyingTest() {
        EmptyFieldDestroyer destroyer = new PublicEmptyFieldDestroyer(
                new EmptyFieldDestroyerConfig(singletonList("io.extremum.everything"), null)
        );

        ContainsTestObjectClass test = new ContainsTestObjectClass();
        test.to = new TestObject("a");

        destroyer.destroy(test);

        Assertions.assertNotNull(test.getTo());
    }

    @Test
    public void emptyFieldIsDestroying() {
        EmptyFieldDestroyer destroyer = new PublicEmptyFieldDestroyer(
                new EmptyFieldDestroyerConfig(singletonList("io.extremum.everything"), null)
        );

        ContainsTestObjectClass test = new ContainsTestObjectClass();
        test.to = new TestObject();

        Assertions.assertNotNull(test.getTo());

        destroyer.destroy(test);

        Assertions.assertNull(test.getTo());
    }

    @Test
    public void emptyFieldNotDestroyedByPredicate() {
        EmptyFieldDestroyer destroyer = new PublicEmptyFieldDestroyer(
                new EmptyFieldDestroyerConfig(
                        singletonList("io.extremum.everything"),
                        singletonList(c -> c.isAssignableFrom(TestObject.class))
                )
        );

        ContainsTestObjectClass test = new ContainsTestObjectClass();
        test.to = new TestObject();

        Assertions.assertNotNull(test.getTo());

        destroyer.destroy(test);

        Assertions.assertNotNull(test.getTo());
    }

    public static class ContainsTestObjectClass {
        public TestObject to;

        public TestObject getTo() {
            return to;
        }

        public void setTo(TestObject to) {
            this.to = to;
        }
    }

    public static class TestObject {
        public String a;

        public TestObject() {
        }

        public TestObject(String a) {
            this.a = a;
        }

        public String getA() {
            return a;
        }
    }
}