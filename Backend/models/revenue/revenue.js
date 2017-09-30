var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const revenueSchema = mongoose.Schema(
{
  revenue_title:{
      type:String,
      required:true
  },
  revenue_description:{
    type:String,
    required:true
  },
  revenue_value:{
    type:Number,
    required:true
  },
  date_logged:{
    type: Number,
    required:false,
    default:Math.floor(new Date().getTime()/1000)//current date in epoch seconds
  },
  creator:{
    type:String,
    required:true
  },
  account:{
    type:String,
    required:true
  },
  revision:{
    type:Number,
    required:false,
    default:0.0
  },
  other:{
    type: String,
    required:false
  }
});

const Revenue = module.exports = mongoose.model('revenues',revenueSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(revenue, callback)
{
  console.log('attempting to create a new revenue.');
  Revenue.create(revenue, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new revenue.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('revenues_timestamp');
  });
}

module.exports.get = function(revenue_id, callback)
{
  var query = {_id: revenue_id};
  Revenue.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
    Revenue.find({}, callback);
}

module.exports.update = function(record_id, revenue, callback)
{
  var query = {_id: record_id};
  console.log('attempting to update revenue[%s].', record_id);
  Revenue.findOneAndUpdate(query, revenue, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated revenue.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('revenues_timestamp');
  });
}

module.exports.isValid = function(revenue)
{
  console.log('validating revenue:\n%s', JSON.stringify(revenue));

  if(isNullOrEmpty(revenue))
    return false;
  //attribute validation
  if(isNullOrEmpty(revenue.revenue_title))
    return false;
  if(isNullOrEmpty(revenue.revenue_description))
    return false;
  if(isNullOrEmpty(revenue.revenue_value))
    return false;
  if(isNullOrEmpty(revenue.date_logged))
    return false;
  if(isNullOrEmpty(revenue.creator))
    return false;
  if(isNullOrEmpty(revenue.account))
    return false;

    console.log('valid revenue.');
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
