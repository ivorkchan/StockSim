package data_transfer.cache;

import entity.User;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** A user cache with thread-safe LRU algorithm. */
public class UserCache {

    private final int MAX_CAPACITY;
    private final Map<String, User> users;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public UserCache(int maxCapacity) {
        MAX_CAPACITY = maxCapacity;
        users = new LinkedHashMap<>();
    }

    // add a new user
    public void add(User user) {
        lock.writeLock().lock();
        try {
            users.put(user.getUsername(), user);
            if (users.size() > MAX_CAPACITY) {
                // remove the least recently used user from the tail of the list
                users.remove(users.entrySet().iterator().next().getKey());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // update an existing user
    public void updateUser(User user) {
        lock.writeLock().lock();
        try {
            // remove the user and re-insert it to update its position (move to head)
            users.remove(user.getUsername());
            // add the updated user to the head of the list
            users.put(user.getUsername(), user);

            if (users.size() > MAX_CAPACITY) {
                // remove the least recently used user (at the tail)
                users.remove(users.entrySet().iterator().next().getKey());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // get a user by username
    public User get(String username) {
        lock.readLock().lock();
        try {
            return users.get(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    // check if the user exists in the cache
    public boolean contains(String username) {
        lock.readLock().lock();
        try {
            return users.containsKey(username);
        } finally {
            lock.readLock().unlock();
        }
    }
}
