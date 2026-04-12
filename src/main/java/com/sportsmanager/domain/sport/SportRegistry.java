package com.sportsmanager.domain.sport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Central registry for all available sports.
 * Populated at startup; UI reads this list to offer sport selection.
 */
public final class SportRegistry {

    private final List<Sport> registeredSports = new ArrayList<>();

    /**
     * Register a sport so it becomes available in the game.
     *
     * @param sport the sport to register
     * @throws IllegalArgumentException if a sport with the same name is already registered
     */
    public void register(Sport sport) {
        if (sport == null) {
            throw new IllegalArgumentException("Sport cannot be null");
        }
        boolean duplicate = registeredSports.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(sport.getName()));
        if (duplicate) {
            throw new IllegalArgumentException("Sport already registered: " + sport.getName());
        }
        registeredSports.add(sport);
    }

    /**
     * @return unmodifiable list of all registered sports
     */
    public List<Sport> getAll() {
        return Collections.unmodifiableList(registeredSports);
    }

    /**
     * Find a registered sport by name (case-insensitive).
     */
    public Optional<Sport> findByName(String name) {
        return registeredSports.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * @return number of registered sports
     */
    public int size() {
        return registeredSports.size();
    }
}