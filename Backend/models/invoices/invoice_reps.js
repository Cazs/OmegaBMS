const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const invoiceRepSchema = mongoose.Schema(
  {
    invoice_id:{
      type:String,
      required:true
    },
    usr:{
      type:String,
      required:true
    }
  });

  const InvoiceReps = module.exports = mongoose.model('invoicereps',invoiceRepSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(invoicerep, callback)
  {
    InvoiceReps.create(invoicerep, callback);
  }

  module.exports.get = function(invoice_id, callback)
  {
    var query = {invoice_id:invoice_id};
    InvoiceReps.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    InvoiceReps.find({}, callback);
  }

  module.exports.update = function(record_id, invoicerep, callback)
  {
    var query = {_id:record_id};
    InvoiceReps.findOneAndUpdate(query, invoicerep, {}, callback);
  }

  module.exports.isValid = function(invoicerep)
  {
    if(isNullOrEmpty(invoicerep))
      return false;
    //attribute validation
    if(isNullOrEmpty(invoicerep.usr))
      return false;
    if(isNullOrEmpty(invoicerep.invoice_id))
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
