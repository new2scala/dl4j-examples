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

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAe3f0GB6Xbc%2bCDBFpdGE%2bet8QSkr%2bk9J1gA6p8ANoJ5/i97rYYrh4I7V9uSXjCtDwd/2u4HNK77L56jKGEwvNyFgiR1g0wseurwxY2sSKO6NJDl/tkiVCVpH6uvB3/NLzXp6lr%2bcsm38EiEcgtsJx60i/rCqJ8eA0tabSa2e01V4Pa%2bfqFhGlq%2bPmr05Zibvg%2bgdPhy5qNpWXkJwJ6wscwPy3jNbtT7/BLCJznqjjRmFs1hPASokOLIy4LVEhUtKkawz4dFnTtvAWanlAePuzz5ZItGdOpUNGPFsMtrIwB8coUhR21dgqkdeXvPpfEhjQiVqWoqLCrJ8/Iy8eyRQ7JEDZgAACI8TUa5Nn2IrMAKnxtw1QeVamEiu/nOGl%2bsF2YSzFz1w3JOu8gJTDgYV/fGXrY3/r3u6bZf6kP7UY4kZnyPgewOUiP2dOh74zN887NvoUmbqWDCvE/%2biY8o2gbspChKrCW7QtFwEaXTg7FX1i2EZ5qKpcawgqWiL3ro%2bgUtzpzL0OEdm77IkdJA/z0KQT94qEcyO3CFhvgDkz4pybfDJa1Da7qlzKIzdxbZ/d3%2bR6pXKaj00wmXBYo9mVtyIMsqKPFjerbFR6WwzuEzFJtdvAzNuc%2b9OeIjCRaPhuuf/w5J720%2bCh3f0lpm/f02iZZu6GIfxiwYNtn7mmO2XJmp/N5sJfUQFmLfGgvSvPLhYSIlSJQgsh7vGkfOs1qjOD7fFZwzURDoELTGr7ekU2g7S%2bqhFZQvH4OK%2b2j5tUvgFTv08M4Hewqd7OE7qcd8DSdiMTLi6y4Ag0BU8R4RgWzDXVQf3r0qYK8pFpnpHQHV7pT9F/xcsTtAL/PLqSZ5/kMtK%2bhomVZlTVDtfFx2UQuGjG%2bIWhrPFp4zMlt4L1NFnwjePzkTz3Nzn%2bvuRC9FViDAzWKqRb%2bzTYuJx3wFkMvUchMk686Adn%2bLIslgLhxyaa58M3I6L3QEnThRMaywlwcv6ETIRjd%2bZcbYvQAwksBrvH0ZDzfl0aeix8WDhqItWwlkVCNMzm3vEspuTw9kKmhNwdLrHIrT%2b/knipbIGdPcWBIWtNhOO2oenxcoXEUJHyu8a5p1SFsnl2hbhPWEC";
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
