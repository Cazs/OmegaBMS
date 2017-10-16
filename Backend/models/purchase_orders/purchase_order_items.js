const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const purchaseOrderItemSchema = mongoose.Schema(
  {
    item_number:{
      type:Number,
      required:true
    },
    purchase_order_id:{
      type:String,
      required:true
    },
    item_id:{
      type:String,
      required:true
    },
    quantity:{
      type:Number,
      required:true
    },
    discount:{
      type:Number,
      required:true
    },
    date_logged:{
      type:Number,
      required:false,
      default:Math.floor(new Date().getTime()/1000)//epoch seconds
    },
    extra:{
      type:String,
      required:false
    }
  });

  const PurchaseOrderItems = module.exports = mongoose.model('purchase_order_item', purchaseOrderItemSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(purchase_order_item, callback)
  {
    console.log('attempting to create new Purchase Order Item for Purchase Order [%s].', purchase_order_item.purchase_order_id);
    PurchaseOrderItems.create(purchase_order_item, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully created new purchase_order_item.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('purchase_orders_timestamp');
    });
  }

  module.exports.get = function(purchase_order_id, callback)
  {
    var query = {purchase_order_id:purchase_order_id};
    PurchaseOrderItems.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    PurchaseOrderItems.find({}, callback);
  }

  module.exports.update = function(record_id, purchase_order_item, callback)
  {
    var query = {_id:record_id};
    console.log('attempting to update purchase_order_item [%s].', record_id);
    PurchaseOrderItems.findOneAndUpdate(query, purchase_order_item, {}, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully updated purchase_order_item.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('purchase_orders_timestamp');
    });
  }

  module.exports.isValid = function(purchase_order_item)
  {
    console.log('validating purchase_order_item object:\n%s', JSON.stringify(purchase_order_item));

    if(isNullOrEmpty(purchase_order_item))
      return false;

    //attribute validation
    if(isNullOrEmpty(purchase_order_item.item_id))
      return false;
    if(isNullOrEmpty(purchase_order_item.item_number))
      return false;
    if(isNullOrEmpty(purchase_order_item.purchase_order_id))
      return false;
    if(isNullOrEmpty(purchase_order_item.discount))
      return false;
    if(isNullOrEmpty(purchase_order_item.quantity))
      return false;

    console.log('valid purchase_order_item object.');

      return true;
  }

  isNullOrEmpty = function(obj)
  {
    if(obj==null)
    {
      return true;
    }
    if(obj.length<=0)
    {
      return true;
    }
    return false;
  }
