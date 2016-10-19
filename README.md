# Fluid Horizontal Layout
Android Custom View Group, that acts like a Horizontal LinearLayout, but allows one element to be a various size and not pushing next Views out of screen.

For example, you have a View of 3 items:
1) User avatar
2) Message's body
3) Message's time

Imagine, that you want to show these items in one line and allow message's body be wrap_content, but not push message's time out of screen. There's no default Android ViewGroup for such case.

See the difference on the image below. The blue is the message's time is pushed out of screen by text by LinearLayout. Fluid horizontal layout leaves it and reduces the text size.

<img src="https://raw.githubusercontent.com/alexoro/fluid-horizontal-layout/master/docs/diff.png" width="240">

# How to include in project
Library is distributed via jitpack.io

```gradle
// Add this lines into your roou build.gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

```gradle
// Add dependency to library in any target project module
dependencies {
    compile 'com.github.alexoro:fluid-horizontal-layout:VERSION'
}
```

# Usage
It is important to mark the fluid View with __app:layout_isFluid__. View will crash if no fluid view is found or more than 1 fluid view is found.
```xml
<com.alexoro.fluidhorizontallayout.FluidHorizontalLayout
    android:layout_width="wrap_content"
    android:layout_height="100dp">
    <View
        android:layout_width="60dp"
        android:layout_height="match_parent"
        android:background="#f00"
        />
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_gravity="center_vertical"
        app:layout_isFluid="true"
        android:text="@string/text_short"
        android:textSize="20sp"
        android:textColor="#000"
        />
    <View
        android:layout_width="60dp"
        android:layout_height="match_parent"
        android:background="#00f"
        />
</com.alexoro.fluidhorizontallayout.FluidHorizontalLayout>
```
Library has 2 custom fields:
```xml
app:layout_gravity=(no_gravity|top|bottom|center_vertical)
app:app:layout_isFluid=(true|false)
```