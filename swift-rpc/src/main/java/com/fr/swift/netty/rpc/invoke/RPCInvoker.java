package com.fr.swift.netty.rpc.invoke;

import com.fr.swift.basics.Invocation;
import com.fr.swift.basics.Invoker;
import com.fr.swift.basics.Result;
import com.fr.swift.basics.URL;
import com.fr.swift.basics.base.SwiftResult;
import com.fr.swift.netty.rpc.bean.RpcRequest;
import com.fr.swift.netty.rpc.bean.RpcResponse;
import com.fr.swift.netty.rpc.client.AbstactRpcClientHandler;
import com.fr.swift.netty.rpc.client.async.AsyncRpcClientHandler;
import com.fr.swift.netty.rpc.client.async.RpcFuture;
import com.fr.swift.netty.rpc.client.sync.SyncRpcClientHandler;
import com.fr.swift.netty.rpc.pool.AsyncRpcPool;
import com.fr.swift.netty.rpc.pool.SyncRpcPool;
import com.fr.swift.util.concurrent.SwiftExecutors;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * This class created on 2018/6/7
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class RPCInvoker<T> implements Invoker<T> {

    private static int nThreads = Runtime.getRuntime().availableProcessors() * 2;

    private static ExecutorService handlerPool = SwiftExecutors.newFixedThreadPool(nThreads);

    private final T proxy;

    private final Class<T> type;

    private final URL url;

    private boolean sync = true;

    public RPCInvoker(T proxy, Class<T> type, URL url, boolean sync) {
        this(proxy, type, url);
        this.sync = sync;
    }

    public RPCInvoker(T proxy, Class<T> type, URL url) {
        if (type == null) {
            throw new IllegalArgumentException("interface == null");
        }
        this.proxy = proxy;
        this.type = type;
        this.url = url;
    }

    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public Result invoke(Invocation invocation) {
        try {
            return new SwiftResult(doInvoke(proxy, invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments()));
        } catch (Throwable e) {
            return new SwiftResult(e);
        }
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {

    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    protected Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Throwable {
        String serviceAddress = url.getDestination().getId();
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(type.getName());
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(arguments);
        return rpcSend(request, serviceAddress);
    }

    private Object rpcSend(RpcRequest request, String serviceAddress) throws Throwable {
        AbstactRpcClientHandler handler = null;
        try {
            if (sync) {
                handler = getAvailableSyncHandler(serviceAddress);
                RpcResponse response = (RpcResponse) handler.send(request);
                SyncRpcPool.getIntance().returnObject(serviceAddress, handler);
                if (response == null) {
                    throw new RuntimeException("response is null");
                }
                if (response.hasException()) {
                    throw response.getException();
                } else {
                    return response.getResult();
                }
            } else {
                handler = getAvailableAsyncHandler(serviceAddress);
                RpcFuture rpcFuture = (RpcFuture) handler.send(request);
                return rpcFuture;
            }
        } catch (Throwable e) {
            if (handler != null) {
                if (handler instanceof SyncRpcClientHandler) {
                    SyncRpcPool.getIntance().invalidateObject(serviceAddress, handler);
                } else {
                    AsyncRpcPool.getIntance().invalidateObject(serviceAddress, handler);
                }
            }
            throw e;
        }
    }

    private SyncRpcClientHandler getAvailableSyncHandler(String serviceAddress) throws Exception {
        SyncRpcClientHandler handler = (SyncRpcClientHandler) SyncRpcPool.getIntance().borrowObject(serviceAddress);
        if (!handler.isActive()) {
            SyncRpcPool.getIntance().returnObject(serviceAddress, handler);
            SyncRpcPool.getIntance().invalidateObject(serviceAddress, handler);
            handler = (SyncRpcClientHandler) SyncRpcPool.getIntance().borrowObject(serviceAddress);
        }
        return handler;
    }

    private AsyncRpcClientHandler getAvailableAsyncHandler(String serviceAddress) throws Exception {
        AsyncRpcClientHandler handler = (AsyncRpcClientHandler) AsyncRpcPool.getIntance().borrowObject(serviceAddress);
        if (!handler.isActive()) {
            AsyncRpcPool.getIntance().returnObject(serviceAddress, handler);
            AsyncRpcPool.getIntance().invalidateObject(serviceAddress, handler);
            handler = (AsyncRpcClientHandler) AsyncRpcPool.getIntance().borrowObject(serviceAddress);
        }
        return handler;
    }
}
