package assignment.dictionary;

import java.util.*;
import java.io.*;

/**
 *
 * @author Mahmoud Algharbawi, Nicolas Hidalgo, Shawana Tahseen
 *mfa0106, nh0277  st0611
 */

public class MyHashTable<K, V> extends Dictionary<K,V> implements Map<K, V>, Cloneable, Serializable {

    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final Entry[] EMPTY_TABLE = new HashtableEntry[MINIMUM_CAPACITY >>> 1];
    private transient HashtableEntry<K, V>[] table;
    private transient int size;
    private transient int modCount;
    private transient int threshold;
    // Views - lazily initialized
    private transient Set<K> keySet;
    private transient Set<Entry<K, V>> entrySet;
    private transient Collection<V> values;
    @SuppressWarnings("unchecked")
    public MyHashTable() {
        table = (HashtableEntry<K, V>[]) EMPTY_TABLE;
        threshold = -1; // Forces first put invocation to replace EMPTY_TABLE
    }
    private void constructorPutAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            constructorPut(e.getKey(), e.getValue());
        }
    }
    @SuppressWarnings("unchecked")
    public synchronized boolean isEmpty() {
        return size == 0;
    }
    public synchronized int size() {
        return size;
    }
    public synchronized V get(Object key) {
        // Doug Lea's supplemental secondaryHash function (inlined)
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);
        HashtableEntry<K, V>[] tab = table;
        for (HashtableEntry<K, V> e = tab[hash & (tab.length - 1)];
             e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                return e.value;
            }
        }
        return null;
    }
    public synchronized boolean containsKey(Object key) {
        // Doug Lea's supplemental secondaryHash function (inlined)
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);
        HashtableEntry<K, V>[] tab = table;
        for (HashtableEntry<K, V> e = tab[hash & (tab.length - 1)];
             e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        HashtableEntry[] tab = table;
        int len = tab.length;
        for (int i = 0; i < len; i++) {
            for (HashtableEntry e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }
    public synchronized V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value == null) {
            throw new NullPointerException("value == null");
        }
        int hash = secondaryHash(key.hashCode());
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        HashtableEntry<K, V> first = tab[index];
        for (HashtableEntry<K, V> e = first; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        // No entry for key is present; create one
        modCount++;
        if (size++ > threshold) {
            rehash();  // Does nothing!!
            tab = doubleCapacity();
            index = hash & (tab.length - 1);
            first = tab[index];
        }
        tab[index] = new HashtableEntry<K, V>(key, value, hash, first);
        return null;
    }
    private void constructorPut(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value == null) {
            throw new NullPointerException("value == null");
        }
        int hash = secondaryHash(key.hashCode());
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        HashtableEntry<K, V> first = tab[index];
        for (HashtableEntry<K, V> e = first; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                e.value = value;
                return;
            }
        }
        // No entry for key is present; create one
        tab[index] = new HashtableEntry<K, V>(key, value, hash, first);
        size++;
    }
    public synchronized void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }



    protected void rehash() {
    }

    private HashtableEntry<K, V>[] makeTable(int newCapacity) {
        @SuppressWarnings("unchecked") HashtableEntry<K, V>[] newTable
                = (HashtableEntry<K, V>[]) new HashtableEntry[newCapacity];
        table = newTable;
        threshold = (newCapacity >> 1) + (newCapacity >> 2); // 3/4 capacity
        return newTable;
    }
    private HashtableEntry<K, V>[] doubleCapacity() {
        HashtableEntry<K, V>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            return oldTable;
        }
        int newCapacity = oldCapacity * 2;
        HashtableEntry<K, V>[] newTable = makeTable(newCapacity);
        if (size == 0) {
            return newTable;
        }
        for (int j = 0; j < oldCapacity; j++) {
            /*
             * Rehash the bucket using the minimum number of field writes.
             * This is the most subtle and delicate code in the class.
             */
            HashtableEntry<K, V> e = oldTable[j];
            if (e == null) {
                continue;
            }
            int highBit = e.hash & oldCapacity;
            HashtableEntry<K, V> broken = null;
            newTable[j | highBit] = e;
            for (HashtableEntry<K,V> n = e.next; n != null; e = n, n = n.next) {
                int nextHighBit = n.hash & oldCapacity;
                if (nextHighBit != highBit) {
                    if (broken == null)
                        newTable[j | nextHighBit] = n;
                    else
                        broken.next = n;
                    broken = e;
                    highBit = nextHighBit;
                }
            }
            if (broken != null)
                broken.next = null;
        }
        return newTable;
    }

    public synchronized V remove(Object key) {
      return null;
    }
    public synchronized void clear() {
        return ;
    }
    public synchronized Set<K> keySet() {
//        Set<K> ks = keySet;
//        return (ks != null) ? ks : (keySet = new KeySet());
        return null;
    }

    public synchronized Collection<V> values() {
        return null;
    }
    public synchronized Set<Entry<K, V>> entrySet() {
        return null;
    }

    private static class HashtableEntry<K, V> implements Entry<K, V> {
        final K key;
        V value;
        final int hash;
        HashtableEntry<K, V> next;
        HashtableEntry(K key, V value, int hash, HashtableEntry<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }
        public final K getKey() {
            return key;
        }
        public final V getValue() {
            return value;
        }
        public final V setValue(V value) {
            if (value == null) {
                throw new NullPointerException("value == null");
            }
            V oldValue = this.value;
            this.value = value;
            return oldValue;
//            return null;
        }
        @Override public final boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry<?, ?>) o;
            return key.equals(e.getKey()) && value.equals(e.getValue());
        }
        @Override public final int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }
        @Override public final String toString() {
            return key + "=" + value;
        }
    }
    private static int secondaryHash(int h) {
        // Doug Lea's supplemental hash function
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
}