/**
 * 
 */
package com.fr.bi.module;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.BICore;
import com.fr.bi.base.BIUser;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.etl.analysis.manager.BIAnalysisETLManagerCenter;
import com.fr.bi.etl.analysis.manager.UserETLCubeManagerProvider;
import com.fr.bi.stable.data.BIField;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;
import com.fr.bi.stable.utils.program.BIConstructorUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel
 *
 */
public class UserETLCubeTILoader implements ICubeDataLoader {

    public static Map<Long, UserETLCubeTILoader> userMap = new ConcurrentHashMap<Long, UserETLCubeTILoader>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 3766201942015813978L;

    private BIUser user;

	/**
	 * @param userId
	 */
	public UserETLCubeTILoader(long userId) {
        this.user = new BIUser(userId);
	}

    public static UserETLCubeTILoader getInstance(long userId) {
        return BIConstructorUtils.constructObject(userId, UserETLCubeTILoader.class, userMap, false);
    }

    private UserETLCubeManagerProvider getCubeManager(){
        return BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider();
    }

    public ICubeTableService getTableIndex(BusinessTable td) {
        return getTableIndex(new BITableID(td.getID()));
    }

    public ICubeTableService getTableIndex(BICore core) {
        return getCubeManager().getTableIndex(core, user);
    }

    public ICubeTableService getTableIndex(BIField td) {
        return getTableIndex(td.getTableID());
    }

    public BIKey getFieldIndex(BIField column) {
        return new IndexKey(column.getFieldName());
    }

    public ICubeTableService getTableIndex(BITableID id) {
        return getCubeManager().getTableIndex(BIAnalysisETLManagerCenter.getDataSourceManager().getCoreByTableID(id, user), user);
    }

    @Override
    public long getUserId() {
        return user.getUserId();
    }

    @Override
    public boolean needReadTempValue() {
        return false;
    }

    @Override
    public boolean needReadCurrentValue() {
        return false;
    }

    @Override
    public SingleUserNIOReadManager getNIOReaderManager() {
        return null;
    }

    @Override
	public void releaseCurrentThread(){
        UserETLCubeManagerProvider manager = BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider();
        if(manager != null){
            manager.releaseCurrentThread();
        }
    }

    @Override
    public ICubeTableService getTableIndex(CubeTableSource tableSource, int start, int end) {
        return getTableIndex(tableSource);
    }

    @Override
    public ICubeTableService getTableIndex(CubeTableSource tableSource) {
        return null;
    }

    @Override
    public BIKey getFieldIndex(BusinessField column) {
        return null;
    }

    @Override
	public void clear() {
        synchronized (this) {
            if(userMap != null){
                userMap.clear();
            }
        }
	}
}