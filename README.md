# `react-native-image-chooser`

[![Greenkeeper badge](https://badges.greenkeeper.io/satya164/react-native-image-chooser.svg)](https://greenkeeper.io/)

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
import com.imagechooser.ImageChooserPackage;  // <--- import

public class MainActivity extends ReactActivity {

  ......

  @Override
  protected List<ReactPackage> getPackages() {
    return Arrays.asList(
      new MainReactPackage(),
      new ImageChooserPackage() // <------ add the package
    );
  }

  ......

}
```

## Usage

First import the module as follows:

```js
import ImageChooser from "react-native-image-chooser";
```

To show the chooser, call the `pickImage` method:

```js
try {
  const data = await ImageChooser.pickImage();

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
