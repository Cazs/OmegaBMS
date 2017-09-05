const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const saleSchema = mongoose.Schema(
  {
    creator:{
      type:String,
      required:true
    },
    date_logged:{
      type:Number,
      required:true,
      default: Math.floor(new Date().getTime()/1000)//current date in epoch seconds
    },
    quote_id:{
      type:String,
      required: true
    },
    invoice_id:{
      type:String,
      required:false
    }
  });

  var Sales = module.exports = mongoose.model('sales',saleSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(sale, callback)
  {
    Sales.create(sale, callback);
  }

  module.exports.get = function(id, callback)
  {
    var query = {_id: id};
    Sales.findOne(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    Sales.find({}, callback);
  }

  module.exports.update = function(sale_id, sale, callback)
  {
    var query = {_id: sale_id};
    Sales.findOneAndUpdate(query, sale, {}, callback);
  }

  module.exports.remove = function(sale_id, callback)
  {
    var query = {_id: sale_id};
    Sales.findOneAndRemove(query, callback);
  }

  module.exports.isValid = function(sale)
  {
    console.log('validating sale object:\n%s.', JSON.stringify(sale));

    if(isNullOrEmpty(sale))
      return false;
    //attribute validation
    if(isNullOrEmpty(sale.creator))
      return false;
    if(isNullOrEmpty(sale.quote_id))
      return false;
    /*if(isNullOrEmpty(sale.invoice_id))
      return false;
    if(isNullOrEmpty(sale.date_logged))
      return false;*/

    console.log('valid sale object.\n');
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
