
//System imports
const express = require('express');
const mongoose = require('mongoose');
const body_parser = require('body-parser');
const path = require('path');
const fs = require('fs');
const hummus = require('hummus');
var multer = require('multer');
//const upload = multer({dest : __dirname + '/public/uploads/'});
//154.0.175.175
//Custom imports
const employees = require('./models/employees/employees.js');
const sessions = require('./models/system/sessions.js');
const errors = require('./models/system/error_msgs.js');
const access_levels = require('./models/system/access_levels.js');
const jobs = require('./models/jobs/jobs.js');
const sales = require('./models/sales/sales.js');
const sale_resources = require('./models/sales/sale_resources.js');
const sale_reps = require('./models/sales/sale_reps.js');
const sale_client_reps = require('./models/sales/sale_client_reps.js');
const job_resources = require('./models/jobs/job_resources.js');
const job_employees = require('./models/jobs/job_employees.js');
const job_safetydocs = require('./models/jobs/job_safetydocs.js');
const clients = require('./models/clients/clients.js');
const client_employees = require('./models/clients/client_employees.js');
const client_jobs = require('./models/clients/client_jobs.js');
const suppliers = require('./models/suppliers/suppliers.js');
const resources = require('./models/resources/resources.js');
const resource_types = require('./models/resources/resource_types.js');
const assets = require('./models/assets/assets.js');
const asset_types = require('./models/assets/asset_types.js');
const quotes = require('./models/quotes/quotes.js');
const quote_resources = require('./models/quotes/quote_resources.js');
const quote_reps = require('./models/quotes/quote_reps.js');
const generic_quotes = require('./models/quotes/generic_quotes.js');
const generic_quote_resources = require('./models/quotes/generic_quote_resources.js');
const invoices = require('./models/invoices/invoices.js');
const invoice_resources = require('./models/invoices/invoice_resources.js');
const invoice_reps = require('./models/invoices/invoice_reps.js');
const safety_index = require('./models/safety/index.js');
const risk_index = require('./models/risk/index.js');
const inspection_index = require('./models/inspection/index.js');
const ohs_index = require('./models/ohs/index.js');
const appointment_index = require('./models/appointment/index.js');
const vericodes = require('./models/system/vericodes.js');
const counters = require('./models/system/counters.js');
const expenses = require('./models/expenses/expenses.js');
const revenues = require('./models/revenue/revenue.js');
const purchase_orders = require('./models/purchase_orders/purchase_orders.js');
const purchase_order_items = require('./models/purchase_orders/purchase_order_items.js');
const purchase_order_assets = require('./models/purchase_orders/purchase_order_assets.js');

mongoose.connect('mongodb://localhost/fadulousbms');

//globals
const db = mongoose.connection;
const app = express();
const SESSION_TTL = 60 * 240;//4 hours
const PORT = 9000;
const APP_NAME = "BMS Engine v1.0";

//init middle-ware
app.use(body_parser.urlencoded({extended:true}));

//route handlers
app.get('/',function(req, res)
{
  res.setHeader("Content-Type","text/plain");
  res.end("Invalid request, please use /api/*.");
});

app.get('/api/timestamp/:object_id', function(req, res)
{
  get(req, res, counters, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested timestamp [%s].', req.headers.cookie, req.params.object_id);
    res.json(obj);
  });
});

/**** Purchase orders route handlers ****/
app.get('/api/purchaseorder/:object_id',function(req, res)
{
  get(req, res, purchase_orders, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested purchase order [%s].', req.headers.cookie, obj._id);
    res.json(obj);
  });
});

app.get('/api/purchaseorders', function(req, res)
{
  getAll(req, res, purchase_orders, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all purchase orders in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/purchaseorder/add',function(req, res)
{
  add(req, res, purchase_orders, function(err, purchaseorder)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new purchase order:\n %s',JSON.stringify(purchaseorder));
    var id =purchaseorder._id;
    res.json({"message":id.toString()});
  });
});

app.post('/api/purchaseorder/update/:object_id',function(req, res)
{
  update(req, res, purchase_orders, function(err, purchaseorder)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated purchase order[%s].\n', purchaseorder._id.toString());
    res.json(purchaseorder);
  });
});

/**** Purchase orders items route handlers ****/
app.get('/api/purchaseorder/item/:object_id', function(req, res)
{
  get(req, res, purchase_order_items, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested Purchase Order Item [%s].', req.headers.cookie, obj._id);
    res.json(obj);
  });
});

app.get('/api/purchaseorder/items/:object_id', function(req, res)
{
  get(req, res, purchase_order_items, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all Resources for Purchase Order [%s]', req.headers.cookie, req.params.object_id);
    res.json(objs);
  });
});

app.post('/api/purchaseorder/item/add',function(req, res)
{
  add(req, res, purchase_order_items, function(err, purchase_order_item)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new purchase_order_resource.');
    var id =purchase_order_item._id;
    res.json({"message":id.toString()});
  });
});

app.post('/api/purchaseorder/item/update/:object_id',function(req, res)
{
  update(req, res, purchase_order_items, function(err, purchase_order_item)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated purchase_order_resource [%s].\n', purchase_order_item._id.toString());
    res.json(purchase_order_item);
  });
});

/**** Purchase order assets route handlers ****/
app.get('/api/purchaseorder/asset/:object_id', function(req, res)
{
  get(req, res, purchase_order_assets, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested Purchase Order Asset [%s].', req.headers.cookie, obj._id);
    res.json(obj);
  });
});

