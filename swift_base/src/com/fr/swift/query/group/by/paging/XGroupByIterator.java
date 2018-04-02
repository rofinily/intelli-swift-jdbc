package com.fr.swift.query.group.by.paging;

import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.result.KeyValue;
import com.fr.swift.result.RowIndexKey;
import com.fr.swift.structure.iterator.RowTraversal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Lyon on 2018/3/21.
 */
public class XGroupByIterator implements Iterator<KeyValue<RowIndexKey<int[]>, List<KeyValue<RowIndexKey<int[]>, RowTraversal>>>> {

    // iterable用来reset迭代器（重新new一个iterator）
    private Iterable<KeyValue<RowIndexKey<int[]>, RowTraversal>> colItCreator;

    private Iterator<KeyValue<RowIndexKey<int[]>, RowTraversal>> rowIt;
    private Iterator<KeyValue<RowIndexKey<int[]>, RowTraversal>> colIt;

    public XGroupByIterator(Iterator<KeyValue<RowIndexKey<int[]>, RowTraversal>> rowIt,
                            Iterable<KeyValue<RowIndexKey<int[]>, RowTraversal>> colItCreator) {
        this.rowIt = rowIt;
        this.colItCreator = colItCreator;
        this.colIt = colItCreator.iterator();
    }

    @Override
    public boolean hasNext() {
        return rowIt.hasNext();
    }

    /**
     * 每次计算交叉表的一行结果
     * 表头的分页在表头索引迭代器colIt里面处理
     * 考虑到多个segment结果合并，先根据行表头合并，再合并列表头
     * @return
     */
    @Override
    public KeyValue<RowIndexKey<int[]>, List<KeyValue<RowIndexKey<int[]>, RowTraversal>>> next() {
        KeyValue<RowIndexKey<int[]>, RowTraversal> rowKV = rowIt.next();
        ImmutableBitMap bitMap = rowKV.getValue().toBitMap();
        List<KeyValue<RowIndexKey<int[]>, RowTraversal>> keyValues = new ArrayList<KeyValue<RowIndexKey<int[]>, RowTraversal>>();
        while (colIt.hasNext()) {
            KeyValue<RowIndexKey<int[]>, RowTraversal> colKV = colIt.next();
            keyValues.add(new KeyValue<RowIndexKey<int[]>, RowTraversal>(colKV.getKey(),
                    // 这边是否选择bitmap计算，可以另外做优化
                    bitMap.getAnd(colKV.getValue().toBitMap())));
        }
        // 重置表头索引迭代器colIt。是否缓存表头的索引可以在colItCreator里面优化
        colIt = colItCreator.iterator();
        return new KeyValue<RowIndexKey<int[]>, List<KeyValue<RowIndexKey<int[]>, RowTraversal>>>(rowKV.getKey(), keyValues);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
