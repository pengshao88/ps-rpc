package cn.pengshao.rpc.core.consumer.http;

import cn.pengshao.rpc.core.api.RpcRequest;
import cn.pengshao.rpc.core.api.RpcResponse;
import cn.pengshao.rpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

/**
 * Description: Interface for http invoker
 *
 * @Author: yezp
 * @date 2024/3/21 21:58
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final OkHttpClient okHttpClient;

    public OkHttpInvoker(int timeout) {
        okHttpClient = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    @Override
    public RpcResponse<Object> post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.debug("reqJson: " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();

        try {
            String respJson = okHttpClient.newCall(request).execute().body().string();
            log.debug("respJson: " + respJson);
            return JSON.parseObject(respJson, RpcResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String post(String requestString, String url) {
        log.debug(" ===> post  url = {}, requestString = {}", requestString, url);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSON_TYPE))
                .build();
        try {
            String respJson = okHttpClient.newCall(request).execute().body().string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
        log.debug(" ===> get url = " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = okHttpClient.newCall(request).execute().body().string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
