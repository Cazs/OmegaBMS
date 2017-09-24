var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const invoiceSchema = mongoose.Schema(
{
  creator:{
    type:String,
    required:true
  },
  job_id:{
      //supplier_id, INTERNAL etc.
      type:String,
      required:true
  },
  date_generated:{
    type: Number,
    required:false,
    default: Math.floor(new Date().getTime()/1000)
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
  console.log('attempting to create a new invoice.');
  Invoices.create(invoice, function(err, new_invoice)
  {
    if(err)
    {
      callback(err);
      return;
    }
    //invoice was successfully created
    callback(err, new_invoice);
    //update timestamp
    counters.timestamp('invoices_timestamp');
  });
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
  console.log('attempting to update invoice[%s].\n', job_id);
  var query = {_id:record_id};
  Invoices.findOneAndUpdate(query, invoice, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated invoice.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('invoices_timestamp');
  });
}

module.exports.isValid = function(invoice)
{
  console.log('validating invoice:\n%s', JSON.stringify(invoice));

  if(isNullOrEmpty(invoice))
    return false;
  //attribute validation
  /*if(isNullOrEmpty(invoice.quote_id))
    return false;*/
  if(isNullOrEmpty(invoice.job_id))
    return false;
  if(isNullOrEmpty(invoice.creator))
    return false;

  console.log('valid invoice.');
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
