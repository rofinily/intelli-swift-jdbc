package com.fr.swift.segment.collate;

import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.io.Types;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.source.alloter.impl.line.LineAllotRule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anchore
 * @date 2018/7/27
 */
public class SwiftFragmentCollectRule implements FragmentCollectRule {
    /**
     * 碎片大小
     */
    private static final int FRAGMENT_SIZE = LineAllotRule.STEP * 2 / 3;

    private final SwiftSegmentManager localSegments = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);

    @Override
    public List<SegmentKey> collect(List<SegmentKey> segKeys) {
        List<SegmentKey> fragmentKeys = new ArrayList<SegmentKey>();
        for (SegmentKey segKey : segKeys) {
            Segment seg = localSegments.getSegment(segKey);
            if (isNeed2Collect(seg)) {
                fragmentKeys.add(segKey);
            }
        }
        return fragmentKeys;
    }

    /**
     *
     * @param seg
     * @return
     */
    private boolean isNeed2Collect(Segment seg) {
        if (seg.getLocation().getStoreType() == Types.StoreType.MEMORY) {
            return true;
        }
        if (seg.getRowCount() < FRAGMENT_SIZE) {
            return true;
        }
        ImmutableBitMap allShowIndex = seg.getAllShowIndex();
        if (!allShowIndex.isFull()) {
            if (allShowIndex.getCardinality() < FRAGMENT_SIZE) {
                return true;
            }
        }
        return false;
    }
}