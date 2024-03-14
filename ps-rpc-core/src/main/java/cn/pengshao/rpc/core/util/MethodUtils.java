package cn.pengshao.rpc.core.util;

import java.lang.reflect.Method;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/11 22:19
 */
public class MethodUtils {

    /**
     * 本地方法不代理
     *
     * @param method 方法
     * @return true 本地方法
     */
    public static boolean checkLocalMethod(final String method) {
        return "toString".equals(method) ||
                "hashCode".equals(method) ||
                "notifyAll".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notify".equals(method);
    }

    public static String getMethodSign(Method method) {
        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(method.getName());
        // 获取并添加参数类型的简单名称
        for (Class<?> paramType : method.getParameterTypes()) {
            signatureBuilder.append('_').append(paramType.getSimpleName());
        }
        // 添加返回类型
//        signatureBuilder.append("_").append(method.getReturnType().getSimpleName());
        // 输出方法签名
        System.out.println("Method Signature: " + signatureBuilder);
        return signatureBuilder.toString();
    }

}
