Release Process
===

### Step 1

Update the `library_version` configuration in the `collage-photo-picker/deploy.gradle` file.

### Step 2
Run the command

```
./gradlew clean build collage-photo-picker:bintrayUpload
```