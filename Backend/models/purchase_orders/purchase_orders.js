const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const purchaseOrderSchema = mongoose.Schema(
  {
    number:{
      type:Number,
      required:false
    },
    supplier_id:{
      type:String,
      required:true
    },
    description:{
      type:String,
      required:true
    },
    quantity:{
      type:Number,
      required:true
    },
    price:{
      type:Number,
      required:true
    },
    discount:{
      type:Number,
      required:true
    },
    vat:{
      type:Number,
      required:true
    },
    date_logged:{
      type:Number,
      required:false,
      default:Math.floor(new Date().getTime()/1000)//epoch seconds
    },
    creator:{
      type:String,
      required:true
    },
    extra:{
      type:String,
      required:false
    }
  });

  const PurchaseOrders = module.exports = mongoose.model('purchaseorders',purchaseOrderSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(purchaseorder, callback)
  {
    console.log('attempting to create new purchase order.');
    PurchaseOrders.create(purchaseorder, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully created new purchaseorder.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('purchaseorders_timestamp');
    });
  }

  module.exports.get = function(record_id, callback)
  {
    var query = {_id:record_id};
    PurchaseOrders.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    PurchaseOrders.find({}, callback);
  }

  module.exports.update = function(record_id, purchaseorder, callback)
  {
    var query = {_id:record_id};
    console.log('attempting to update purchase order[%s].', record_id);
    PurchaseOrders.findOneAndUpdate(query, purchaseorder, {}, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully updated purchase order.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('purchaseorders_timestamp');
    });
  }

  module.exports.isValid = function(purchaseorder)
  {
    console.log('validating purchase order object:\n%s', JSON.stringify(purchaseorder));

    if(isNullOrEmpty(purchaseorder))
      return false;

    //attribute validation
    if(isNullOrEmpty(purchaseorder.supplier_id))
      return false;
    if(isNullOrEmpty(purchaseorder.description))
      return false;
    if(isNullOrEmpty(purchaseorder.quantity))
      return false;
    if(isNullOrEmpty(purchaseorder.price))
      return false;
    if(isNullOrEmpty(purchaseorder.discount))
      return false;
    if(isNullOrEmpty(purchaseorder.vat))
      return false;
    if(isNullOrEmpty(purchaseorder.creator))
      return false;
    
    console.log('valid purchase order object.');

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
