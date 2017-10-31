const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const saleClientRepSchema = mongoose.Schema(
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

  const SaleClientReps = module.exports = mongoose.model('saleclientreps',saleClientRepSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(saleclientrep, callback)
  {
    SaleClientReps.create(saleclientrep, callback);
  }

  module.exports.get = function(sale_id, callback)
  {
    var query = {sale_id:sale_id};
    SaleClientReps.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    SaleClientReps.find({}, callback);
  }

  module.exports.update = function(record_id, saleclientrep, callback)
  {
    var query = {_id:record_id};
    SaleClientReps.findOneAndUpdate(query, saleclientrep, {}, callback);
  }

  module.exports.isValid = function(saleclientrep)
  {
    if(isNullOrEmpty(saleclientrep))
      return false;
    //attribute validation
    if(isNullOrEmpty(saleclientrep.employee_id))
      return false;
    if(isNullOrEmpty(saleclientrep.sale_id))
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
