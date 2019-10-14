package com.jrodolfo.elasticsearch;

import com.jrodolfo.elasticsearch.util.PropertyUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Properties;

class TargetSystemElasticSearch {

    private String hostName;
    private String port;
    private String userName;
    private String passWord;

    TargetSystemElasticSearch() {
        Properties properties = PropertyUtil.getProperties();
        hostName = properties.getProperty("elasticsearch.hostName");
        port = properties.getProperty("elasticsearch.port");
        userName = properties.getProperty("elasticsearch.userName");
        passWord = properties.getProperty("elasticsearch.passWord");
    }

    RestHighLevelClient createClient() {
        // not necessary if you run a local elasticsearch
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passWord));
        RestClientBuilder builder = RestClient.builder(new HttpHost(hostName, Integer.parseInt(port), "https"))
                .setHttpClientConfigCallback(
                        httpAsyncClientBuilder ->
                                httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        return new RestHighLevelClient(builder);
    }
}
