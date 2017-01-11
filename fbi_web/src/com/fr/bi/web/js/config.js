//工程配置
$(function () {
    var isSupportFlex = !BI.isIE() && BI.isSupportCss3("flex");
    BI.Plugin.registerWidget("bi.horizontal", function (ob) {
        if (isSupportFlex) {
            return BI.extend({}, ob, {type: "bi.flex_horizontal"});
        } else {
            return ob;
        }
    });
    BI.Plugin.registerWidget("bi.center_adapt", function (ob) {
        if (isSupportFlex && ob.items && ob.items.length <= 1) {
            return BI.extend({}, ob, {type: "bi.flex_center"});
        } else {
            return ob;
        }
    });
    BI.Plugin.registerWidget("bi.vertical_adapt", function (ob) {
        if (isSupportFlex) {
            return BI.extend({}, ob, {type: "bi.flex_vertical_center"});
        } else {
            return ob;
        }
    });
    BI.Plugin.registerWidget("bi.float_center_adapt", function (ob) {
        if (isSupportFlex) {
            return BI.extend({}, ob, {type: "bi.flex_center"});
        } else {
            return ob;
        }
    });

    BI.Plugin.registerWidget("bi.detail_table", function (ob) {
        if (BI.isChrome() || BI.isSafari() || BI.isFireFox()) {
            ob.type = "bi.detail_table_react";
            return ob;
        }
    });
    BI.Plugin.registerWidget("bi.summary_table", function (ob) {
        if (BI.isChrome() || BI.isSafari() || BI.isFireFox()) {
            ob.type = "bi.summary_table_react";
            return ob;
        }
    });
});