package com.fr.swift.source.alloter;

import com.fr.stable.StringUtils;
import com.fr.swift.config.bean.MetaDataColumnBean;
import com.fr.swift.config.bean.SwiftMetaDataBean;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.io.location.ResourceLocation;
import com.fr.swift.manager.LocalDataOperatorProvider;
import com.fr.swift.segment.HistorySegmentImpl;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.segment.column.DetailColumn;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.ListBasedRow;
import com.fr.swift.source.Row;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.source.SwiftSourceAlloter;
import com.fr.swift.source.SwiftSourceAlloterFactory;
import com.fr.swift.source.core.Core;
import com.fr.swift.test.TestIo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yee
 * @date 2017/12/19
 */
public class LineSegmentAlloterTest extends TestIo {
    SwiftResultSet resultSet;
    int count;

    @Before
    public void setUp() throws Exception {
        List<Row> datas = new ArrayList<>();
        count = (int) (Math.random() * 1000000);
        System.err.println(count);
        for (int i = 0; i < count; i++) {
            List data = new ArrayList<Long>();
            data.add((long) i);
            datas.add(new ListBasedRow(data));
        }
        List data = new ArrayList<Long>();
        data.add(null);
        datas.add(new ListBasedRow(data));
        resultSet = new SwiftResultSet() {
            int position = 0;

            @Override
            public void close() {

            }

            @Override
            public boolean next() {
                return position < datas.size();
            }

            @Override
            public SwiftMetaData getMetaData() {
                return new SwiftMetaDataBean("A",
                        Arrays.asList(new MetaDataColumnBean("long", Types.BIGINT)));
            }

            @Override
            public Row getRowData() {
                return datas.get(position++);
            }
        };

    }

    @Test
    public void testAlloc() throws Exception {
        SourceKey sourceKey = new SourceKey("A");

        DataSource dataSource = new DataSource() {
            @Override
            public SwiftMetaData getMetadata() {
                try {
                    return resultSet.getMetaData();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public SourceKey getSourceKey() {
                return sourceKey;
            }

            @Override
            public Core fetchObjectCore() {
                return null;
            }
        };

        Inserter inserter = SwiftContext.getInstance().getBean(LocalDataOperatorProvider.class).getHistoryBlockSwiftInserter(dataSource);
        inserter.insertData(resultSet);
        SwiftSourceAlloter alloter = SwiftSourceAlloterFactory.createLineSourceAlloter(sourceKey, sourceKey.getId());
        int lastIndex = -1;
        Segment segment = null;
        DetailColumn column = null;
        for (int i = 0; i < count; i++) {
            int index = alloter.allot(i, StringUtils.EMPTY, null);
            if (lastIndex != index || null == segment) {
                lastIndex = index;
                ResourceLocation location = new ResourceLocation(System.getProperty("user.dir") + "/cubes/" + sourceKey.getId() + "/seg" + index);
                segment = new HistorySegmentImpl(location, resultSet.getMetaData());
                column = segment.getColumn(new ColumnKey("long")).getDetailColumn();
            }
            Assert.assertEquals(column.getLong(i % 100000), (long) i);
        }
    }
}
