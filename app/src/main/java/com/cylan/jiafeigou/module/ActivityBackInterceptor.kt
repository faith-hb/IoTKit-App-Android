package com.cylan.jiafeigou.module

/**
 * Created by yanzhendong on 2017/10/27.
 */
interface ActivityBackInterceptor {

    fun performBackIntercept(willExit: Boolean): Boolean
}

