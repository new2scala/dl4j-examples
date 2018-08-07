package org.ditw.learning.javafx.thermoapp;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AuthHelper {

    private static String _AuthUrl = "https://login.live.com/oauth20_authorize.srf?client_id=dcfbb7e5-d75f-4726-9201-bb35a438ef9b&scope=files.read&response_type=token&redirect_uri=https://login.live.com/oauth20_desktop.srf";
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
        wc.getEngine().load(_AuthUrl);

        return wc;
    }

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAUqxeu0uhAIBvVtxkbkuYCnoXz6mOjxZExlA8tduCxWJusn0RW/6m4phUcMI75m2TzE3Krwvti0gkP6KMkGt2rgCJCUXuV2qE7qMuT/hv5WsaniJoJ0MVQtLTLamsZqaClnAxwHWT%2bPrLDhsar5u/7M0IkvMu0LVTIOjHQ17d/7137WnF6krTRvNCwxNxLpP9CePGBfkhxeQTX2OHBG/nI7B7vzZxzD9seU53wTFDOCpLso1hKet0HXUaeHrr24wUKH3c482X2RHwxj3LKboXANDE76C5jDhv7zc/xJ5AZrCMAC0dHDaAEMi52LgA9yCJJ/VP6F17iqFFyGeLcNbvcADZgAACF0rXwUfFENiMAJ5p7Pz68cvZCJe%2bG7rpZ9keRxnSsodgTuPeCArk2dWCZNmYIY8qJMKCY7PnpeT01Rw7IIMpqz2zHk4vKhgK3IBmAI8pHbOI4qYb%2bkeNpfb8naCCXiav8Xzqr9%2buctp7%2bZkv2ea74u2HJoVJm857SE%2bIrkp%2bo83gnjT7Wd7Z4ZuKGI/6b0SZWl2IDRfxOADJAAIwyVxPEFTD9EtT45MyXawYtjCcuK1mELe0Uq1fOXcQPlr94EJn8V6tvwAkoCtNm9eeaWTO1eKSLGwfksXjshJrxnzpZic93qQI9QPOrRWDKRvvp2guwEAVK3Wky82F7xV2pl4vEGvGETYQAvYSn5l%2bCqb1UG6uIL/BtitHnOB3ADXShmkweHuBipMT0LxTFA53vkdyQUl3GAuwWqs4M2%2bwyGNvrMEsEtOCPmQ4ZuxyLyJrCRfujB5wBP6hSwpQxaNmn5u1scJxy6PCJtdOCvwZST6F47cBqTQx8OE6QqndMODoMZZ/I9aYVh1vTZEXJHSwNcQEJRJBgaxbRP4vBOgWfFVaCMuw2HZLI62dBaoVYDLXr7tqRVaOXX/7f4%2bhXHjIENs4juVfUrDv1DQlnnp5EK%2bCgn9ktUsi2hHCeTLCIuoap2xjbBoVh8ksjoTxm7NVUm0D/QXukVcRo2ShOx/CdoW%2bAPVhLZOLrVDntJ2JW5QOwiqLovMY3JxUaemDPJ/5nf442LElOcLhvEMmTQfPPqL0cQYmNPvqLdMF9L80GEC";
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
