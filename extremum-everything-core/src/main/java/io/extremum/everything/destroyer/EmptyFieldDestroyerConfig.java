package io.extremum.everything.destroyer;

import java.util.List;
import java.util.function.Predicate;

public class EmptyFieldDestroyerConfig {
    private List<String> analyzablePackagePrefixes;
    private List<Predicate<Class<?>>> notAnalyzableTypePredicates;

    public EmptyFieldDestroyerConfig() {
    }

    public EmptyFieldDestroyerConfig(List<String> analyzablePackagePrefixes, List<Predicate<Class<?>>> notAnalyzableTypePredicates) {
        this.analyzablePackagePrefixes = analyzablePackagePrefixes;
        this.notAnalyzableTypePredicates = notAnalyzableTypePredicates;
    }

    public List<String> getAnalyzablePackagePrefixes() {
        return analyzablePackagePrefixes;
    }

    public void setAnalyzablePackagePrefixes(List<String> analyzablePackagePrefixes) {
        this.analyzablePackagePrefixes = analyzablePackagePrefixes;
    }

    public List<Predicate<Class<?>>> getNotAnalyzableTypePredicates() {
        return notAnalyzableTypePredicates;
    }

    public void setNotAnalyzableTypePredicates(List<Predicate<Class<?>>> notAnalyzableTypePredicates) {
        this.notAnalyzableTypePredicates = notAnalyzableTypePredicates;
    }
}
