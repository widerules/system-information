var ListView = function (model, controller, elements) {
    this._model = model;
    this._controller = controller;
    this._elements = elements;
  
    var _this = this;
  
    // attach model listeners
    this._model.itemAdded.attach(function () {
        _this.rebuildList();
    });
    this._model.itemRemoved.attach(function () {
        _this.rebuildList();
    });
  
    // attach listeners to HTML controls
    this._elements.list.change(function (e) {
        _this._controller.updateSelected(e);
    });
  
};
  
  
ListView.prototype = {
  
    show : function () {
        this.rebuildList();
        var e = this._elements;
        var _this = this;
        e.addButton.click(function () { _this._controller.addItem() });
        e.delButton.click(function () { _this._controller.delItem() });
    },
  
    rebuildList : function () {
        var list = this._elements.list;
        list.html('');
        var items = this._model.getItems();
        for (var key in items)
            list.append($('<option>' + items[key] + '</option>'))
        this._model.setSelectedIndex(-1);
    }
  
};
  