app.get('/api/purchaseorder/assets/:object_id', function(req, res)
{
  get(req, res, purchase_order_assets, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all Assets for Purchase Order [%s]', req.headers.cookie, req.params.object_id);
    res.json(objs);
  });
});

app.post('/api/purchaseorder/asset/add',function(req, res)
{
  add(req, res, purchase_order_assets, function(err, purchase_order_item)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new purchase_order_asset.');
    var id =purchase_order_item._id;
    res.json({"message":id.toString()});
  });
});

app.post('/api/purchaseorder/asset/update/:object_id',function(req, res)
{
  update(req, res, purchase_order_assets, function(err, purchase_order_item)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated purchase_order_asset [%s].\n', purchase_order_item._id.toString());
    res.json(purchase_order_item);
  });
});

/**** Quotes route handlers ****/
app.get('/api/quote/:object_id',function(req, res)
{
  get(req, res, quotes, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    if(obj)
    {
      console.log('user with session_id [%s] requested quote [%s].', req.headers.cookie, obj._id);
      res.json(obj);
    }else{
      console.log('database returned a null object for the request of %s', obj._id);
      res.end();
    }
  });
});

app.get('/api/quotes', function(req, res)
{
  getAll(req, res, quotes, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all quotes in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/quote/add',function(req, res)
{
  add(req, res, quotes, function(err, quote)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new quote:\n %s',JSON.stringify(quote));
    var id =quote._id;
    res.json({"message":id.toString()});
  });
});

app.post('/api/quote/update/:object_id',function(req, res)
{
  update(req, res, quotes, function(err, quote)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated quote[%s].\n', quote._id.toString());
    res.json(quote);
  });
});

//Quote resources
app.get('/api/quote/resources/:object_id',function(req, res)
{
  getObject(req, res, quote_resources);
});

app.post('/api/quote/resource/add',function(req, res)
{
  add(req, res, quote_resources, function(err, quote_resource)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new quote_resource:\n%s\n', JSON.stringify(quote_resource));
    res.json({'message':'successfully created new quote resource.'});
  });
});

app.post('/api/quote/resource/update/:object_id',function(req, res)
{
  update(req, res, quote_resources, function(err, quote_resource)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated quote_resource for quote[%s].\n', quote_resource.quote_id);
    res.json(quote_resource);
  });
});
//Quote reps
app.get('/api/quote/reps/:object_id',function(req, res)
{
  getObject(req, res, quote_reps);
});

app.post('/api/quote/rep/add/:object_id',function(req, res)
{
  add(req, res, quote_reps, function(err, quote_rep)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new quote representative:\n%s\n', JSON.stringify(quote_rep));
    var id =quote_rep._id;
    res.json({"message":id});
  });
});

app.post('/api/quote/rep/update/:object_id',function(req, res)
{
  updateObject(req, res, quote_reps);
});

/**** Generic Quote route handlers ****/
app.get('/api/quotes/generic', function(req, res)
{
  getAll(req, res, generic_quotes, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all generic quotes in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/quote/generic/add',function(req, res)
{
  add(req, res, generic_quotes, function(err, quote)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new generic quote:\n %s',JSON.stringify(quote));
    var id =quote._id;
    res.json({"message":id.toString()});
  });
});

app.post('/api/quote/generic/update/:object_id',function(req, res)
{
  update(req, res, generic_quotes, function(err, quote)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated generic quote[%s].\n', quote._id.toString());
    res.json(quote);
  });
});

//Generic Quote Resources
app.get('/api/quote/generic/resources/:object_id',function(req, res)
{
  //getObject(req, res, generic_quote_resources);
  get(req, res, generic_quote_resources, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested resources for generic quote [%s] in the database.', req.headers.cookie, req.params.object_id);
    res.json(objs);
  });
});

app.post('/api/quote/generic/resource/add',function(req, res)
{
  add(req, res, generic_quote_resources, function(err, quote_resource)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new generic_quote_resource:\n%s\n', JSON.stringify(quote_resource));
    res.json({'message':'successfully created new generic quote resource.'});
  });
});

app.post('/api/quote/generic/resource/update/:object_id',function(req, res)
{
  update(req, res, generic_quote_resources, function(err, quote_resource)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated generic_quote_resources for generic quote[%s].\n', quote_resource.quote_id);
    res.json(quote_resource);
  });
});

//Expenses handlers
app.get('/api/expense/:object_id',function(req, res)
{
  get(req, res, expenses, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested expense [%s].', req.headers.cookie, req.params.object_id);
    res.json(obj);
  });
});

app.get('/api/expenses',function(req, res)
{
  getAll(req, res, expenses, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all expenses in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/expense/add',function(req, res)
{
  add(req, res, expenses, function(err, expense)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new expense:\n%s\n', JSON.stringify(expense));
    res.json({'message':'successfully created new expense.'});
  });
});

app.post('/api/expense/update/:object_id',function(req, res)
{
  update(req, res, expenses, function(err, expense)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated expense[%s].\n', expense._id);
    res.json(expense);
  });
});

//Additional Revenue/Income handlers
app.get('/api/revenue/:object_id',function(req, res)
{
  get(req, res, revenues, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested additional revenue [%s].', req.headers.cookie, req.params.object_id);
    res.json(obj);
  });
});

app.get('/api/revenues',function(req, res)
{
  getAll(req, res, revenues, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all additional revenue in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/revenue/add',function(req, res)
{
  add(req, res, revenues, function(err, revenue)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created additional revenue:\n%s\n', JSON.stringify(revenue));
    res.json({'message':'successfully created new additional revenue.'});
  });
});

app.post('/api/revenue/update/:object_id',function(req, res)
{
  update(req, res, revenues, function(err, revenue)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated revenue[%s].\n', revenue._id);
    res.json(revenue);
  });
});

