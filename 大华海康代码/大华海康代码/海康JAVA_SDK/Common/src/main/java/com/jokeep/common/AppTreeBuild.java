package com.jokeep.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class AppTreeBuild {
    private static JSONArray BuildTempTable(JSONArray PRoot, JSONObject dRow) {
        JSONArray tempArr = new JSONArray();
        if (PRoot != null && dRow != null) {
            tempArr.add(dRow);
        }
        return tempArr;
    }

    private static String stringConvert(String strTemp) {
        return strTemp.replace("\n\r", "").replace("\r", "").replace("\n", "").replace("'", "").replace("&", "").replace("@", "");
    }

    public String TreeViewBuild(JSONArray dTable, String F_ZJDZD, String F_FJDZD, String F_XSZD) {
        return TreeViewBuild(dTable, F_ZJDZD, F_FJDZD, F_XSZD, true);
    }

    /**
     * 目录树构造
     *
     * @param dTable    数据源
     * @param F_ZJDZD   子节点
     * @param F_FJDZD   父节点
     * @param F_XSZD    显示字段
     * @param isColNode 是否关闭节点(true关闭,false展开)
     * @return
     */
    public static String TreeViewBuild(JSONArray dTable, String F_ZJDZD, String F_FJDZD, String F_XSZD, boolean isColNode) {
        String nodeState = isColNode ? "closed" : "open";

        StringBuilder strApp = new StringBuilder("");

        strApp.append("[");
        strApp.append("\n");
        JSONArray rootRows = new JSONArray();
        for (int i = 0; i < dTable.size(); i++) {
            JSONObject row = dTable.getJSONObject(i);
            if (row.containsKey(F_FJDZD)) {
                String fjd = row.getString(F_FJDZD);
                if (StringUtils.isEmpty(fjd) || fjd.equals("0")) {
                    rootRows.add(row);
                }
            }
        }
        if (rootRows.size() <= 0) {//如果没有过滤到数据，重新加载处理
            String strfristNodeCode = "";
            if (dTable.size() > 0) {
                strfristNodeCode = dTable.getJSONObject(0).getString(F_FJDZD);
            }
            for (int i = 0; i < dTable.size(); i++) {
                JSONObject row = dTable.getJSONObject(i);
                if (row.containsKey(F_FJDZD)) {
                    String fjd = row.getString(F_FJDZD);
                    if (fjd.equals(strfristNodeCode)) {
                        rootRows.add(row);
                    }
                }

            }
        }


        int rowIndex = 0;
        int rootIndex = 0;
        for (int i = 0; i < rootRows.size(); i++) {
            JSONObject dRow = rootRows.getJSONObject(i);
            strApp.append("{");
            strApp.append("\n");
            //字段
            strApp.append("\"id\":");
            //字段值
            strApp.append("\"" + stringConvert(dRow.getString(F_ZJDZD)) + "\"");
            strApp.append(",");
            strApp.append("\n");

            //显示字段
            strApp.append("\"text\":");
            //显示字段值
            strApp.append("\"" + stringConvert(dRow.getString(F_XSZD)) + "\"");
            strApp.append(",");
            strApp.append("\n");

            //状态字段
            strApp.append("\"state\":");
            //显示字段值
            strApp.append("\"" + nodeState + "\"");
            strApp.append(",");
            strApp.append("\n");

            //所有字段
            strApp.append("\"attributes\":{");
            String strJavascript = "";
            //for (int j = 0; j < dTable.Columns.Count; j++) strJavascript += stringConvert(dRow[j].ToString()) + "\t";
            //Newtonsoft.Json.Converters.IsoDateTimeConverter timeConverter = new Newtonsoft.Json.Converters.IsoDateTimeConverter();
            //timeConverter.DateTimeFormat = "yyyy'-'MM'-'dd' 'HH':'mm':'ss";
            //JsonDBNullConverter mDBNull = new JsonDBNullConverter();
            //strJavascript = Newtonsoft.Json.JsonConvert.SerializeObject(BulidTempTable(dTable, dRow), Newtonsoft.Json.Formatting.Indented, timeConverter, mDBNull);
            strJavascript = JSON.toJSONString(BuildTempTable(dTable, dRow));//JsonConstructUtils.SerializeObject()
            strJavascript = strJavascript.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
            strApp.append(strJavascript);
            //所有字段值
            //strApp.Append("\"nodedata\":");
            //strApp.Append("\"" +  + "\"");

            strApp.append("\n");
            strApp.append("}");
            strApp.append("\n");

            JSONArray rows = new JSONArray();
            //节点类型不定
            for (int j = 0; j < dTable.size(); j++) {
                JSONObject row = dTable.getJSONObject(j);
                if (row.containsKey(F_FJDZD)) {
                    String fjd = row.getString(F_FJDZD);
                    if (fjd.equals(dRow.getString(F_ZJDZD))) {
                        rows.add(row);
                    }
                }

            }
            if (rows.size() > 0) {
                strApp.append(",");
                strApp.append("\"children\":");
                strApp.append("[");
                strApp.append("\n");
                //****不载加子节点处理
                rowIndex = 0;
                for (int j = 0; j < rows.size(); j++) {
                    JSONObject CDRow = rows.getJSONObject(j);
                    TreeViewBuildChild(rowIndex, strApp, dTable, CDRow, F_FJDZD, F_ZJDZD, F_XSZD, nodeState);

                    rowIndex++;
                    if (rowIndex != rows.size()) {
                        strApp.append(",");
                    }
                }
                strApp.append("]");
                strApp.append("\n");
            }

            strApp.append("}");

            rootIndex++;

            if (rootIndex != rootRows.size()) {
                strApp.append(",");
            }
        }

        strApp.append("]");

        return strApp.toString();
    }

    /**
     * 目录树构造
     *
     * @param dTable    数据源
     * @param F_ZJDZD   子节点
     * @param F_FJDZD   父节点
     * @param F_XSZD    显示字段
     * @param isColNode 是否关闭节点(true关闭,false展开)
     * @param orderbyId 排序的字段
     * @return
     */
    public static String TreeViewBuilds(JSONArray dTable, String F_ZJDZD, String F_FJDZD, String F_XSZD, boolean isColNode, String orderbyId) {
        String nodeState = isColNode ? "closed" : "open";

        StringBuilder strApp = new StringBuilder("");

        strApp.append("[");
        strApp.append("\n");
        JSONArray rootRows = new JSONArray();
        for (int i = 0; i < dTable.size(); i++) {
            JSONObject row = dTable.getJSONObject(i);
            if (row.containsKey(F_FJDZD)) {
                String fjd = row.getString(F_FJDZD);
                if (StringUtils.isEmpty(fjd) || fjd.equals("-1")) {
                    rootRows.add(row);
                }
            }
        }
        //排序1

        if (rootRows.size() <= 0) {//如果没有过滤到数据，重新加载处理
            String strfristNodeCode = "";
            if (dTable.size() > 0) {
                strfristNodeCode = dTable.getJSONObject(0).getString(F_FJDZD);
            }
            for (int i = 0; i < dTable.size(); i++) {
                JSONObject row = dTable.getJSONObject(i);
                if (row.containsKey(F_FJDZD)) {
                    String fjd = row.getString(F_FJDZD);
                    if (fjd.equals(strfristNodeCode)) {
                        rootRows.add(row);
                    }
                }
            }
        }
        rootRows = JSONArraySort.jsonArraySort(rootRows,orderbyId,false);


        int rowIndex = 0;
        int rootIndex = 0;
        for (int i = 0; i < rootRows.size(); i++) {
            JSONObject dRow = rootRows.getJSONObject(i);
            strApp.append("{");
            strApp.append("\n");
            //字段
            strApp.append("\"id\":");
            //字段值
            strApp.append("\"" + stringConvert(dRow.getString(F_ZJDZD)) + "\"");
            strApp.append(",");
            strApp.append("\n");

            //显示字段
            strApp.append("\"text\":");
            //显示字段值
            strApp.append("\"" + stringConvert(dRow.getString(F_XSZD)) + "\"");
            strApp.append(",");
            strApp.append("\n");

            //状态字段
            strApp.append("\"state\":");
            //显示字段值
            strApp.append("\"" + nodeState + "\"");
            strApp.append(",");
            strApp.append("\n");

            //所有字段
            strApp.append("\"attributes\":{");
            String strJavascript = "";
            //for (int j = 0; j < dTable.Columns.Count; j++) strJavascript += stringConvert(dRow[j].ToString()) + "\t";
            //Newtonsoft.Json.Converters.IsoDateTimeConverter timeConverter = new Newtonsoft.Json.Converters.IsoDateTimeConverter();
            //timeConverter.DateTimeFormat = "yyyy'-'MM'-'dd' 'HH':'mm':'ss";
            //JsonDBNullConverter mDBNull = new JsonDBNullConverter();
            //strJavascript = Newtonsoft.Json.JsonConvert.SerializeObject(BulidTempTable(dTable, dRow), Newtonsoft.Json.Formatting.Indented, timeConverter, mDBNull);
            strJavascript = JSON.toJSONString(BuildTempTable(dTable, dRow));//JsonConstructUtils.SerializeObject()
            strJavascript = strJavascript.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
            strApp.append(strJavascript);
            //所有字段值
            //strApp.Append("\"nodedata\":");
            //strApp.Append("\"" +  + "\"");

            strApp.append("\n");
            strApp.append("}");
            strApp.append("\n");

            JSONArray rows = new JSONArray();
            //节点类型不定
            for (int j = 0; j < dTable.size(); j++) {
                JSONObject row = dTable.getJSONObject(j);
                if (row.containsKey(F_FJDZD)) {
                    String fjd = row.getString(F_FJDZD);
                    if (fjd.equals(dRow.getString(F_ZJDZD))) {
                        rows.add(row);
                    }
                }
            }
            //排序3
            rows =JSONArraySort.jsonArraySort(rows,orderbyId,false);
            if (rows.size() > 0) {
                strApp.append(",");
                strApp.append("\"children\":");
                strApp.append("[");
                strApp.append("\n");
                //****不载加子节点处理
                rowIndex = 0;
                for (int j = 0; j < rows.size(); j++) {
                    JSONObject CDRow = rows.getJSONObject(j);
                    TreeViewBuildChilds(rowIndex, strApp, dTable, CDRow, F_FJDZD, F_ZJDZD, F_XSZD, nodeState,orderbyId);

                    rowIndex++;
                    if (rowIndex != rows.size()) {
                        strApp.append(",");
                    }
                }
                strApp.append("]");
                strApp.append("\n");
            }

            strApp.append("}");

            rootIndex++;

            if (rootIndex != rootRows.size()) {
                strApp.append(",");
            }
        }

        strApp.append("]");

        return strApp.toString();
    }

    private static void TreeViewBuildChild(int rowIndex, StringBuilder strApp, JSONArray PRootDSet, JSONObject dRow, String F_FJDZD, String F_ZJDZD, String F_XSZD, String nodeState) {
        strApp.append("{");
        strApp.append("\n");
        //字段
        strApp.append("\"id\":");
        //字段值
        strApp.append("\"" + stringConvert(dRow.getString(F_ZJDZD)) + "\"");
        strApp.append(",");
        strApp.append("\n");

        //显示字段
        strApp.append("\"text\":");
        //显示字段值
        strApp.append("\"" + stringConvert(dRow.getString(F_XSZD)) + "\"");
        strApp.append(",");
        strApp.append("\n");

        //状态字段
        strApp.append("\"state\":");
        //显示字段值
        strApp.append("\"" + nodeState + "\"");
        strApp.append(",");
        strApp.append("\n");


        //所有字段
        strApp.append("\"attributes\":{");
        String strJavascript = "";
        //for (int j = 0; j < PRootDSet.Columns.Count; j++) strJavascript += stringConvert(dRow[j].ToString()) + "\t";
        strJavascript = JSON.toJSONString(BuildTempTable(PRootDSet, dRow));
        strJavascript = strJavascript.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
        strApp.append(strJavascript);
        //所有字段值
        //strApp.Append("\"nodedata\":");
        //strApp.Append("\"" + strJavascript + "\"");
        strApp.append("\n");
        strApp.append("}");
        strApp.append("\n");

        JSONArray rows = new JSONArray();
        //节点类型不定
        for (int j = 0; j < PRootDSet.size(); j++) {
            JSONObject row = PRootDSet.getJSONObject(j);
            if (row.containsKey(F_FJDZD)) {
                String fjd = row.getString(F_FJDZD);
                if (fjd.equals(dRow.getString(F_ZJDZD))) {
                    rows.add(row);
                }
            }

        }
        if (rows.size() > 0) {
            strApp.append(",");
            strApp.append("\"children\":");
            strApp.append("[");
            strApp.append("\n");
            //****不载加子节点处理
            rowIndex = 0;
            for (int j = 0; j < rows.size(); j++) {
                JSONObject CDRow = rows.getJSONObject(j);
                TreeViewBuildChild(rowIndex, strApp, PRootDSet, CDRow, F_FJDZD, F_ZJDZD, F_XSZD, nodeState);

                rowIndex++;
                if (rowIndex != rows.size()) {
                    strApp.append(",");
                }
            }
            strApp.append("]");
            strApp.append("\n");
        }

        strApp.append("}");
    }


    private static void TreeViewBuildChilds(int rowIndex, StringBuilder strApp, JSONArray PRootDSet, JSONObject dRow, String F_FJDZD, String F_ZJDZD, String F_XSZD, String nodeState,String orderbyId) {
        strApp.append("{");
        strApp.append("\n");
        //字段
        strApp.append("\"id\":");
        //字段值
        strApp.append("\"" + stringConvert(dRow.getString(F_ZJDZD)) + "\"");
        strApp.append(",");
        strApp.append("\n");

        //显示字段
        strApp.append("\"text\":");
        //显示字段值
        strApp.append("\"" + stringConvert(dRow.getString(F_XSZD)) + "\"");
        strApp.append(",");
        strApp.append("\n");

        //状态字段
        strApp.append("\"state\":");
        //显示字段值
        strApp.append("\"" + nodeState + "\"");
        strApp.append(",");
        strApp.append("\n");


        //所有字段
        strApp.append("\"attributes\":{");
        String strJavascript = "";
        //for (int j = 0; j < PRootDSet.Columns.Count; j++) strJavascript += stringConvert(dRow[j].ToString()) + "\t";
        strJavascript = JSON.toJSONString(BuildTempTable(PRootDSet, dRow));
        strJavascript = strJavascript.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
        strApp.append(strJavascript);
        //所有字段值
        //strApp.Append("\"nodedata\":");
        //strApp.Append("\"" + strJavascript + "\"");
        strApp.append("\n");
        strApp.append("}");
        strApp.append("\n");

        JSONArray rows = new JSONArray();
        //节点类型不定
        for (int j = 0; j < PRootDSet.size(); j++) {
            JSONObject row = PRootDSet.getJSONObject(j);
            if (row.containsKey(F_FJDZD)) {
                String fjd = row.getString(F_FJDZD);
                if (fjd.equals(dRow.getString(F_ZJDZD))) {
                    rows.add(row);
                }
            }
        }
        rows =JSONArraySort.jsonArraySort(rows,orderbyId,false);
        if (rows.size() > 0) {
            strApp.append(",");
            strApp.append("\"children\":");
            strApp.append("[");
            strApp.append("\n");
            //****不载加子节点处理
            rowIndex = 0;
            for (int j = 0; j < rows.size(); j++) {
                JSONObject CDRow = rows.getJSONObject(j);
                TreeViewBuildChilds(rowIndex, strApp, PRootDSet, CDRow, F_FJDZD, F_ZJDZD, F_XSZD, nodeState,orderbyId);

                rowIndex++;
                if (rowIndex != rows.size()) {
                    strApp.append(",");
                }
            }
            strApp.append("]");
            strApp.append("\n");
        }

        strApp.append("}");
    }


    /// <summary>
    /// 目录树构造
    /// </summary>
    /// <param name="dTable">数据源</param>
    /// <param name="F_ZJDZD">子节点</param>
    /// <param name="F_FJDZD">父节点</param>
    /// <param name="F_XSZD">显示字段</param>
    ///  <param name="isColNode">是否关闭节点(true关闭,false展开)</param>
    /// <returns></returns>
    public static String TreeViewBuild(JSONArray dTable, String F_ZJDZD, String F_FJDZD, String F_XSZD, boolean isColNode, String icon) {
        String nodeState = isColNode == true ? "false" : "true";

        StringBuilder strApp = new StringBuilder("");

        strApp.append("[");
        strApp.append("\n");

        JSONArray rootRows = new JSONArray();
        for (int i = 0; i < dTable.size(); i++) {
            JSONObject row = dTable.getJSONObject(i);
            if (row.containsKey(F_FJDZD)) {
                String fjd = row.getString(F_FJDZD);
                if (StringUtils.isEmpty(fjd) || fjd.equals("0")) {
                    rootRows.add(row);
                }
            }

        }


        if (rootRows.size() <= 0) {//如果没有过滤到数据，重新加载处理
            String strfristNodeCode = "";
            if (dTable.size() > 0) {
                strfristNodeCode = dTable.getJSONObject(0).getString(F_FJDZD);
            }
            for (int i = 0; i < dTable.size(); i++) {
                JSONObject row = dTable.getJSONObject(i);
                if (row.containsKey(F_FJDZD)) {
                    String fjd = row.getString(F_FJDZD);
                    if (fjd.equals(strfristNodeCode)) {
                        rootRows.add(row);
                    }
                }

            }
        }


        int rowIndex = 0;
        int rootIndex = 0;
        for (int i = 0; i < rootRows.size(); i++) {
            JSONObject dRow = rootRows.getJSONObject(i);
            strApp.append("{");
            strApp.append("\n");
            //字段
            strApp.append("\"id\":");
            //字段值
            strApp.append("\"" + dRow.getString(F_ZJDZD) + "\"");
            strApp.append(",");
            strApp.append("\n");

            //父节点id
            strApp.append("\"pid\":");
            //显示字段值
            strApp.append("\"" + dRow.getString(F_FJDZD) + "\"");
            strApp.append(",");
            strApp.append("\n");

            //显示字段
            strApp.append("\"name\":");
            //显示字段值
            strApp.append("\"" + dRow.getString(F_XSZD) + "\"");
            strApp.append(",");
            strApp.append("\n");

            //状态字段
            strApp.append("\"open\":");
            //显示字段值
            strApp.append("\"" + nodeState + "\"");
            strApp.append(",");
            strApp.append("\n");

            if (icon != "") {
                //图标
                strApp.append("\"icon\":");
                //显示字段值
                strApp.append("\"" + dRow.getString(icon) + "\"");
                strApp.append(",");
                strApp.append("\n");
            }


            //所有字段
            strApp.append("\"attributes\":{");
            String strJavascript = "";
            strJavascript = JSON.toJSONString(BuildTempTable(dTable, dRow));//JsonConstructUtils.SerializeObject()
            strJavascript = strJavascript.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
            strApp.append(strJavascript);
            //所有字段值
            //strApp.Append("\"nodedata\":");
            //strApp.Append("\"" +  + "\"");

            strApp.append("\n");
            strApp.append("}");
            strApp.append("\n");

            JSONArray rows = new JSONArray();
            //节点类型不定
            for (int j = 0; j < dTable.size(); j++) {
                JSONObject row = dTable.getJSONObject(j);
                if (row.containsKey(F_FJDZD)) {
                    String fjd = row.getString(F_FJDZD);
                    if (fjd.equals(dRow.getString(F_ZJDZD))) {
                        rows.add(row);
                    }
                }

            }
            if (rows.size() > 0) {
                strApp.append(",");
                strApp.append("\"children\":");
                strApp.append("[");
                strApp.append("\n");
                //****不载加子节点处理
                rowIndex = 0;

                for (int j = 0; j < rows.size(); j++) {
                    JSONObject CDRow = rows.getJSONObject(j);
                    TreeViewBuildChild(rowIndex, strApp, dTable, CDRow, F_FJDZD, F_ZJDZD, F_XSZD, nodeState, icon);

                    rowIndex++;

                    if (rowIndex != rows.size()) {
                        strApp.append(",");
                    }
                }
                strApp.append("]");
                strApp.append("\n");
            }

            strApp.append("}");

            rootIndex++;

            if (rootIndex != rootRows.size()) {
                strApp.append(",");
            }
        }

        strApp.append("]");

        return strApp.toString();
    }


    private static void TreeViewBuildChild(int rowIndex, StringBuilder strApp, JSONArray PRootDSet, JSONObject dRow, String F_FJDZD, String F_ZJDZD, String F_XSZD, String nodeState, String icon) {
        strApp.append("{");
        strApp.append("\n");
        //字段
        strApp.append("\"id\":");
        //字段值
        strApp.append("\"" + dRow.getString(F_ZJDZD) + "\"");
        strApp.append(",");
        strApp.append("\n");

        //父节点id
        strApp.append("\"pid\":");
        //显示字段值
        strApp.append("\"" + dRow.getString(F_FJDZD) + "\"");
        strApp.append(",");
        strApp.append("\n");

        //显示字段
        strApp.append("\"name\":");
        //显示字段值
        strApp.append("\"" + dRow.getString(F_XSZD) + "\"");
        strApp.append(",");
        strApp.append("\n");


        if (icon != "") {
            //图标
            strApp.append("\"icon\":");
            //显示字段值
            strApp.append("\"" + dRow.getString(icon) + "\"");
            strApp.append(",");
            strApp.append("\n");
        }
        //状态字段
        strApp.append("\"open\":");
        //显示字段值
        strApp.append("\"" + nodeState + "\"");
        strApp.append(",");
        strApp.append("\n");


        //所有字段
        strApp.append("\"attributes\":{");
        String strJavascript = "";
        strJavascript = JSON.toJSONString(BuildTempTable(PRootDSet, dRow));//JsonConstructUtils.SerializeObject()
        strJavascript = strJavascript.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
        strApp.append(strJavascript);

        //所有字段值
        //strApp.Append("\"nodedata\":");
        //strApp.Append("\"" + strJavascript + "\"");
        strApp.append("\n");
        strApp.append("}");
        strApp.append("\n");

        JSONArray rows = new JSONArray();
        //节点类型不定
        for (int j = 0; j < PRootDSet.size(); j++) {
            JSONObject row = PRootDSet.getJSONObject(j);
            if (row.containsKey(F_FJDZD)) {
                String fjd = row.getString(F_FJDZD);
                if (fjd.equals(dRow.getString(F_ZJDZD))) {
                    rows.add(row);
                }
            }

        }
        if (rows.size() > 0) {
            strApp.append(",");
            strApp.append("\"children\":");
            strApp.append("[");
            strApp.append("\n");
            rowIndex = 0;
            for (int i = 0; i < rows.size(); i++) {
                JSONObject CDRow = rows.getJSONObject(i);
                TreeViewBuildChild(rowIndex, strApp, PRootDSet, CDRow, F_FJDZD, F_ZJDZD, F_XSZD, nodeState, icon);

                rowIndex++;

                if (rowIndex != rows.size()) {
                    strApp.append(",");
                }
            }
            strApp.append("]");
            strApp.append("\n");
        }

        strApp.append("}");
    }
}