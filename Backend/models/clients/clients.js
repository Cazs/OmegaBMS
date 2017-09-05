const mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');

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
    other:{
      type:String,
      required:false
    }
  }
);

const Client = module.exports = mongoose.model('clients',clientSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(client,callback)
{
  Client.create(client,callback);
}

module.exports.get = function(client_id,callback)
{
  var query = {_id:client_id};
  return Client.findOne(query,callback);
}

module.exports.getAll = function(callback)
{
  return Client.find({},callback);
}

module.exports.update = function(client_id,client,callback)
{
  var query = {_id:client_id};
  return Client.findOneAndUpdate(query,client,{},callback);
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
