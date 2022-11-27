/*
Copyright 2017 - 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
    http://aws.amazon.com/apache2.0/
or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/



const AWS = require('aws-sdk')
const awsServerlessExpressMiddleware = require('aws-serverless-express/middleware')
const bodyParser = require('body-parser')
const express = require('express')

AWS.config.update({ region: process.env.TABLE_REGION });

const dynamodb = new AWS.DynamoDB.DocumentClient();

let tableName = "Plantie";

const path = "/items";

// declare a new express app
const app = express()
app.use(bodyParser.json())
app.use(awsServerlessExpressMiddleware.eventContext())

// Enable CORS for all methods
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*")
  res.header("Access-Control-Allow-Headers", "*")
  next()
});

// convert url string param to expected Type
const convertUrlType = (param, type) => {
  switch(type) {
    case "N":
      return Number.parseInt(param);
    default:
      return param;
  }
}

app.get(path, function(req, res) {
  if (req.query.id){
    var getItemParams = {
      TableName: tableName,
      KeyConditionExpression: "id = :id",
      ExpressionAttributeValues: {
        ":id": req.query.id
      }
    }    
    dynamodb.query(getItemParams,(err, data) => {
      if(err) {
        res.json({error: err, url: req.url, body: req.body, status: 500});
      } else {
        if (data.Item) {
          res.json({data: data.Item, status: 204});
        } else {
          res.json({result: 'succeed', url: req.url, data: data, status: 204}) ;
        }
      }
    });
  }
});


app.post(path, function(req, res) {

  var params = {
    TableName: tableName,
    Key: {
      "id": req.body['id']
    },
    UpdateExpression: "set value = :x",
    ExpressionAttributeValues: {
      ":x": req.body['value']
    }
  }
  dynamodb.update(params, (err, data) => {
    if (err) {
      res.json({error: err, url: req.url, body: req.body, status: 500});
    } else {
      res.json({result: 'succeed', url: req.url, data: data, status: 204})
    }
  });
});

app.listen(3000, function() {
  console.log("App started")
});

// Export the app object. When executing the application local this does nothing. However,
// to port it to AWS Lambda we will create a wrapper around that will load the app from
// this file
module.exports = app
