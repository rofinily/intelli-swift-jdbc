package com.fr.swift.query.group.by2.node;

import com.fr.swift.query.aggregator.AggregatorValue;
import com.fr.swift.query.group.info.GroupByInfo;
import com.fr.swift.query.group.info.MetricInfo;
import com.fr.swift.result.GroupNode;
import com.fr.swift.result.NodeMergeResultSet;
import com.fr.swift.result.NodeMergeResultSetImpl;
import com.fr.swift.structure.iterator.RowTraversal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Lyon on 2018/4/27.
 */
public class NodeGroupByUtils {

    /**
     * 聚合单块segment数据，得到node结果集和压缩的字典值
     *
     * @param groupByInfo
     * @param metricInfo
     * @param pageSize  Integer.MAX_VALUE为不分页
     * @return
     */
    public static Iterator<NodeMergeResultSet<GroupNode>> groupBy(GroupByInfo groupByInfo, MetricInfo metricInfo, int pageSize) {
        if (groupByInfo.getDimensions().isEmpty()) {
            // 只有指标的情况
            GroupNode root = new GroupNode(-1, null);
            aggregateRoot(root, groupByInfo.getDetailFilter().createFilterIndex(), metricInfo);
            List<NodeMergeResultSet<GroupNode>> list = new ArrayList<NodeMergeResultSet<GroupNode>>();
            list.add(new NodeMergeResultSetImpl<GroupNode>(root, new ArrayList<Map<Integer, Object>>()));
            return list.iterator();
        }
        return new MergeResultSetIterator(pageSize, groupByInfo, metricInfo);
    }

    private static void aggregateRoot(GroupNode root, RowTraversal traversal, MetricInfo metricInfo) {
        AggregatorValue[] values = RowMapper.aggregateRow(traversal, metricInfo.getTargetLength(),
                metricInfo.getMetrics(), metricInfo.getAggregators());
        root.setAggregatorValue(values);
    }
}
