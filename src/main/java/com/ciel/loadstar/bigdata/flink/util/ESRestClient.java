package com.ciel.loadstar.bigdata.flink.util;

import com.ciel.loadstar.bigdata.flink.config.ElkConfig;
import com.ciel.loadstar.bigdata.flink.config.ElkHost;
import com.ciel.loadstar.bigdata.flink.config.EsConfigUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.ArrayList;

/**
 * @author cielqian
 * @email qianhong91@outlook.com
 * @date 2019/5/7 16:57
 */
public class ESRestClient {
    public static ElkConfig elkConfig;

    public static RestHighLevelClient client = null;

    public static RestHighLevelClient getClient(){
        if(client != null){
            return client;
        }else {
            synchronized(ESRestClient.class) {
                if (elkConfig == null){
                    ElkConfig newElkConfig = EsConfigUtil.getConfig();
                    elkConfig = newElkConfig;
                }
                HttpHost[] httpHosts = new HttpHost[elkConfig.getHosts().size()];
                String username = "", password = "";
                for (int i = 0; i<httpHosts.length;i++){
                    ElkHost host = elkConfig.getHosts().get(i);
                    httpHosts[i] = new HttpHost(host.getIp(),Integer.parseInt(host.getPort()),host.getSchema());
                    username = host.getUsername();
                    password = host.getPassword();
                }
                RestClientBuilder clientBuilder = RestClient.builder(httpHosts);

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

                clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder ) {
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider);
                    }
                })
                        .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                            @Override
                            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                                return builder.setConnectTimeout(5000).setSocketTimeout(60000);
                            }
                        });

                client = new RestHighLevelClient(clientBuilder);
                return client;
            }
        }
    }
}
