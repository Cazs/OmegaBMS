const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const quoteResourceSchema = mongoose.Schema(
  {
    quote_id:{
      type:String,
      required:true
    },
    item_number:{
      type:Number,
      required:true
    },
    equipment_name:{
      type:String,
      required:true
    },
    equipment_description:{
      type:String,
      required:true
    },
    unit:{
      type:String,
      required:true
    },
    quantity:{
      type:Number,
      required:true
    },
    value:{
      type:Number,
      required:true
    },
    rate:{
      type:Number,
      required:true
    },
    labour:{
      type:Number,
      required:true
    },
    markup:{
      type:Number,
      required:true
    },
    extra:{
      type:String,
      required:false
    }
  });

  const QuoteResources = module.exports = mongoose.model('genericquoteresources',quoteResourceSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(quoteresource, callback)
  {
    console.log('attempting to create a new generic_quote_resource for genric quote [%s].', quoteresource.quote_id);
    QuoteResources.create(quoteresource, callback);
  }

  module.exports.get = function(quote_id, callback)
  {
    var query = {quote_id:quote_id};
    QuoteResources.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    QuoteResources.find({}, callback);
  }

  module.exports.update = function(record_id, quoteresource, callback)
  {
    var query = {_id:record_id};
    console.log('attempting to update generic_quote_resource[%s].', record_id);
    QuoteResources.findOneAndUpdate(query, quoteresource, {}, callback);
  }

  module.exports.isValid = function(quoteresource)
  {
    console.log('validating generic_quote_resources object:\n%s', JSON.stringify(quoteresource));

    if(isNullOrEmpty(quoteresource))
      return false;

    //attribute validation
    if(isNullOrEmpty(quoteresource.item_number))
      return false;
    if(isNullOrEmpty(quoteresource.quote_id))
      return false;
    if(isNullOrEmpty(quoteresource.equipment_name))
      return false;
    if(isNullOrEmpty(quoteresource.equipment_description))
      return false;
    if(isNullOrEmpty(quoteresource.unit))
      return false;
    if(isNullOrEmpty(quoteresource.quantity))
      return false;
    if(isNullOrEmpty(quoteresource.value))
      return false;
    if(isNullOrEmpty(quoteresource.rate))
      return false;
    if(isNullOrEmpty(quoteresource.labour))
      return false;
    if(isNullOrEmpty(quoteresource.markup))
      return false;

    console.log('valid generic_quote_resources object.');

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
