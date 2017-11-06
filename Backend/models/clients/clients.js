const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const clientSchema = mongoose.Schema(
  {
    client_name:{
      type:String,
      required:true
    },
    physical_address:{
      type:String,
      required:true
    },
    postal_address:{
      type:String,
      required:true
    },
    tel:{
      type:String,
      required:true
    },
    fax:{
      type:String,
      required:false
    },
    active:{
      type:Boolean,
      required:true
    },
    date_partnered:{
      type:Number,
      required:true
    },
    website:{
      type:String,
      required:true
    },
    registration:{
      type:String,
      required:true
    },
    vat:{
      type:String,
      required:true
    },
    other:{
      type:String,
      required:false
    }
  }
);

const Client = module.exports = mongoose.model('clients',clientSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(client, callback)
{
  console.log('attempting to create new client.');
  Client.create(client, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new client.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('clients_timestamp');
  });
}

module.exports.get = function(client_id,callback)
{
  var query = {_id: client_id};
  Client.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  return Client.find({}, callback);
}

module.exports.update = function(client_id, client, callback)
{
  console.log('attempting to update client [%s].', client_id);
  var query = {_id: client_id};
  Client.findOneAndUpdate(query, client, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated client.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('clients_timestamp');
  });
}

module.exports.isValid = function(client)
{
  console.log('validating client object:\n%s', JSON.stringify(client));

  if(isNullOrEmpty(client))
    return false;
  //attribute validation
  if(isNullOrEmpty(client.client_name))
    return false;
  if(isNullOrEmpty(client.physical_address))
    return false;
  if(isNullOrEmpty(client.postal_address))
    return false;
  if(isNullOrEmpty(client.tel))
    return false;
  if(isNullOrEmpty(client.active))
    return false;
  if(isNullOrEmpty(client.date_partnered))
    return false;
  if(isNullOrEmpty(client.website))
    return false;
  if(isNullOrEmpty(client.registration))
    return false;
  if(isNullOrEmpty(client.vat))
    return false;
  /*if(isNullOrEmpty(client.other))
    return false;*/
    console.log('valid client object.');

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
