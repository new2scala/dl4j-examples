package org.ditw.learning.javafx.thermoapp;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

final class OneDriveHelpers {

    static void testToken() {
        HttpGet hget = new HttpGet(
            //"https://graph.microsoft.com/v1.0/drives/cfb035373190649d/root/children"
            //"https://graph.microsoft.com/v1.0/drive/items/CFB035373190649D!120"
            //"https://graph.microsoft.com/v1.0/drive/root:/Documents:/children"
            "https://graph.microsoft.com/v1.0/drive/items/CFB035373190649D!1792/content"
        );
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String authHeader = String.format("bearer {%s}", AuthHelper.getToken());
        hget.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        try {
            HttpResponse resp = httpClient.execute(hget);
            String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
            System.out.println(respBody);
        }
        catch (Exception ex) {
            System.out.println("http req failed");
        }

    }
}
