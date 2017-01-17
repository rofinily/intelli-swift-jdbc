/**
 * Created by GUY on 2016/2/2.
 *
 * @class BI.MapTypeCombo
 * @extend BI.Widget
 */
BI.MapTypeCombo = BI.inherit(BI.Widget, {

    _CONST: {
        SHOW_MAP_LAYER: 2
    },

    _defaultConfig: function () {
        return BI.extend(BI.MapTypeCombo.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-map-type-combo",
            width: 25,
            height: 25
        })
    },

    _init: function () {
        BI.MapTypeCombo.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        var items = [];
        BI.each(MapConst.INNER_MAP_INFO.MAP_TYPE_NAME, function (key, value) {
            if (MapConst.INNER_MAP_INFO.MAP_LAYER[key] < self._CONST.SHOW_MAP_LAYER) {
                items.push({
                    text: value,
                    value: key,
                    title: value,
                    iconClass: MapConst.INNER_MAP_INFO.MAP_LAYER[key] === 0 ? "drag-map-china-icon" : "drag-map-svg-icon"
                });
            }
        });
        BI.each(MapConst.CUSTOM_MAP_INFO.MAP_TYPE_NAME, function (key, value) {
            items.push({
                text: value,
                value: key,
                title: value,
                iconClass: "drag-map-svg-icon"
            });
        });
        this.trigger = BI.createWidget({
            type: "bi.icon_combo_trigger",
            iconClass: "drag-map-china-icon",
            title: o.title,
            items: items,
            width: o.width,
            height: o.height,
            iconWidth: o.iconWidth,
            iconHeight: o.iconHeight
        });
        this.popup = BI.createWidget({
            type: "bi.map_type_popup"
        });
        this.popup.on(BI.MapTypePopup.EVENT_CHANGE, function () {
            self.setValue(self.popup.getValue());
            self.mapTypeCombo.hideView();
            self.fireEvent(BI.MapTypeCombo.EVENT_CHANGE);
        });
        this.popup.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.Controller.EVENT_CHANGE, arguments);
        });
        this.mapTypeCombo = BI.createWidget({
            type: "bi.combo",
            element: this.element,
            direction: "bottom",
            adjustLength: 3,
            el: this.trigger,
            popup: {
                el: this.popup,
                maxHeight: 300
            }
        });
    },

    showView: function () {
        this.mapTypeCombo.showView();
    },

    hideView: function () {
        this.mapTypeCombo.hideView();
    },

    setValue: function (v) {
        v = v || {};
        this.mapTypeCombo.setValue(v.sub_type || []);
    },

    setEnable: function (v) {
        BI.MapTypeCombo.superclass.setEnable.apply(this, arguments);
        this.mapTypeCombo.setEnable(v);
    },

    getValue: function () {
        var arr = this.mapTypeCombo.getValue();
        return BI.isEmptyArray(arr) ? [] : {type: BICst.WIDGET.MAP, subType: arr};
    }
});
BI.MapTypeCombo.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.map_type_combo", BI.MapTypeCombo);