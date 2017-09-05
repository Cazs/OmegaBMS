const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const jobSafetySchema = mongoose.Schema(
  {
    job_id:{
      type:String,
      required:true
    },
    safety_id:{
      type:String,
      required:true
    }
  });

  const jobSafetyDocs = module.exports = mongoose.model('jobsafetydocuments',jobSafetySchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(jobsafetydoc, callback)
  {
    jobSafetyDocs.create(jobsafetydoc, callback);
  }

  module.exports.get = function(job_id, callback)
  {
    var query = {job_id:job_id};
    jobSafetyDocs.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    jobSafetyDocs.find({}, callback);
  }

  module.exports.update = function(record_id, jobsafetydoc, callback)
  {
    var query = {_id:record_id};
    jobSafetyDocs.findOneAndUpdate(query, jobsafetydoc, {}, callback);
  }

  module.exports.isValid = function(jobsafetydoc)
  {
    if(isNullOrEmpty(jobsafetydoc))
      return false;
    //attribute validation
    if(isNullOrEmpty(jobsafetydoc.safety_id))
      return false;
    if(isNullOrEmpty(jobsafetydoc.job_id))
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
