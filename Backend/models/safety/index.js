var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js')

const safetyIndexSchema = mongoose.Schema(
{
  index:{
    type:Number,
    required:true
  },
  label:{
    type:String,
    required:true
  },
  required:{
    type:Boolean,
    required:true
  },
  pdf_path:{
    type:String,
    required:true
  },
  type:{
    type:String,
    required:true
  },
  logo_options:{
    type:String,
    required:false
  }
});

var SafetyIndex = module.exports = mongoose.model('safetyindex', safetyIndexSchema);
module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(safetyindex, callback)
{
  SafetyIndex.create(safetyindex, callback);
}

module.exports.get = function(safetyindex_id, callback)
{
  var query = {_id: safetyindex_id};
  SafetyIndex.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  SafetyIndex.find({},callback);
}

module.exports.update = function(safetyindex_id, safetyindex, callback)
{
  var query = {_id: safetyindex_id};
  SafetyIndex.findOneAndUpdate(query, safetyindex, {}, callback);
}

module.exports.isValid = function(safetyindex)
{
  if(isNullOrEmpty(safetyindex))
    return false;
  //attribute validation
  if(isNullOrEmpty(safetyindex.index))
    return false;
  if(isNullOrEmpty(safetyindex.label))
    return false;
  if(isNullOrEmpty(safetyindex.required))
    return false;
  if(isNullOrEmpty(safetyindex.pdf_path))
    return false;
  if(isNullOrEmpty(safetyindex.type))
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
