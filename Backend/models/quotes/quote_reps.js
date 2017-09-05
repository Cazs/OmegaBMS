const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const quoteRepSchema = mongoose.Schema(
  {
    quote_id:{
      type:String,
      required:true
    },
    usr:{
      type:String,
      required:true
    }
  });

  const QuoteReps = module.exports = mongoose.model('quotereps',quoteRepSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(quoterep, callback)
  {
    console.log('attempting to create new quote_rep for quote[%s]', quoterep.quote_id);
    QuoteReps.create(quoterep, callback);
  }

  module.exports.get = function(quote_id, callback)
  {
    var query = {quote_id:quote_id};
    QuoteReps.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    QuoteReps.find({}, callback);
  }

  module.exports.update = function(record_id, quoterep, callback)
  {
    var query = {_id:record_id};
    console.log('attempting to update quote_rep[%s] for quote[%s]', record_id, quoterep.quote_id);
    QuoteReps.findOneAndUpdate(query, quoterep, {}, callback);
  }

  module.exports.isValid = function(quoterep)
  {
    console.log('validating quote_rep object:\n%s', JSON.stringify(quoterep));

    if(isNullOrEmpty(quoterep))
      return false;
    //attribute validation
    if(isNullOrEmpty(quoterep.usr))
      return false;
    if(isNullOrEmpty(quoterep.quote_id))
      return false;

    console.log('valid quote_rep object.');

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
