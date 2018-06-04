package com.fr.swift.query.info.group.post;

import com.fr.swift.query.filter.match.MatchFilter;
import com.fr.swift.query.post.PostQueryType;

import java.util.List;

/**
 * Created by Lyon on 2018/6/3.
 */
public class HavingFilterQueryInfo implements PostQueryInfo {

    private List<MatchFilter> matchFilterList;

    public HavingFilterQueryInfo(List<MatchFilter> matchFilterList) {
        this.matchFilterList = matchFilterList;
    }

    /**
     * 对应NodeResultSet最后一个维度上面的过滤
     *
     * @return
     */
    public List<MatchFilter> getMatchFilterList() {
        return matchFilterList;
    }

    @Override
    public PostQueryType getType() {
        return PostQueryType.HAVING_FILTER;
    }
}
