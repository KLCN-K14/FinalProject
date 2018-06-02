package com.klcn.xuant.transporter.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.klcn.xuant.transporter.common.Common;

public class ConvertBodyDataUtils {

    public static String convertToBodyData(String[] list) {
        String data = "";
        for(int i=0;i<list.length;i++){
            data = data+list[i]+ Common.keySplit;
        }
        return data;
    }
}
