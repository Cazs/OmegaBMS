const mongoose = require('mongoose');

const rankSchema = mongoose.Schema(
  {
    rank_title:{
      type:String,
      required:true
    },
    rank_salary_modifier:{
      type:Number,
      required:true
    }
  });
