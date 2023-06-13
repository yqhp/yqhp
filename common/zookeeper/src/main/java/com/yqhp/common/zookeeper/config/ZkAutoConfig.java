/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yqhp.common.zookeeper.config;

import com.yqhp.common.zookeeper.ZkTemplate;
import com.yqhp.common.zookeeper.exception.ZkException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.ensemble.fixed.FixedEnsembleProvider;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.CompressionProvider;
import org.apache.curator.framework.imps.GzipCompressionProvider;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.curator.utils.DefaultZookeeperFactory;
import org.apache.curator.utils.ZookeeperFactory;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * @author jiangyitao
 */
@Configuration
@ConditionalOnClass(ZkTemplate.class)
@EnableConfigurationProperties(ZkProperties.class)
public class ZkAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public EnsembleProvider ensembleProvider(ZkProperties zkProperties) {
        return new FixedEnsembleProvider(zkProperties.getAddr());
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryPolicy retryPolicy(ZkProperties zkProperties) {
        ZkProperties.Retry retry = zkProperties.getRetry();
        return new BoundedExponentialBackoffRetry(retry.getBaseSleepTimeMs(), retry.getMaxSleepTimeMs(), retry.getMaxRetries());
    }

    @Bean
    @ConditionalOnMissingBean
    public CompressionProvider compressionProvider() {
        return new GzipCompressionProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public ZookeeperFactory zookeeperFactory() {
        return new DefaultZookeeperFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public ACLProvider aclProvider() {
        return new ACLProvider() {
            @Override
            public List<ACL> getDefaultAcl() {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }

            @Override
            public List<ACL> getAclForPath(String path) {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public CuratorFrameworkFactory.Builder builder(EnsembleProvider ensembleProvider,
                                                   RetryPolicy retryPolicy,
                                                   CompressionProvider compressionProvider,
                                                   ZookeeperFactory zookeeperFactory,
                                                   ACLProvider aclProvider,
                                                   ZkProperties zkProperties) {
        Charset charset = Charset.forName(zkProperties.getCharset());

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .ensembleProvider(ensembleProvider)
                .retryPolicy(retryPolicy)
                .compressionProvider(compressionProvider)
                .zookeeperFactory(zookeeperFactory)
                .namespace(zkProperties.getNamespace())
                .sessionTimeoutMs(zkProperties.getSessionTimeoutMs())
                .connectionTimeoutMs(zkProperties.getConnectionTimeoutMs())
                .maxCloseWaitMs(zkProperties.getMaxCloseWaitMs())
                .defaultData(zkProperties.getDefaultData().getBytes(charset))
                .canBeReadOnly(zkProperties.isCanBeReadOnly());

        if (!zkProperties.isUseContainerParentsIfAvailable()) {
            builder.dontUseContainerParents();
        }

        ZkProperties.Authorization authorization = zkProperties.getAuthorization();
        if (StringUtils.hasText(authorization.getAuth())) {
            builder.authorization(authorization.getScheme(), authorization.getAuth().getBytes(charset));
            builder.aclProvider(aclProvider);
        }

        String threadFactoryClassName = zkProperties.getThreadFactoryClassName();
        if (StringUtils.hasText(threadFactoryClassName)) {
            try {
                ThreadFactory threadFactory = (ThreadFactory) (Class.forName(threadFactoryClassName).newInstance());
                builder.threadFactory(threadFactory);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new ZkException(e);
            }
        }

        return builder;
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean
    public ZkTemplate zkTemplate(CuratorFrameworkFactory.Builder builder) {
        return new ZkTemplate(builder);
    }
}