/**** Invoices route handlers ****/
app.get('/api/invoice/:object_id',function(req, res)
{
  get(req, res, invoices, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested invoice [%s].', req.headers.cookie, req.params.object_id);
    res.json(obj);
  });
});

app.get('/api/invoices',function(req, res)
{
  getAll(req, res, invoices, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all invoices in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/invoice/add',function(req, res)
{
  add(req, res, invoices, function(err, invoice)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new invoice:\n%s\n', JSON.stringify(invoice));
    res.json({'message':'successfully created new invoice.'});
  });
});

app.post('/api/invoice/update/:object_id',function(req, res)
{
  update(req, res, invoices, function(err, invoice)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated invoice [%s].\n', invoice._id);
    res.json(invoice);
  });
});

//Invoice resources
/*app.get('/api/invoice/resources/:object_id',function(req, res)
{
  getObject(req, res, invoice_resources);
});

app.post('/api/invoice/resource/add',function(req, res)
{
  addObject(req, res, invoice_resources);
});

app.post('/api/invoice/resource/update/:object_id',function(req, res)
{
  updateObject(req, res, invoice_resources);
});

//Invoice reps
app.get('/api/invoice/reps/:object_id',function(req, res)
{
  getObject(req, res, invoice_reps);
});

app.post('/api/invoice/rep/add',function(req, res)
{
  addObject(req, res, invoice_reps);
});

app.post('/api/invoice/rep/update/:object_id',function(req, res)
{
  updateObject(req, res, invoice_reps);
});*/

/**** Resources route handlers ****/
//Resource types handlers
app.get('/api/resource/types',function(req, res)
{
  getAll(req, res, resource_types, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all resource_types in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/resource/type/add',function(req, res)
{
  add(req, res, resource_types, function(err, resource_type)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new resource_type:\n%s\n', JSON.stringify(resource_type));
    res.json({'message':'successfully created new resource_type.'});
  });
});

app.post('/api/resource/type/update/:object_id',function(req, res)
{
  update(req, res, resource_types, function(err, resource_type)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated resource_type [%s].\n', resource_type._id);
    res.json(resource_type);
  });
});

//Actual Resource handlers
app.get('/api/resource/:object_id',function(req, res)
{
  get(req, res, resources, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested resource [%s].', req.headers.cookie, req.params.object_id);
    res.json(obj);
  });
});

app.get('/api/resources',function(req, res)
{
  getAll(req, res, resources, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all resources in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/resource/add',function(req, res)
{
  add(req, res, resources, function(err, resource)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new resource:\n%s\n', JSON.stringify(resource));
    res.json({'message':'successfully created new resource.'});
  });
});

app.post('/api/resource/update/:object_id',function(req, res)
{
  update(req, res, resources, function(err, resource)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated resource [%s].\n', resource._id);
    res.json(resource);
  });
});

app.post('/api/resource/increment_quantity/:object_id',function(req, res)
{
  var obj_id = req.params.object_id;
  var resource = req.body;
  var quantity = resource.quantity;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  if(isNullOrEmpty(obj_id))
  {
    console.log('invalid object id: "%s".', JSON.stringify(obj_id));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  //validate resource
  if(!resources.isValid(resource))
  {
    console.log('invalid resource: %s.', resource)
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(isNullOrEmpty(quantity))
  {
    console.log('invalid quantity: "%s".', JSON.stringify(quantity));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=resources.ACCESS_MODE)
      {
        resources.incrementQuantity(obj_id, resource, function(error, updated_resource)
        {
          if(error)
          {
            logServerError(err);
            errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
            return;
          }
          res.json(updated_resource);
        });
      }else {
        errorAndCloseConnection(res, 502, errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
    }
  }else{
    errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
  }
});

/**** Assets route handlers ****/
//Asset types handlers
app.get('/api/asset/types',function(req, res)
{
  getAll(req, res, asset_types, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all asset_types in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/asset/type/add',function(req, res)
{
  add(req, res, asset_types, function(err, asset_type)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new asset_type:\n%s\n', JSON.stringify(asset_type));
    res.json({'message':'successfully created new asset type.'});
  });
});

app.post('/api/asset/type/update/:object_id',function(req, res)
{
  update(req, res, asset_types, function(err, asset_type)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated asset_type [%s].\n', asset_type._id);
    res.json(asset_type);
  });
});

//Actual Asset handlers
app.get('/api/asset/:object_id',function(req, res)
{
  get(req, res, assets, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested asset [%s].', req.headers.cookie, req.params.object_id);
    res.json(obj);
  });
});

app.get('/api/assets',function(req, res)
{
  getAll(req, res, assets, function(err, objs)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('user with session_id [%s] requested all assets in the database.', req.headers.cookie);
    res.json(objs);
  });
});

app.post('/api/asset/add',function(req, res)
{
  add(req, res, assets, function(err, asset)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new asset:\n%s\n', JSON.stringify(asset));
    res.json({'message':'successfully created new asset.'});
  });
});

app.post('/api/asset/update/:object_id',function(req, res)
{
  update(req, res, assets, function(err, asset)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated asset [%s].\n', asset._id);
    res.json(asset);
  });
});

