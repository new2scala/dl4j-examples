package org.ditw.learning.javafx.thermoapp;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLFormElement;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AuthHelper {

    private static String _AuthUrl = "https://login.live.com/oauth20_authorize.srf?client_id=dcfbb7e5-d75f-4726-9201-bb35a438ef9b&scope=files.read%20offline_access&response_type=token&redirect_uri=https://login.live.com/oauth20_desktop.srf";
    private static URI uri = URI.create(_AuthUrl);

    static WebView createWebClient() {
        WebView wc = new WebView();

        //        CookieManager cm = new CookieManager();
//        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//        java.net.CookieHandler.setDefault(cm);

//        try {
//
//            Map<String, List<String>> headers = new LinkedHashMap<>();
//            Map<String, List<String>> cookies = java.net.CookieHandler.getDefault().get(uri, headers);
//            //List<HttpCookie> cookies = manager.getCookieStore().get(uri);
//            //System.out.println("cookies: " + cookies.size());
//            //List<String> cookieStrs = new ArrayList<>(cookies.size());
//            for (String k : cookies.keySet()) {
//                System.out.println(String.format("\t%s(%d)", cookies.get(k).get(0), cookies.get(k).size()));
//
//                //cookieStrs.add(cookies.get(k).get(0));
////                cookieStrs.add()
//            }
////            headers.put("Set-Cookie", cookieStrs);
//
//        }
//        catch (Exception ex) {
//            System.out.println("failed to setup cookie handler: " + ex.getMessage());
//        }
        //headers.put("Set-Cookie", Arrays.asList("name=value"));
        wc.getEngine().getLoadWorker().stateProperty().addListener(
            new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                    try {
                        if (newValue == Worker.State.SUCCEEDED) {
                            Map<String, List<String>> headers = new LinkedHashMap<>();
                            //java.net.CookieManager manager = new java.net.CookieManager();
                            Map<String, List<String>> cookies = java.net.CookieHandler.getDefault().get(uri, headers);

                            //cm.getCookieStore().get(uri);
                            //List<HttpCookie> cookies = manager.getCookieStore().get(uri);
                            System.out.println("cookies: " + cookies.size());
                            for (String k : cookies.keySet()) {
                                System.out.println(String.format("\t%s: %s(%d)", k, cookies.get(k).get(0), cookies.get(k).size()));
                            }

                            System.out.println("Current location: " + wc.getEngine().getLocation());
                            //wc.getEngine().

                            parseTokenFromURL(wc.getEngine().getLocation());
                            //java.net.CookieHandler.getDefault().put(uri, cookies);
                        }
                    }
                    catch (Exception ex) {
                        System.out.println("failed to setup cookie handler: " + ex.getMessage());
                    }

                }
            }
        );

        wc.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> ov, Document oldDoc, Document doc) {
                System.out.println(doc);
            }
        });
        wc.getEngine().load(_AuthUrl);

        return wc;
    }

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAYUXzrLMQ7yk4yIWh/nvV2sRTpMpQxSKaAThjbuNAXVif5wGSzTMywxlxGQCpSEgfOUJ/kIQ686Fs51IWxPilL%2b9ii3Qhaj0qkk9UnR4HVwG17IqdPx7WigrcK5VBrvzMjX/udvBnzSwF9%2btUo%2bKQ0X0HUxEff1KGokGxVXlhPqKCNiVgzBKj81GuEv%2b197D2SktTKIRV49MVsI7w3yAjBMPkxLbO0Rfwt55AuyQpdVOKL4pw9yCERkcGHApknn69%2baFN065%2b5SPXbNofvw4mvmSehHjgZdA9YEiAr9q9mAIEkhHBIXy33wWmAIGJu/StOfKekpClZXKZlenFWjrCX0DZgAACIAB9emeOFVpMAK%2bgWEaW8FmawJjlA1ccH9vDVImW64f7ECf3Bhef0QhAmvJicGJzYYvoyryJozZP0UXRp/bEqiI6ZbjTml7wrxklon0qvqBukZJMbFLRQ1j2JdrFNRqTpvjwjBKgUfwsZBA%2bXD9lPTu%2b9zw4OCD3nlrFrFZDAA54yrfCByvefOfmRB715vIhlk6aYnL%2bWR6Rqi/nhB3ZDwpe7otdHXvbhLaiuEDb7LAEdRf8pwJ63MW42jZRPIQva8PzyIDl%2bFrOPQm98NwEAIR9GfUeif4etqBj09tQ1fX%2b61E8JoVxQqCAAGtsZre1nq3XVY5NaN3FYREJdfsqQaKzDx1mWpjrcj4IqkeI2sVger2YnC6UaC5%2bmztMakd4XY2QBWy6nnP6EPGkci0t5M61NB2Gi9hmkPl2gPySk5ZZWBrnKbAvSTg8vODtNqTKPG8/C%2bcneCXWzZZcXmWEWW4Tj4laZtHBUQ3/awSVpybd%2b02qWx8L9%2bvVAQ%2b18/RJezXMfW1d%2bDN043shHuPYfQ5Plb4m75Ps3wkkT6%2bqFcSq2DZZg41ttJIB7Pkh15YIb8j7WFJp6FYjHdYBWDQYVm%2biqvVvPFimqjwcH2lTLVMl2fCJXsRB/8zRyWz8XXqGWJC1orNaio73tAphLj7/Iped6nMeBEQIDPDGbaSU%2b4Nv8Exe7rnO2FZceyA8UzILEgIOmesb3xk%2bHTVOe%2bCOj8pfHH0OanXlxACv7OfdrYWwfxw5CA37b10h2EC";
    private static String accessTokenStr2Find = "access_token=";

    private static void parseTokenFromURL(String url) {
        int start = url.indexOf(accessTokenStr2Find);
        if (start != -1) {
            int end = url.indexOf("&", start);
            accessToken = url.substring(
                start+accessTokenStr2Find.length(),
                (end != -1) ? end : url.length()
            );
        }
    }

    static String getToken() {
        return accessToken;
    }

}
