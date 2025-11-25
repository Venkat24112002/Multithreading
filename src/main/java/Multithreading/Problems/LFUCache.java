package Multithreading.Problems;


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

class LFUCache {
    // 🍪 Each "cookie" (entry) in our cache
    class Node {
        int key;     // Cookie ID (e.g., "Chocolate Chip")
        int value;   // Actual cookie 🍪
        int freq;    // Number of stickers (how often eaten)

        Node(int key, int value) {
            this.key = key;
            this.value = value;
            this.freq = 1;  // New cookie starts with 1 sticker
        }
    }

    private final int capacity;  // Max cookies the jar can hold
    private final Map<Integer, Node> cache;  // Tracks all cookies by ID
    private final Map<Integer, LinkedHashSet<Node>> freqMap;  // Groups cookies by sticker count
    private int minFreq;  // The smallest sticker count in the jar

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();          // Empty jar
        this.freqMap = new HashMap<>();       // Empty sticker catalog
        this.minFreq = 0;                     // No stickers yet!
    }

    // 🍴 Eat a cookie (get its value)
    public int get(int key) {
        if (!cache.containsKey(key)) return -1;  // Cookie not found 😢
        Node node = cache.get(key);
        increaseFreq(node);  // Add a sticker (it got eaten again!)
        return node.value;   // Yum! 🍪
    }

    // 🛒 Buy a new cookie (add to jar)
    public void put(int key, int value) {
        if (capacity <= 0) return;  // No jar? No cookies! 🚫

        // Cookie already in jar? Just update its flavor
        if (cache.containsKey(key)) {
            Node node = cache.get(key);
            node.value = value;      // New recipe!
            increaseFreq(node);      // Add a sticker
            return;
        }

        // Jar full? Toss the least popular cookie!
        if (cache.size() >= capacity) evict();

        // Add new cookie to jar
        Node newNode = new Node(key, value);
        cache.put(key, newNode);
        freqMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(newNode);  // 1 sticker
        minFreq = 1;  // New cookies always start with 1 sticker
    }

    // 📈 Add a sticker to a cookie
    private void increaseFreq(Node node) {
        int oldFreq = node.freq;
        freqMap.get(oldFreq).remove(node);  // Remove from old sticker group

        int newFreq = oldFreq + 1;          // New sticker count
        node.freq = newFreq;
        freqMap.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(node);  // Add to new group

        // If the old group is now empty AND it was the min, update minFreq
        if (freqMap.get(oldFreq).isEmpty() && oldFreq == minFreq) {
            minFreq++;
        }
    }

    // 🗑️ Toss the least popular cookie
    private void evict() {
        LinkedHashSet<Node> minFreqSet = freqMap.get(minFreq);
        Node nodeToEvict = minFreqSet.iterator().next();  // Oldest in least-sticker group
        minFreqSet.remove(nodeToEvict);
        cache.remove(nodeToEvict.key);  // Goodbye, sad cookie 😢
    }
}