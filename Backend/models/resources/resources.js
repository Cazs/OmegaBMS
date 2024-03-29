const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

'use strict'

const resourceSchema = mongoose.Schema(
  {
    resource_name:{
      type:String,
      required:true
    },
    resource_serial:{//a.k.a part-number
      type:String,
      required:true
    },
    resource_type:{
      type:String,
      required:true
    },
    resource_description:{
      type:String,
      required:true
    },
    resource_value:{
      type:Number,
      required:true
    },
    unit:{
      type:String,
      required:true
    },
    quantity:{//amount available
      type:Number,
      required:true
    },
    date_acquired:{
      type:Number,
      required:false
    },
    date_exhausted:{
      type:Number,
      required:false
    },
    extra:{
      type:String,
      required:false
    }
  }
);

var Resources = module.exports = mongoose.model('resources', resourceSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function (resource, callback)
{
  console.log('attempting to create new resource.');
  Resources.create(resource, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new resource.');
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('resources_timestamp');
  });
};

module.exports.get = function (resource_id, callback)
{
  var query = {_id: resource_id};
  Resources.findOne(query, callback);
};

module.exports.getAll = function (callback)
{
  Resources.find({}, callback);
};

module.exports.update = function(resource_id, resource, callback)
{
  console.log('attempting to update resource [%s].', resource_id);
  var query = {_id :resource_id};
  Resources.findOneAndUpdate(query, resource, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated resource.');
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('resources_timestamp');
  });
};

module.exports.incrementQuantity = function(resource_id, resource, callback)
{
  console.log("attempting to increment resource [%s] quantity.", resource_id);
  var _update = this.update;
  this.get(resource_id, function(err, old_resource)
  {
    if(err)
    {
      console.log(error);
      return;
    }

    var old_qty = new Number(old_resource.quantity);
    var qty_inc_val = new Number(resource.quantity);
    var qty = (old_qty + qty_inc_val);

    console.log("incrementing resource [%s] quantity from [%s] to [%s].", resource.resource_name, old_qty, qty);

    resource.quantity = qty;
    _update(resource_id, resource, callback);
  });
};

module.exports.isValid = function(resource)
{
  console.log('validating resource:\n%s', JSON.stringify(resource));

  if(isNullOrEmpty(resource))
    return false;
  //attribute validation
  if(isNullOrEmpty(resource.resource_name))
    return false;
  if(isNullOrEmpty(resource.resource_description))
    return false;
  if(isNullOrEmpty(resource.resource_value))
    return false;
  if(isNullOrEmpty(resource.resource_serial))
    return false;
  /*if(isNullOrEmpty(resource.date_acquired))
    return false;*/
  if(isNullOrEmpty(resource.resource_type))
    return false;
  if(isNullOrEmpty(resource.unit))
    return false;
  if(isNullOrEmpty(resource.quantity))
    return false;
  /*if(isNullOrEmpty(resource.date_exhausted))
    return false;*/
  console.log('valid resource.');
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
