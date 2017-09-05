var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js')

const riskIndexSchema = mongoose.Schema(
{
  index:{
    type:Number,
    required:true
  },
  label:{
    type:String,
    required:true
  },
  pdf_path:{
    type:String,
    required:true
  }
});

var RiskIndex = module.exports = mongoose.model('riskindex', riskIndexSchema);
module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(riskindex, callback)
{
  RiskIndex.create(riskindex, callback);
}

module.exports.get = function(riskindex_id, callback)
{
  var query = {_id: riskindex_id};
  RiskIndex.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  RiskIndex.find({},callback);
}

module.exports.update = function(riskindex_id, riskindex, callback)
{
  var query = {_id: riskindex_id};
  RiskIndex.findOneAndUpdate(query, riskindex, {}, callback);
}

module.exports.isValid = function(riskindex)
{
  if(isNullOrEmpty(riskindex))
    return false;
  //attribute validation
  if(isNullOrEmpty(riskindex.index))
    return false;
  if(isNullOrEmpty(riskindex.label))
    return false;
  if(isNullOrEmpty(riskindex.pdf_path))
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
