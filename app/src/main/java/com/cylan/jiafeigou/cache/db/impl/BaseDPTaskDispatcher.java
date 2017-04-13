package com.cylan.jiafeigou.cache.db.impl;

/**
 * Created by yanzhendong on 2017/3/1.
 */


import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 用于客户端和服务器之间的数据交互,query task 在无网络时将被丢弃
 * 其他 task
 */
public class BaseDPTaskDispatcher implements IDPTaskDispatcher {
    private IDPTaskFactory taskFactory;
    private IDBHelper dbHelper;
    private JFGSourceManager sourceManager;
    private IPropertyParser propertyParser;

    @Override
    public synchronized void perform() {
        dbHelper.queryUnConfirmDpMsg(null, null)
                .observeOn(Schedulers.io())
                .flatMap(Observable::from)
                .subscribe(new Subscriber<DPEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onStart() {
                        request(1);
                    }

                    @Override
                    public void onNext(DPEntity entity) {
                        IDPTask<IDPTaskResult> task = taskFactory.getTask(entity.action(), false, entity);
                        if (task != null) {
                            task.inject(dbHelper, sourceManager, propertyParser);
                            task.performServer().subscribe(result -> request(1), e -> {
                                AppLogger.e(e.getMessage());
                                e.printStackTrace();
                            });
                        } else {
                            request(1);
                        }
                    }
                });
    }

    @Override
    public Observable<IDPTaskResult> perform(IDPEntity entity) {
        return Observable.just(taskFactory.getTask(entity.action(), false, entity))
                .filter(task -> {
                    if (task != null) {
                        task.inject(dbHelper, sourceManager, propertyParser);
                    }
                    return task != null;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(task -> sourceManager.isOnline()
                        ? task.performLocal().observeOn(Schedulers.io()).flatMap(ret -> task.performServer())
                        : task.performLocal().observeOn(Schedulers.io())
                );
    }

    @Override
    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entities) {
        if (sourceManager.getAJFGAccount() == null) {
            return Observable.just(BaseDPTaskResult.SUCCESS);
        }
        return Observable.just(taskFactory.getTask(entities.get(0).action(), true, entities))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .filter(task -> {
                    if (task != null) {
                        task.inject(dbHelper, sourceManager, propertyParser);
                    }
                    return task != null;
                })
                .flatMap(task -> sourceManager.isOnline()
                        ? task.performLocal().observeOn(Schedulers.io()).flatMap(ret -> task.performServer())
                        : task.performLocal().observeOn(Schedulers.io())
                );
    }

    @Override
    public void setDBHelper(IDBHelper helper) {
        this.dbHelper = helper;
    }

    @Override
    public void setSourceManager(JFGSourceManager manager) {
        this.sourceManager = manager;
    }

    @Override
    public void setTaskFactory(IDPTaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    @Override
    public void setPropertyParser(IPropertyParser parser) {
        this.propertyParser = parser;
    }
}
