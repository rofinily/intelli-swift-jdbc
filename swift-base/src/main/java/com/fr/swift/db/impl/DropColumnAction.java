package com.fr.swift.db.impl;

import com.fr.swift.config.bean.SwiftMetaDataBean;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.CubeUtil;
import com.fr.swift.cube.io.ResourceDiscovery;
import com.fr.swift.cube.io.Types.StoreType;
import com.fr.swift.db.Table;
import com.fr.swift.exception.meta.SwiftMetaDataException;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.util.FileUtil;
import com.fr.swift.util.Util;
import com.fr.swift.util.function.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anchore
 * @date 2018/8/20
 */
public class DropColumnAction extends BaseAlterTableAction {
    public DropColumnAction(SwiftMetaDataColumn relatedColumnMeta) {
        super(relatedColumnMeta);
    }

    @Override
    public void alter(final Table table) {
        SwiftLoggers.getLogger().info("drop column: {} of {}", relatedColumnMeta, table);

        List<SegmentKey> segKeys = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class).getSegmentKeys(table.getSourceKey());
        for (final SegmentKey segKey : segKeys) {
            final String columnId = relatedColumnMeta.getColumnId();
            if (segKey.getStoreType() == StoreType.MEMORY) {
                // 删内存
                ResourceDiscovery.getInstance().removeIf(new Predicate<String>() {
                    @Override
                    public boolean test(String s) {
                        return s.contains(CubeUtil.getColumnPath(segKey, columnId));
                    }
                });
                // 删备份
                FileUtil.delete(CubeUtil.getAbsoluteColumnPath(segKey, columnId).replace(segKey.getSwiftSchema().getDir(), segKey.getSwiftSchema().getBackupDir()));
                continue;
            }

            // 删history todo 还要删共享存储
            FileUtil.delete(CubeUtil.getAbsoluteColumnPath(segKey, columnId));
        }

        alterMeta(table);
    }

    private void alterMeta(Table table) {
        SwiftMetaData oldMeta = table.getMetadata();
        try {
            List<SwiftMetaDataColumn> columnMetas = new ArrayList<SwiftMetaDataColumn>(oldMeta.getColumnCount() - 1);
            for (int i = 0; i < oldMeta.getColumnCount(); i++) {
                SwiftMetaDataColumn columnMeta = oldMeta.getColumn(i + 1);
                if (!columnMeta.getName().equals(relatedColumnMeta.getName())) {
                    columnMetas.add(columnMeta);
                }
            }
            SwiftMetaData newMeta = new SwiftMetaDataBean(oldMeta.getTableName(), columnMetas);
            CONF_SVC.updateMetaData(table.getSourceKey().getId(), newMeta);
        } catch (SwiftMetaDataException e) {
            SwiftLoggers.getLogger().warn("alter meta failed, {}", Util.getRootCauseMessage(e));
        }
    }
}