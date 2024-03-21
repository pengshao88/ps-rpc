package cn.pengshao.rpc.core.api;

import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.registry.ChangedListener;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/17 10:45
 */
public interface RegistryCenter {

    void start();

    void stop();

    // provider 注册
    void register(String serviceName, InstanceMeta instance);

    void unregister(String serviceName, InstanceMeta instance);

    // consumer 侧
    List<InstanceMeta> fetchAll(String service);

    void subscribe(String service, ChangedListener changedListener);

    class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        public StaticRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void register(String serviceName, InstanceMeta instance) {}

        @Override
        public void unregister(String serviceName, InstanceMeta instance) {}

        @Override
        public List<InstanceMeta> fetchAll(String service) {
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener changedListener) {

        }
    }

}
