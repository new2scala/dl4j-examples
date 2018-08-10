package org.ditw.learning.javafx.thermoapp.onedrive;

import org.apache.http.HttpResponse;

/**
 * Created by dev on 2018-08-10.
 */
public interface HttpRespHandler {
    void handle(HttpResponse resp);
}
