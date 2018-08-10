package org.ditw.learning.javafx.thermoapp;

import com.sun.tools.doclets.formats.html.markup.HtmlDocument;
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

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAYS1u3Pa9UpaxjUvqv4sjTYMU8xaz1R37COquuhrsz9644nAdwik5Gp/3jswEiPWi6TfpJqroT%2bDK2D04OnfEjPY1YIOL62X379LNHiEITOPNOGyX9xB74DJKo9egLJ7XY1P6SIxLnhPk4WYSLp1vlYgDRjDHwYId2JDRFshZzihAPeZQHf4hpMgju7ZuflDzLH5AmGAs%2bur5ZZ9%2bZwckKJufnTHRaZgFVtSgiWYMzeZ1/26VKyMLMLHsSnlHXC5NmM6ITdg5Ga7uyhdSTPkZUZxppu7DAWLN48eCSkl3gO9esuc7QSis1quxkEmcAlI78/rCCaSl3FAUHgCYl86ICQDZgAACLCx/lBdTkYuMAIXOEDAExG%2bi2K/LmziQrmRtJ2ZMcokDy0vsuWmQGoMFmjcI8FtyMyu%2bEvwnC4tFaSNIAplVBcbejH76IIRZlNHiT366FaiXPy0ljXQdxmKno3zKtk0PwVywwZk4C5s9AP%2bVMesa4m9%2baO8QEjuaqKvwZQQC%2b2Q9kYVJ6dHQekO6GoTkWDzWW5XuPFhsP0CnFvFIl67AunBxW%2bCphloO6pGqRuQq9iDLBXKADhgodfmGuo61yH9YXABW8GfA5qylCBCjFv0OvXCHW8kSDLSERNlALnu4pdEpAhCvPm9I8a0ai8LTgKFQLbY09AI39vROG/gXp1z0ET3OmBRrZdUht4UHtiMlTHd1OahtS0JcEoFpWxrouKA/nkUNO9020Gsecw9G6kZ4Q%2bwLD%2bsO/j8CYhJ/E1MSMNoNHfDZTonePZpYInOXhOhDxOP%2bKtrDWhN5FAYGnW5w1l0hxKUW/%2b8VIINsV13R7QDWvGLpxohl/pbgMjxZ8bH318vtqvHtHZBq5BfR4Es8ggHujV3E8HGz9eEq5/RXHFA%2bzOEYoYts4gYdj8CA5vWk1m2Q2uX6UiO5WGmru6izei72gf%2bVP7Yr%2bBMoBMaklRWER2QpSIeoIPyoEJ5gWXKYbOg4lv1%2b4kTWG50U%2bUyGbWX5svCgDemcLhlg6kM1hq0N9OyesB3T74ohk/lKjGs6DC18stTZPpQdf9zo3QkpoJ54gV1WiKyl3o/5tFndrXZ6sQnwzA75fvJCGEC";
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
