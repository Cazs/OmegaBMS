const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const purchase_orderSchema = mongoose.Schema(
  {
    number:{
      type:Number,
      required:false
    },
    supplier_id:{
      type:String,
      required:true
    },
    contact_person_id:{
      type:String,
      required:true
    },
    vat:{
      type:Number,
      required:true
    },
    account:{
      type:String,
      required:true
    },
    status:{
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

  const purchase_orders = module.exports = mongoose.model('purchase_orders',purchase_orderSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(purchase_order, callback)
  {
    console.log('attempting to create new purchase order.');
    counters.get('purchaseorder_count', function(err, counter)
    {
      if(err)
      {
        callback(err);
        return;
      }
      purchase_order.number = counter.count+1;
      //create new purchase_order object
      purchase_orders.create(purchase_order, function(error, res_obj)
      {
        if(error)
        {
          console.log(error);
          if(callback)
            callback(error);
          return;
        }
        console.log('successfully created new purchase_order.')
        if(callback)
          callback(error, res_obj);
        //update timestamp
        counters.timestamp('purchase_orders_timestamp');

        //update purchaseorder_count
        counter.count++;
        counters.update('purchaseorder_count', counter, function(err)
        {
          if(err)
          {
            console.log(err);
            return;
          }
          console.log('successfully updated purchaseorder_count to %s', counter.count);
        });
      });
    });
  }

  module.exports.get = function(record_id, callback)
  {
    var query = {_id:record_id};
    purchase_orders.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    purchase_orders.find({}, callback);
  }

  module.exports.update = function(record_id, purchase_order, callback)
  {
    var query = {_id:record_id};
    console.log('attempting to update purchase order[%s].', record_id);
    purchase_orders.findOneAndUpdate(query, purchase_order, {}, function(error, res_obj)
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
      counters.timestamp('purchase_orders_timestamp');
    });
  }

  module.exports.isValid = function(purchase_order)
  {
    console.log('validating purchase_order object:\n%s', JSON.stringify(purchase_order));

    if(isNullOrEmpty(purchase_order))
      return false;

    //attribute validation
    if(isNullOrEmpty(purchase_order.supplier_id))
      return false;
    /*if(isNullOrEmpty(purchase_order.resource_id) && isNullOrEmpty(purchase_order.asset_id))
      return false;*/
    if(isNullOrEmpty(purchase_order.vat))
      return false;
    if(isNullOrEmpty(purchase_order.status))
      return false;
    if(isNullOrEmpty(purchase_order.contact_person_id))
      return false;
    if(isNullOrEmpty(purchase_order.account))
      return false;
    if(isNullOrEmpty(purchase_order.creator))
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
