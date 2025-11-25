package Multithreading.Problems;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Entry node for each key-value pair
class Entry<K, V> {
    K key;
    V value;
    Entry<K, V> next;

    Entry(K k, V v) {
        key = k;
        value = v;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}

// Custom Concurrent HashMap-like implementation
public class MyHashMap<K, V> {

    private static final int INITIAL_SIZE = 1 << 4; // 16
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private final Entry<K, V>[] hashTable;
    private final ReadWriteLock[] locks; // One lock per bucket

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        hashTable = new Entry[INITIAL_SIZE];
        locks = new ReadWriteLock[INITIAL_SIZE];
        for (int i = 0; i < INITIAL_SIZE; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity) {
        int tableSize = tableSizeFor(capacity);
        hashTable = new Entry[tableSize];
        locks = new ReadWriteLock[tableSize];
        for (int i = 0; i < tableSize; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    // Utility to compute nearest power of 2 >= capacity
    private int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private int getIndex(Object key) {
        return Math.abs(key.hashCode() % hashTable.length);
    }

    // -------------------------
    // Core Operations
    // -------------------------

    public void put(K key, V value) {
        int index = getIndex(key);
        ReadWriteLock rwLock = locks[index];
        rwLock.writeLock().lock();
        try {
            Entry<K, V> node = hashTable[index];
            if (node == null) {
                hashTable[index] = new Entry<>(key, value);
                return;
            }
            Entry<K, V> prev = null;
            while (node != null) {
                if (node.key.equals(key)) {
                    node.value = value; // Update existing
                    return;
                }
                prev = node;
                node = node.next;
            }
            prev.next = new Entry<>(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public V get(K key) {
        int index = getIndex(key);
        ReadWriteLock rwLock = locks[index];
        rwLock.readLock().lock();
        try {
            Entry<K, V> node = hashTable[index];
            while (node != null) {
                if (node.key.equals(key)) {
                    return node.value;
                }
                node = node.next;
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public V remove(K key) {
        int index = getIndex(key);
        ReadWriteLock rwLock = locks[index];
        rwLock.writeLock().lock();
        try {
            Entry<K, V> node = hashTable[index];
            Entry<K, V> prev = null;
            while (node != null) {
                if (node.key.equals(key)) {
                    if (prev == null) {
                        hashTable[index] = node.next;
                    } else {
                        prev.next = node.next;
                    }
                    return node.value;
                }
                prev = node;
                node = node.next;
            }
            return null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public int size() {
        int count = 0;
        for (int i = 0; i < hashTable.length; i++) {
            ReadWriteLock rwLock = locks[i];
            rwLock.readLock().lock();
            try {
                Entry<K, V> node = hashTable[i];
                while (node != null) {
                    count++;
                    node = node.next;
                }
            } finally {
                rwLock.readLock().unlock();
            }
        }
        return count;
    }

    // -------------------------
    // Test Driver
    // -------------------------
    public static void main(String[] args) throws InterruptedException {
        MyHashMap<Integer, String> map = new MyHashMap<>(16);

        // Multiple writer threads
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    int key = threadId * 100 + j;
                    map.put(key, "Thread-" + threadId + "-Value-" + j);
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Total entries: " + map.size());
        System.out.println("Sample value for key 250: " + map.get(250));

        // Manual testing
        map.put(1, "hi");
        map.put(2, "my");
        map.put(3, "name");
        map.put(4, "is");
        map.put(5, "Venkat");

        System.out.println("Key 3 => " + map.get(3));
        map.remove(2);
        System.out.println("After removing key 2, size: " + map.size());
    }
}