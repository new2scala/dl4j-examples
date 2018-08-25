package org.ditw.thermapp;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLInputElement;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AuthHelper {

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
                try {
                    //Thread.sleep(2000);
                    EventTarget evtTarget = (EventTarget)doc;

                    evtTarget.addEventListener("DOMNodeInserted", new EventListener() {
                        @Override
                        public void handleEvent(Event evt) {
                            if (evt.getType().equals("DOMNodeInserted")) {
                                NodeList nodes = doc.getElementsByTagName("form");
                                if (nodes.getLength() > 0) {
                                    HTMLFormElement form = (HTMLFormElement) doc.getElementsByTagName("form").item(0);
                                    if (form.getAttribute("name").equals("f1")) {
                                        System.out.println("found form");
                                        NodeList inputs = form.getElementsByTagName("input");
                                        System.out.println("Inputs length: " + inputs.getLength());
                                        Node emailLogin = null;
                                        for (int i = 0; i < inputs.getLength(); i++) {
                                            Node n = inputs.item(i);
                                            Node nameAttr = n.getAttributes().getNamedItem("name");
                                            if (nameAttr != null) {
                                                System.out.println("\tinput name: "+nameAttr.getNodeValue());

                                                if (nameAttr.getNodeValue().equals("loginfmt")) {
                                                    emailLogin = n;
                                                    break;
                                                }
                                            }

                                        }
                                        if (emailLogin != null) {
                                            System.out.println("found login input!");
                                            HTMLInputElement loginInput = (HTMLInputElement)emailLogin;
                                            try {
                                                EventTarget et = (EventTarget)emailLogin;
                                                DocumentEvent docEvt = (DocumentEvent)doc;
                                                Event tt = docEvt.createEvent("MouseEvent");
                                                tt.initEvent("click", true, true);

                                                et.dispatchEvent(tt);
                                                loginInput.setValue("jiajiwu@live.com");
                                                //form.submit();
//                                                loginInput.setAttribute("placeholder", "");
                                                //loginInput.setAttribute("value", "jiajiwu@live.com");
//                                                Node v = emailLogin.getAttributes().getNamedItem("value");
//                                                if (v == null) {
//                                                    v = doc.createAttribute("value");
//                                                    System.out.println("node created");
//                                                    System.out.println("node inserted");
//                                                }
//                                                v.setTextContent("jiajiwu@live.com");
//                                                emailLogin.getAttributes().setNamedItem(v);
                                            }
                                            catch (Exception ex) {
                                                System.out.println(ex.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, false);



                }
                catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }
            }
        });
        wc.getEngine().load(_AuthUrl);

        return wc;
    }

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAa9i/uw6LehjGnjHN0lUJSocBYIzdqc31IpXk4BZK8QEhKhwx1CrVRWPKR5bBhnw6zabCPTdn2o7Cu0mAbTIDL4j1R04mrPtnSv7/1d9%2bS2hCooxnRZ%2buNSECw2AG4yTg5JXFaxeuUjTPD5m9jUUluoF1FrhMvzSD1DGhcdqe6FlV103IcEz%2b7S9eE8ZRsXnYRNJhiypr0k7dpDB2XissjWyD1mhZ7n80u2Oua%2btHGMfD1gGwCMimOPXUQZ0mrnR3vau/aaYh/NZZYT%2bn/twHhIdE6eVW9SKfKyLQTlbZrZ36u8/52euitHH3%2bfDKEz0Z0X9nPYdje9YvBV6csic3j0DZgAACPaURamFDpwOMAKgTXS0R6HjOA3fvV2qWFLnL8OxGKTUY9OraDafXYT3T8UAOlzHZ5JhTuyp8Q5bPCwXd%2bDZAmzADWVFdhJGT0ILLG6AIouA0W9tArabL/81Y2x4979zzd7g46cHHJ9pGy6Sgf0hOvhAm7YxE5gpr2SA8EHus7DqDl1cu%2bGKIA7nrx7nrYAl2jz6qL/UQFUYX1anO9JahIkrUkiJNO6CL%2bCy8iC0B%2bjuNfnD%2bJd4Kf9hRN0FDfRofQ%2b501IKcBBCDiPE2tlTolzLgV8eP2WxGa05A8hDQnurB2cOW19Mb29PMaO52%2b2czYjD%2b0vwBZxsPMAZMuKwf6w64dtCuaxrqytq4IpkTpa/5UDhxwxXB4DtTsH7uOinvWn2AUyTRwRYYNSHrCfivMBrqWYZmvVnavCp%2bD2/J0ddae1UR5b1PhDZDivchX6yxs6LOLWAQA7KnI9pBiPxqGEIRJKTJByD0lpQC4hATMdqOXLRRF2h9DeToKvOMbIDNXfzIn6FnMUE%2b%2bnfb/qLdvSYJbgmOuuRn62ONRKx0NdVRwuykPi30nlapG1OgMaowCkzOwdr82D2UKFPYnVdBmNqfrdYqItRqy67eR2wXyJCThajqtXb9wGIulC3P4tXwbuu5OpmuILICo3WEbiIl9F3LaDzZMijo1HKZw59zyflw0lZPhjj8W1RjWfweSG3Hd6X/6AK3TzEBL%2bDL0SOQwPE43/5xbHZ%2bzHT79Gb%2b5aXMbcKmGyOLKCeHGEC";
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

    public static String getToken() {
        return accessToken;
    }

}
