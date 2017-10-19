var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const quoteSchema = mongoose.Schema(
{
  client_id:{
      type:String,
      required:true
  },
  contact_person_id:{
    type:String,
    required:true
  },
  sitename:{
    type:String,
    required:true
  },
  request:{
    type:String,
    required:true,
    default:"n/a"
  },
  date_generated:{
    type: Number,
    required:true
  },
  creator:{
    type:String,
    required:true
  },
  revision:{
    type:Number,
    required:true,
    default:0.0
  },
  status:{
    type:Number,
    required:true,
    default:0
  },
  original_quote:{//for revision control
    type:String,
    required:false
  },
  extra:{
    type: String,
    required:false
  }
});

const Quotes = module.exports = mongoose.model('quotes',quoteSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(quote, callback)
{
  console.log('attempting to create a new quote.');
  Quotes.create(quote, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new quote.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('quotes_timestamp');
  });
}

module.exports.get = function(quote_id, callback)
{
  var query = {_id: quote_id};
  Quotes.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  Quotes.find({}, callback);
}

module.exports.update = function(record_id, quote, callback)
{
  console.log('attempting to update quote[%s].', record_id);
  var query = {_id:record_id};

  Quotes.findOneAndUpdate(query, quote, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated quote.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('quotes_timestamp');
  });
    //backup old quote
    /*var obj = new Object(q);
    obj.quote_id = record_id;
    console.log('obj:\n%s', obj);
    Quotes.create(q, function(err, new_quote)
    {
      if(err)
      {
        callback(err);
        return;
      }
      console.log('new quote:\n%s', new_quote);
      //increment revision counter
      var rev = new Number(quote.revision);
      rev += .1;
      quote.revision=rev;
      Quotes.findOneAndUpdate(query, quote, {}, callback);
    });*/
};

module.exports.isValid = function(quote)
{
  console.log('validating quote:\n%s', JSON.stringify(quote));

  if(isNullOrEmpty(quote))
    return false;
  //attribute validation
  if(isNullOrEmpty(quote.client_id))
    return false;
  if(isNullOrEmpty(quote.contact_person_id))
    return false;
  if(isNullOrEmpty(quote.sitename))
    return false;
  if(isNullOrEmpty(quote.request))
    return false;
  if(isNullOrEmpty(quote.creator))
    return false;
  if(isNullOrEmpty(quote.revision))
    return false;
  if(isNullOrEmpty(quote.status))
    return false;
  if(isNullOrEmpty(quote.date_generated))
    return false;

    console.log('valid quote.');
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
