package com.fr.swift.cal.segment.group;

import com.fr.swift.query.adapter.dimension.Expander;
import com.fr.swift.query.aggregator.Aggregator;
import com.fr.swift.query.filter.detail.DetailFilter;
import com.fr.swift.query.group.by.XGroupByUtils;
import com.fr.swift.query.sort.Sort;
import com.fr.swift.result.NodeResultSet;
import com.fr.swift.result.NodeResultSetImpl;
import com.fr.swift.result.XGroupByResultSet;
import com.fr.swift.result.node.xnode.XLeftNode;
import com.fr.swift.result.node.xnode.XLeftNodeFactory;
import com.fr.swift.segment.column.Column;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Lyon on 2018/4/1.
 */
public class XGroupAllSegmentQuery extends GroupAllSegmentQuery {

    private List<Column> colDimensions;
    private List<Sort> colIndexSorts;
    private int[] xCursor;
    private Expander colExpander;

    public XGroupAllSegmentQuery(List<Column> rowDimensions, List<Column> colDimensions, List<Column> metrics,
                                 List<Aggregator> aggregators, DetailFilter filter,
                                 List<Sort> rowIndexSorts, List<Sort> colIndexSorts, Expander colExpander) {
        super(aggregators.size(), rowDimensions, metrics, aggregators, filter, rowIndexSorts, null);
        this.colDimensions = colDimensions;
        this.colIndexSorts = colIndexSorts;
        this.colExpander = colExpander;
    }

    @Override
    public NodeResultSet getQueryResult() {
        cursor = new int[dimensions.size()];
        Arrays.fill(cursor, 0);
        xCursor = new int[colDimensions.size()];
        Arrays.fill(xCursor, 0);
        XGroupByResultSet resultSet =  XGroupByUtils.query(dimensions, colDimensions, metrics, aggregators, filter, indexSorts, colIndexSorts,
                cursor, xCursor, -1, -1);
        XLeftNode node = XLeftNodeFactory.createXLeftNode(resultSet, aggregators.size());
        return new NodeResultSetImpl(node);
    }
}
