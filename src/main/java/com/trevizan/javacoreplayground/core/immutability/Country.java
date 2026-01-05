package com.trevizan.javacoreplayground.core.immutability;

import java.util.ArrayList;
import java.util.List;

public final class Country {

    private final String name;
    private final String code;
    private final List<String> states;

    public Country(String name, String code, List<String> states) {
        this.name = name;
        this.code = code;
        // defensive copy, creates a new immutable list.
        // use List.copyOf instead of unmodifiableList, since it prevents external mutations
        this.states = List.copyOf(states);
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

    public static void main(String[] args) {
        List<String> states = new ArrayList<>();
        states.add("São Paulo");
        states.add("Rio de Janeiro");

        Country country = new Country("Brazil", "BR", states);
        System.out.println("states: " + country.getStates());

        states.add("Minas Gerais");
        System.out.println("states again: " + country.getStates());

        // thread-safe example
        Runnable task = () ->
            System.out.println(Thread.currentThread().getName() + ": " + country.getStates());

        for (int i = 0; i < 5; i++) {
            new Thread(task).start();
        }

        // attempt to modify retrieved list throws exception
        List<String> retrieved = country.getStates();
        try {
            retrieved.add("Pernambuco");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify internal list via getter: " + e);
        }
    }

}
