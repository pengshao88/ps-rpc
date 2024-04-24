package cn.pengshao.rpc.core.registry.ps;

import cn.pengshao.rpc.core.api.RegistryCenter;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import cn.pengshao.rpc.core.meta.ServiceMeta;
import cn.pengshao.rpc.core.registry.ChangedListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Description: implement for psRegistry center
 *
 * @Author: yezp
 * @date 2024/4/24 22:32
 */
@Slf4j
public class PsRegistryCenter implements RegistryCenter {


    @Value("${psregistry.servers}")
    private List<String> servers;

    @Override
    public void start() {
        log.info(" ====>>>> [PsRegistryCenter] : start with server : {}", servers);
    }

    @Override
    public void stop() {
        log.info(" ====>>>> [PsRegistryCenter] : stop with server : {}", servers);
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [PsRegistryCenter] : register instance:{} for {}", instance, service);

    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {

    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        return null;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener changedListener) {

    }
}
