const mongoose = require('mongoose');
const crypto = require('crypto');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

var VALIDATION_MODE_STRICT = "strict";
var VALIDATION_MODE_PASSIVE = "passive";

const employeeSchema = mongoose.Schema(
  {
    usr:{
      type:String,
      required:true
    },
    pwd:{
      type:String,
      required:true
    },
    access_level:{
      type:Number,
      required:true
    },
    firstname:{
      type:String,
      required:true
    },
    lastname:{
      type:String,
      required:true
    },
    gender:{
      type:String,
      required:true
    },
    email:{
      type:String,
      required:true
    },
    tel:{
      type:String,
      required:true
    },
    cell:{
      type:String,
      required:true
    },
    date_joined:{
      type:Number,
      required:true
    },
    active:{
      type:Boolean,
      required:true
    },
    other:{
      type:String,
      required:false
    }
  });

const Employees = module.exports = mongoose.model('employees',employeeSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods
var VALIDATION_MODE = VALIDATION_MODE_STRICT;

module.exports.getAll = function(callback)
{
  Employees.find({}, callback);
};

module.exports.get = function(id,callback)
{
  var query = {usr:id};
  Employees.findOne(query, callback);
};

module.exports.add = function(employee, callback)
{
  //Check if user being created is not greater than a normal user
  if(employee.access_level)
  {
    if(employee.access_level>access_levels.NORMAL)
    {
      console.log('attempting to create an account with elevated privilleges.');
      if(employee.access_level>access_levels.ADMIN)//creating super user account
      {
        //check if super user account exists
        Employees.findOne({access_level:3}, function(err, employee)
        {
          if(err)
          {
            callback(err);
            return;
          }
          if(employee)
          {
            callback(new Error('super user account already exists.'));
            return;
          }
          //set date_joined & active status - vericodes
          //creating super user account
          console.log('info: creating a super user account.');
        });
      }else{
        //set date_joined & active status - vericodes
        //creating an admin account
        console.log('info: creating an admin account.');
      }
    }else{
      //set date_joined & active status - vericodes
      //creating normal account
    }
  }
  /*var sha = crypto.createHash('sha1');
  employee.pwd = sha.update(employee.pwd).digest('hex');
  Employees.create(employee, callback);*/
};

module.exports.update = function(employee_id,employee,callback)
{
  var query = {_id:employee_id};
  //var sha = crypto.createHash('sha1');
  //employee.pwd = sha.update(employee.pwd).digest('hex');
  Employees.findOneAndUpdate(query,employee,{},callback);
}

//intended mainly for login/authentication
module.exports.validate = function(usr, pwd, callback)
{
  var pass = crypto.createHash('sha1').update(pwd).digest('hex');
  var query = {usr:usr, pwd:pass};
  Employees.findOne(query, callback);
}

module.exports.isValid = function(employee)
{
  if(isNullOrEmpty(employee))
    return false;
  //attribute validation
  if(VALIDATION_MODE==VALIDATION_MODE_STRICT)
  {
    console.log('validating employee with strict mode enabled.');
    if(isNullOrEmpty(employee.usr))
      return false;
    if(isNullOrEmpty(employee.pwd))
      return false;
  }
  if(isNullOrEmpty(employee.firstname))
    return false;
  if(isNullOrEmpty(employee.lastname))
    return false;
  if(isNullOrEmpty(employee.gender))
    return false;
  if(isNullOrEmpty(employee.access_level))
    return false;
  if(isNullOrEmpty(employee.email))
    return false;
  if(isNullOrEmpty(employee.tel))
    return false;
  if(isNullOrEmpty(employee.cell))
    return false;
  /*if(isNullOrEmpty(employee.date_joined))
    return false;
  if(isNullOrEmpty(employee.active))
    return false;*/

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
