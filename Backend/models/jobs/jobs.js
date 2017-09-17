const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const jobSchema = mongoose.Schema(
  {
    quote_id:{
      type:String,
      required:true
    },
    date_logged:{
      type:Number,
      required:true,
      default: Math.floor(new Date().getTime()/1000)//current date in epoch seconds
    },
    date_assigned:{
      type:Number,
      required:false
    },
    planned_start_date:{
      type: Number,
      required: false
    },
    date_started:{
      type:Number,
      required:false
    },
    date_completed:{
      type:Number,
      required:false
    },
    invoice_id:{
      type:String,
      required:false
    },
    job_number:{
      type:Number,
      required:false,
      default:0
    }
  });

  var Job = module.exports = mongoose.model('jobs',jobSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(job, callback)
  {
    console.log('attempting to create a new job.');
    //get job number for new job object
    counters.get('job_count', function(err, counter)
    {
      if(err)
      {
        callback(err);
        return;
      }
      job.job_number = counter.count+1;
      //create new job object
      Job.create(job, function(err, new_job)
      {
        if(err)
        {
          callback(err);
          return;
        }
        //job was successfully created
        callback(err, new_job);
        //update job_count
        counter.count++;
        counters.update('job_count', counter, function(err)
        {
          if(err)
          {
            console.log(err);
            return;
          }
          console.log('successfully updated job_count to %s', counter.count);
        });
        //update timestamp
        counters.timestamp('jobs_timestamp');
      });
    });
  }

  module.exports.get = function(id, callback)
  {
    var query = {_id: id};
    return Job.findOne(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    return Job.find({}, callback);
  }

  module.exports.update = function(job_id, job, callback)
  {
    console.log('attempting to update job[%s].\n', job_id);
    var query = {_id: job_id};
    Job.findOneAndUpdate(query, job, {}, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully updated job.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('jobs_timestamp');
    });
  }

  module.exports.remove = function(job_id, callback)
  {
    var query = {_id: job_id};
    Job.findOneAndRemove(query, callback);
  }

  module.exports.isValid = function(job)
  {
    console.log('validating job:\n%s', JSON.stringify(job));

    if(isNullOrEmpty(job))
      return false;
    //attribute validation
    if(isNullOrEmpty(job.quote_id))
      return false;
    /*if(isNullOrEmpty(job.date_logged))
      return false;
    if(isNullOrEmpty(job.planned_start_date))
      return false;
    if(isNullOrEmpty(job.date_assigned))
      return false;
    if(isNullOrEmpty(job.date_started))
      return false;
    if(isNullOrEmpty(job.date_completed))
      return false;*/
      console.log('valid job.');
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
