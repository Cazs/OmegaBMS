const mongoose = require('mongoose');

const occupationSchema = mongoose.Schema(
  {
    occ_title:{
      type:String,
      required:true
    },
    occ_description:{
      type:String,
      required:true
    },
    occ_rank_id:{
      type:String,
      required:true
    },
    occ_salary:{
      type:Number,
      required:true
    },
    occ_salary_freq:{
      type:String,
      required:true
    }
  }
);
