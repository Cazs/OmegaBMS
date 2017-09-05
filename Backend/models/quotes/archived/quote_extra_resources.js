const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

//These are resources that are not 
const quoteExtraResourceSchema = mongoose.Schema(
  {
    quote_id:{
      type:String,
      required:true
    },
    resource_id:{
      type:String,
      required:true
    }
  });

  const QuoteExtraResources = module.exports = mongoose.model('quoteextraresources',quoteExtraResourceSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(quoteextraresource, callback)
  {
    QuoteExtraResources.create(quoteextraresource, callback);
  }

  module.exports.get = function(quote_id, callback)
  {
    var query = {quote_id:quote_id};
    QuoteExtraResources.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    QuoteExtraResources.find({}, callback);
  }

  module.exports.update = function(record_id, quoteextraresource, callback)
  {
    var query = {_id:record_id};
    QuoteExtraResources.findOneAndUpdate(query, quoteextraresource, {}, callback);
  }

  module.exports.isValid = function(quoteextraresource)
  {
    if(isNullOrEmpty(quoteextraresource))
      return false;
    //attribute validation
    if(isNullOrEmpty(quoteextraresource.resource_id))
      return false;
    if(isNullOrEmpty(quoteextraresource.quote_id))
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
