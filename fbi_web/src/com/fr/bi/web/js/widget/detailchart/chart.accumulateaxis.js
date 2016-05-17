/**
 * 图表控件
 * @class BI.AccumulateAxisChart
 * @extends BI.Widget
 */
BI.AccumulateAxisChart = BI.inherit(BI.Widget, {

    _defaultConfig: function () {
        return BI.extend(BI.AccumulateAxisChart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-accumulate-axis-chart"
        })
    },

    _init: function () {
        BI.AccumulateAxisChart.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.AccumulateAxisChart = BI.createWidget({
            type: "bi.chart",
            element: this.element
        });
        self.AccumulateAxisChart.setChartType(BICst.WIDGET.ACCUMULATE_AXIS);
        this.AccumulateAxisChart.on(BI.Chart.EVENT_CHANGE, function (obj) {
            self.fireEvent(BI.AccumulateAxisChart.EVENT_CHANGE, obj);
        });
    },

    populate: function (items) {
        this.AccumulateAxisChart.resize();
        this.AccumulateAxisChart.populate(items);
    },

    loading: function(){
        this.AccumulateAxisChart.loading();
    },

    loaded: function(){
        this.AccumulateAxisChart.loaded();
    },

    resize: function () {
        this.AccumulateAxisChart.resize();
    }
});
BI.AccumulateAxisChart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.accumulate_axis_chart', BI.AccumulateAxisChart);