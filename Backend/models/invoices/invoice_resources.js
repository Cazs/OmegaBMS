const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const invoiceResourceSchema = mongoose.Schema(
  {
    invoice_id:{
      type:String,
      required:true
    },
    resource_id:{
      type:String,
      required:true
    }
  });

  const InvoiceResources = module.exports = mongoose.model('invoiceresources',invoiceResourceSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(invoiceresource, callback)
  {
    InvoiceResources.create(invoiceresource, callback);
  }

  module.exports.get = function(invoice_id, callback)
  {
    var query = {invoice_id:invoice_id};
    InvoiceResources.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    InvoiceResources.find({}, callback);
  }

  module.exports.update = function(record_id, invoiceresource, callback)
  {
    var query = {_id:record_id};
    InvoiceResources.findOneAndUpdate(query, invoiceresource, {}, callback);
  }

  module.exports.isValid = function(invoiceresource)
  {
    if(isNullOrEmpty(invoiceresource))
      return false;
    //attribute validation
    if(isNullOrEmpty(invoiceresource.resource_id))
      return false;
    if(isNullOrEmpty(invoiceresource.invoice_id))
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
