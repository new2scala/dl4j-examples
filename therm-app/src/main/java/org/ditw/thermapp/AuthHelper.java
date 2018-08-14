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

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAdlmrIlpgewBHYPa6XcpjGf0cVfytOl9QTh024cvpNVBDS6R0PgXWwzUlV1CrX%2bHZd%2bYt03drrB0hMtA5Dtqz9GUO%2bugPqFfkRy17CfRBOFsxgD0uS5BBgjVO%2bAzGkQFQLwDWI0uweZQTLgVZizx8nFXZpGr4XOS3srQZQYHog9wbSh5IE7u/RO1a/gHZeC2VMTJQS1BmYkkii2q1pGetbYlW3EPv9uskRJx4w5HbOAYBS1iICEgQZEavlb7IK0fI6QxUEBrpr3aACJEqHgfHQLsnblQHhdIkRrqvOcKHUSr%2bu3SOzdqV%2b8yvuyLxcY/Q9LDuoKEkcylrKyyiw/p6JADZgAACJ22SukfZ29vMAL%2bgCmy2OEMynNGYA5MVFBkg/tGZIaICASYu6mnDhC%2bBrBh/3M/kUmd%2bL5XQP2yPynz0LS9APWbx3fgFpxsM9LLHEWjSDa7b3zq20dCkfcN2%2bf5j3tWGm51Rso13GB3ypnVdmHbDAWfvZEWSMS7rX0dAPRqZYeEjxStZYXoost84jo7wa9ci23fXlDPydw9QOkikFU9XDAbbJeGSeFkd7BWRkkP3MICEluTbQi%2b9O0wRN4gQNXGfgWZwi3vOVe3cUwGmIXKkNbfvFdgcB87aFggRVaIiZcfSkjjc6s2ktCsrzzSpZkQKDYiL%2bjqKnRSii/OxHv2ugK6lGVtBgBrN5et5ZtkGJAQj3vXEOGIOL2QCEc/6t7GZlLMFhlPdSIhJpM8qOqw8a0Iy1dgs83wUxucekloVvXNs35juxRlr4BKbpulZO7VHgWVrTuy2OEebus%2bXzerxPJw1jvGmPnFpPJPUMO9J5H/FLxBB3NKjmVK7ynVpe8hyFmkeogY%2bGFMvptAh5TacVlxikGr4YL8I9PN3NZIcx0zBrh/HtyHpE1lZxkVGY6kcBOCVvWdCHXK2d8%2bYtBJST4mv8GWGdoC9Gsd%2bcH2Y7yvuqa/bk99mM1ccMdMpeIMPidRLiaK9aZAm%2bmbv2vxaJef9sNGro08Xs9PIg8YsWN2n0SU1Tr3PwctDB%2bo5Te5OtIWkMbWLV9vlYwlNjCq1sYXpexyGJoHhbRJ86rOKZIw9E1z3sax30Yd3WEC";
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