app.post('/api/asset/increment_quantity/:object_id',function(req, res)
{
  var obj_id = req.params.object_id;
  var asset = req.body;
  var quantity = asset.quantity;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  if(isNullOrEmpty(obj_id))
  {
    console.log('invalid object id: "%s".', JSON.stringify(obj_id));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  //validate asset
  if(!assets.isValid(asset))
  {
    console.log('invalid asset: %s.', asset)
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(isNullOrEmpty(quantity))
  {
    console.log('invalid quantity: "%s".', JSON.stringify(quantity));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=assets.ACCESS_MODE)
      {
        assets.incrementQuantity(obj_id, asset, function(error, updated_asset)
        {
          if(error)
          {
            logServerError(err);
            errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
            return;
          }
          res.json(updated_asset);
        });
      }else {
        errorAndCloseConnection(res, 502, errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
    }
  }else{
    errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
  }
});

/**** Suppliers route handlers ****/
app.get('/api/supplier/:object_id',function(req, res)
{
  getObject(req, res, suppliers);
});

app.get('/api/suppliers',function(req, res)
{
  getAllObjects(req, res, suppliers);
});

app.post('/api/supplier/add',function(req, res)
{
  addObject(req, res, suppliers);
});

app.post('/api/supplier/update/:object_id',function(req, res)
{
  updateObject(req, res, suppliers);
});

/**** Client route handlers ****/
app.get('/api/client/:object_id',function(req,res)
{
  getObject(req, res, clients);
});

app.get('/api/clients',function(req,res)
{
  getAllObjects(req, res, clients);
});

app.post('/api/client/add',function(req,res)
{
  addObject(req, res, clients);
});

app.post('/api/client/update/:object_id',function(req,res)
{
  updateObject(req, res, clients);
});

/**** ClientJobs route handlers ****/
app.get('/api/client/jobs/:object_id', function(req, res)
{
  getObject(req, res, client_jobs);
});

app.post('/api/client/job/add',function(req, res)
{
  addObject(req, res, client_jobs);
});

app.post('/api/client/job/update/:object_id', function(req, res)
{
  updateObject(req, res, client_jobs);
});

/**** ClientEmployee route handlers ****/
//Where: @object_id = client_id
app.get('/api/client/employees/:object_id', function(req, res)
{
  getObject(req, res, client_employees);
});

app.post('/api/client/employee/add',function(req, res)
{
  addObject(req, res, client_employees);
});

//Where: @object_id = client_id
app.post('/api/client/employee/update/:object_id', function(req, res)
{
  updateObject(req, res, client_employees);
});

/**** Sale route handlers ****/
//Where: @object_id = sale_id
app.get('/api/sale/:object_id',function(req,res)
{
  getObject(req, res, sales);
});

app.get('/api/sales',function(req,res)
{
  getAllObjects(req, res, sales);
});

app.post('/api/sale/add',function(req,res)
{
  addObject(req, res, sales);
});

app.post('/api/sale/update/:object_id',function(req,res)
{
  updateObject(req, res, sales);
});

/**** Sale client reps route handlers ****/
//Where: @object_id = sale_id
app.get('/api/sale/client/reps/:object_id',function(req,res)
{
  getObject(req, res, sale_client_reps);
});

app.get('/api/sale/client/reps',function(req,res)
{
  getAllObjects(req, res, sale_client_reps);
});

app.post('/api/sale/client/rep/add',function(req,res)
{
  addObject(req, res, sale_client_reps);
});

//Where: @object_id = sale_id
app.post('/api/sale/client/rep/update/:object_id',function(req,res)
{
  updateObject(req, res, sale_client_reps);
});

/**** Sale internal reps route handlers ****/
app.get('/api/sale/reps/:object_id',function(req,res)
{
  getObject(req, res, sale_reps);
});

app.get('/api/sale/reps',function(req,res)
{
  getAllObjects(req, res, sale_reps);
});

app.post('/api/sale/rep/add',function(req,res)
{
  addObject(req, res, sale_reps);
});

//Where: @object_id = sale_id
app.post('/api/sale/rep/update/:object_id',function(req,res)
{
  updateObject(req, res, sale_reps);
});

/**** Sale resources route handlers ****/
//Where: @object_id = sale_id
app.get('/api/sale/resources/:object_id',function(req, res)
{
  getObject(req, res, sale_resources);
});

app.post('/api/sale/resource/add',function(req, res)
{
  addObject(req, res, sale_resources);
});

//Where: @object_id = sale_id
app.post('/api/sale/resource/update/:object_id',function(req, res)
{
  updateObject(req, res, sale_resources);
});

/**** Job route handlers ****/
//Where: @object_id = job_id
app.get('/api/job/:object_id',function(req,res)
{
  getObject(req, res, jobs);
});

app.get('/api/jobs',function(req,res)
{
  getAllObjects(req, res, jobs);
});

app.post('/api/job/add',function(req,res)
{
  add(req, res, jobs, function(err, job)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new job:\n %s',JSON.stringify(job));
    var id =job._id;
    res.json({"message":id.toString()});
  });
});

app.post('/api/job/update/:object_id',function(req,res)
{
  updateObject(req, res, jobs, function(err, job)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('successfully updated job[%s].\n', job._id.toString());
    res.json(job);
  });
});

//Job Safety Catalogue
app.get('/api/job/safetycatalogue/:object_id',function(req,res)
{
  getObject(req, res, job_safetydocs);
});

app.post('/api/job/safetycatalogue/add',function(req,res)
{
  addObject(req, res, job_safetydocs);
});

app.post('/api/job/safetycatalogue/update/:object_id',function(req,res)
{
  updateObject(req, res, job_safetydocs);
});

/**** Job resources route handlers ****/
//Where: @object_id = job_id
app.get('/api/job/resources/:object_id',function(req, res)
{
  getObject(req, res, job_resources);
});

app.post('/api/job/resource/add',function(req, res)
{
  addObject(req, res, job_resources);
});

