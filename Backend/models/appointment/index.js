var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js')

const appointmentIndexSchema = mongoose.Schema(
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

var AppointmentIndex = module.exports = mongoose.model('appointmentindices', appointmentIndexSchema);
module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(appointmentindex, callback)
{
  AppointmentIndex.create(appointmentindex, callback);
}

module.exports.get = function(appointmentindex_id, callback)
{
  var query = {_id: appointmentindex_id};
  AppointmentIndex.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  AppointmentIndex.find({},callback);
}

module.exports.update = function(appointmentindex_id, appointmentindex, callback)
{
  var query = {_id: appointmentindex_id};
  AppointmentIndex.findOneAndUpdate(query, appointmentindex, {}, callback);
}

module.exports.isValid = function(appointmentindex)
{
  if(isNullOrEmpty(appointmentindex))
    return false;
  //attribute validation
  if(isNullOrEmpty(appointmentindex.index))
    return false;
  if(isNullOrEmpty(appointmentindex.label))
    return false;
  if(isNullOrEmpty(appointmentindex.pdf_path))
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
