package cn.pengshao.rpc.core.provider;

import cn.pengshao.rpc.core.api.RpcException;
import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.enums.ErrorCodeEnum;
import cn.pengshao.rpc.core.meta.ProviderMeta;
import cn.pengshao.rpc.core.util.TypeUtils;
import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/21 22:29
 */
@Data
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            ProviderMeta providerMeta = findProviderMeta(providerMetas, request.getMethodSign());
            if (providerMeta == null) {
                return new RpcResponse<>(false, null, new RpcException(ErrorCodeEnum.NO_SUCH_METHOD.getErrorMsg()));
            }

            Method method = providerMeta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(providerMeta.getServiceImpl(), args);
            return new RpcResponse<>(true, result, null);
        } catch (InvocationTargetException e) {
            return new RpcResponse<>(false, null, new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            return new RpcResponse<>(false, null, new RpcException(e.getMessage()));
        }
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actualArr = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actualArr[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return actualArr;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }
}
