package com.jokeep.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JSONArraySort {

    /**
     * @param jsonArr
     * @param sortKey
     * @param is_desc false升序列  true降序 (排序字段为数字类型)
     * @return
     */
    public static JSONArray jsonArraySort(JSONArray jsonArr, final String sortKey, final boolean is_desc) {
        JSONArray sortedJsonArray = new JSONArray();
        //不需要排序的集合
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        //需要排序的集合
        List<JSONObject> jsonValueSorts = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObject = jsonArr.getJSONObject(i);
            if (jsonObject.containsKey("F_PARENTID") && jsonObject.getString("F_PARENTID").equals("-1")
                    || jsonObject.containsKey("F_PARENTID") && jsonObject.getString("F_PARENTID").equals("1")) {
                jsonValues.add(jsonObject);
            } else {
                jsonValueSorts.add(jsonObject);
            }

        }
        if (jsonValueSorts.size() > 0) {
            Collections.sort(jsonValueSorts, new Comparator<JSONObject>() {
                private final String KEY_NAME = sortKey;

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();
                    try {
                        valA = a.getString(KEY_NAME);
                        valB = b.getString(KEY_NAME);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (is_desc) {
                        return -valA.compareTo(valB);
                    } else {
                        return -valB.compareTo(valA);
                    }

                }
            });
        }

        sortedJsonArray.addAll(jsonValues);
        if (jsonValueSorts.size() > 0) {
            sortedJsonArray.addAll(jsonValueSorts);
        }
        return sortedJsonArray;
    }
}
