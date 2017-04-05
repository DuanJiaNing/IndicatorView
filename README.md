### 自定义View和属性动画ValueAnimator实现圆点指示器

>**自定义View和属性动画相结合实现支持动态修改指示点位置，拖拽或点击改变指示点位置，点击位置监听及切换动画自定义的圆点指示器。**

#### 效果图

![](http://img.blog.csdn.net/20170406005647275?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWltZWltZWlUUw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

自定 View 代码写在 `IndicatorView.java`中
#### IndicatorView由以下几个重要的图形构成
- 小圆点：固定不动的圆形
- 指示点：在小圆点上来回移动，通过改变指示点当前所在位置来实现 `指示器` 的功能，为了实现“挤扁”的动画效果，绘制时用的是椭圆。
- 线段：用于连接两个小圆点，绘制时以两个相邻小圆点间的距离为一个 `线段` 单位。循环绘制 `线段` ，绘制`小圆点个数减一` 次后连通所有小圆点，*在布局文件或代码中可修改其可见性（`lineVisible`）*

![](http://img.blog.csdn.net/20170406005737300?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWltZWltZWlUUw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

#### 实现的功能：
1. 支持通过xml定义IndicatorView的属性
2. 属性包括：
- 指示点大小、颜色
- 固定显示的小圆点的大小、颜色以及数量
- 连接小圆点的线条的可见性，线条宽度、长度、颜色
- 默认提供了两个用于指示点间切换的动画（平移和挤扁），也可选择不使用动画或自定义
- 默认提供的切换动画的时间可指定
- 启用/禁用拖拽切换（点击切换或两者）功能
3. 通过代码动态修改部分属性
4. 通过代码或得属性值，如当前指示点位置，颜色等
5. 通过代码自定义指示点间切换动画，指示点被触摸的反馈动画及点击事件监听的回调
