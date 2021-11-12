package com.ez.kotlin.frame.utils

import com.tencent.mmkv.MMKV

/**
 * @author : ezhuwx
 * Describe : MMKV 用户信息管理工具类
 * Designed on 2021/10/28
 * E-mail : ezhuwx@163.com
 * Update on 10:57 by ezhuwx
 */
class MMKVUtil {

    companion object {
        private const val fileName =  "prefs"

        /**
         * 初始化mmkv
         */
        private val mmkv: MMKV
            get() = MMKV.mmkvWithID(fileName)

        /**
         * 删除全部数据(传了参数就是按key删除)
         */
        fun deleteKeyOrAll(key: String? = null) {
            if (key == null) mmkv.clearAll()
            else mmkv.removeValueForKey(key)
        }

        /** 查询某个key是否已经存在
         *
         * @param key
         * @return
         */
        fun contains(key: String) = mmkv.contains(key)
    }
}