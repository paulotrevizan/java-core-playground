package com.trevizan.javacoreplayground.core.immutability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CountryImmutabilityDemo {

    public static void main(String[] args) {
        List<String> states = new ArrayList<>();
        states.add("São Paulo");
        states.add("Rio de Janeiro");

        Set<String> regions = new HashSet<>();
        regions.add("Norte");
        regions.add("Sudeste");

        Map<String, String> currencies = new HashMap<>();
        currencies.put("BRL", "Real");
        currencies.put("USD", "Dolar");

        Country country = new Country("Brazil", "BR", states, regions, currencies);
        System.out.println("states: " + country.getStates());
        System.out.println("regions: " + country.getRegions());
        System.out.println("currencies: " + country.getCurrencies().entrySet());

        states.add("Minas Gerais");
        System.out.println("states again: " + country.getStates());

        regions.add("Centro Oeste");
        System.out.println("regions again: " + country.getRegions());

        currencies.put("GBP", "Pounds");
        System.out.println("currencies: " + country.getCurrencies().entrySet());

        // thread-safe example
        Runnable task = () -> {
            System.out.println(Thread.currentThread().getName() + ": " + country.getStates());
            System.out.println(Thread.currentThread().getName() + ": " + country.getRegions());
            System.out.println(Thread.currentThread().getName() + ": " + country.getCurrencies().entrySet());
        };

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
