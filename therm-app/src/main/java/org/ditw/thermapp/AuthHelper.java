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

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAe6D5qpo//FhZyhkDNvUpOrWVK9zBglw5gTuFIAfZhJtqoyH8l2pNIViLADNZE6vmqa46Xh2LCeZdfwmYGMrR54sJ6/UdnuOsPQ1X6G77GFR29JSm4lV/okDiOY3aF1KvmeMAljhk5Xg7YPugG044wKKhxKhK0ucy/oZXxhOPq9zFq%2bWHlguA8t2VAOk0XTXAq2rLPDNUTkP%2bAiI6URiGxa85oMxWi0kaS06NGONkUI707Kix36RkSpBaZwbKVuEUdOYoNQ60RgvaFwBSk5KeDMJyVliOoTB6Sv1O4Rq6H6zROL3cfzR63yaoWr515dcWrrTiaBLCAvS7ZtoE3dLPC0DZgAACD11tM3%2b/R7LMALb4MjyjZ7HfNZ0VGMzYoO5BDxPg3vqeA9dV3Ymz1KA%2b0qJKM9QceID07O7AroG87jXmKYJ/4HGAEroVfbGN45f5xES0q65yS0KPgbSsiBYjmNdVY20z7aNoVKPmbyBgI11aT79xmhrciF7tYaHboxiJBX1FzRZdY2y9k97Q2emlOmBrT3OEZAUyPiXnal%2bGRNj/O8NAfEjkNBmqK113x/ZQ2NmWrFwW/xiu/zJrZN/n/B7qzxlsdWKphtUVKCRcarF5ttEqB%2b9ZSK2EY3sdPqDRQwyAnH3VTFiX8MJByZBCOR4WhMbVxIeLeIUeNSaMCNzuxV9ovNqCc6fokwYJyLqInerVEtZxjZUw2Tg3AE0fZdlk3yaHO3JE/p0qOptzucBfqi5QvahLsapC3QQrrXc5kJeL1kT6CZmO2PCfw0p99srTc6C2Dw03Z4qY%2beybZMruL0/6zAYaKMCHEgYM0EgpBcqZeBqWusM2ezJJYxjupeFbSl%2bTyiZMh7z9RRDs95zQb9qEwHxeDt8JOEZKyQAoYe%2br2w67RiFwMie0qeROqlslPqI/kdnyLkWX/RnxY8vyDhBksj4kOSbxKG6/klhDd0FtF%2bOuLd74FgGL/gCoN6SBFVPx0i9lIhL6m7D0qNkBpcM2ldOJ4Lzm24SiL1TQGdncQ/kbZSTzfRReyC1E0ICKw0WShLf6MZMKqRnK%2bggYSEuTnrpWSdrzcIlkOs6vC0PPrJCkwidQuRvUk1zuWEC";
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
