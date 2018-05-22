package com.fr.swift.result.node;

import com.fr.swift.query.aggregator.Aggregator;
import com.fr.swift.query.aggregator.AggregatorValue;
import com.fr.swift.query.aggregator.AggregatorValueUtils;
import com.fr.swift.result.GroupNode;
import com.fr.swift.result.TopGroupNode;
import com.fr.swift.result.XLeftNode;
import com.fr.swift.result.node.iterator.LeafNodeIterator;
import com.fr.swift.result.node.iterator.NLevelGroupNodeIterator;
import com.fr.swift.structure.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Lyon on 2018/4/11.
 */
public class GroupNodeAggregateUtils {

    public static GroupNode aggregateMetric(NodeType type, int dimensionSize, GroupNode root, List<Aggregator> aggregators) {
        List<Pair<Aggregator, Integer>> pairs = new ArrayList<Pair<Aggregator, Integer>>();
        for (int i = 0; i < aggregators.size(); i++) {
            pairs.add(Pair.of(aggregators.get(i), i));
        }
        return aggregate(type, dimensionSize, root, pairs);
    }

    /**
     * 如果要显示汇总值的话，不管是否分页，根节点的汇总（如果是交叉表，包括列向和横向的汇总）都是对全部数据的汇总，
     * 暂时对于根节点汇总都在明细聚合的过程中做好（全部计算的情况下有优化空间）。下面的结果聚合不处理根节点。
     *
     * @param type
     * @param dimensionSize
     * @param root
     * @param aggregators
     * @return
     */
    public static GroupNode aggregate(NodeType type, int dimensionSize, GroupNode root,
                                      List<Pair<Aggregator, Integer>> aggregators) {
        // 从第n个维度到第-1个维度(根节点)进行汇总
        for (int depth = dimensionSize - 1; depth >= -1; depth--) {
            Iterator<GroupNode> iterator = new NLevelGroupNodeIterator(depth, root);
            while (iterator.hasNext()) {
                GroupNode node = iterator.next();
                if (type == NodeType.GROUP) {
                    mergeChildGroupNode(node, aggregators);
                } else  {
                    mergeChildNode(type, node, aggregators);
                }
            }
        }
        return root;
    }

    /**
     * 汇总XLeftNode或者TopGroupNode
     *
     * @param type
     * @param groupNode
     * @param aggregators
     */
    private static void mergeChildNode(NodeType type, GroupNode groupNode, List<Pair<Aggregator, Integer>> aggregators) {
        // 大于0个子节点才汇总
        // 子节点的数量为1则把子节点的值复制过来，方便做更上一个维度的汇总
        if (groupNode.getChildrenSize() == 0) {
            return;
        }
        Iterator<GroupNode> iterator = new LeafNodeIterator(groupNode);
        List<AggregatorValue[]> valuesListOfParent;
        if (type == NodeType.X_LEFT) {
            valuesListOfParent = ((XLeftNode) groupNode).getValueArrayList();
        } else {
            valuesListOfParent = ((TopGroupNode) groupNode).getTopGroupValues();
        }
        while (iterator.hasNext()) {
            List<AggregatorValue[]> valuesListOfChild;
            if (type == NodeType.X_LEFT) {
                valuesListOfChild = ((XLeftNode) iterator.next()).getValueArrayList();
            } else {
                valuesListOfChild = ((TopGroupNode) iterator.next()).getTopGroupValues();
            }
            if (valuesListOfParent == null || valuesListOfParent.isEmpty()) {
                // 父节点为空的情况，先把子节点复制过来
                valuesListOfParent = createXAggregateValues(valuesListOfChild, aggregators);
                continue;
            }
            for (int i = 0; i < valuesListOfParent.size(); i++) {
                AggregatorValue[] valuesOfChild = valuesListOfChild.get(i);
                AggregatorValue[] valuesOfParent = valuesListOfParent.get(i);
                combine(valuesOfParent, valuesOfChild, aggregators);
            }
        }
        if (type == NodeType.X_LEFT) {
            ((XLeftNode) groupNode).setValueArrayList(valuesListOfParent);
        } else {
            ((TopGroupNode) groupNode).setTopGroupValues(valuesListOfParent);
        }
    }

    /**
     * 汇总GroupNode
     *
     * @param groupNode
     * @param aggregators
     */
    private static void mergeChildGroupNode(GroupNode groupNode, List<Pair<Aggregator, Integer>> aggregators) {
        // >= 两个子节点才汇总
        if (groupNode.getChildrenSize() == 0) {
            return;
        }
        if (groupNode.getChildrenSize() == 1) {
            // 把子节点的values复制过来
            groupNode.setAggregatorValue(createAggregateValues(groupNode.getChild(0).getAggregatorValue(), aggregators));
            return;
        }
        // 节点的合计都是在叶子节点（聚合结果的二维表）的基础上做（如果没有切换汇总方式可以在聚合子节点的基础上做，后面再优化吧）
        Iterator<GroupNode> iterator = new LeafNodeIterator(groupNode);
        // 默认清空父节点的值。
        // FIXME: 2018/5/4 多个segment同时有expander的情况下父节点有可能不为空的，这时就有bug了！
        AggregatorValue[] valuesOfParent = createAggregateValues(iterator.next().getAggregatorValue(), aggregators);
        // aggregator的个数和values的长度要想等，不管是哪种类型的value，都要有个对应的aggregator
        assert aggregators.size() == valuesOfParent.length;
        while (iterator.hasNext()) {
            AggregatorValue[] valuesOfChild = iterator.next().getAggregatorValue();
            combine(valuesOfParent, valuesOfChild, aggregators);
        }
        groupNode.setAggregatorValue(valuesOfParent);
    }

    private static void combine(AggregatorValue[] valuesOfParent, AggregatorValue[] valuesOfChild,
                                List<Pair<Aggregator, Integer>> pairs) {
        for (Pair<Aggregator, Integer> pair : pairs) {
            int resultIndex = pair.getValue();
            Aggregator aggregator = pair.getKey();
            if (valuesOfParent[resultIndex] == null) {
                valuesOfParent[resultIndex] = valuesOfChild[resultIndex] == null ? null : aggregator.createAggregatorValue(valuesOfChild[resultIndex]);
            } else {
                // TODO: 2018/5/7 如果没有切换汇总方式，用明细的方式合计还是在明细汇总的基础上合计？
                valuesOfParent[resultIndex] = AggregatorValueUtils.combine(valuesOfParent[resultIndex], valuesOfChild[resultIndex], aggregator);
            }
        }
    }

    private static List<AggregatorValue[]> createXAggregateValues(List<AggregatorValue[]> valuesOfFirstChild,
                                                                  List<Pair<Aggregator, Integer>> aggregators) {
        List<AggregatorValue[]> values = new ArrayList<AggregatorValue[]>();
        for (AggregatorValue[] value : valuesOfFirstChild) {
            values.add(createAggregateValues(value, aggregators));
        }
        return values;
    }

    private static AggregatorValue[] createAggregateValues(AggregatorValue[] valuesOfFirstChild,
                                                           List<Pair<Aggregator, Integer>> aggregators) {
        AggregatorValue[] values = Arrays.copyOf(valuesOfFirstChild, valuesOfFirstChild.length);
        for (Pair<Aggregator, Integer> pair : aggregators) {
            Aggregator aggregator = pair.getKey();
            int resultIndex = pair.getValue();
            values[resultIndex] = valuesOfFirstChild[resultIndex] == null ? null : aggregator.createAggregatorValue(valuesOfFirstChild[resultIndex]);
        }
        return values;
    }
}
