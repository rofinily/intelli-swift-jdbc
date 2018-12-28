package com.fr.swift.basics.base.handler;

import com.fr.swift.SwiftContext;
import com.fr.swift.basic.URL;
import com.fr.swift.basics.Invoker;
import com.fr.swift.basics.InvokerCreator;
import com.fr.swift.basics.annotation.Target;
import com.fr.swift.basics.base.selector.UrlSelector;
import com.fr.swift.basics.handler.MasterProcessHandler;
import com.fr.swift.config.bean.SwiftServiceInfoBean;
import com.fr.swift.config.service.SwiftServiceInfoService;
import com.fr.swift.util.MonitorUtil;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author yee
 * @date 2018/10/24
 */
public class SwiftMasterProcessHandler extends AbstractProcessHandler<URL> implements MasterProcessHandler {

    public SwiftMasterProcessHandler(InvokerCreator invokerCreator) {
        super(invokerCreator);
    }

    /**
     * 处理master的远程调用
     *
     * @param method
     * @param target
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object processResult(Method method, Target target, Object... args) throws Throwable {
        Class proxyClass = method.getDeclaringClass();
        Class<?>[] parameterTypes = method.getParameterTypes();
        String methodName = method.getName();
        try {
            MonitorUtil.start();
            URL masterUrl = processUrl(target, args);
            Invoker invoker = invokerCreator.createSyncInvoker(proxyClass, masterUrl);
            Object object = invoke(invoker, proxyClass, method, methodName, parameterTypes, args);
            return object;
        } finally {
            MonitorUtil.finish(methodName);
        }
    }

    /**
     * 计算master的url
     *
     * @param target
     * @param args
     * @return
     */
    @Override
    public URL processUrl(Target target, Object... args) {
        List<SwiftServiceInfoBean> swiftServiceInfoBeans = SwiftContext.get().getBean(SwiftServiceInfoService.class)
                .getServiceInfoByService(SwiftServiceInfoService.SERVICE);
        SwiftServiceInfoBean swiftServiceInfoBean = swiftServiceInfoBeans.get(0);
        URL url = UrlSelector.getInstance().getFactory().getURL(swiftServiceInfoBean.getServiceInfo());
        return url;
    }
}
