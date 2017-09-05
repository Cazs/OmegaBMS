const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const assetSchema = mongoose.Schema(
  {
    asset_name:{
      type:String,
      required:true
    },
    asset_type:{
      type:String,
      required:true
    },
    asset_description:{
      type:String,
      required:true
    },
    asset_value:{
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
    other:{
      type:String,
      required:false
    }
  }
);

var Assets = module.exports = mongoose.model('assets', assetSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function (asset, callback)
{
  Assets.create(asset, callback);
};

module.exports.get = function (asset_id, callback)
{
  var query = {_id: asset_id};
  Assets.findOne(query, callback);
};

module.exports.getAll = function (callback)
{
  Assets.find({}, callback);
};

module.exports.update = function (asset_id, asset, callback)
{
  var query = {_id :asset_id};
  Assets.findOneAndUpdate(query, asset, {}, callback);
};

module.exports.isValid = function(asset)
{
  if(isNullOrEmpty(asset))
    return false;
  //attribute validation
  if(isNullOrEmpty(asset.asset_name))
    return false;
  if(isNullOrEmpty(asset.asset_description))
    return false;
  if(isNullOrEmpty(asset.asset_value))
    return false;
  if(isNullOrEmpty(asset.date_acquired))
    return false;
  if(isNullOrEmpty(asset.asset_type))
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
