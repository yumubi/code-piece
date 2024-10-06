package io.goji.exp.spider;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProxyTest {

    public static void main(String[] args) throws Exception {

        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "7890");


        // Configure HttpClient with Clash proxy
        HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("localhost", 7890)))  // Clash HTTP proxy
            .build();

        // Make a request to a service that returns your IP address
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.youtube.com/"))  // Use ipinfo.io/ip for another option
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the response (should show the IP address of the proxy, not your local IP)
        System.out.println("Response: " + response.body());
        System.out.println("Headers: " + response.headers());
    }
}
