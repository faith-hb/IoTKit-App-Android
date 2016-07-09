package com.cylan.jiafeigou.misc;

import android.net.wifi.ScanResult;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cylan-hunt on 16-7-9.
 */
public class ScanResultListFilter {

    /**
     * filter the  bad one
     *
     * @param list
     * @return
     */
    public List<ScanResult> extractPretty(List<ScanResult> list) {
        List<ScanResult> results = new ArrayList<>();
        if (list == null) {
            return results;
        }
        for (ScanResult result : list) {
            if (TextUtils.isEmpty(result.SSID)
                    || TextUtils.equals(result.SSID, "<unknown ssid>")
                    || TextUtils.equals(result.SSID, "0x"))
                continue;
            results.add(result);
        }
        return results;
    }

    public List<ScanResult> extractJFG(List<ScanResult> resultList, String... filters) {
        if (filters == null)
            return resultList;
        List<ScanResult> scanResultList = new ArrayList<>();
        if (resultList == null)
            return scanResultList;
        List<String> filterList = filters == null ? new ArrayList<String>() : Arrays.asList(filters);
        for (ScanResult result : resultList) {
            if (filterList.contains(result.SSID)) {
                scanResultList.add(result);
            }
        }
        return scanResultList;
    }

}
