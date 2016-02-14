# Cognito Sample App
This is a demo app for Amazon Cognito. Currently it allows for unauthenticated users, logging in through Facebook, and logging in through Google.

This app is dependent upon the following API's:
- [Amazon Cognito](https://aws.amazon.com/cognito/)
- [Facebook](https://developers.facebook.com/)
- [Google](https://developers.google.com/identity/sign-in/android/)

I have intentionally excluded my config.xml and google-services.json files from this project. Here's an example of what my config.xml file looks like:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <string name="AWS_COGNITO_IDENTITY_POOL_ID">KEY-PROVIDED-BY-AMAZON-COGNITO</string>
  <string name="FACEBOOK_APP_ID">APP-ID-PROVIDED-BY-FACEBOOK</string>
    <string name="GOOGLE_SERVER_CLIENT_ID">CLIENT-ID-PROVIDED-BY-GOOGLE</string>
</resources>
```

You can generate your google-services.json file [here](https://developers.google.com/mobile/add?platform=android&cntapi=signin&cntapp=Default%20Demo%20App&cntpkg=com.google.samples.quickstart.signin&cnturl=https:%2F%2Fdevelopers.google.com%2Fidentity%2Fsign-in%2Fandroid%2Fstart%3Fconfigured%3Dtrue&cntlbl=Continue%20with%20Try%20Sign-In).

## Future Plans
I want to continue building out this sample app to make life easier with future projects. Here are integrations you can plan on seeing in the future:
- Twitter
- Digits
- AWS DynamoDB
- AWS Lambda
- AWS API Gateway
