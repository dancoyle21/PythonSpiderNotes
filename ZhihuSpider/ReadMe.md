### Web crawler username and password and verification code login: Crawl [Know] (http://www.zhihu.com/) website

** Some notes: **

* Use the requests package to crawl. First try to log in automatically with the username and password. If it fails, you need to use a cookie to log in.

* The configuration file config.ini, which includes the username and password information. If there is a verification code, you need to manually log in to the website to obtain the cookie information.

* Determine whether the login is successful or not, and see if there is any user information in the generated html file.
