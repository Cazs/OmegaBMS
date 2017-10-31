var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js')

const ohsIndexSchema = mongoose.Schema(
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

var OHSIndex = module.exports = mongoose.model('ohsindices', ohsIndexSchema);
module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(ohsindex, callback)
{
  OHSIndex.create(ohsindex, callback);
}

module.exports.get = function(ohsindex_id, callback)
{
  var query = {_id: ohsindex_id};
  OHSIndex.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  OHSIndex.find({},callback);
}

module.exports.update = function(ohsindex_id, ohsindex, callback)
{
  var query = {_id: ohsindex_id};
  OHSIndex.findOneAndUpdate(query, ohsindex, {}, callback);
}

module.exports.isValid = function(ohsindex)
{
  if(isNullOrEmpty(ohsindex))
    return false;
  //attribute validation
  if(isNullOrEmpty(ohsindex.index))
    return false;
  if(isNullOrEmpty(ohsindex.label))
    return false;
  if(isNullOrEmpty(ohsindex.pdf_path))
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
