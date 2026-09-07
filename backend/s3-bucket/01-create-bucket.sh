#!/bin/bash
# Create the S3 bucket in LocalStack on startup
awslocal s3 mb s3://form-generator-bucket
echo "S3 bucket 'form-generator-bucket' created successfully."
