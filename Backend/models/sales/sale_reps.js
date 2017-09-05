const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const saleRepSchema = mongoose.Schema(
  {
    sale_id:{
      type:String,
      required:true
    },
    employee_id:{
      type:String,
      required:true
    }
  });

  const SaleReps = module.exports = mongoose.model('salereps',saleRepSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(salerep, callback)
  {
    SaleReps.create(salerep, callback);
  }

  module.exports.get = function(sale_id, callback)
  {
    var query = {sale_id:sale_id};
    SaleReps.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    SaleReps.find({}, callback);
  }

  module.exports.update = function(record_id, salerep, callback)
  {
    var query = {_id:record_id};
    SaleReps.findOneAndUpdate(query, salerep, {}, callback);
  }

  module.exports.isValid = function(salerep)
  {
    if(isNullOrEmpty(salerep))
      return false;
    //attribute validation
    if(isNullOrEmpty(salerep.employee_id))
      return false;
    if(isNullOrEmpty(salerep.sale_id))
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
