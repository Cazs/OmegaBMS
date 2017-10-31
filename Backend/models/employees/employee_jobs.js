const mongoose = require('mongoose');

const employee_jobSchema = mongoose.Schema(
  {
    employee_id:{
      type:String,
      required:true
    },
    job_id:{
      type:String,
      required:true
    }
  });
