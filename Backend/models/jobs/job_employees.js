const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const jobEmployeeSchema = mongoose.Schema(
  {
    job_id:{
      type:String,
      required:true
    },
    usr:{
      type:String,
      required:true
    },
    date_logged:{
      type:Number,
      required:true,
      default:Math.floor(new Date().getTime()/1000)//current date in epoch seconds
    }
  });

  const JobEmployees = module.exports = mongoose.model('jobemployees',jobEmployeeSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(jobemployee, callback)
  {
    console.log('attempting to create a new job_employee.');
    JobEmployees.create(jobemployee, callback);
  }

  module.exports.get = function(job_id, callback)
  {
    var query = {job_id:job_id};
    JobEmployees.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    JobEmployees.find({}, callback);
  }

  module.exports.update = function(record_id, jobemployee, callback)
  {
    console.log('attempting to update job_employee[%s].', record_id);
    var query = {_id:record_id};
    JobEmployees.findOneAndUpdate(query, jobemployee, {}, callback);
  }

  module.exports.isValid = function(jobemployee)
  {
    console.log('validating job_employee:\n%s', JSON.stringify(jobemployee));

    if(isNullOrEmpty(jobemployee))
      return false;
    //attribute validation
    if(isNullOrEmpty(jobemployee.job_id))
      return false;
    if(isNullOrEmpty(jobemployee.usr))
      return false;
      console.log('valid job_employee.');

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
