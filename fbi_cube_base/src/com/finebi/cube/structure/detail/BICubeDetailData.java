package com.finebi.cube.structure.detail;

import com.finebi.cube.CubeVersion;
import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.data.input.ICubeReader;
import com.finebi.cube.data.output.ICubeWriter;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.structure.ICubeDetailDataService;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.bi.stable.utils.program.BINonValueUtils;

/**
 * This class created on 2016/3/11.
 *
 * @author Connery
 * @since 4.0
 */
public abstract class BICubeDetailData<T> implements ICubeDetailDataService<T> {
    protected ICubeWriter<T> cubeWriter;
    protected ICubeReader<T> cubeReader;
    protected ICubeResourceLocation currentLocation;
    private ICubeResourceDiscovery discovery;

    public BICubeDetailData(ICubeResourceDiscovery discovery, ICubeResourceLocation superLocation) {
        try {
            this.discovery = discovery;
            currentLocation = superLocation.buildChildLocation("detail.fbi");
        } catch (Exception e) {
            BINonValueUtils.beyondControl(e.getMessage(), e);
        }
    }


    protected abstract ICubeResourceLocation setDetailType();

    @Override
    public void addDetailDataValue(int rowNumber, T originalValue) {
        getCubeWriter().recordSpecificValue(rowNumber, originalValue);
    }

    protected boolean isCubeWriterAvailable() {
        return cubeWriter != null;
    }

    protected boolean isCubeReaderAvailable() {
        return cubeReader != null;
    }

    public ICubeWriter<T> getCubeWriter() {
        if (!isCubeWriterAvailable()) {
            initCubeWriter();
        }
        return cubeWriter;
    }

    public ICubeReader<T> getCubeReader() {
        if (!isCubeReaderAvailable()) {
            initCubeReader();
        }
        return cubeReader;
    }

    private void initCubeReader() {
        try {
            currentLocation = setDetailType();
            ICubeResourceDiscovery resourceDiscovery = discovery;
            currentLocation.setReaderSourceLocation();
            cubeReader = resourceDiscovery.getCubeReader(currentLocation);
        } catch (Exception e) {
            BINonValueUtils.beyondControl(e.getMessage(), e);
        }
    }

    private void initCubeWriter() {
        try {
            currentLocation = setDetailType();
            ICubeResourceDiscovery resourceDiscovery = discovery;
            currentLocation.setWriterSourceLocation();
            cubeWriter = resourceDiscovery.getCubeWriter(currentLocation);
        } catch (Exception e) {
            BINonValueUtils.beyondControl(e.getMessage(), e);
        }
    }

    @Override
    public T getOriginalValueByRow(int rowNumber) {
        try {
            return getCubeReader().getSpecificValue(rowNumber);
        } catch (BIResourceInvalidException e) {
            BILogger.getLogger().error(e.getMessage(), e);
        }
        return null;

    }

    protected void resetCubeWriter() {
        if (isCubeWriterAvailable()) {
            cubeWriter.clear();
            cubeWriter = null;
        }
    }

    protected void resetCubeReader() {
        if (isCubeReaderAvailable()) {
            cubeReader.clear();
            cubeReader = null;
        }
    }

    @Override
    public void clear() {
        resetCubeReader();
        resetCubeWriter();

    }

    @Override
    public int getClassType() {
        return 0;
    }

    @Override
    public CubeVersion getVersion() {
        return null;
    }

    @Override
    public void recordVersion(CubeVersion version) {

    }
}
