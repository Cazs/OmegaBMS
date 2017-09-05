var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const invoiceSchema = mongoose.Schema(
{
  invoice_description:{
    type:String,
    required:true
  },
  issuer_org_id:{
      //supplier_id, INTERNAL etc.
      type:String,
      required:true
  },
  receiver_org_id:{
      //supplier_id, INTERNAL etc.
      type:String,
      required:true
  },
  labour:{
    type:Number,
    required:true
  },
  tax:{
    type:Number,
    required:true
  },
  request_date:{
    type: Number,
    required:true
  },
  date_generated:{
    type: Number,
    required:true
  },
  extra:{
    type: String,
    required:false
  }
});

const Invoices = module.exports = mongoose.model('invoices',invoiceSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(invoice, callback)
{
  Invoices.create(invoice, callback);
}

module.exports.get = function(invoice_id, callback)
{
  var query = {_id: invoice_id};
  Invoices.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  Invoices.find({}, callback);
}

module.exports.update = function(record_id, invoice, callback)
{
  var query = {_id:record_id};
  Invoices.findOneAndUpdate(query, invoice, {}, callback);
}

module.exports.isValid = function(invoice)
{
  if(isNullOrEmpty(invoice))
    return false;
  //attribute validation
  if(isNullOrEmpty(invoice.invoice_description))
    return false;
  if(isNullOrEmpty(invoice.issuer_org_id))
    return false;
  if(isNullOrEmpty(invoice.receiver_org_id))
    return false;
  if(isNullOrEmpty(invoice.labour))
    return false;
  if(isNullOrEmpty(invoice.tax))
    return false;
  if(isNullOrEmpty(invoice.date_generated))
    return false;
  if(isNullOrEmpty(invoice.request_date))
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
