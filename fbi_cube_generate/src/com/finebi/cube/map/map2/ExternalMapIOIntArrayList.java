package com.finebi.cube.map.map2;

import com.finebi.cube.common.log.BILogger;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.map.ExternalMapIO;
import com.fr.bi.stable.io.newio.NIOReader;
import com.fr.bi.stable.io.newio.NIOWriter;
import com.fr.bi.stable.io.newio.read.IntNIOReader;
import com.fr.bi.stable.io.newio.write.IntNIOWriter;
import com.fr.bi.stable.structure.array.IntList;
import com.fr.bi.stable.structure.array.IntListFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by wang on 2016/9/2.
 */
public abstract class ExternalMapIOIntArrayList<K> implements ExternalMapIO<K, IntList> {
    protected IntNIOWriter valueWriter = null;
    protected IntNIOReader valueReader = null;
    protected Position positionWriter;
    protected Position positionReader;
    protected File keyFile;
    protected File valueFile;
    protected NIOWriter<K> keyWriter = null;
    protected NIOReader<K> keyReader = null;
    protected int size;
    private static BILogger LOGGER = BILoggerFactory.getLogger(ExternalMapIOIntArrayList.class);
    

    public ExternalMapIOIntArrayList(String ID_path) {
        String intPath = getValuePath(ID_path);
        String keyPath = getKeyPath(ID_path);
        keyFile = initialFile(keyPath);
        valueFile = initialFile(intPath);
        positionReader = new Position();
        positionWriter = new Position();
    }

    protected String getValuePath(String ID_path) {
        if (ID_path != null) {
            return ID_path + "_value";
        }
        return null;
    }

    protected String getKeyPath(String ID_path) {
        if (ID_path != null) {
            return ID_path + "_key";
        }
        return null;
    }

    public File initialFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if (file.getParentFile().exists()) {
                    file.createNewFile();
                } else {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return file;
    }

    public NIOWriter<K> getKeyWriter() {
        if (keyWriter == null) {
            initialKeyWriter();
        }
        return keyWriter;
    }

    public NIOReader<K> getKeyReader() throws FileNotFoundException {
        if (keyReader == null) {
            initialKeyReader();
        }
        return keyReader;
    }

    public IntNIOWriter getValueWriter() {
        if (valueWriter == null) {
            initialValueWriter();
        }
        return valueWriter;
    }

    public IntNIOReader getValueReader() throws FileNotFoundException {
        if (valueReader == null) {
            initialValueReader();

        }
        return valueReader;
    }

    abstract void initialKeyReader() throws FileNotFoundException;

    abstract void initialKeyWriter();

    private  void initialValueReader() throws FileNotFoundException{
        if (valueFile.exists()) {
            valueReader = new IntNIOReader(valueFile);
        } else {
            throw new FileNotFoundException();
        }
    }


    private void initialValueWriter(){
        valueWriter = new IntNIOWriter(valueFile);
    }

    @Override
    public void write(K key, IntList value) {
        writeKey(key);
        /**
         * 记录下来有多少个数据，以为用到writer对象。
         */
        getValueWriter().add(positionWriter.valuePosition++, value.size());
        for (int i = 0; i < value.size(); i++) {
            getValueWriter().add(positionWriter.valuePosition++, value.get(i));
        }
        value.clear();
    }

//    abstract int recordAmount(IntArrayList value);

    public void writeKey(K key) {
        getKeyWriter().add(positionWriter.keyPosition++, key);
    }

    public K readKey() throws FileNotFoundException {
        return getKeyReader().get(positionReader.keyPosition++);
    }

    @Override
    public Map<K, IntList> read() throws FileNotFoundException {
        if (canRead()) {
            K key = readKey();
            int amount = getValueReader().get(positionReader.valuePosition++);
            IntList list = generateList();
            for (int i = 0; compare(i, amount); i++) {
                list.add(getValueReader().get(positionReader.valuePosition++));
            }
            if (!isEmpty(key) || list.size() != 0) {
                Map<K, IntList> result = new HashMap<K, IntList>();
                result.put(key, list);
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private Boolean compare(int i, Integer amount){
        return i < amount;
    }

    private IntList generateList(){
        return IntListFactory.createIntList();
    }

    private boolean canRead() {
        return positionReader.keyPosition < size;
    }

    public abstract boolean isEmpty(K key);

    @Override
    public void close() {
        getValueWriter().clear();
        try {
            getValueReader().clear();
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        try {
            getKeyReader().clear();
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        getKeyWriter().clear();
    }

    @Override
    public void setSize(Integer size) {
        this.size = size;
    }

    class Position {
        public long keyPosition;
        public long valuePosition;
    }
}
