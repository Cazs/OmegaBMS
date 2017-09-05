var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js')

const inspectionIndexSchema = mongoose.Schema(
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

var InspectionIndex = module.exports = mongoose.model('inspectionindices', inspectionIndexSchema);
module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(inspectionindex, callback)
{
  InspectionIndex.create(inspectionindex, callback);
}

module.exports.get = function(inspectionindex_id, callback)
{
  var query = {_id: inspectionindex_id};
  InspectionIndex.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  InspectionIndex.find({},callback);
}

module.exports.update = function(inspectionindex_id, inspectionindex, callback)
{
  var query = {_id: inspectionindex_id};
  InspectionIndex.findOneAndUpdate(query, inspectionindex, {}, callback);
}

module.exports.isValid = function(inspectionindex)
{
  if(isNullOrEmpty(inspectionindex))
    return false;
  //attribute validation
  if(isNullOrEmpty(inspectionindex.index))
    return false;
  if(isNullOrEmpty(inspectionindex.label))
    return false;
  if(isNullOrEmpty(inspectionindex.pdf_path))
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
