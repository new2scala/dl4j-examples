package org.ditw.learning.javafx.thermoapp;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.ditw.learning.thermoapp.DataHelpers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

final class OneDriveHelpers {

    private final static DefaultHttpClient httpClient = new DefaultHttpClient();
    static void download(String downUrl, ImageView img) {
        try {
            HttpResponse resp = httpClient.execute(new HttpGet(downUrl));
            byte[] bytes = EntityUtils.toByteArray(resp.getEntity());

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            img.setImage(
                new Image(bis)
            );
            bis.close();
        }
        catch (Exception ex) {
            System.out.println("http req failed 2");
        }
    }

//    static DataHelpers.OneDriveFolderResp testToken(ImageView img) {
//        HttpGet hget = new HttpGet(
//            //"https://graph.microsoft.com/v1.0/drives/cfb035373190649d/root/children"
//            //"https://graph.microsoft.com/v1.0/drive/items/CFB035373190649D!120"
//            "https://graph.microsoft.com/v1.0/drive/root:/Icons64:/children"
//            //"https://graph.microsoft.com/v1.0/drive/items/CFB035373190649D!1792/content"
//        );
//
//        String authHeader = String.format("bearer {%s}", AuthHelper.getToken());
//        hget.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
//        //String last = null;
//        try {
//            HttpResponse resp = httpClient.execute(hget);
//            String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
//            DataHelpers.OneDriveFolderResp folder = DataHelpers.parseOneDriveItemJson(respBody);
//            System.out.println(folder.value().length);
//            return folder;
//
//        //    last = folder.downloadLinks()[folder.downloadLinks().length-1];
//
//        }
//        catch (Exception ex) {
//            System.out.println("http req failed");
//            return null;
//        }
//
//
////        if (last != null) {
////            HttpGet getLast = new HttpGet(last);
////            try {
////                HttpResponse resp = httpClient.execute(getLast);
////                byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
////
////                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
////                img.setImage(
////                    new Image(bis)
////                );
////                bis.close();
////            }
////            catch (Exception ex) {
////                System.out.println("http req failed 2");
////            }
////        }
//    }
}
