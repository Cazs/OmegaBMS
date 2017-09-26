const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const resourceSchema = mongoose.Schema(
  {
    resource_name:{
      type:String,
      required:true
    },
    resource_serial:{//a.k.a part-number
      type:String,
      required:false
    },
    resource_type:{
      type:String,
      required:false
    },
    resource_description:{
      type:String,
      required:true
    },
    resource_value:{
      type:Number,
      required:true
    },
    account:{
      type:String,
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
      required:true
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
    console.log('successfully created new resource.')
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

module.exports.update = function (resource_id, resource, callback)
{
  console.log('attempting to update resource [%s].', resource_id);
  console.log('\resource object: %s\n', JSON.stringify(resource));
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
    console.log('successfully updated resource.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('resources_timestamp');
  });
};

module.exports.isValid = function(resource)
{
  if(isNullOrEmpty(resource))
    return false;
  //attribute validation
  if(isNullOrEmpty(resource.resource_name))
    return false;
  if(isNullOrEmpty(resource.resource_description))
    return false;
  if(isNullOrEmpty(resource.resource_value))
    return false;
  /*if(isNullOrEmpty(resource.resource_serial))
    return false;*/
  if(isNullOrEmpty(resource.date_acquired))
    return false;
  /*if(isNullOrEmpty(resource.resource_type))
    return false;*/
  if(isNullOrEmpty(resource.unit))
    return false;
  if(isNullOrEmpty(resource.quantity))
    return false;
  if(isNullOrEmpty(resource.account))
    return false;
  /*if(isNullOrEmpty(resource.date_exhausted))
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
