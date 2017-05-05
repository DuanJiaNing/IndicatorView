## 自定义View和属性动画ValueAnimator实现圆点指示器:blush:

>**自定义View和属性动画相结合实现支持动态修改指示点位置，拖拽或点击改变指示点位置，点击位置监听及切换动画自定义的圆点指示器。**

#### 一.开发背景
最近学习了自定义View和属性动画的知识，开发这个简单的圆点指示器以巩固所学知识。

#### 二.效果图

![](https://raw.githubusercontent.com/DuanJiaNing/IndicatorViewDemo/master/screenshot001.gif)

#### 三.IndicatorView主要属性
##### 3.1 构成元素
- 小圆点：固定不动的圆形
- 指示点：在小圆点上来回移动，通过改变指示点当前所在位置来实现 `指示器` 的功能，为了实现“挤扁”的动画效果，绘制时用的是椭圆。
- 线段：用于连接两个小圆点，绘制时以两个相邻小圆点间的距离为一个 `线段` 单位。循环绘制 `线段` ，绘制`小圆点个数减一` 次后连通所有小圆点，*在布局文件或代码中可修改其可见性（`lineVisible`）*

![](http://img.blog.csdn.net/20170406005737300?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWltZWltZWlUUw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
##### 3.2 xml属性
- 指示点大小、颜色
- 固定显示的小圆点的大小、颜色以及数量
- 连接小圆点的线条的可见性，线条宽度、长度、颜色
- 默认提供了两个用于指示点间切换的动画（平移和挤扁），也可选择不使用动画或自定义
- 默认提供的切换动画的时间可指定
- 启用/禁用拖拽切换（点击切换或两者）功能
- 指定控件的显示方向，水平或是纵向（默认为水平）
##### 3.3 功能
- 通过代码动态修改部分属性
- 通过代码获得属性值，如当前指示点位置，颜色等
- 通过代码自定义指示点间切换动画，指示点被触摸的反馈动画及点击事件监听的回调
#### 四.用途：

#### 五.如何使用
可以在布局文件中直接使用：
``` xml
      <com.duan.indicatorviewdemo.IndicatorView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"

        app:IndicatorSwitchAnimation="squeeze"
        app:dotColor="#2d2b2b"
        app:dotNum="4"
        app:dotSize="10dp"
        app:duration="800"
        app:indicatorColor="#ff9500"
        app:indicatorPos="1"

        app:indicatorSize="25dp"
        app:lineColor="#b3b3b3"
        app:lineHeight="4dp"
        app:lineWidth="85dp" />
```
具体使用可参看博文：[自定义View和属性动画ValueAnimator实现圆点指示器](http://blog.csdn.net/aimeimeiTS/article/details/69370853)
#### 六.版本变化
- v1.0 2017-04-03：初始化
- v1.1 2017-05-04：添加**纵向视图**支持，现在可以在`xml`文件中通过指定`indicatorOrientation`属性为`vertical`使控件以纵向视图显示
``` xml
  app:indicatorOrientation="vertical"
```
![](https://raw.githubusercontent.com/DuanJiaNing/IndicatorViewDemo/master/screenshot002.gif)

具体使用可参看博文：[自定义View和属性动画ValueAnimator实现圆点指示器——支持“纵向视图”](http://blog.csdn.net/aimeimeits/article/details/71158500)

- v1.1.1 2017-05-05：添加指示点拖拽监听和指示点位置改变监听，在代码中设置监听器即可监听指示点拖拽时的位置改变（映射到的小圆点对应的位置），及其间距变化（与最左或最下的小圆点间的距离）。
```java
IndicatorView indicator = (IndicatorView) findViewById(R.id.main2_indicator);
        indicator.setOnIndicatorSeekListener(new IndicatorView.OnIndicatorSeekListener() {
            @Override
            public void onSeekChange(IndicatorView view, int distance, int dotPos) {
                Log.i(TAG, "onSeekChange: distance=" + distance + " dot=" + dotPos);
            }

            @Override
            public void onStartTrackingTouch(IndicatorView view) {
                Toast.makeText(MainActivity.this, "touched", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSopTrackingTouch(IndicatorView view) {
                Toast.makeText(MainActivity.this, "touch leave", Toast.LENGTH_SHORT).show();
            }
        });

        indicator.setOnIndicatorChangeListener(new IndicatorView.OnIndicatorChangeListener() {
            @Override
            public void onIndicatorChange(int currentPos, int oldPos) {
                Log.i(TAG, "onIndicatorChange: cuPos=" + currentPos + " oldPos=" + oldPos);
            }
        });
```
#### 六.未来的开发计划
- [X] 添加**纵向视图**支持
- [X] 添加指示点拖拽监听和指示点位置改变监听
- [ ] 自定义指示点在各个位置的颜色

#### 六.Q&A
