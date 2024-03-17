package cn.pengshao.rpc.core.consumer;

import cn.pengshao.rpc.core.api.*;
import cn.pengshao.rpc.core.util.MethodUtils;
import cn.pengshao.rpc.core.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/3/11 22:15
 */
public class PsInvocationHandler implements InvocationHandler {

    private final Class<?> service;
    private final RpcContext context;
    private final List<String> providers;

    public PsInvocationHandler(Class<?> service, RpcContext context, List<String> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
    }

    final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }
        List<String> routedProviders = context.getRouter().route(providers);
        String url = (String) context.getLoadBalancer().choose(routedProviders);

        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtils.getMethodSign(method));
        request.setArgs(args);

        // todo typeReference 无法使用 class,需要知道真实的类型
//        String response = postReturnString(request);
//        Class<? extends Type> actualType = method.getGenericReturnType().getClass();
//        JSON.parseObject(response, new TypeReference<RpcResponse<actualType>>());

        System.out.println("loadBalancer.choose(urls) ===> " + url);
        RpcResponse rpcResponse = post(request, url);
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            Class<?> type = method.getReturnType();
            System.out.println("method.getReturnType() = " + type);
            if (data instanceof JSONObject jsonResult) {
                if (Map.class.isAssignableFrom(type)) {
                    Map resultMap = new HashMap();
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println(genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                        System.out.println("keyType  : " + keyType);
                        System.out.println("valueType: " + valueType);
                        jsonResult.forEach((k, v) -> {
                            Object key = TypeUtils.cast(k, keyType);
                            Object value = TypeUtils.cast(v, valueType);
                            resultMap.put(key, value);
                        });
                    }
                    return resultMap;
                }
                return jsonResult.toJavaObject(type);
            } else if (data instanceof JSONArray jsonArray) {
                Object[] array = jsonArray.toArray();
                if (type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    Object resultArray = Array.newInstance(componentType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        Array.set(resultArray, i, array[i]);
                    }
                    return resultArray;
                } else if (List.class.isAssignableFrom(type)) {
                    List<Object> resultList = new ArrayList<>(array.length);
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println(genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Type actualType = parameterizedType.getActualTypeArguments()[0];
                        System.out.println(actualType);
                        for (Object o : array) {
                            resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
                        }
                    } else {
                        resultList.addAll(Arrays.asList(array));
                    }
                    return resultList;
                } else {
                    return null;
                }
            } else {
                return TypeUtils.cast(data, method.getReturnType());
            }
        } else {
            throw new RuntimeException(rpcResponse.getMsg());
        }
    }

    private RpcResponse post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("reqJson: " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();

        try {
            String respJson = HTTP_CLIENT.newCall(request).execute().body().string();
            System.out.println("respJson: " + respJson);
            RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String postReturnString(RpcRequest rpcRequest) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("reqJson: " + reqJson);
        Request request = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();

        try {
            String respJson = Objects.requireNonNull(HTTP_CLIENT.newCall(request).execute().body()).string();
            System.out.println("respJson: " + respJson);
            return respJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
