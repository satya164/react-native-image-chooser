# `react-native-image-chooser`

A React Native module to show system Image chooser. Currently only supports Android.

### Installation

```sh
npm i --save react-native-image-chooser
```

### Add it to your android project

In `android/settings.gradle`

```gradle
...

include ':react-native-image-chooser'
project(':react-native-image-chooser').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-image-chooser/android')
```

In `android/app/build.gradle`

```gradle
...

dependencies {
    ...

    compile project(':react-native-image-chooser')
}
```

Register module (in `MainActivity.java`)

```java
import android.content.Intent;  // <--- import
import com.imagechooser.ImageChooserPackage;  // <--- import

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {
  ......

  private ImageChooserPackage mChoosersPackage = new ImageChooserPackage(this); // <------ create new instance

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mReactRootView = new ReactRootView(this);

    mReactInstanceManager = ReactInstanceManager.builder()
      .setApplication(getApplication())
      .setBundleAssetName("index.android.bundle")
      .setJSMainModuleName("index.android")
      .addPackage(new MainReactPackage())
      .addPackage(mChoosersPackage) // <------ add the package
      .setUseDeveloperSupport(BuildConfig.DEBUG)
      .setInitialLifecycleState(LifecycleState.RESUMED)
      .build();

    mReactRootView.startReactApplication(mReactInstanceManager, "ExampleApp", null);

    setContentView(mReactRootView);
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      mChoosersPackage.handleActivityResult(requestCode, resultCode, data); // <------ handle activity result
  }

  ......

}
```

## Usage

First import the module as follows:

```js
import React from "react-native";

const {
  NativeModules: {
    ImageChooserModule
  }
} = React;
```

To show the chooser, call the `pickImage` method:

```js
try {
  const data = await ImageChooserModule.pickImage();

  // do something with the data
} catch (err) {
  // handle error
}
```

The `pickImage` method returns a `Promise` with data. The data object has the following properties:

```js
height: number;
width: number;
size: number;
name: string;
uri: string;
```
