var ListController = function (model) {
    this._model = model;
};
  
ListController.prototype = {
  
    addItem : function () {
        var item = prompt('Add item:', '');
        if (item)
            this._model.addItem(item);
    },
  
    delItem : function () {
        var index = this._model.getSelectedIndex();
        if (index != -1)
            this._model.removeItemAt(this._model.getSelectedIndex());
    },
  
    updateSelected : function (e) {
        this._model.setSelectedIndex(e.target.selectedIndex);
    }
  
};
