package com.fr.swift.decision.config;

import com.fr.config.ConfigContext;
import com.fr.config.Configuration;
import com.fr.config.holder.Conf;
import com.fr.config.holder.factory.Holders;
import com.fr.swift.config.SwiftConfigConstants;
import com.fr.swift.config.bean.SwiftFileSystemType;
import com.fr.swift.config.service.SwiftRepositoryConfService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.decision.config.base.SwiftAbstractObjectMapConfig;
import com.fr.swift.decision.config.unique.RepositoryConfigUnique;
import com.fr.transaction.Configurations;
import com.fr.transaction.Worker;

/**
 * @author yee
 * @date 2018/6/15
 */
public class SwiftRepositoryConfig extends SwiftAbstractObjectMapConfig<RepositoryConfigUnique> {

    private static SwiftRepositoryConfig config;
    private Conf<String> type = Holders.simple(SwiftFileSystemType.FR.name());

    public SwiftRepositoryConfig() {
        super(RepositoryConfigUnique.class);
    }

    public static SwiftRepositoryConfig getInstance() {
        if (null == config) {
            config = ConfigContext.getConfigInstance(SwiftRepositoryConfig.class);
        }
        return config;
    }

    public RepositoryConfigUnique getCurrentRepository() {
        return get(type.get());
    }

    public void setCurrentRepository(final SwiftFileSystemType type, RepositoryConfigUnique bean) {
        Configurations.update(new Worker() {
            @Override
            public void run() {
                SwiftRepositoryConfig.this.type.set(type.name());
            }

            @Override
            public Class<? extends Configuration>[] targets() {
                return new Class[]{SwiftRepositoryConfig.class};
            }
        });
        addOrUpdate(type.name(), bean);
    }

    @Override
    public boolean addOrUpdate(String key, RepositoryConfigUnique bean) {
        if (SwiftFileSystemType.valueOf(key) != SwiftFileSystemType.FR) {
            super.addOrUpdate(key, bean);
            return SwiftContext.getInstance().getBean(SwiftRepositoryConfService.class).setCurrentRepository(bean.convert());
        }
        return true;
    }

    @Override
    public String getNameSpace() {
        return SwiftConfigConstants.FRConfiguration.REPOSITORY_CONF_NAMESPACE;
    }
}
