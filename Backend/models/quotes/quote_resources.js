const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const quoteResourceSchema = mongoose.Schema(
  {
    quote_id:{
      type:String,
      required:true
    },
    resource_id:{
      type:String,
      required:true
    },
    item_number:{
      type:Number,
      required:true
    },
    markup:{
      type:Number,
      required:true
    },
    labour:{
      type:Number,
      required:true
    },
    quantity:{
      type:Number,
      required:true
    },
    additional_costs:{
      type:String,
      required:false
    },
    extra:{
      type:String,
      required:false
    }
  });

  const QuoteResources = module.exports = mongoose.model('quoteresources',quoteResourceSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(quoteresource, callback)
  {
    console.log('attempting to create new quote_resource for quote [%s].', quoteresource.quote_id);
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
    console.log('attempting to update quote_resource[%s].', record_id);
    QuoteResources.findOneAndUpdate(query, quoteresource, {}, callback);
  }

  module.exports.isValid = function(quoteresource)
  {
    console.log('validating quote_resource object:\n%s', JSON.stringify(quoteresource));

    if(isNullOrEmpty(quoteresource))
      return false;

    //attribute validation
    if(isNullOrEmpty(quoteresource.resource_id))
      return false;
    if(isNullOrEmpty(quoteresource.item_number))
      return false;
    if(isNullOrEmpty(quoteresource.quote_id))
      return false;
    if(isNullOrEmpty(quoteresource.markup))
      return false;
    if(isNullOrEmpty(quoteresource.labour))
      return false;
    if(isNullOrEmpty(quoteresource.quantity))
      return false;

    console.log('valid quote_resource object.');

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
