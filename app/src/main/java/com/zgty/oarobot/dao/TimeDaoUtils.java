package com.zgty.oarobot.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zgty.oarobot.bean.DaoMaster;
import com.zgty.oarobot.bean.DaoSession;
import com.zgty.oarobot.bean.Time;
import com.zgty.oarobot.bean.TimeDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by zy on 2017/11/3.
 * 时间表
 */

public class TimeDaoUtils {
    private TimeDao timeDao;


    public TimeDaoUtils(Context context) {
        SQLiteDatabase writableDatabase = DBManager.getInstance(context).getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writableDatabase);
        DaoSession daoSession = daoMaster.newSession();
        timeDao = daoSession.getTimeDao();
    }


    /**
     * 插入时间集合
     *
     * @param times 时间集合
     */
    public void insertTimeList(List<Time> times) {
        if (times == null || times.isEmpty()) {
            return;
        }

        timeDao.insertInTx(times);
    }

    /**
     * 更新时间集合
     *
     * @param times 时间集合
     */
    public void updateStaff(List<Time> times) {
        if (times == null || times.isEmpty()) {
            return;
        }
        timeDao.updateInTx(times);
    }

    /**
     * 查询时间列表
     */
    public List<Time> queryTimeList() {
        QueryBuilder<Time> qb = timeDao.queryBuilder();
        return qb.list();
    }

    /**
     * 查询时间
     */
    public List<Time> queryTimeList(String id) {
        QueryBuilder<Time> qb = timeDao.queryBuilder();
        qb.where(TimeDao.Properties.Id.eq(id));
        return qb.list();
    }
}
