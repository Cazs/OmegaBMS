var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

var supplierSchema = mongoose.Schema(
  {
    supplier_name:{
      type:String,
      required:true
    },
    physical_address:{
      type:String,
      required:true
    },
    postal_address:{
      type:String,
      required:true
    },
    tel:{
      type:String,
      required:true
    },
    fax:{
      type:String,
      required:true
    },
    speciality:{
      type:String,
      required:true
    },
    active:{
      type:Boolean,
      required:true
    },
    date_partnered:{
      type:Number,
      required:true
    },
    website:{
      type:String,
      required:true
    },
    contact_email:{
      type:String,
      required:true
    },
    other:{
      type:String,
      required:false
    }
  });

  var Suppliers = module.exports = mongoose.model('suppliers',supplierSchema);
  
  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.get = function (supplier_id, callback)
  {
    var query = {_id: supplier_id};
    Suppliers.findOne(query, callback);
  };

  module.exports.getAll = function (callback)
  {
    Suppliers.find({},callback);
  };

  module.exports.add = function (supplier, callback)
  {
    console.log('attempting to create new supplier.');
    Suppliers.create(supplier, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully created new supplier.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('suppliers_timestamp');
    });
  };

  module.exports.update = function (supplier_id, supplier, callback)
  {
    console.log('attempting to update supplier [%s].', supplier_id);
    var query = {_id :supplier_id};
    Suppliers.findOneAndUpdate(query, supplier, {}, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully updated supplier.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('suppliers_timestamp');
    });
  };

  module.exports.isValid = function(supplier)
  {
    if(isNullOrEmpty(supplier))
      return false;
    //attribute validation
    if(isNullOrEmpty(supplier.supplier_name))
      return false;
    if(isNullOrEmpty(supplier.physical_address))
      return false;
    if(isNullOrEmpty(supplier.postal_address))
      return false;
    if(isNullOrEmpty(supplier.tel))
      return false;
    if(isNullOrEmpty(supplier.speciality))
      return false;
    if(isNullOrEmpty(supplier.active))
      return false;
    if(isNullOrEmpty(supplier.date_partnered))
      return false;
    if(isNullOrEmpty(supplier.website))
      return false;
    if(isNullOrEmpty(supplier.contact_email))
      return false;
    /*if(isNullOrEmpty(supplier.other))
      return false;*/

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
