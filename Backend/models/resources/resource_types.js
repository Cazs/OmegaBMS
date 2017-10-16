const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const resourceTypeSchema = mongoose.Schema(
  {
    type_name:{
      type:String,
      required:true
    },
    type_description:{
      type:String,
      required:true
    },
    other:{
      type:String,
      required:false
    }
  }
);

var ResourceTypes = module.exports = mongoose.model('resource_types', resourceTypeSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function (resource_type, callback)
{
  console.log('attempting to create new resource_type.');
  ResourceTypes.create(resource_type, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new resource_type.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('resources_timestamp');
  });
};

module.exports.get = function (type_id, callback)
{
  var query = {_id: type_id};
  ResourceTypes.findOne(query, callback);
};

module.exports.getAll = function (callback)
{
  ResourceTypes.find({}, callback);
};

module.exports.update = function (type_id, resource_type, callback)
{
  var query = {_id :type_id};
  ResourceTypes.findOneAndUpdate(query, resource_type, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated resource_type.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('resources_timestamp');
  });
};

module.exports.isValid = function(resource_type)
{
  if(isNullOrEmpty(resource_type))
    return false;
  //attribute validation
  if(isNullOrEmpty(resource_type.type_name))
    return false;
  if(isNullOrEmpty(resource_type.type_description))
    return false;

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
