BIDezi.WidgetModel = BI.inherit(BI.Model, {
    _defaultConfig: function () {
        return BI.extend(BIDezi.WidgetModel.superclass._defaultConfig.apply(this), {
            name: "",
            bounds: {},
            linkages: [],
            type: BICst.WIDGET.TABLE,
            dimensions: {},
            view: {},
            settings: {},
            _page_: {}  //一个只前台使用的当前page属性 不要随便拿了用
        })
    },

    change: function (changed, pre) {
        if (BI.has(changed, "detail")) {
            this.set(this.get("detail"), {
                notrefresh: true
            });
        }
        //维度或指标改变时需要调节联动设置
        if (BI.has(changed, "dimensions")) {
            var dimensions = this.cat("dimensions");
            var dids = BI.keys(dimensions);
            var linkages = this.get("linkages");
            BI.remove(linkages, function (i, linkage) {
                var change = false;
                //计算指标修改时删除相关的联动
                if(linkage.cids[0]) {
                    var ids = BI.concat(BI.Utils.getCalculateTargetIdsByID(linkage.cids[0]), linkage.cids[0]);
                    BI.any(linkage.cids, function (idx, id) {
                        if(!BI.contains(ids, id)) {
                            return change = true;
                        }
                    });
                }
                return !dids.contains(linkage.from) || (linkage.cids && linkage.cids.length > 0 && !dids.contains(linkage.cids[0])) || change;
            });
            this.set("linkages", linkages);
            this.refresh();
        }
        if (BI.has(changed, "linkages")) {
            //找到所有被删除掉的linkages，通知到相关的组件
            BI.each(pre.linkages, function (i, preLink) {
                var found = BI.some(changed.linkages, function (j, link) {
                    if (link.from === preLink.from && link.to === preLink.to) {
                        return true;
                    }
                });
                if (found === false && BI.Utils.isWidgetExistByID(preLink.to)) {
                    BI.Broadcasts.send(BICst.BROADCAST.LINKAGE_PREFIX + preLink.to, preLink.from);
                }
            });
        }
        if (BI.has(changed, "filter_value")) {
            this.refresh();
        }
        if (BI.has(changed, "settings")) {
            this.refresh();
            //联动传递过滤条件发生改变的时候，清一下联动到的组件
            if (changed.settings.transfer_filter !== pre.settings.transfer_filter) {
                BI.each(this.get("linkages"), function (i, link) {
                    if (BI.Utils.isWidgetExistByID(link.to)) {
                        BI.Broadcasts.send(BICst.BROADCAST.LINKAGE_PREFIX + link.to, link.from);
                    }
                });
            }
        }
    },

    refresh: function () {
        this.tmp({
            detail: {
                dimensions: this.get("dimensions"),
                view: this.get("view"),
                type: this.get("type"),
                settings: this.get("settings"),
                filter_value: this.get("filter_value"),
                real_data: this.get("real_data")
            }
        }, {
            silent: true
        });
    },

    local: function () {
        if (this.has("expand")) {
            this.get("expand");
            return true;
        }
        return false;
    },

    _init: function () {
        BIDezi.WidgetModel.superclass._init.apply(this, arguments);
    }
});