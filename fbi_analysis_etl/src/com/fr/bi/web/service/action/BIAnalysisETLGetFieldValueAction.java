package com.fr.bi.web.service.action;

import com.finebi.cube.api.ICubeTableService;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.etl.analysis.Constants;
import com.fr.bi.etl.analysis.data.AnalysisETLSourceFactory;
import com.fr.bi.etl.analysis.data.UserTableSource;
import com.fr.bi.stable.constant.BIJSONConstant;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by 小灰灰 on 2016/4/19.
 */
public class BIAnalysisETLGetFieldValueAction extends AbstractAnalysisETLAction{
    private static final int MAX_ROW = 1000;

    @Override
    public void actionCMD(HttpServletRequest req, HttpServletResponse res, String sessionID) throws Exception {
        String field = WebUtils.getHTTPRequestParameter(req, "field");
        if(StringUtils.isEmpty(field)){
            WebUtils.printAsJSON(res, new JSONObject());
            return;
        }
        long userId = ServiceUtils.getCurrentUserID(req);
        String tableJSON = WebUtils.getHTTPRequestParameter(req, "table");
        JSONObject jo = new JSONObject(tableJSON);
        JSONArray items = jo.getJSONArray(Constants.ITEMS);
        UserTableSource source = AnalysisETLSourceFactory.createTableSource(items, userId).createUserTableSource(userId);
        ICubeTableService service = new PartCubeDataLoader(userId, source).getTableIndex(source.fetchObjectCore());
        JSONArray ja = new JSONArray();
        BIKey key = new IndexKey(field);
        Set set = new HashSet();
        for (int i = 0; i < service.getRowCount() && set.size() < MAX_ROW; i ++){
            set.add(service.getRow(key, i));
        }
        for (Object ob : set){
            ja.put(ob);
        }
        JSONObject result = new JSONObject();
        result.put(BIJSONConstant.JSON_KEYS.VALUE, ja);
        WebUtils.printAsJSON(res, result);
    }

    @Override
    public String getCMD() {
        return "get_field_value";
    }
}
