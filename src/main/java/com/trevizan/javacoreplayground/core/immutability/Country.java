package com.trevizan.javacoreplayground.core.immutability;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Country {

    private final String name;
    private final String code;
    private final List<String> states;
    private final Set<String> regions;
    private final Map<String, String> currencies;

    public Country(
        String name, String code, List<String> states, Set<String> regions, Map<String, String> currencies
    ) {
        this.name = name;
        this.code = code;
        // defensive copy, creates a new immutable list.
        // use List.copyOf instead of unmodifiableList, since it prevents external mutations
        this.states = List.copyOf(states);
        this.regions = Set.copyOf(regions);
        this.currencies = Map.copyOf(currencies);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public List<String> getStates() {
        // already immutable
        return states;
    }

    public Set<String> getRegions() {
        return regions;
    }

    public Map<String, String> getCurrencies() {
        return currencies;
    }

}
