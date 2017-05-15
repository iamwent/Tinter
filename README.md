# Tinter

> 当前开发环境：Android Studio 2.1.3，compileSdkVersion 24，buildToolsVersion "24.0.2"，support:appcompat-v7:24.2.0

首先放个图，这就是我要做成的效果，Toolbar 和 Status Bar 一体共用背景图，实际上就是 Toolbar 的背景图延伸到 Status Bar。

![效果图](http://upload-images.jianshu.io/upload_images/42817-54839f916d2b44e5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

先做一点点思考。我不打算修改 toolbar 的高度，设置为 `android:layout_height="?attr/actionBarSize"` 就好，否则 `fitsSystemWindows` 之后就需要设置 toolbar 高度为 25dp ＋ 48dp ＝ 73dp，同时其他的内容也会改变。那么，可以考虑在 `AppBarLayout` 中设置背景，然后让它侵入到状态栏去。现在 `style.xml` 看起来会是这样：

```xml
<!-- values/style.xml --->
<resources>

    <style name="AppTheme" parent="@style/BaseAppTheme"/>

    <!-- Base application theme. -->
    <style name="BaseAppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>

    <style name="AppTheme.NoActionBar">
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
    </style>

</resources>

<!-- values-v19/style.xml --->
<resources>

    <style name="AppTheme" parent="@style/BaseAppTheme">
        <item name="android:windowTranslucentStatus">true</item>
    </style>

</resources>

<!-- values-v21/style.xml --->
<resources>

    <style name="AppTheme" parent="@style/BaseAppTheme">
        <item name="android:windowTranslucentStatus">false</item>
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
    </style>

    <style name="AppTheme.NoActionBar">
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>

    </style>
</resources>
```
toolbar 的布局文件差不多是这样子：
```xml
<!-- layout/toolbar.xml --->
<?xml version="1.0" encoding="utf-8"?>

<android.support.design.widget.AppBarLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:fitsSystemWindows="true"
    android:paddingTop="@dimen/appbar_top_padding"
    android:background="@drawable/bg_bar"
    android:theme="@style/AppTheme.AppBarOverlay">

    <android.support.v7.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:popupTheme="@style/AppTheme.PopupOverlay" />

</android.support.design.widget.AppBarLayout>
```
主布局文件差不多是这样子：
```xml
<!-- layout/activity_main.xml --->
<?xml version="1.0" encoding="utf-8"?>

<android.support.design.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context="com.iamwent.tinter.MainActivity">

    <include layout="@layout/toolbar" />

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" >

        <TextView
            android:id="@+id/tv_sdk"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="16dp"/>
    </RelativeLayout>

</android.support.design.widget.CoordinatorLayout>
```
然后你运行，发现 v21 的效果出来了，但是 v19 是这么个鬼！
![v19效果](http://upload-images.jianshu.io/upload_images/42817-8efa0310ea90f391.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

那个灰色条是什么？又是怎么来的呢？给跟布局设置背景色就可以发现，灰色条实际上是因为根布局侵入了状态栏，从我们给根布局设置的 `android:fitsSystemWindows="true"` 就是让布局上去

对于 `android:windowTranslucentStatus`[文档上是这么说的：](https://developer.android.com/about/versions/android-4.4.html#UI)
> By enabling translucent system bars, your layout will fill the area behind the system bars, so you must also enable [fitsSystemWindows](https://developer.android.com/reference/android/R.attr.html#fitsSystemWindows)
 for the portion of your layout that should not be covered by the system bars.
>开启透明状态栏后，你的布局会填充状态栏下面的区域，所以你应当同时设置布局 fitsSystemWindows 以防止被状态栏覆盖。

而对于 `android:fitsSystemWindows` 文档又是这么说的：
> Boolean internal attribute to adjust view layout based on system windows such as the status bar. If true, adjusts the padding of this view to leave space for the system windows. Will only take effect if this view is in a [non-embedded activity](http://stackoverflow.com/questions/25874412/what-is-a-non-embedded-activity-and-why-doesnt-androidfitssystemwindows-work-i).
>配置 fitsSystemWindows 后，系统就会调整 view 的 padding 以给 system windows 留出空间。

解决办法是根布局不设置 `android:fitsSystemWindows`，然后在代码中判断，在 v21 以上手动设置这一属性，代码如下：
```java
private void setTranslucentStatusBar() {
    int sdkInt = Build.VERSION.SDK_INT;

    if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
        setTranslucentStatusBarLollipop();
    }
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
private void setTranslucentStatusBarLollipop() {
    ((ViewGroup) getWindow().findViewById(android.R.id.content)).getChildAt(0).setFitsSystemWindows(true);
}
```
昨晚吐槽的时候，有位朋友提醒了我，这个值也可以用 `style` 的方式设置：

```xml
<!-- values/style.xml --->
<style name="AppTheme.FitsSystemWindows">
</style>

<!-- values-v21/style.xml --->
<style name="AppTheme.FitsSystemWindows">
    <item name="android:fitsSystemWindows">true</item>
</style>
```

我脑袋一抽，觉得以下这个方式可能也可以：
```xml
<!-- values/style.xml --->
<bool name="fit_system_status_bar">false</bool>

<!-- values-v21/style.xml --->
<bool name="fit_system_status_bar">true</bool>
```
## 坑位零
在上面的配置中涉及到了多个属性的设置，建议修改为不同的值，看看它们会造成什么样的效果。
- [android:windowDrawsSystemBarBackgrounds](https://developer.android.com/reference/android/R.attr.html#windowDrawsSystemBarBackgrounds)
- android:statusBarColor
- android:windowDrawsSystemBarBackgrounds

## 坑位一
![奇怪的间隔](http://upload-images.jianshu.io/upload_images/42817-dad2d3b4ac67bfaf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

眼尖的人可能在最前面的效果图就看到了，那就是在 v23 上这个显示系统版本的 TextView 有一个奇怪的 margin，而且很巧合的就是 25dp！最后我发现，是由于给主布局的 RelativeLayout 设置了 `app:layout_behavior="@string/appbar_scrolling_view_behavior"` 造成的。解决办法是在 v23 上给 RelativeLayout 设置 `android:layout_marginTop="-25dp"`，可以在 dimens 中做。

## 坑位二
在修改的过程中我还发现一个奇怪的问题，status bar 是个灰色的条，超级奇怪！最后对比发现是 `AppTheme.NoActionBar` 采用继承 `Theme.AppCompat.Light.NoActionBar` 的方式，解决办法是不继承 parent，采用手动配置。

![Theme 造成的怪现象](http://upload-images.jianshu.io/upload_images/42817-5702ec362f11feee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 结语
我是要给一个半成品的 APP 做适配，所以先单独做了一个 demo 实现想要的——后来踩到的坑证明我这个决定是多么的正确！特别是坑位三。所以一旦出现莫名其妙的错误，我就对照 demo 的配置一个个地方去排除。
另外，多试试各种配置的作用，明白它们影响的是什么区域，碰到问题才好修改。

[Demo 在这里](https://github.com/iamwent/Tinter)
