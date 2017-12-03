CollagePhotoPicker
===

Gradle
---

Add this into your dependencies block.

```
// For gradle < 3.0
compile 'com.cardinalblue.photopicker:collage-photo-picker:1.0.0'

// For gradle >= 3.0
implementation 'com.cardinalblue.photopicker:collage-photo-picker:1.0.0'
```

If you cannot find the package, add this to your gradle repository

```
maven {
    url 'https://dl.bintray.com/cblue/android'
}
```

Wiki
---

### General

The photo-picker is a componenet that is composed by Model, View, and Presenter sub-components.

constructing...

### Usage

```
supportFragmentManager
    .beginTransaction()
    .replace(R.id.picker, GalleryPhotoPickerFragment() as Fragment)
    .commit()
```