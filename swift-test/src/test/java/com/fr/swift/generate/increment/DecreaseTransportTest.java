package com.fr.swift.generate.increment;

import com.fr.base.FRContext;
import com.fr.dav.LocalEnv;
import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.config.TestConfDb;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.generate.BaseConfigTest;
import com.fr.swift.generate.TestIndexer;
import com.fr.swift.generate.TestTransport;
import com.fr.swift.generate.realtime.RealtimeDataTransporter;
import com.fr.swift.increase.IncrementImpl;
import com.fr.swift.increment.Increment;
import com.fr.swift.manager.LocalSegmentProvider;
import com.fr.swift.segment.Segment;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.db.QueryDBSource;
import com.fr.swift.source.db.TestConnectionProvider;
import com.fr.swift.test.Preparer;

import java.util.List;

/**
 * This class created on 2018-1-8 13:53:30
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI Analysis 1.0
 */
public class DecreaseTransportTest extends BaseConfigTest {

    private DataSource dataSource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FRContext.setCurrentEnv(new LocalEnv(System.getProperty("user.dir") + "\\" + System.currentTimeMillis()));
        TestConnectionProvider.createConnection();
        TestConfDb.setConfDb();
        dataSource = new QueryDBSource("select 记录人 from DEMO_CAPITAL_RETURN", "DecreaseTest");
        Preparer.prepareCubeBuild();
    }

    public void testDecreaseTransport() throws Exception {

        TestIndexer.historyIndex(dataSource, TestTransport.historyTransport(dataSource));

        Increment increment = new IncrementImpl(null, "select 记录人 from DEMO_CAPITAL_RETURN where 记录人 ='庆芳'", null, dataSource.getSourceKey(), "local1");
        RealtimeDataTransporter transport = new RealtimeDataTransporter(dataSource, increment);
        transport.work();

        List<Segment> segments = SwiftContext.getInstance().getBean(LocalSegmentProvider.class).getSegment(dataSource.getSourceKey());
        Segment segment = segments.get(0);
        assertEquals(segment.getRowCount(), 682);
        ImmutableBitMap bitMap = segment.getAllShowIndex();

        int containCount = 0;
        for (int i = 0; i < 682; i++) {
            if (bitMap.contains(i)) {
                containCount++;
            }
        }
        assertEquals(containCount, 650);
        assertTrue(true);
    }
}
