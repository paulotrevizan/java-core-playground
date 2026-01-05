package com.trevizan.javacoreplayground.core.streams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionsStreamsSnippet {

    public static void main(String[] args) {
        List<String> countries = new ArrayList<>();
        countries.add("Brazil");
        countries.add("Portugal");
        countries.add("United Kingdom");
        countries.add("Brazil");

        // filter + forEach: readable for short pipelines, not for control flow
        countries.stream()
            .filter(c -> c.equals("Brazil"))
            .forEach(System.out::println);

        // map transformation
        List<Integer> lengths = countries.stream()
            .map(String::length)
            .toList();
        System.out.println(lengths);

        Set<String> countriesSet = new HashSet<>();
        countriesSet.add("Brazil");
        countriesSet.add("Portugal");
        countriesSet.add("United Kingdom");
        countriesSet.add("Brazil");

        countriesSet.stream()
            .filter(c -> c.startsWith("U"))
            .forEach(System.out::println);

        Map<String, String> countriesMap = new HashMap<>();
        countriesMap.put("BR", "Brazil");
        countriesMap.put("PT", "Portugal");
        countriesMap.put("UK", "United Kingdom");
        // overwrites previous
        countriesMap.put("BR", "Brasil");

        // always use get(key) if the key is known
        // Stream here is overkill
        String country = countriesMap.get("PT");
        System.out.println(country);

        // use stream when key/value operations are small and clear
        countriesMap.entrySet().stream()
            .filter(e -> e.getKey().equals("BR"))
            .map(Map.Entry::getValue)
            .forEach(System.out::println);

        // stream vs for-loop:
        // stream = readable
        countries.stream()
            .filter(c -> c.length() > 6)
            .map(String::toUpperCase)
            .forEach(System.out::println);

        // for = controllable/debuggable
        for (String c : countries) {
            if (c.length() > 6) {
                System.out.println(c.toUpperCase());
            }
        }

        // reminder:
        // use streams for short, clear transformations
        // use loops when controlling flow, debugging or in performance-critical paths
    }

}
