使用websdk前，请先阅读doc目录下的H5PlayerWEBSDK开发向导.pdf，注意环境配置，请先确认设备是否支持无插件，参考以上文档的1.2章节【支持设备】

==================服务挂载========================

如果需要获取设备数据、调用云台等相关接口（参考doc目录下的H5PlayerWEBSDK开发向导.pdf）

则需要使用http server的代理功能，按以下步骤进行：
1、打开nginx-1.10.2双击start.bat  开启服务
2、输入http://127.0.0.1/index.html
3、界面上修改设备ip、用户名、密码，端口号设备默认80，点击登录
4、登陆成功后，登陆状态变更为已登录，并自动获取展示通道列表，双击通道进行拉流

停止代理进程：
1、打开nginx-1.10.2双击stop.bat  停止服务

所有api调用的，可以参考index.html中嵌入的index.js中的方法；SDK具体方法和业务请求介绍，请参考doc目录下说明文件
===================文件说明=========================
文件夹说明：
	doc		相关文档存放目录
	nginx-1.10.2	web服务器目录
	webs		demo目录


==================注意事项===========================

1、Q：如果开发环境是react、vue或者其他框架，应该怎么加载WEB SDK。
	A：不能使用import方式进行加载，需要使用script的方式，将playerControl类挂在在window下，比如
		<script src="module/playerControl.js"></script>

2、Q：拉流失败，无法看到视频画面。
	A：请访问设备web，检查安全中心-音视频加密页面，RTSP over TLS是否被启用，若开启需要关闭。

3、Q：提示登录失败
	A：在保证用户名密码正确的基础上，无插件WEB SDK 需要运行在http server中，
		如果在资源管理器中双击index.html后在浏览器中打开的，则会提示登录失败（正确运行方式参考【服务挂载】）。

4、如果浏览器提示WebSocket connection to 'ws://192.168.1.105/' failed: Error in connection类似错误
	A：先确认该ip的网络连接是可用的，和端口是否正确。再确认设备是否支持无插件，参考doc目录下的H5PlayerWEBSDK开发向导.pdf的1.2章节【支持设备】

5、Q：提示登录失败，响应（Status Code: 302 Moved Temporarily）
	A：确认设备是否开启Https，nginx.conf默认不支持代理https请求，请参考doc目录下Nginx环境搭建_Linux.docx第三节Nginx配置https访问

6、Q：控制台报错，index.js:9:42（addEventListener）
	A：请清除下浏览器缓存，一般是先访问了IPC_SD Demo导致index.js被缓存导致

7、Q：拉流和协议请求用的同一个通道号，但是协议请求失败
	A：拉流通道下标从1开始，协议通道下标从0开始