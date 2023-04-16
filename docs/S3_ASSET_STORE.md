# Writing Assetmod results to S3

To reduce the computational intense generation of resized images, in addition to long-life file cacheing in
NGINX we can also enable the S3 Asset Store functionality.

Make sure your environment variables are updated to be as follows

```
S3_ASSETS_ENABLED=true
S3_ASSETS_BUCKET=s3-bucket-name
S3_ASSETS_PREFIX=store-here/
```

Where you have to fill out the correct values.  

You also need to make sure the instance running the CMS code has AWS environment variables set or
at least have some kind of credential provider trigger that can read and write to that bucket. 

```
AWS_REGION=ap-southeast-2
AWS_SECRET_ACCESS_KEY=
AWS_ACCESS_KEY_ID=
```
