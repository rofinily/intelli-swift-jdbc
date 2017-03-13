package com.finebi.cube.gen.oper;

import com.finebi.cube.conf.utils.BILogHelper;
import com.finebi.cube.exception.BICubeColumnAbsentException;
import com.finebi.cube.exception.BICubeRelationAbsentException;
import com.finebi.cube.exception.IllegalRelationPathException;
import com.finebi.cube.structure.*;
import com.finebi.cube.structure.column.BIColumnKey;
import com.finebi.cube.structure.column.CubeColumnReaderService;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.exception.BITablePathConfusionException;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class created on 2016/11/13.
 *
 * @author Connery
 * @since Advanced FineBI Analysis 1.0
 */
public class CubeChooser implements Cube {
    private static final Logger logger = LoggerFactory.getLogger(CubeChooser.class);
    private static final long serialVersionUID = -115599916764191095L;

    /**
     * 当前生成的cube
     */
    protected Cube cube;
    /**
     * 当前被分析的cube。数据不再转移，而是从老cube中读取
     */
    protected Cube integrityCube;

    private Map<String, CubeTableSource> tablesNeed2GenerateMap;

    public CubeChooser(Cube cube, Cube integrityCube, Map<String, CubeTableSource> tablesNeed2GenerateMap) {
        this.cube = cube;
        this.integrityCube = integrityCube;
        this.tablesNeed2GenerateMap = tablesNeed2GenerateMap;
    }

    public CubeChooser(Cube cube, Cube integrityCube) {
        this.cube = cube;
        this.integrityCube = integrityCube;
    }

    /**
     * 如果table不需要生成，那么久从老Cube中读取数据
     *
     * @param tableKey 数据源
     * @return
     */
    @Override
    public CubeTableEntityGetterService getCubeTable(ITableKey tableKey) {
        if (tablesNeed2GenerateMap == null || tablesNeed2GenerateMap.get(tableKey.getSourceID()) != null) {
            if (cube.exist(tableKey)) {
                return cube.getCubeTable(tableKey);
            } else if (integrityCube != null && integrityCube.exist(tableKey)) {
                logger.warn("The table need to be generate but not exists in the tCube, try to get data from Advanced cube,the tableSourceID is: " + tableKey.getSourceID() + " table info is: " + BILogHelper.logCubeLogTableSourceInfo(tableKey.getSourceID()));
                return integrityCube.getCubeTable(tableKey);
            } else {
                throw BINonValueUtils.beyondControl("The table disappear,the tableSourceID is:" + tableKey.getSourceID() + " the table info is: " + BILogHelper.logCubeLogTableSourceInfo(tableKey.getSourceID()));
            }
        } else {
            logger.info("The table need not to be generate,try to get data from Advanced cube,the tableSourceID is: " + tableKey.getSourceID() + " table info is: " + BILogHelper.logCubeLogTableSourceInfo(tableKey.getSourceID()));
            if (integrityCube != null && integrityCube.exist(tableKey)) {
                return integrityCube.getCubeTable(tableKey);
            } else {
                throw BINonValueUtils.beyondControl("The table disappear, the table info is: " + BILogHelper.logCubeLogTableSourceInfo(tableKey.getSourceID()));
            }
        }


    }

    @Override
    public CubeTableEntityService getCubeTableWriter(ITableKey tableKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CubeColumnReaderService getCubeColumn(ITableKey tableKey, BIColumnKey field) throws BICubeColumnAbsentException {
        return getCubeTable(tableKey).getColumnDataGetter(field);
    }

    @Override
    public CubeRelationEntityGetterService getCubeRelation(ITableKey tableKey, BICubeTablePath relationPath) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        if (cube.exist(tableKey, relationPath)) {
            return cube.getCubeRelation(tableKey, relationPath);
        } else if (integrityCube != null && integrityCube.exist(tableKey, relationPath)) {
            logger.warn("The table:" + tableKey.getSourceID() + " the Path:" + BIRelationIDUtils.calculatePathID(relationPath) + " is generated by analysis cube");
            return integrityCube.getCubeRelation(tableKey, relationPath);
        } else {
            throw BINonValueUtils.beyondControl("The table:" + tableKey.getSourceID() + " the Path:" + BIRelationIDUtils.calculatePathID(relationPath) + " disappear");
        }
    }

    @Override
    public CubeRelationEntityGetterService getCubeRelation(ITableKey tableKey, BICubeRelation relation) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        BICubeTablePath relationPath = new BICubeTablePath();
        try {
            relationPath.addRelationAtHead(relation);
        } catch (BITablePathConfusionException e) {
            throw BINonValueUtils.illegalArgument(relation.toString() + " the relation is so terrible");
        }
        return getCubeRelation(tableKey, relationPath);
    }

    @Override
    public ICubeRelationEntityService getCubeRelationWriter(ITableKey tableKey, BICubeRelation relation) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICubeRelationEntityService getCubeRelationWriter(ITableKey tableKey, BICubeTablePath relationPath) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exist(ITableKey tableKey) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean exist(ITableKey tableKey, BICubeRelation relation) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean exist(ITableKey tableKey, BICubeTablePath relationPath) {
        throw new UnsupportedOperationException();

    }

    @Override
    public long getCubeVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addVersion(long version) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean exist(ITableKey tableKey, BIColumnKey field, BICubeTablePath relationPath) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Boolean isVersionAvailable() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void clear() {
        this.cube.clear();
        this.integrityCube.clear();
    }
}
