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

    private static String accessToken = "EwBgA8l6BAAURSN/FHlDW5xN74t6GzbtsBBeBUYAAZT2vu1ibBLqz72hPaMjF0aH07WkUvJXELOrcS13jqCtIstCUnmrNslGueRJMIqznESkXMCWoF%2bgSD9vBVdj8LAYPiyKn3SzCJzbMSrci4/h4t7vQULtRSGnlTTs%2b9A4EgPMR8a7FcPl/bhfyo2xUuhwcXcRmmFUapDgTohescmu8M9qy3/DPd9X4437vDDDZpz15SypjUCcBJJC8gttXLrlwvvFze7YrlUA0vQ4uobBNqqBiWYYOWfa1paymJTTQFpO0akvZ%2b9Bmtsa8QgFqJPmRSnGszlLCtziGKtKPovReVqmPg8w7hOYyWQ%2bFWcg56Jsref02tAOapwPbUqfDTwDZgAACFzA0R33/kUwMAIFPJD2TkjjvH0/MIOCAsCcbaGzqR6/%2bOMHQPoPzQLAWabQ1Y58/rb/YNSbbS8vqCTAycwDWWUkLUKOT6F189HieZuaN2zn%2b4lwdg%2b5g11rZy0wrSwENRsGcWuwBq%2bPOFbRXLkRsJCnbhm4mG/mblvw0Xn4iIamI96Uq7kwE9wIPaBduKmS4%2bZEsF9k2U%2btQMezdNMpKobvblzI5k8PAU5VoFFenODejdEvuT3KVAtvdm3f6ZcAObmJYnbKO2Fx8oolFs%2bh0XGYbxrAVaNOAUvIIhn78hGYE6JQaG4iiGnbc0fTzOZhI75B28hsYidkpTM05sxQUa2atV0OtOlpUMbV8ZQriDRty/i9W4hJ8gBh%2bNiJ3uk3F76Qa/VuAKrN/3XBvNcTromEDz7NPpv%2bb2i8eeuONzqcMOfnz%2bj6N1nKqei6z7wV7HvrxfnLYO9NSwiNjF1Um6VXkmV3Lqy9VDWhKzjTa70VXRlWhIwrxfbNBblCqX/fzhx9EX2xE5BlwuNn5GXSE2R9p4PbQL634wrfPhlRc7A1SJXHovBsZjUrLDJkMLF3VuxICQd0Ajnb0IQvnRXjtjE/Y6phB1WXt2aGbsM9He%2b9lsdyCMhShSn78rHLaolYyuiCC2j0%2bZVjCt9OTPHR4KVkiGDkPVwKe9%2bU7Wx8tk0h86fKTLsXpoRw8AUKe9xjoh0QTxBXutDT4kxGTSX6oGoeiX7DxHttQ99u/8zy%2blGpLbyTW6ltLDH3UmEC";
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
