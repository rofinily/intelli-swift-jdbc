package com.fr.bi.stable.utils.code;


/**
 * BI日志输出
 */
public class BILogger {
    boolean verbose = true;
    public static BILogger logger = null;
//    public CubeLogInfoTotal topicLog;
//    public CubeLogInfoTotal fragmentLog;
//    public CubeLogInfoTotal status;
    public CubeLogInfoTotal cubeLogInfo=new CubeLogInfoTotal(0,"","");

    public static BILogger getLogger() {
        if (logger != null) {
            return logger;
        }
        synchronized (BILogger.class) {
            if (logger == null) {
                logger = new BILogger();
                logger.cubeLogInfo=new CubeLogInfoTotal(0,"","");
            }
        }
        return logger;
    }
    public void error(String message) {
        System.err.println(message);
        addLog(0,"",message);
    }

    public void error(String message, Throwable e) {
        System.err.println(message);
        e.printStackTrace();
        addLog(0,"",message);
    }

    public void info(String message) {
        System.out.println(message);
        addLog(0,message,"");
    }

    public void debug(String message) {
        if (verbose) {
            System.out.println(message);
            addLog(0,message,"");
        }
    }
    
    public void addLog(long costTime,String message,String errMsg){
        this.cubeLogInfo.setCostTime(this.cubeLogInfo.getCostTime()+costTime);
        this.cubeLogInfo.setMessage(this.cubeLogInfo.getMessage()+message);
        this.cubeLogInfo.setErrorMsg(this.cubeLogInfo.getErrorMsg()+errMsg);
    }
    public CubeLogInfoTotal getCubeLogInfo(){
        return this.cubeLogInfo;
    }
}
