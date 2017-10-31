const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const client_jobSchema = mongoose.Schema(
  {
    client_id:{
      type:String,
      required:true
    },
    job_id:{
      type:String,
      required:true
    },
    date_assigned:{
      type:Number,
      required:true
    }
  });


var ClientJobs = module.exports = mongoose.model('clientjobs',client_jobSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(client_job, callback)
{
  ClientJobs.create(client_job,callback);
}

module.exports.get = function(client_id, callback)
{
  var query = {client_id:client_id};
  ClientJobs.find(query, callback);
}

module.exports.getAll = function(callback)
{
  ClientJobs.find({},callback);
}

module.exports.update = function(record_id, client_job, callback)
{
  var query = {_id:record_id};
  ClientJobs.findOneAndUpdate(query,client_job,{},callback)
}

module.exports.isValid = function(client_job)
{
  if(isNullOrEmpty(client_job))
    return false;
  //attribute validation
  if(isNullOrEmpty(client_job.job_id))
    return false;
  if(isNullOrEmpty(client_job.client_id))
    return false;
  if(isNullOrEmpty(client_job.date_assigned))
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
