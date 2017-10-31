const mongoose = require('mongoose');

const bonusSchema = mongoose.Schema(
  {
    bonus_name:{
      type:String,
      required:true
    },
    bonus_description:{
      type:String,
      required:true
    },
    bonus_value:{
      type:Number,
      required:true
    },
    date_activated:{
      type:Number,
      required:true
    },
  });
