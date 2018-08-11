package org.ditw.thermapp.onedrive;

import org.ditw.thermapp.onedrive.localcache.CacheHelper;

/**
 * Created by dev on 2018-08-11.
 */
public interface FolderCachedRespHandler {
    void handle(CacheHelper.FolderCache folderCache);
}
