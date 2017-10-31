const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

const quoteClientRepSchema = mongoose.Schema(
  {
    quote_id:{
      type:String,
      required:true
    },
    employee_id:{
      type:String,
      required:true
    }
  });

  const QuoteClientReps = module.exports = mongoose.model('quoteclientreps',quoteClientRepSchema);

  module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

  module.exports.add = function(quoteclientrep, callback)
  {
    QuoteClientReps.create(quoteclientrep, callback);
  }

  module.exports.get = function(quote_id, callback)
  {
    var query = {quote_id:quote_id};
    QuoteClientReps.find(query, callback);
  }

  module.exports.getAll = function(callback)
  {
    QuoteClientReps.find({}, callback);
  }

  module.exports.update = function(record_id, quoteclientrep, callback)
  {
    var query = {_id:record_id};
    QuoteClientReps.findOneAndUpdate(query, quoteclientrep, {}, callback);
  }

  module.exports.isValid = function(quoteclientrep)
  {
    if(isNullOrEmpty(quoteclientrep))
      return false;
    //attribute validation
    if(isNullOrEmpty(quoteclientrep.employee_id))
      return false;
    if(isNullOrEmpty(quoteclientrep.quote_id))
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
