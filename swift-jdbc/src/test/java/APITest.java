import com.fr.swift.api.rpc.SimpleDetailQueryBean;
import com.fr.swift.jdbc.rpc.RpcCaller;
import com.fr.swift.source.ListBasedRow;
import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftResultSet;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author yee
 * @date 2018/8/24
 */
public class APITest implements Serializable {

    public static RpcCaller api;
    private static List<Row> datas;

    @BeforeClass
    public static void setUp() {
        api = RpcCaller.connect("192.168.0.102:7000");
        datas = new ArrayList<Row>();
        for (int i = 0; i < 100; i++) {
            datas.add(new ListBasedRow(Arrays.<Object>asList(i + 90, "zorgname_t" + i)));
        }
    }

    @Test
    @Ignore
    public void insert() throws SQLException {
        assertEquals(api.insert("test_table", Arrays.asList("id", "name"), datas), datas.size());
    }

    @Test
    @Ignore
    public void insert1() throws SQLException {
        assertEquals(api.insert("test_table", datas), datas.size());
    }

    @Test
    @Ignore
    public void query() throws SQLException {
//        aa4f69b2
        SimpleDetailQueryBean bean = new SimpleDetailQueryBean();
        bean.setTable("e88238c2");
        bean.setColumns(Arrays.asList("id", "name"));
        SwiftResultSet resultSet = api.query(bean.getQueryString());
        while (resultSet.hasNext()) {
            System.out.println(resultSet.getNextRow().getValue(0));
        }
        resultSet.close();
    }

}