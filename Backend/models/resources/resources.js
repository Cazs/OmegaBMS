const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

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
  Resources.create(resource, callback);
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
  var query = {_id :resource_id};
  Resources.findOneAndUpdate(query, resource, {}, callback);
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