//Where: @object_id = job_id
app.post('/api/job/resource/update/:object_id',function(req, res)
{
  updateObject(req, res, job_resources);
});

/**** Job employee route handlers ****/
//Where: @object_id = job_id
app.get('/api/job/employees/:object_id',function(req, res)
{
  getObject(req, res, job_employees);
});

app.post('/api/job/employee/add',function(req, res)
{
  add(req, res, job_employees, function(err, job_employee)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new job_employee:\n %s', JSON.stringify(job_employee));
    res.json({"message":job_employee});
  });
});

//Where: @object_id = job_id
app.post('/api/job/employees/update/:object_id',function(req, res)
{
  updateObject(req, res, job_employees);
});

/**** Employee route handlers ****/
app.get('/api/employee/:object_id',function(req,res)
{
  get(req, res, employees, function(err, obj)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    if(obj)
    {
      console.log('user with session_id [%s] requested employee object [%s].', req.headers.cookie, obj._id);
      res.json(obj);
    }else{
      console.log('database returned a null object for the request of %s', obj_id);
      res.end();
    }
  });
});

app.get('/api/employees',function(req,res)
{
  getAllObjects(req, res, employees);
});

app.post('/api/employee/add',function(req,res)
{
  /*add(req, res, employees, function(err, employee)
  {
    if(err)
    {
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    console.log('created new employee:\n %s', JSON.stringify(employee));
    res.json({"message":employee});
  });*/

  /*var employee_obj = req.body;

  res.setHeader('Content-Type','application/json');
  //validate obj
  if(!employees.isValid(employee_obj))
  {
    console.log('invalid employee object [%s].', JSON.stringify(employee_obj))
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(employee_obj.access_level)
  {
    if(employee_obj.access_level<access_levels.SUPER)
    {
      addObject(req, res, employees);
    }else
    {
      //create super account
      employees.add(employee_obj, function(err)
      {
        if(err)
        {
          errorAndCloseConnection(res, 500, err.message);
          logServerError(err);
          return;
        }
        res.json({'message':'successfully created new user.'});
      });
    }
  }*/
  var obj = req.body;
  //res.setHeader('Content-Type','application/json');

  //validate employee obj
  if(!employees.isValid(obj))
  {
    console.log('invalid employees.')
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }
  employees.add(obj, function(err, employee)
  {
    if(err)
    {
      logServerError(err);
      errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
      return;
    }
    console.log("successfully created new employee.");
    res.json(employee);
  });
});

app.post('/api/employee/update/:object_id',function(req,res)
{
  //TODO: only employee and authorized users can update
  updateObject(req, res, employees);
});

app.post('/api/employee/pwdreset',function(req, res)
{
  var vericode = req.body;
  if(vericode)
  {
    if(vericodes.isValid(vericode))
    {
      var pwd = vericode.pwd;
      if(pwd)
      {
        vericodes.validate(vericode, pwd, function(err)
        {
          if(err)
          {
            errorAndCloseConnection(res, 409, err.message);
            logServerError(err);
            return;
          }
          //successfully reset
          res.json({'message':'password has been successfully reset.'});
        });
      }else{
          errorAndCloseConnection(res, 409, 'invalid password.');
          logServerError(new Error('invalid password.'));
          return;
      }
    }else{
        errorAndCloseConnection(res, 409, 'invalid input data.');
        logServerError(new Error('invalid input data.'));
        return;
    }
  }else{
      errorAndCloseConnection(res, 409, 'invalid input data.');
      logServerError(new Error('invalid input data.'));
      return;
  }
});

app.post('/api/vericode/add', function(req, res)
{
  console.log('received vericode reset request.');
  res.setHeader('Content-Type','application/json');

  var vcode = req.body;

  if(vcode)
  {
    vcode.code="";
    vcode.date_issued=0;

    vericodes.add(vcode, function(err)
    {
      if(err)
      {
        errorAndCloseConnection(res, 500, err.message);
        logServerError(err);
        return;
      }
      res.json({'message':'successfully created vericode.'});
    });
  }else
  {
    errorAndCloseConnection(res, 500, 'invalid vericode object.');
    logServerError('invalid vericode object.');
  }
});

//##################Begin Safety
app.get('/api/safety/index/:object_id',function(req,res)
{
  getObject(req, res, safety_index);
});

app.get('/api/safety/indices',function(req,res)
{
  getAllObjects(req, res, safety_index);
});

app.post('/api/safety/index/add',function(req,res)
{
  addObject(req, res, safety_index);
});

app.post('/api/safety/index/update/:object_id',function(req,res)
{
  updateObject(req, res, safety_index);
});

//##################Begin Risk
app.get('/api/risk/index/:object_id',function(req,res)
{
  getObject(req, res, risk_index);
});

app.get('/api/risk/indices',function(req,res)
{
  getAllObjects(req, res, risk_index);
});

app.post('/api/risk/index/add',function(req,res)
{
  addObject(req, res, risk_index);
});

app.post('/api/risk/index/update/:object_id',function(req,res)
{
  updateObject(req, res, risk_index);
});

//##################Begin Inspection
app.get('/api/inspection/index/:object_id',function(req,res)
{
  getObject(req, res, inspection_index);
});

app.get('/api/inspection/indices',function(req,res)
{
  getAllObjects(req, res, inspection_index);
});

app.post('/api/inspection/index/add',function(req,res)
{
  addObject(req, res, inspection_index);
});

app.post('/api/inspection/index/update/:object_id',function(req,res)
{
  updateObject(req, res, inspection_index);
});

//##################Begin OHS
app.get('/api/ohs/index/:object_id',function(req,res)
{
  getObject(req, res, ohs_index);
});

