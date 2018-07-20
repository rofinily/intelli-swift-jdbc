package com.fr.swift.service;

import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.db.Table;
import com.fr.swift.db.Where;
import com.fr.swift.query.builder.QueryIndexBuilder;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.query.DetailQueryInfoBean;
import com.fr.swift.query.query.FilterBean;
import com.fr.swift.query.query.IndexQuery;
import com.fr.swift.query.query.QueryBean;
import com.fr.swift.query.query.QueryIndexRunner;
import com.fr.swift.segment.Segment;

import java.net.URI;
import java.util.Map;

/**
 * This class created on 2018/7/4
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class QueryIndexService implements QueryIndexRunner {

    @Override
    public Map<URI, IndexQuery<ImmutableBitMap>> getBitMap(Table table, Where where) throws Exception {
        return QueryIndexBuilder.buildQuery(createQueryBean(table, where));
    }

    @Override
    public IndexQuery<ImmutableBitMap> getBitMap(Table table, Where where, Segment segment) {
        return QueryIndexBuilder.buildQuery(createQueryBean(table, where), segment);
    }

    private static QueryBean createQueryBean(Table table, Where where) {
        FilterBean filterBean = where.getQueryCondition();
        DetailQueryInfoBean queryInfoBean = new DetailQueryInfoBean();
        queryInfoBean.setQueryId("" + System.currentTimeMillis());
        queryInfoBean.setTableName(table.getSourceKey().getId());
        queryInfoBean.setFilterInfoBean((FilterInfoBean) filterBean);
        return queryInfoBean;
    }
}
