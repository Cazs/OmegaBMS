const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const assetTypeSchema = mongoose.Schema(
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

var AssetTypes = module.exports = mongoose.model('asset_types', assetTypeSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function (asset_type, callback)
{
  console.log('attempting to create new asset_type.');
  AssetTypes.create(asset_type, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new asset_type.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('assets_timestamp');
  });
};

module.exports.get = function (type_id, callback)
{
  var query = {_id: type_id};
  AssetTypes.findOne(query, callback);
};

module.exports.getAll = function (callback)
{
  AssetTypes.find({}, callback);
};

module.exports.update = function (type_id, asset_type, callback)
{
  console.log('attempting to update asset_type[%s].', type_id);
  var query = {_id :type_id};
  AssetTypes.findOneAndUpdate(query, asset_type, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated asset_type.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('assets_timestamp');
  });
};

module.exports.isValid = function(asset_type)
{
  console.log('validating asset_type object:\n%s', JSON.stringify(asset_type));

  if(isNullOrEmpty(asset_type))
    return false;
  //attribute validation
  if(isNullOrEmpty(asset_type.type_name))
    return false;
  if(isNullOrEmpty(asset_type.type_description))
    return false;

  console.log('valid asset_type.');
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
