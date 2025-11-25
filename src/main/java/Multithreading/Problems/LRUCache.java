package Multithreading.Problems;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;


class LRUCache {
    class Node {
        public Node next;
        public Node prev;
        public int value;
        public int key;

        public Node(int key, int value) {
            this.key = key;
            this.value = value;
            next = prev = null;
        }
    }


    private Node head;
    private Node tail;
    private int capacity;
    Map<Integer,Node> keyToNode;
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    Lock readLock = lock.readLock();
    Lock writeLock = lock.writeLock();
    private final StampedLock sl = new StampedLock();


    public LRUCache(int capacity){
        head = new Node(-1,-1);
        tail = new Node(-1,-1);
        head.next = tail;
        tail.prev = head;
        this.capacity = capacity;
        keyToNode = new HashMap<>();
    }

    public int get(int key){
        readLock.lock();
        Node node = keyToNode.get(key);
        readLock.unlock();
        if(node == null) return -1;
        try {
            writeLock.lock();
            node = keyToNode.get(key);// checkin again if some evcition happened
            if(node == null) return -1;
            moveToFront(node);
            return node.value;
        } finally {
            writeLock.unlock();
        }
    }

    public void put(int key,int value){
        try{
            writeLock.lock();
            if(keyToNode.containsKey(key)) {
                Node node = keyToNode.get(key);
                node.value = value;
                moveToFront(node);
            } else {
                if(keyToNode.size() == capacity){
                    keyToNode.remove(tail.prev.key);
                    remove(tail.prev);
                }
                Node newNode = new Node(key,value);
                keyToNode.put(key,newNode);
                add(newNode);
            }
        }  finally {
            writeLock.unlock();
        }
    }

    public void add(Node node){
        Node afterHead = head.next;
        head.next = node;
        node.next = afterHead;
        node.prev = head;
        afterHead.prev = node;
    }

    public void remove(Node node){
        Node prev = node.prev;
        Node next = node.next;
        prev.next = next;
        next.prev = prev;
    }

    public void moveToFront(Node node){
        remove(node);
        add(node);
    }

    public int getReadHeavy(int key){
        long stamp = sl.tryOptimisticRead();
        Node node = keyToNode.get(key);

        if(!sl.validate(stamp)){
            stamp = sl.readLock();
            node = keyToNode.get(key);
        }

        if(node == null){
            if(sl.isReadLocked()) {
                sl.unlockRead(stamp);
            }
            return -1;
        }

        long writeStamp = sl.tryConvertToWriteLock(stamp);
        if(writeStamp == 0){
            if(sl.isReadLocked()){
                sl.unlockRead(stamp);
            }
            writeStamp = sl.writeLock();
            node = keyToNode.get(key);
            if(node == null) {
                sl.unlockWrite(writeStamp);
                return -1;
            }
        }
        try {
            moveToFront(node);
            return node.value;
        } finally {
            sl.unlockWrite(writeStamp);
        }
    }



//    public void put(int key, int value) {
//        long stamp = sl.writeLock();
//        try {
//            Node node = map.get(key);
//            if (node != null) {
//                node.value = value;
//                moveToFront(node);
//                return;
//            }
//
//            if (map.size() == capacity) {
//                Node lru = tail.prev;
//                remove(lru);
//                map.remove(lru.key);
//            }
//
//            Node newNode = new Node(key, value);
//            addFirst(newNode);
//            map.put(key, newNode);
//        } finally {
//            sl.unlockWrite(stamp);
//        }
//    }
}




//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.locks.StampedLock;
//
//public class LruCacheReadHeavy {
//    private final int capacity;
//    private final Map<Integer, Node> map;
//    private final Node head, tail;
//    private final StampedLock sl = new StampedLock();
//
//    static class Node {
//        int key, value;
//        Node prev, next;
//        Node(int k, int v) { key = k; value = v; }
//    }
//
//    public LruCacheReadHeavy(int capacity) {
//        this.capacity = capacity;
//        this.map = new HashMap<>(capacity);
//        this.head = new Node(0, 0);
//        this.tail = new Node(0, 0);
//        head.next = tail;
//        tail.prev = head;
//    }
//
//    public int get(int key) {
//        long stamp = sl.tryOptimisticRead();
//        Node node = map.get(key);
//        int value = (node == null) ? -1 : node.value;
//
//        if (!sl.validate(stamp)) {
//            // Fallback to pessimistic read
//            stamp = sl.readLock();
//            try {
//                node = map.get(key);
//                value = (node == null) ? -1 : node.value;
//            } finally {
//                sl.unlockRead(stamp);
//            }
//        }
//
//        // Only promote to write if we need to update order
//        if (value != -1) {
//            long writeStamp = sl.tryConvertToWriteLock(stamp);
//            if (writeStamp != 0L) {
//                stamp = writeStamp;
//                moveToFront(node);
//            } else {
//                sl.unlock(stamp);
//                stamp = sl.writeLock();
//                try {
//                    node = map.get(key);
//                    if (node != null) {
//                        moveToFront(node);
//                        value = node.value;
//                    } else {
//                        value = -1;
//                    }
//                } finally {
//                    sl.unlockWrite(stamp);
//                }
//            }
//        }
//        return value;
//    }
//
//    public void put(int key, int value) {
//        long stamp = sl.writeLock();
//        try {
//            Node node = map.get(key);
//            if (node != null) {
//                node.value = value;
//                moveToFront(node);
//                return;
//            }
//
//            if (map.size() == capacity) {
//                Node lru = tail.prev;
//                remove(lru);
//                map.remove(lru.key);
//            }
//
//            Node newNode = new Node(key, value);
//            addFirst(newNode);
//            map.put(key, newNode);
//        } finally {
//            sl.unlockWrite(stamp);
//        }
//    }
//
//    // --- DLL Helpers (must be called under write lock) ---
//    private void moveToFront(Node node) {
//        remove(node);
//        addFirst(node);
//    }
//
//    private void remove(Node node) {
//        node.prev.next = node.next;
//        node.next.prev = node.prev;
//    }
//
//    private void addFirst(Node node) {
//        node.next = head.next;
//        node.prev = head;
//        head.next.prev = node;
//        head.next = node;
//    }
//
//    public int size() {
//        long stamp = sl.readLock();
//        try {
//            return map.size();
//        } finally {
//            sl.unlockRead(stamp);
//        }
//    }
//}
//Result
//
//Pure reads → zero synchronization (optimistic stamp).
//Only when a read must promote to move-to-front do we take a write lock.
//        Under 99 % reads → near lock-free read throughput.

























//
//
//Follow-upAnswerCan we avoid HashMap entirely?Yes – use a custom open-addressing table with linear probing, but HashMap is already fast and cache-friendly. The bottleneck is the lock, not the map.What if capacity is huge (millions)?Use sharding (multiple independent LruCacheBest instances) + consistent hashing on key. Each shard has its own ReadWriteLock.How to unit-test thread-safety?Use java.util.concurrent ExecutorService + CountDownLatch. Run 100 threads doing mixed get/put, assert no missing/evicted entries and order is correct.What about ConcurrentLinkedHashMap?It exists (Guava), but internally uses segmented locking (like striped). Our ReadWriteLock version is simpler and performs equally well.
//
//        TL;DR – Recommended production code
//        java// Use LruCacheBest with ReentrantReadWriteLock
//// For >95% reads → switch to StampedLock version
//No ConcurrentHashMap needed – we control the critical sections ourselves, get maximum read concurrency, and keep exact LRU semantics.