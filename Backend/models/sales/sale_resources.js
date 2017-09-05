const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const saleResourceSchema = mongoose.Schema(
  {
    sale_id:{
      type:String,
      required:true
    },
    resource_id:{
      type:String,
      required:true
    }
  });

  const SaleResources = module.exports = mongoose.model('saleresources',saleResourceSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(saleresource, callback)
  {
    SaleResources.create(saleresource, callback);
  }

  module.exports.get = function(sale_id, callback)
  {
    var query = {sale_id:sale_id};
    SaleResources.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    SaleResources.find({}, callback);
  }

  module.exports.update = function(record_id, saleresource, callback)
  {
    var query = {_id:record_id};
    SaleResources.findOneAndUpdate(query, saleresource, {}, callback);
  }

  module.exports.isValid = function(saleresource)
  {
    if(isNullOrEmpty(saleresource))
      return false;
    //attribute validation
    if(isNullOrEmpty(saleresource.resource_id))
      return false;
    if(isNullOrEmpty(saleresource.sale_id))
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
