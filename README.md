# SlideViewer

## need NDK
https://developer.android.com/studio/projects/add-native-code.html

## encrypt

```
KEY=HOGE
openssl aes-256-cbc -e -in app/google-services.json -out app/encrypted_google-services.json -k $KEY
```
