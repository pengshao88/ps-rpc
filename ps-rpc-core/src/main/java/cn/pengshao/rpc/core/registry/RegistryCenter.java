package cn.pengshao.rpc.core.registry;

import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;

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
    void register(ServiceMeta service, InstanceMeta instance);

    void unregister(ServiceMeta service, InstanceMeta instance);

    // consumer 侧
    List<InstanceMeta> fetchAll(ServiceMeta service);

    void subscribe(ServiceMeta service, ChangedListener changedListener);

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
        public void register(ServiceMeta service, InstanceMeta instance) {}

        @Override
        public void unregister(ServiceMeta service, InstanceMeta instance) {}

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener changedListener) {

        }
    }

}
