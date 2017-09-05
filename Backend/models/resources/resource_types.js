const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

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
  ResourceTypes.create(resource_type, callback);
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
  ResourceTypes.findOneAndUpdate(query, resource_type, {}, callback);
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
