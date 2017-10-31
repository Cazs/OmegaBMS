var mongoose = require('mongoose');
var access_levels = require('../system/access_levels.js');
var counters = require('../system/counters.js');

const expensesSchema = mongoose.Schema(
{
  expense_title:{
      type:String,
      required:true
  },
  expense_description:{
    type:String,
    required:true
  },
  expense_value:{
    type:Number,
    required:true
  },
  supplier:{
    type:String,
    required:true
  },
  date_logged:{
    type: Number,
    required:false,
    default:Math.floor(new Date().getTime()/1000)//current date in epoch seconds
  },
  creator:{
    type:String,
    required:true
  },
  account:{
    type:String,
    required:true
  },
  revision:{
    type:Number,
    required:false,
    default:0.0
  },
  other:{
    type: String,
    required:false
  }
});

const Expenses = module.exports = mongoose.model('expenses',expensesSchema);

module.exports.ACCESS_MODE = access_levels.NORMAL;//Required access level to execute these methods

module.exports.add = function(expense, callback)
{
  console.log('attempting to create a new expense.');
  Expenses.create(expense, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully created new expense.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('expenses_timestamp');
  });
}

module.exports.get = function(expense_id, callback)
{
  var query = {_id: expense_id};
  Expenses.findOne(query, callback);
}

module.exports.getAll = function(callback)
{
  Expenses.find({}, callback);
}

module.exports.update = function(record_id, expense, callback)
{
  var query = {_id: record_id};
  console.log('attempting to update expense[%s].', record_id);
  Expenses.findOneAndUpdate(query, expense, {}, function(error, res_obj)
  {
    if(error)
    {
      console.log(error);
      if(callback)
        callback(error);
      return;
    }
    console.log('successfully updated expense.')
    if(callback)
      callback(error, res_obj);
    //update timestamp
    counters.timestamp('expenses_timestamp');
  });
}

module.exports.isValid = function(expense)
{
  console.log('validating expense:\n%s', JSON.stringify(expense));

  if(isNullOrEmpty(expense))
    return false;
  //attribute validation
  if(isNullOrEmpty(expense.expense_title))
    return false;
  if(isNullOrEmpty(expense.expense_description))
    return false;
  if(isNullOrEmpty(expense.expense_value))
    return false;
  if(isNullOrEmpty(expense.supplier))
    return false;
  if(isNullOrEmpty(expense.date_logged))
    return false;
  if(isNullOrEmpty(expense.creator))
    return false;
  if(isNullOrEmpty(expense.account))
    return false;

    console.log('valid expense.');
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
