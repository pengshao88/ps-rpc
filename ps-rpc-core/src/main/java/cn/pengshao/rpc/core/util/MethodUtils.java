package cn.pengshao.rpc.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        // 输出方法签名
        System.out.println("Method Signature: " + signatureBuilder);
        return signatureBuilder.toString();
    }

    public static List<Field> findAnnotatedField(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(annotationClass)) {
                    result.add(field);
                }
            }
            // 被代理的类 是原始类的子类
            clazz = clazz.getSuperclass();
        }
        return result;
    }

}
