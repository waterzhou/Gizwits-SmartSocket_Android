智能云插座
=============

Gizwits Power Socket Android Demo App

XPGWifiSDK 版本号

    XPGWifiSDK-Android-1.6.2.16031908


功能介绍

    这是一款基于机智云XPGWifiSDK的开源代码示例APP开发的产品。
    该APP针对的是智能家电中的插座类产品。包括了以下几点插座常用功能：

    ▪ 插座电源的开关
    ▪ 插座定时开关
    ▪ 插座倒计时开关
    ▪ 定时周重复

    以下功能是机智云开源App的几个通用功能，除UI有些许差异外，流程和代码都几乎一致：

    ▪	机智云账户系统的注册、登陆、修改密码、注销等功能
    ▪	机智云设备管理系统的AirLink配置入网、SoftAP配置入网，设备与账号绑定、解绑定，修改设备别名等功能
    ▪	机智云设备的登陆，控制指令发送，状态接收，设备连接断开等功能

    该项目对应的实体硬件设备为Atmel的WINC1510PB。可以进行设备的配置入网，绑定，控制等流程。


项目依赖和安装

    ▪	XPGWifiSDK的jar包和支持库
    登录机智云官方网站http://gizwits.com的开发者中心，下载并解压最新版本的SDK。
    下载后，将解压后的目录拷贝到复制到 Android 项目 libs 目录即可。

    ▪	Atmel WiFi设备
    使用Atmel WINC1510PB模块。

    ▪	虚拟设备
    使用机智云实验室的相对应虚拟设备，可以体验设备指令收发，状态的获取等功能。

项目工程结构

    ▪	包结构说明
    com.gizwits.powersocket                              -智能云插座独有代码，包含控制部分和侧边栏部分
    com.gizwits.powersocket.activity.control             -智能云插座控制界面activity
    com.gizwits.powersocket.activity.slipbar             -智能云插座侧边栏activity
    com.gizwits.framework                                -机智云设备开源APP框架,包含除控制界面Activity外的代码，暂时机智云实验室中的其他开源APP所用框架一致
    com.gizwits.framework.activity                       -机智云设备开源APP框架相关activity
    com.gizwits.framework.adapter                        -机智云设备开源APP框架相关数据适配器
    com.gizwits.framework.config                         -机智云设备开源APP框架配置类
    com.gizwits.framework.entity                         -机智云设备开源APP框架实体类
    com.gizwits.framework.sdk                            -机智云设备开源APP框架操作SDK相关类
    com.gizwits.framework.utils                          -机智云设备开源APP框架工具类
    com.gizwits.framework.widget                         -机智云设备开源APP框架自定义控件
    com.gizwits.framework.XpgApplication                 -机智云设备开源APP框架自定义Application
    com.xpg.XXX                                          -机智云通用开发API
    zxing                                                -第三方二维码扫描控件


使用流程

    ▪	虚拟设备＋app使用流程（体验指令发、状态获取等流程）

    1.在机智云官网上注册并登录帐号
    2.使用机智云实验室里面的智能云插座启动虚拟设备
    3.在app上注册并登录帐号
    4.通过扫描网页上的二维码添加虚拟设备
    5.进入控制界面与虚拟设备进行交互

    ▪	Atmel Winc1510＋app使用流程（体验配置、绑定实体设备等流程）

    1.在app上注册并登录帐号
    2.通过配置"+"按钮，使用Airlink配置gokit入网
    3.按键使设备进入待配置界面。
    4.开始配置，并绑定wifi 设备
    5.进入控制界面

问题反馈

    您可以给在此网站反馈提出问题。
