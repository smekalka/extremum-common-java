package io.extremum.common.slug;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class DefaultSlugGenerationStrategy implements SlugGenerationStrategy {

    private final Integer capacity;

    @Override
    public List<String> generate(String name) {
        String[] words = name.split(" ");
        ArrayList<String> result = new ArrayList<>();

        for (int length = 1; length <= Arrays.stream(words).map(String::length).mapToInt(i -> i).max().getAsInt(); length++) {
            for (int startIndex = 1; startIndex <= words.length; startIndex++) {
                String slug = generate(Arrays.asList(words), startIndex, length);
                if (!result.contains(slug)) {
                    result.add(slug);
                }
            }
        }

        if (result.size() > capacity) {
            return result.subList(0, capacity);
        } else {
            int remaining = capacity - result.size();
            String lastSlug = result.get(result.size() - 1);
            for(int i=0; i<=remaining; i++){
                result.add(lastSlug+"-"+i);
            }
        }

        return result;
    }


    private static String generate(List<String> words, int startIndex, int length) {
        StringBuilder slug = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String currentWord = words.get(i);
            if (i < startIndex) {
                if (length > currentWord.length()) {
                    slug.append(getSlugPart(currentWord, 0, currentWord.length(), isLastWord(i, words.size())));

                } else {
                    slug.append(getSlugPart(currentWord, 0, length, isLastWord(i, words.size())));
                }
            } else {
                if (length == 1) {
                    slug.append(getSlugPart(currentWord, 0, 1, isLastWord(i, words.size())));
                } else {
                    int wordLength = currentWord.length();
                    if (length - 1 <= wordLength) {
                        slug.append(getSlugPart(currentWord, 0, length - 1, isLastWord(i, words.size())));
                    } else {
                        slug.append(getSlugPart(currentWord, 0, wordLength, isLastWord(i, words.size())));
                    }

                }
            }
        }

        return slug.toString().toLowerCase();
    }


    private static boolean isLastWord(int wordIndex, int totalWords) {
        return wordIndex == totalWords - 1;
    }

    private static String getSlugPart(String currentWord, int startIndex, int endIndex, boolean lastWord) {
        String result = currentWord.substring(startIndex, endIndex);
        if (result.length() > 1 && !lastWord) {
            result += "-";
        }

        return result;
    }
}
