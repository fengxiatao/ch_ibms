使用websdk前，请先阅读doc目录下的H5PlayerWEBSDK开发向导.pdf，注意环境配置，请先确认设备是否支持无插件，参考以上文档的1.2章节【支持设备】

==================服务挂载========================
如果需要云台等相关接口（参考doc目录下的H5PlayerWEBSDK开发向导.pdf）
则需要使用http server的代理功能，按以下步骤进行。
1、修改登录设备的ip，请编辑webs/index.html的第15行开始。
2、打开nginx-1.10.2双击start.bat  开启服务
3、输入http://127.0.0.1/index.html
4、打开nginx-1.10.2双击stop.bat  停止服务
启动服务后，访问index.html即可。
所有api调用的，可以参考index.html中嵌入的index.js中的方法

如果不需要云台，只需要拉流，抓图等，可以将webs放置于任意http server中（比如tomcat或者iis下）
调用流程可以参考简易版demo（simple.html），修改ip位于46,47行
对讲sdk只有2个接口，可以参考talk.html


===================文件说明=========================
文件夹说明：
	doc		相关文档存放目录
	nginx-1.10.2	web服务器目录
	webs		demo目录


==================注意事项===========================

1、Q：如果开发环境是react、vue或者其他框架，应该怎么加载WEB SDK。
	A：不能使用import方式进行加载，需要使用script的方式，将playerControl类挂在在window下，比如
		<script src="module/playerControl.js"></script>，vue可以参考video-player--vue-demo-proxy_V2.zip
2、Q：拉流失败，无法看到视频画面。
	A：请编辑webs/index.html的第15行，修改IP和端口，用户名和密码。
		一般拉流的默认端口都为80，而不是37777。
3、Q：提示登录失败
	A：在保证用户名密码正确的基础上，无插件WEB SDK 需要运行在http server中，
		如果在资源管理器中双击index.html后在浏览器中打开的，则会提示登录失败（正确运行方式参考【服务挂载】）。
4、如果浏览器提示WebSocket connection to 'ws://192.168.1.105/' failed: Error in connection类似错误。
	A：先确认该ip的网络连接是可用的，和端口是否正确。再确认设备是否支持无插件，参考doc目录下的H5PlayerWEBSDK开发向导.pdf的1.2章节【支持设备】。
5、IPCSD新增了智能规则框显示
		