app.get('/api/ohs/indices',function(req,res)
{
  getAllObjects(req, res, ohs_index);
});

app.post('/api/ohs/index/add',function(req,res)
{
  addObject(req, res, ohs_index);
});

app.post('/api/ohs/index/update/:object_id',function(req,res)
{
  updateObject(req, res, ohs_index);
});

//##################Begin Appointment
app.get('/api/appointment/index/:object_id',function(req,res)
{
  getObject(req, res, appointment_index);
});

app.get('/api/appointment/indices',function(req,res)
{
  getAllObjects(req, res, appointment_index);
});

app.post('/api/appointment/index/add',function(req,res)
{
  addObject(req, res, appointment_index);
});

app.post('/api/appointment/index/update/:object_id',function(req,res)
{
  updateObject(req, res, appointment_index);
});

//Begin file upload
app.post('/api/upload', function(req, res, next)
{
  //TODO: check if file exists - and manage revisioning if it does.
  console.log('received upload request for "%s"', req.headers.filename);
  var write_stream = fs.createWriteStream( __dirname + '/uploads/' + req.headers.filename);
  req.pipe(write_stream);

  //In case any errors occur
  write_stream.on('error', function (err)
  {
    console.log(err);
    res.writeHead(409, {'content-type':'text/plain'});
    res.end(err);
    return;
  });

  console.log('%s has been successfully uploaded.', req.headers.filename);
  res.writeHead(200, {'content-type':'text/plain'});
  res.end(req.headers.filename + ' has been successfully uploaded.');
});

app.post('/api/upload/logo', function(req, res, next)
{
  //TODO: check if file exists - and manage revisioning if it does.
  console.log('received upload request for company logo.');
  var write_stream = fs.createWriteStream( __dirname + '/public/logos/logo.' + req.headers.filetype);
  req.pipe(write_stream);

  //In case any errors occur
  write_stream.on('error', function (err)
  {
    console.log(err);
    res.writeHead(409, {'content-type':'text/plain'});
    res.end(err);
    return;
  });

  console.log('company logo has been successfully updated.');
  res.writeHead(200, {'content-type':'text/plain'});
  res.end('company logo has been successfully updated.');
});

/*var cpUpload = upload.fields([{ name: 'pdf', maxCount: 1 }, { name: 'gallery', maxCount: 8 }]);
app.post('/cool-profile', cpUpload, function (req, res, next)
{
  // req.files is an object (String -> Array) where fieldname is the key, and the value is array of files
  //
  // e.g.
  //  req.files['avatar'][0] -> File
  //  req.files['gallery'] -> Array
  //
  // req.body will contain the text fields, if there were any
});*/

//#################Begin File Transfer
app.get('/api/file/uploads/:file_id', function(req, res)
{
  var fpath = path.join(__dirname, '/uploads/'+req.params.file_id);
  var stat = fs.statSync(fpath);
  res.writeHead(200,{'Content-Type':'application/pdf','Content-Length':stat.size});

  var dataStream = fs.createReadStream(fpath);
  dataStream.pipe(res);
  console.log('served file "%s"', req.params.file_id);
});

app.get('/api/file/safety/:file_id', function(req, res)
{
  var ctx, imageXObject, pageModifier, source, targetFileWithHeaders, writer, reader;

  reader = hummus.createReader(__dirname + '/public/pdf/safety/' + req.params.file_id);
  var page_count = reader.getPagesCount();

  var pdf_path = __dirname + '/public/pdf/safety/' + req.params.file_id;
  //source = path.resolve(pdf_path);

  if(!req.headers.logo_options)
  {
    console.log('no logo options specified for requested file. [%s]', req.params.file_id);
    res.writeHead(409,{'Content-Type':'text/plain'});
    res.end('no logo options specified for requested file. [%s]');
    return;
  }
  res.writeHead(200,{'Content-Type':'application/pdf'});//,'Content-Length':stat.size

  targetFileWithHeaders = "" + (new Date()) + "_out.pdf";

  writer = hummus.createWriterToModify(new hummus.PDFRStreamForFile(pdf_path),
                                        new hummus.PDFStreamForResponse(res));
  /*{
    modifiedFilePath: __dirname + '/public/pdf/' + targetFileWithHeaders
  });*/

  var logo_options = JSON.parse(req.headers.logo_options);
  console.log(logo_options.all);

  var i,
      page_top = writer.getModifiedFileParser().parsePage(0).getMediaBox()[3],
      page_w = writer.getModifiedFileParser().parsePage(0).getMediaBox()[2],
      image_height = logo_options.all.h,//75,//writer.getImageDimensions(img_path).height,
      image_width = logo_options.all.w;//160;//writer.getImageDimensions(img_path).width;

  var img_path = __dirname + '/public/logos/logo.jpg';
  if(!fs.existsSync(img_path))
  {
    console.log('logo.jpg DNE, trying logo.png');
    img_path = __dirname + '/public/logos/logo.png';
    if(!fs.existsSync(img_path))
    {
      console.log('logo.png DNE, using default image.');
    }
  }

  for(i=0;i<page_count;i++)
  {
    pageModifier = new hummus.PDFPageModifier(writer, i);

    ctx = pageModifier.startContext().getContext();

    var x = logo_options.all.x, y = logo_options.all.y;
    if(typeof logo_options.all.x == "string")
    {
      if(logo_options.all.x.toLowerCase()=='center')
      {
        x = (page_w/2)-(image_width*0.5);//center logo on h-axis
      }else{
        console.log('unknown logo horizontal position.');
        x = 0;
      }
    }
    if(typeof logo_options.all.y == "string")
    {
      y = 0;
    }

    ctx.drawImage(x, page_top-image_height-y, img_path,
                  {transformation:{width:image_width, height:image_height}});

    pageModifier.endContext().writePage();
  }
  //writer.writePage();
  writer.end();
  res.end();
  /*
  var image_path = __dirname + '/public/logos/fadulous.png';
  var pdfWriter = hummus.createWriterToModify(__dirname + '/public/pdf/safety/'
                    + req.params.file_id,
                    {
                      modifiedFilePath:__dirname + '/public/' + req.params.file_id + '.new.pdf'
                    });
    //var img = pdfWriter.createImageXObjectFromJPG(image_path);
    var pageModifier = new hummus.PDFPageModifier(pdfWriter, 0, true);

    var pageTop = pdfWriter.getModifiedFileParser().parsePage(0).getMediaBox()[3];
    var imageHeight = pdfWriter.getImageDimensions(image_path).height;

    pageModifier.startContext().getContext().drawImage(0, pageTop-imageHeight, image_path);
  //var context = pdfWriter.startPageContentContext();
  //var context = pageModifier.startContext().getContext();

  //context.q().cm(500,0,0,400,0,-100).doXObject(img).Q();
  //context.drawImage(50, 50, __dirname + '/public/logos/fadulous.png',{transformation:{width:216,height:216}});
  pageModifier.endContext().writePage();
  pdfWriter.end();

  console.log('modified and saved document.');*/
  /*var fpath = path.join(__dirname, '/public/pdf/' + targetFileWithHeaders);
  var stat = fs.statSync(fpath);
  res.writeHead(200,{'Content-Type':'application/pdf','Content-Length':stat.size});

  var dataStream = fs.createReadStream(fpath);
  dataStream.pipe(res);*/
  console.log('served file "%s"', req.params.file_id);
});

