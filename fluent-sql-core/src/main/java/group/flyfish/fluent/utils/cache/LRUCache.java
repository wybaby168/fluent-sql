package group.flyfish.fluent.utils.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU (least recently used)最近最久未使用缓存<br>
 * 根据使用时间来判定对象是否被持续缓存<br>
 * 当对象被访问时放入缓存，当缓存满了，最久未被使用的对象将被移除。<br>
 * 此缓存基于LinkedHashMap，因此当被缓存的对象每被访问一次，这个对象的key就到链表头部。<br>
 * 这个算法简单并且非常快，他比FIFO有一个显著优势是经常使用的对象不太可能被移除缓存。<br>
 * 缺点是当缓存满时，不能被很快的访问。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author wangyu
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private final int maxSize;

    public LRUCache(int maxSize) {
        this(maxSize, 16, 0.75f, false);
    }

    public LRUCache(int maxSize, int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > this.maxSize;
    }
}
