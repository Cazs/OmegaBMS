const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const client_employeeSchema = mongoose.Schema(
  {
    employee_id:{
      type:String,
      required:true
    },
    client_id:{
      type:String,
      required:true
    },
    date_assigned:{
      type:Number,
      required:true
    }
  });

  const ClientEmployee = module.exports = mongoose.model('ClientEmployee',client_employeeSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(client_employee, callback)
  {
    ClientEmployee.create(client_employee, callback);
  }

  module.exports.get = function(client_id, callback)
  {
    var query = {client_id: client_id};
    ClientEmployee.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    ClientEmployee.find({},callback);
  }

  module.exports.update = function(record_id, client_employee, callback)
  {
    var query = {_id:record_id};
    ClientEmployee.findOneAndUpdate(query, client_employee, {}, callback);
  }

  module.exports.isValid = function(client_employee)
  {
    if(isNullOrEmpty(client_employee))
      return false;
    //attribute validation
    if(isNullOrEmpty(client_employee.employee_id))
      return false;
    if(isNullOrEmpty(client_employee.client_id))
      return false;
    if(isNullOrEmpty(client_employee.date_assigned))
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