app.get('/api/file/risk/:file_id', function(req, res)
{
  var fpath = path.join(__dirname, '/public/pdf/risk/'+req.params.file_id);
  var stat = fs.statSync(fpath);
  res.writeHead(200,{'Content-Type':'application/pdf','Content-Length':stat.size});

  var dataStream = fs.createReadStream(fpath);
  dataStream.pipe(res);
  console.log('served file "%s"', req.params.file_id);
});

app.get('/api/file/inspection/:file_id', function(req, res)
{
  var fpath = path.join(__dirname, '/public/pdf/inspection/'+req.params.file_id);
  var stat = fs.statSync(fpath);
  res.writeHead(200,{'Content-Type':'application/pdf','Content-Length':stat.size});

  var dataStream = fs.createReadStream(fpath);
  dataStream.pipe(res);
  console.log('served file "%s"', req.params.file_id);
});

app.get('/api/file/ohs/:file_id', function(req, res)
{
  var fpath = path.join(__dirname, '/public/pdf/ohs/'+req.params.file_id);
  var stat = fs.statSync(fpath);
  res.writeHead(200,{'Content-Type':'application/pdf','Content-Length':stat.size});

  var dataStream = fs.createReadStream(fpath);
  dataStream.pipe(res);
  console.log('served file "%s"', req.params.file_id);
});

app.get('/api/file/appointment/:file_id', function(req, res)
{
  var fpath = path.join(__dirname, '/public/pdf/appointment/'+req.params.file_id);
  var stat = fs.statSync(fpath);
  res.writeHead(200,{'Content-Type':'application/pdf','Content-Length':stat.size});

  var dataStream = fs.createReadStream(fpath);
  dataStream.pipe(res);
  console.log('served file "%s"', req.params.file_id);
});

app.get('/api/file/logo', function(req, res)
{
  var fpath = path.join(__dirname, '/public/logos/logo.jpg');//<-- TODO: check, hard-coded
  var stat = fs.statSync(fpath);
  res.writeHead(200,{'Content-Type':'image/jpg','Content-Length':stat.size});

  var dataStream = fs.createReadStream(fpath);
  dataStream.pipe(res);
  console.log('served logo file');
});

addObject = function(req, res, obj_model)
{
  var obj = req.body;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  //validate obj
  if(!obj_model.isValid(obj))
  {
    console.log('invalid object %s', obj)
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        obj_model.add(obj, function(err)
        {
          if(err)
          {
            errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
            logServerError(err);
            return;
          }
          res.json({'message':'success'});
        });
      }else {
        errorAndCloseConnection(res, 502, errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
    }
  }else{
    errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
  }
}

add = function(req, res, obj_model, callback)
{
  var obj = req.body;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  //validate obj
  if(!obj_model.isValid(obj))
  {
    console.log('invalid object %s', obj)
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        obj_model.add(obj, callback);
      }else {
        errorAndCloseConnection(res, 502, errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
    }
  }else{
    errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
  }
}

