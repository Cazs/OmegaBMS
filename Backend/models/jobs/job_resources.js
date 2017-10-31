const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const jobResourceSchema = mongoose.Schema(
  {
    job_id:{
      type:String,
      required:true
    },
    resource_id:{
      type:String,
      required:true
    }
  });

  const JobResources = module.exports = mongoose.model('jobresources',jobResourceSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(jobresource, callback)
  {
    JobResources.create(jobresource, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully created new job resources.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('jobs_timestamp');
    });
  }

  module.exports.get = function(job_id, callback)
  {
    var query = {job_id:job_id};
    JobResources.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    JobResources.find({}, callback);
  }

  module.exports.update = function(record_id, jobresource, callback)
  {
    var query = {_id:record_id};
    JobResources.findOneAndUpdate(query, jobresource, {}, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully updated job resources.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('jobs_timestamp');
    });
  }

  module.exports.isValid = function(jobresource)
  {
    if(isNullOrEmpty(jobresource))
      return false;
    //attribute validation
    if(isNullOrEmpty(jobresource.resource_id))
      return false;
    if(isNullOrEmpty(jobresource.job_id))
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
