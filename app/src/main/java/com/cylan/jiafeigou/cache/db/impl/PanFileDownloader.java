package com.cylan.jiafeigou.cache.db.impl;

import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cylan.jiafeigou.cache.db.module.DaoMaster;
import com.cylan.jiafeigou.cache.db.module.DaoSession;
import com.cylan.jiafeigou.cache.db.module.DownloadFile;
import com.cylan.jiafeigou.cache.db.module.DownloadFileDao;
import com.cylan.jiafeigou.cache.db.view.IPanFileDbHelper;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by holy on 2017/3/19.
 */

public class PanFileDownloader implements IPanFileDbHelper {
    private static PanFileDownloader downloader;
    private DownloadFileDao downloadFileDao;

    private PanFileDownloader() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(new GreenDaoContext(), "pan_cache.db");
        DaoMaster master = new DaoMaster(helper.getWritableDb());
        DaoSession daoSession = master.newSession();
        downloadFileDao = daoSession.getDownloadFileDao();
    }

    public static PanFileDownloader getDownloader() {
        if (downloader == null)
            synchronized (PanFileDownloader.class) {
                if (downloader == null)
                    downloader = new PanFileDownloader();
            }
        return downloader;
    }

    @Override
    public Observable<List<DownloadFile>> getFileFrom(int time, boolean asc, int count) {

//        downloadFileDao.queryBuilder().uniqueOrThrow()
        return null;
    }

    @Override
    public Observable<Long> updateOrSaveFile(DownloadFile downloadFile) {
        return Observable.just(downloadFile)
                .flatMap(r -> downloadFileDao.queryBuilder().where(DownloadFileDao.Properties.FileName.eq(downloadFile.fileName),
                        DownloadFileDao.Properties.Md5.eq(downloadFile.md5))
                        .rx().unique()
                        .flatMap(item -> {
                            if (item == null) {
                                return downloadFileDao.rx().save(downloadFile);
                            } else {
                                long id = item.id;
                                item = downloadFile;
                                item.id = id;//id 不能变
                                return downloadFileDao.rx().refresh(item);
                            }
                        }))
                .flatMap(new Func1<DownloadFile, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(DownloadFile downloadFile) {
                        Log.d("PanFileDownloader", "PanFileDownloader update:" + downloadFile);
                        return Observable.just(downloadFile.id);
                    }
                });
    }

    @Override
    public Observable<List<Long>> updateOrSaveFile(List<DownloadFile> downloadFileList) {
//        return Observable.from(downloadFileList)
//                .flatMap(new Func1<DownloadFile, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(DownloadFile downloadFile) {
//                        downloadFileDao.queryBuilder()
//                        return null;
//                    }
//                })
        return null;
    }

    public class GreenDaoContext extends ContextWrapper {

        public GreenDaoContext() {
            super(ContextUtils.getContext());
        }

        /**
         * 获得数据库路径，如果不存在，则创建对象
         *
         * @param dbName
         */
        @Override
        public File getDatabasePath(String dbName) {
            File baseFile = new File(JConstant.ROOT_DIR + File.separator + "db", dbName);
            File parentFile = baseFile.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            return baseFile;
        }

        /**
         * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
         *
         * @param name
         * @param mode
         * @param factory
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                                   SQLiteDatabase.CursorFactory factory) {
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
            return result;
        }

        /**
         * Android 4.0会调用此方法获取数据库。
         *
         * @param name
         * @param mode
         * @param factory
         * @param errorHandler
         * @see ContextWrapper#openOrCreateDatabase(String, int,
         * SQLiteDatabase.CursorFactory,
         * DatabaseErrorHandler)
         */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
                                                   DatabaseErrorHandler errorHandler) {
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
            return result;
        }
    }
}