updateObject = function(req, res, obj_model)
{
  var obj_id = req.params.object_id;
  var obj = req.body;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  if(isNullOrEmpty(obj_id))
  {
    console.log('invalid object id: "%s".', JSON.stringify(obj_id));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(!obj_model.isValid(obj))
  {
    console.log('invalid object "%s"', JSON.stringify(obj));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        obj_model.update(obj_id, obj, function(err)
        {
          if(err)
          {
            errorAndCloseConnection(res, 500, errors.INTERNAL_ERR);
            logServerError(err);
            return;
          }
          res.json({'message':'successfully updated object.'});
        });
      }else {
        errorAndCloseConnection(res, 502, errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
    }
  }else{
    errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
  }
}

update = function(req, res, obj_model, callback)
{
  var obj_id = req.params.object_id;
  var obj = req.body;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  if(isNullOrEmpty(obj_id))
  {
    console.log('invalid object id: "%s".', JSON.stringify(obj_id));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(!obj_model.isValid(obj))
  {
    console.log('invalid object "%s"', JSON.stringify(obj));
    errorAndCloseConnection(res, 503, errors.INVALID_DATA);
    return;
  }

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        obj_model.update(obj_id, obj, callback);
      }else {
        errorAndCloseConnection(res, 502, errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
    }
  }else{
    errorAndCloseConnection(res, 501, errors.SESSION_EXPIRED);
  }
}

getObject = function(req, res, obj_model)
{
  var obj_id = req.params.object_id;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  if(isNullOrEmpty(obj_id))
  {
    console.log('invalid object id "%s"', obj_id)
    errorAndCloseConnection(res,503,errors.INVALID_DATA);
    return;
  }

  res.setHeader('Content-Type','application/json');

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        //console.log('user [%s] GET request [%s]', session.user_id, obj_model.constructor.name);
        obj_model.get(obj_id, function(err, obj)
        {
          if(err)
          {
            errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
            logServerError(err);
            return;
          }
          if(obj)
          {
            /*if(obj._id)
            {
              var timestamp = obj._id.toString().substring(0,8);
              obj._id = timestamp;
            }else{
              console.log('object %s has no _id attribute.', obj_id);
            }*/
            res.json(obj);
          }else{
            console.log('database returned a null object for the request of %s', obj_id);
            res.end();
          }
        });
      }else {
        errorAndCloseConnection(res,502,errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
    }
  }else {
    errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
  }
}

get = function(req, res, obj_model, callback)
{
  var obj_id = req.params.object_id;
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  if(isNullOrEmpty(obj_id))
  {
    console.log('invalid object_id [%s].', obj_id)
    errorAndCloseConnection(res,503,errors.INVALID_DATA);
    return;
  }

  res.setHeader('Content-Type','application/json');

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        //console.log('user [%s] GET request [%s]', session.user_id, obj_model.constructor.name);
        obj_model.get(obj_id, callback);
      }else {
        errorAndCloseConnection(res,502,errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
    }
  }else {
    errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
  }
}

getAllObjects = function(req, res, obj_model)
{
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        obj_model.getAll(function(err, objs)
        {
          if(err)
          {
            errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
            logServerError(err);
            return;
          }
          res.json(objs);
        });
      }else{
        errorAndCloseConnection(res,502,errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
    }
  }else {
    errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
  }
}

getAll = function(req, res, obj_model, callback)
{
  var session_id = req.headers.cookie;
  var session = sessions.search(session_id);

  res.setHeader('Content-Type','application/json');

  if(session!=null)
  {
    if(!session.isExpired())
    {
      if(session.access_level>=obj_model.ACCESS_MODE)
      {
        obj_model.getAll(callback);
      }else{
        errorAndCloseConnection(res,502,errors.UNAUTH);
      }
    }else {
      errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
    }
  }else {
    errorAndCloseConnection(res,501,errors.SESSION_EXPIRED);
  }
}

errorAndCloseConnection = function(res,status,msg)
{
  res.status(status);
  res.setHeader('Connection','close');
  res.json({'message':msg});
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

logServerError = function(err)
{
  //TODO: log to file
  console.error(err.stack);
}

/**** user authentication ****/
app.post('/api/auth',function(req, res)
{
  var usr = req.body.usr;
  var pwd = req.body.pwd;

  console.log('[%s] login request.', usr);

  res.setHeader('Content-Type','application/json');

  //validate input from client
  if(isNullOrEmpty(usr) || isNullOrEmpty(pwd))
  {
    console.log('invalid usr/pwd object.');
    errorAndCloseConnection(res,404,errors.NOT_FOUND);
    return;
  }

  //check if credentials match the ones in the database
  employees.validate(usr,pwd,function(err, employee)
  {
    if(err)
    {
      errorAndCloseConnection(res,500,errors.INTERNAL_ERR);
      logServerError(err);
      return;
    }
    if(employee)
    {
      console.log('user [%s] successfully logged in.', employee.usr);
      var session = sessions.newSession(employee._id, SESSION_TTL, employee.access_level);
      res.setHeader('Set-Cookie','session=' + session.session_id + ';ttl=' + session.ttl +
                      ';date=' + session.date_issued);
      res.setHeader('Content-Type','text/plain');
      res.send(session.session_id);
    }else{
      errorAndCloseConnection(res,404,errors.NOT_FOUND);
    }
  });
});

createCounter = function(counter_name)
{
  counters.get(counter_name, function(err, counter)
  {
    if(err)
    {
      logServerError(err);
      return;
    }
    if(!counter)//if job counter not added to database add it
    {
      var count = {counter_name:counter_name};
      counters.add(count, function(err)
      {
        if(err)
        {
          logServerError(err);
          return;
        }
        console.log('successfully created new counter "%s"', counter_name);
      });
    }else console.log('counter "%s" already exists.', counter_name);
  });
}

//Init Counters & Timestamps
createCounter('job_count');
createCounter('quote_count');
createCounter('quotes_timestamp');
createCounter('jobs_timestamp');
createCounter('invoices_timestamp');
createCounter('suppliers_timestamp');
createCounter('clients_timestamp');
createCounter('resources_timestamp');
createCounter('assets_timestamp');
createCounter('expenses_timestamp');
createCounter('revenues_timestamp');
createCounter('purchase_orders_timestamp');
createCounter('purchaseorder_count');

app.listen(PORT);
console.log('..::%s server is now running at localhost on port %s::..',APP_NAME, PORT);
