const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

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
    asset_serial:{
      type:String,
      required:true
    },
    asset_value:{
      type:Number,
      required:true
    },
    account:{
      type:String,
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
    quantity:{
      type:Number,
      required:false
    },
    unit:{
      type:String,
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
  console.log('attempting to create a new asset.');
  Assets.create(asset, function(err, new_asset)
  {
    if(err)
    {
      callback(err);
      return;
    }
    //asset was successfully created
    callback(err, new_asset);
    //update timestamp
    counters.timestamp('assets_timestamp');
  });
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
  console.log('attempting to update asset [%s].', asset_id);
  var query = {_id :asset_id};
  Assets.findOneAndUpdate(query, asset, {}, function(err, asset_obj)
  {
    if(err)
    {
      callback(err);
      return;
    }
    //asset was successfully updated
    console.log('successfully updated asset.');
    callback(err, asset_obj);
    //update timestamp
    counters.timestamp('assets_timestamp');
  });
};

module.exports.isValid = function(asset)
{
  console.log('validating asset object:\n%s', JSON.stringify(asset));

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
  if(isNullOrEmpty(asset.account))
    return false;
  /*if(isNullOrEmpty(resource.date_exhausted))
    return false;*/
  console.log('valid asset.');
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
