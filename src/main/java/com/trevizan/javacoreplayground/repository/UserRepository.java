package com.trevizan.javacoreplayground.repository;

import com.trevizan.javacoreplayground.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public User save(User user) {
        long id = idGenerator.incrementAndGet();
        User userWithId = user.withId(id);
        store.put(id, userWithId);
        return userWithId;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

}
