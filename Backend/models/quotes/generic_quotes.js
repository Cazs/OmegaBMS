var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const quoteSchema = mongoose.Schema(
{
  client:{
      type:String,
      required:true
  },
  contact_person:{
    type:String,
    required:true
  },
  email:{
    type:String,
    required:true
  },
  tel:{
    type:String,
    required:true
  },
  cell:{
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
    default:'N/A'
  },
  date_generated:{
    type: Number,
    required:true,
    default:Math.floor(new Date().getTime()/1000)//current date in epoch seconds
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
  extra:{
    type: String,
    required:false
  }
});

const Quotes = module.exports = mongoose.model('genericquotes',quoteSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(quote, callback)
{
  console.log('attempting to create a new generic quote.');
  Quotes.create(quote, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new generic quote.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('generic_quotes_timestamp');
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
  var query = {_id:record_id};
  console.log('attempting to update generic quote[%s].', record_id);
  Quotes.find(query, function(err, q)
  {
    if(err)
    {
      callback(err);
      return;
    }
    Quotes.findOneAndUpdate(query, quote, {}, function(error, res_obj)
    {
      if(error)
      {
        console.log(error);
        if(callback)
          callback(error);
        return;
      }
      console.log('successfully updated generic quote.')
      if(callback)
        callback(error, res_obj);
      //update timestamp
      counters.timestamp('generic_quotes_timestamp');
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
  });
}

module.exports.isValid = function(quote)
{
  console.log('validating generic quote:\n%s', JSON.stringify(quote));

  if(isNullOrEmpty(quote))
    return false;
  //attribute validation
  if(isNullOrEmpty(quote.client))
    return false;
  if(isNullOrEmpty(quote.contact_person))
    return false;
  if(isNullOrEmpty(quote.email))
    return false;
  if(isNullOrEmpty(quote.tel))
    return false;
  if(isNullOrEmpty(quote.cell))
    return false;
  if(isNullOrEmpty(quote.sitename))
    return false;
  if(isNullOrEmpty(quote.request))
    return false;
  if(isNullOrEmpty(quote.creator))
    return false;
  if(isNullOrEmpty(quote.status))
    return false;

    console.log('valid generic quote.');
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
