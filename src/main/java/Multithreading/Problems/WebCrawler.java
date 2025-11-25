package Multithreading.Problems;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WebCrawler {

    private String getHostName(String url){

        int idx = url.indexOf('/',7);
        return idx != -1 ? url.substring(0,idx) : url;
    }

    public List<String> crawl(String startUrl, HtmlParser htmlParser){
        Set<String> result = ConcurrentHashMap.newKeySet();

        String hostName = getHostName(startUrl);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        result.add(startUrl);
        crawl(startUrl,result,executorService,hostName,htmlParser);

        executorService.shutdown();

        return new ArrayList<>(result);
    }

    private void crawl(String start, Set<String> result, ExecutorService executorService, String hostName, HtmlParser htmlParser){
        List<Future> futures = new ArrayList<>();
        for(String url : htmlParser.getUrls(start)){
            if(getHostName(url).equals(hostName) && !result.contains(url)){
                result.add(url);
                futures.add(executorService.submit(() -> crawl(url,result,executorService,hostName,htmlParser)));
            }
        }

        for(Future f : futures){
            try{
                f.get();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String []args){

        WebCrawler crawler = new WebCrawler();
        HtmlParser parser = new MockHtmlParser();

        String startUrl = "http://example.com/page1";

        List<String> crawled = crawler.crawl(startUrl, parser);
        for(String s : crawled){
            System.out.println(s);
        }
    }
}



//this is given by leetcode you dont create this
interface HtmlParser {
    List<String> getUrls(String url);
}

/**
 * Mock implementation for local testing.
 * Simulates a small web of connected pages under the same hostname.
 */
class MockHtmlParser implements HtmlParser {
    private static final Map<String, List<String>> mockWeb = new HashMap<>();

    static {
        mockWeb.put("http://example.com/page1", Arrays.asList(
                "http://example.com/page2",
                "http://example.com/page3",
                "http://othersite.com/pageA" // different domain, ignored
        ));
        mockWeb.put("http://example.com/page2", Arrays.asList(
                "http://example.com/page1",
                "http://example.com/page4"
        ));
        mockWeb.put("http://example.com/page3", Arrays.asList(
                "http://example.com/page5"
        ));
        mockWeb.put("http://example.com/page4", Collections.emptyList());
        mockWeb.put("http://example.com/page5", Collections.emptyList());
    }

    @Override
    public List<String> getUrls(String url) {
        // Simulate network latency
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}

        return mockWeb.getOrDefault(url, Collections.emptyList());
    }
}


//import java.util.*;
//   import java.util.concurrent.locks.*;
//
//public class RWConcurrentHashSet<E> implements Set<E> {
//    private final Set<E> set = new HashSet<>();
//    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
//    private final Lock readLock = rwLock.readLock();
//    private final Lock writeLock = rwLock.writeLock();
//
//    @Override
//    public boolean add(E e) {
//        writeLock.lock();
//        try {
//            return set.add(e);
//        } finally {
//            writeLock.unlock();
//        }
//    }
//
//    @Override
//    public boolean remove(Object o) {
//        writeLock.lock();
//        try {
//            return set.remove(o);
//        } finally {
//            writeLock.unlock();
//        }
//    }
//
//    @Override
//    public boolean contains(Object o) {
//        readLock.lock();
//        try {
//            return set.contains(o);
//        } finally {
//            readLock.unlock();
//        }
//    }
//
//    @Override
//    public void clear() {
//        writeLock.lock();
//        try {
//            set.clear();
//        } finally {
//            writeLock.unlock();
//        }
//    }
//
//    @Override
//    public int size() {
//        readLock.lock();
//        try {
//            return set.size();
//        } finally {
//            readLock.unlock();
//        }
//    }
//
//    @Override
//    public Iterator<E> iterator() {
//        // To avoid holding a lock while iterating, return a snapshot
//        readLock.lock();
//        try {
//            return new HashSet<>(set).iterator();
//        } finally {
//            readLock.unlock();
//        }
//    }
//}
//
//Set<String> result = new RWConcurrentHashSet<>();




//public class ConcurrentHashSet<E> {
//
//    private static final int INITIAL_SIZE = 16;
//    private static final int MAXIMUM_CAPACITY = 1 << 30;
//    private static final float LOAD_FACTOR = 0.75f;
//
//    private SetNode<E>[] table;
//    private int size;
//    private final ReadWriteLock lock = new ReentrantReadWriteLock();
//
//    @SuppressWarnings("unchecked")
//    public ConcurrentHashSet() {
//        table = new SetNode[INITIAL_SIZE];
//        size = 0;
//    }
//
//    @SuppressWarnings("unchecked")
//    public ConcurrentHashSet(int capacity) {
//        int tableSize = tableSizeFor(capacity);
//        table = new SetNode[tableSize];
//        size = 0;
//    }
//
//    private int tableSizeFor(int cap) {
//        int n = cap - 1;
//        n |= n >>> 1;
//        n |= n >>> 2;
//        n |= n >>> 4;
//        n |= n >>> 8;
//        n |= n >>> 16;
//        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
//    }
//
//    private int hash(E element) {
//        if (element == null) {
//            return 0;
//        }
//        int h = element.hashCode();
//        return Math.abs(h) % table.length;
//    }
//
//    /**
//     * Adds an element to the set
//     * @param element the element to add
//     * @return true if element was added, false if it already exists
//     */
//    public boolean add(E element) {
//        lock.writeLock().lock();
//        try {
//            int index = hash(element);
//            SetNode<E> node = table[index];
//
//            // Check if element already exists
//            SetNode<E> current = node;
//            while (current != null) {
//                if (current.value == null && element == null) {
//                    return false;
//                }
//                if (current.value != null && current.value.equals(element)) {
//                    return false;
//                }
//                current = current.next;
//            }
//            // Add new element at the beginning of the chain
//            SetNode<E> newNode = new SetNode<>(element);
//            newNode.next = table[index];
//            table[index] = newNode;
//            size++;
//            return true;
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    /**
//     * Checks if the set contains an element
//     * @param element the element to check
//     * @return true if element exists, false otherwise
//     */
//    public boolean contains(E element) {
//        lock.readLock().lock();
//        try {
//            int index = hash(element);
//            SetNode<E> node = table[index];
//
//            while (node != null) {
//                if (node.value == null && element == null) {
//                    return true;
//                }
//                if (node.value != null && node.value.equals(element)) {
//                    return true;
//                }
//                node = node.next;
//            }
//            return false;
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    /**
//     * Removes an element from the set
//     * @param element the element to remove
//     * @return true if element was removed, false if it didn't exist
//     */
//    public boolean remove(E element) {
//        lock.writeLock().lock();
//        try {
//            int index = hash(element);
//            SetNode<E> node = table[index];
//            SetNode<E> prev = null;
//
//            while (node != null) {
//                boolean match = (node.value == null && element == null) ||
//                        (node.value != null && node.value.equals(element));
//
//                if (match) {
//                    if (prev == null) {
//                        table[index] = node.next;
//                    } else {
//                        prev.next = node.next;
//                    }
//                    size--;
//                    return true;
//                }
//                prev = node;
//                node = node.next;
//            }
//            return false;
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public static void main(String[] args) throws InterruptedException {
//        ConcurrentHashSet<String> set = new ConcurrentHashSet<>();
//
//        // Test basic operations
//        System.out.println("=== Basic Operations Test ===");
//        System.out.println("Add 'apple': " + set.add("apple"));
//        System.out.println("Add 'banana': " + set.add("banana"));
//        System.out.println("Add 'apple' again: " + set.add("apple")); // Should return false
//        System.out.println("Contains 'apple': " + set.contains("apple"));
//        System.out.println("Contains 'orange': " + set.contains("orange"));
//        System.out.println("Size: " + set.size());
//        System.out.println("Set: " + set);
//
//        // Test concurrent operations
//        System.out.println("\n=== Concurrent Operations Test ===");
//        ConcurrentHashSet<Integer> concurrentSet = new ConcurrentHashSet<>();
//        Thread[] threads = new Thread[10];
//
//        // Create threads that add elements
//        for (int i = 0; i < threads.length; i++) {
//            final int threadId = i;
//            threads[i] = new Thread(() -> {
//                for (int j = 0; j < 100; j++) {
//                    concurrentSet.add(threadId * 100 + j);
//                }
//            });
//            threads[i].start();
//        }
//
//        // Wait for all threads to complete
//        for (Thread thread : threads) {
//            thread.join();
//        }
//
//        System.out.println("Total unique elements after concurrent adds: " + concurrentSet.size());
//        System.out.println("Expected: 1000");
//        System.out.println("Contains 555: " + concurrentSet.contains(555));
//
//        // Test concurrent add/remove
//        System.out.println("\n=== Concurrent Add/Remove Test ===");
//        ConcurrentHashSet<Integer> mixedSet = new ConcurrentHashSet<>();
//
//        Thread adder = new Thread(() -> {
//            for (int i = 0; i < 1000; i++) {
//                mixedSet.add(i);
//            }
//        });
//
//        Thread remover = new Thread(() -> {
//            for (int i = 0; i < 500; i++) {
//                mixedSet.remove(i);
//            }
//        });
//
//        adder.start();
//        remover.start();
//        adder.join();
//        remover.join();
//
//        System.out.println("Final size after concurrent add/remove: " + mixedSet.size());
//        System.out.println("Contains 250: " + mixedSet.contains(250));
//        System.out.println("Contains 750: " + mixedSet.contains(750));
//
//        // Test remove operation
//        System.out.println("\n=== Remove Test ===");
//        System.out.println("Remove 'banana': " + set.remove("banana"));
//        System.out.println("Remove 'banana' again: " + set.remove("banana"));
//        System.out.println("Size after remove: " + set.size());
//        System.out.println("Set: " + set);
//
//        // Test clear
//        System.out.println("\n=== Clear Test ===");
//        set.clear();
//        System.out.println("Size after clear: " + set.size());
//        System.out.println("Is empty: " + set.isEmpty());
//    }
//}