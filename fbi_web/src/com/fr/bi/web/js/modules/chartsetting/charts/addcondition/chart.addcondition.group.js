/**
 * 图标条件选择组
 * Created by GameJian on 2016/7/1.
 */
BI.ChartAddConditionGroup = BI.inherit(BI.Widget, {

    _defaultConfig: function () {
        return BI.extend(BI.ChartAddConditionGroup.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-chart-add-condition-group",
            items: []
        })
    },

    _init: function () {
        BI.ChartAddConditionGroup.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.buttongroup = BI.createWidget({
            type: "bi.button_group",
            element: this.element,
            items: o.items,
            layouts: [{
                type: "bi.vertical",
                bgap: 5
            }]
        });

        this.buttons = this.buttongroup.getAllButtons();

        self._sendEventForButton(this.buttons);
    },

    _sendEventForButton: function (buttons) {
        var self = this;

        BI.each(buttons, function (idx, button) {
            button.on(BI.ChartAddConditionItem.EVENT_CHANGE, function () {
                self._checkNextItemState(this.getValue());
                self.fireEvent(BI.ChartAddConditionGroup.EVENT_CHANGE)
            })
        })
    },

    _checkNextItemState: function (value) {
        var self = this;
        var nextButton = null;

        BI.any(this.buttongroup.getAllButtons(), function (index, button) {
            if (BI.isEqual(button.getValue(), value)) {
                nextButton = self.buttons[index + 1];
                return true;
            }
        });

        if (BI.isNotNull(nextButton)) {
            nextButton.setValue({
                range: BI.extend(nextButton.getValue().range, {
                    min: value.range.max,
                    closemin: !value.range.closemax
                }),
                color: nextButton.getValue().color
            });
        }
    },

    _checkButtonEnable: function () {
        BI.each(this.buttongroup.getAllButtons(), function (idx, button) {
            if (idx !== 0) {
                button.setSmallIntervalEnable(false);
            } else {
                button.setSmallIntervalEnable(true);
            }
        })
    },

    addItem: function () {
        var self = this;
        var item = {
            type: "bi.chart_add_condition_item",
            range: {
                min: 0,
                max: 100,
                closemin: false,
                clasemax: false
            },
            color: "#09ABE9",
            cid: BI.UUID(),
            onRemoveCondition: function (cid) {
                self._removeCondition(cid)
            }
        };

        if (this.buttons.length === 0) {
            item.range.closemin = true;
            item.range.closemax = false;
        } else {
            var beforeButton = this.buttons[this.buttons.length - 1];
            var beforeValue = beforeButton.getValue().range;
            BI.extend(item, {
                range: {
                    min: BI.parseInt(beforeValue.max),
                    max: BI.parseInt(beforeValue.max) + 100,
                    closemin: !beforeValue.closemax,
                    closemax: false
                }
            })
        }

        this.buttongroup.addItems([item]);
        this.buttons = this.buttongroup.getAllButtons();
        if (this.buttons.length !== 1) {
            this.buttons[this.buttons.length - 1].setSmallIntervalEnable(false);
        }

        this._sendEventForButton([this.buttons[this.buttons.length - 1]])
    },

    _removeCondition: function (id) {
        var allConditions = this.buttongroup.getAllButtons();
        var index = -1;

        BI.some(allConditions, function (i, con) {
            if (con.getValue().cid === id) {
                index = i;
                return true;
            }
        });

        this.buttongroup.removeItemAt(index);
        this._checkButtonEnable();
        if (index != 0) {
            this._checkNextItemState(this.buttongroup.getAllButtons()[index - 1].getValue());
        }
        this.fireEvent(BI.ChartAddConditionGroup.EVENT_CHANGE)
    },

    setNumTip: function (numTip) {
        var buttons = this.buttongroup.getAllButtons();
        BI.each(buttons, function (idx, button) {
            button.setNumTip(numTip)
        })
    },

    setValue: function (v) {
        var self = this;
        this.options.items = v || [];

        BI.each(v, function (idx, button) {
            BI.extend(button, {
                type: "bi.chart_add_condition_item",
                onRemoveCondition: function (cid) {
                    self._removeCondition(cid)
                },
                cid: BI.UUID()
            });
        });

        this.buttongroup.addItems(v);
        this._checkButtonEnable();
        this.buttons = this.buttongroup.getAllButtons();
        this._sendEventForButton(this.buttons)
    },

    getValue: function () {
        var buttons = [];

        BI.each(this.buttons, function (inx, button) {
            buttons.push(button.getValue())
        });

        return buttons;
    }

});
BI.ChartAddConditionGroup.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.chart_add_condition_group", BI.ChartAddConditionGroup);