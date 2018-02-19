package com.xyz.drivingRecorder;

import java.util.ArrayList;
import java.util.List;

class FunctionList {

    public class FunctionItem {
        private String name;
        private String context;

        public FunctionItem(String name, String context) {
            this.name = name;
            this.context = context;
        }

        public String getName() {
            return name;
        }

        public String getContext() {
            return context;
        }
    }

    private List<FunctionItem> functionItemList;

    public List<FunctionItem> get() {
        if (null == functionItemList) {
            functionItemList = new ArrayList<FunctionItem>();
            functionItemList.add(new FunctionItem("管理视频", "管理录制视频"));
            functionItemList.add(new FunctionItem("视频录制", "管理录制视频"));
            functionItemList.add(new FunctionItem("设置", "多种设置选项"));
        }

        return functionItemList;
    }

    private static FunctionList inst = null;
    public static FunctionList instance() {
        if (inst == null) {
            inst = new FunctionList();
        }

        return inst;
    }
}
