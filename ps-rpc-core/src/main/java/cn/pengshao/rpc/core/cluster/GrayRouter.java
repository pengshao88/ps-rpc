package cn.pengshao.rpc.core.cluster;

import cn.pengshao.rpc.core.api.Router;
import cn.pengshao.rpc.core.meta.InstanceMeta;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:灰度路由器
 *
 * @Author: yezp
 * @date 2024/4/1 23:00
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    int grayRatio;
    private final SecureRandom random = new SecureRandom();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        List<InstanceMeta> normalList = new ArrayList<>();
        List<InstanceMeta> grayList = new ArrayList<>();
        providers.forEach(p -> {
            if (p.getParameters().containsKey("gray") && "true".equals(p.getParameters().get("gray"))) {
                grayList.add(p);
            } else {
                normalList.add(p);
            }
        });

        log.debug(" grayRouter grayNodes/normalNodes,grayRatio ===> {}/{},{}",
                grayList.size(), normalList.size(), grayRatio);
        if (normalList.isEmpty() || grayList.isEmpty()) {
            return providers;
        }
        if (grayRatio <= 0) {
            return normalList;
        } else if (grayRatio >= 100) {
            return grayList;
        }

        if (random.nextInt(100) < grayRatio) {
            log.debug(" grayRouter grayNodes ===> {}", grayList);
            return grayList;
        }
        log.debug(" grayRouter normalNodes ===> {}", normalList);
        return normalList;
    }

    public void setGrayRatio(int grayRatio) {
        this.grayRatio = grayRatio;
    }
}
