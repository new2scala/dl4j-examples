package org.ditw.learning.javafx.thermoapp.onedrive;

import org.apache.http.HttpResponse;

/**
 * Created by dev on 2018-08-10.
 */
public interface HttpRespHandlerT<T> {
    T handle(HttpResponse resp);
}
