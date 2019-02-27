# [Python Getting Started Web Crawler Essentials] (https://github.com/lining0806/PythonSpiderNotes)

***

Python learning web crawlers are divided into 3 major sections: **crawl**,**analysis**,**store**

In addition, the more commonly used crawler framework [Scrapy] (http://scrapy.org/), here is also a detailed description.

First, I will list some related articles that I have summarized. These cover the basic concepts and skills needed to get started with web crawlers: [Ningge's Station - Web Crawler] (http://www.lining0806.com/category/spider/)
***

When we enter a url in the browser and press Enter, what happens in the background? For example, if you enter [http://www.lining0806.com/] (http://www.lining0806.com/), you will see Ning Ge's small station home page.

Simply put, the following four steps occur in this process:

* Find the IP address corresponding to the domain name.
* Send a request to the server corresponding to the IP.
* The server responds to the request and sends back the content of the web page.
* The browser parses the content of the web page.

The web crawler has to do, in simple terms, to implement the browser's functionality. By specifying the url, the data needed by the user is directly returned, without the need to manually manipulate the browser to obtain it.

## Grab
In this step, what do you want to know clearly? Is the HTML source code, or the Json format string.

#### 1. The most basic crawl

Grab most cases as get requests, that is, get data directly from the other server.

First of all, Python comes with two modules, urllib and urllib2, which basically satisfy the general page crawling. In addition, [requests] (https://github.com/kennethreitz/requests) is also a very useful package, similar to [httplib2] (https://github.com/jcgregorio/httplib2) and so on.

```
Requests:
Import requests
Response = requests.get(url)
Content = requests.get(url).content
Print "response headers:", response.headers
Print "content:", content
Urllib2:
Import urllib2
Response = urllib2.urlopen(url)
Content = urllib2.urlopen(url).read()
Print "response headers:", response.headers
Print "content:", content
Httplib2:
Import httplib2
Http = httplib2.Http()
Response_headers, content = http.request(url, 'GET')
Print "response headers:", response_headers
Print "content:", content
```

In addition, for a url with a query field, the get request will generally attach the requested data to the url, to split the url and transfer the data, and multiple parameters are connected with &.

```
Data = {'data1':'XXXXX', 'data2':'XXXXX'}
Requests: data is dict, json
Import requests
Response = requests.get(url=url, params=data)
Urllib2:data is a string
Import urllib, urllib2
Data = urllib.urlencode(data)
Full_url = url+'?'+data
Response = urllib2.urlopen(full_url)
```

Related reference: [Netease News Rankings Grab Review] (http://www.lining0806.com/%E7%BD%91%E6%98%93%E6%96%B0%E9%97%BB%E6% 8E%92%E8%A1%8C%E6%A6%9C%E6%8A%93%E5%8F%96%E5%9B%9E%E9%A1%BE/)

Reference Project: [The most basic crawler for web crawlers: Crawl Netease News Leaderboard] (https://github.com/lining0806/PythonSpiderNotes/blob/master/NewsSpider)

### 2. Dealing with the login situation

**2.1 Login using form**

This situation is a post request, that is, the form data is sent to the server first, and the server stores the returned cookie locally.

```
Data = {'data1':'XXXXX', 'data2':'XXXXX'}
Requests: data is dict, json
Import requests
Response = requests.post(url=url, data=data)
Urllib2:data is a string
Import urllib, urllib2
Data = urllib.urlencode(data)
Req = urllib2.Request(url=url, data=data)
Response = urllib2.urlopen(req)
```

**2.2 Login using cookies**

Login with a cookie, the server will think that you are a logged-in user, so it will return you a logged-in content. Therefore, the case where a verification code is required can be solved using a cookie with a verification code login.

```
Import requests
Requests_session = requests.session()
Response = requests_session.post(url=url_login, data=data)
```

If there is a verification code, it is not acceptable to use response = requests_session.post(url=url_login, data=data). The practice should be as follows:

```
Response_captcha = requests_session.get(url=url_login, cookies=cookies)
Response1 = requests.get(url_login) # not logged in
Response2 = requests_session.get(url_login) # logged in, because I got the Response Cookie!
Response3 = requests_session.get(url_results) # logged in, because I got the Response Cookie!
```

Related reference: [Web crawler - verification code login] (http://www.lining0806.com/6-%E7%BD%91%E7%BB%9C%E7%88%AC%E8%99%AB-% E9%AA%8C%E8%AF%81%E7%A0%81%E7%99%BB%E9%99%86/)

Reference project: [Web crawler username and password and verification code login: Crawl to know the website] (https://github.com/lining0806/PythonSpiderNotes/blob/master/ZhihuSpider)

### 3. Treatment of anti-reptile mechanism

**3.1 Using a proxy**

Applicable situation: Restricting the IP address, it can also solve the problem that you need to enter the verification code to log in due to "frequent clicks".

The best way to do this is to maintain a proxy IP pool. There are a lot of free proxy IPs on the Internet, which are not good enough. You can find out what can be used by filtering. For "frequent clicks", we can also avoid being banned by the site by limiting the frequency with which crawlers visit the site.

```
Proxies = {'http': 'http://XX.XX.XX.XX:XXXX'}
Requests:
Import requests
Response = requests.get(url=url, proxies=proxies)
Urllib2:
Import urllib2
Proxy_support = urllib2.ProxyHandler(proxies)
Opener = urllib2.build_opener(proxy_support, urllib2.HTTPHandler)
Urllib2.install_opener(opener) # Install opener, then use urlopen() to use the installed opener object
Response = urllib2.urlopen(url)
```

**3.2 Time setting**

Applicable conditions: Limit the frequency.

Requests, Urllib2 can use the sleep() function of the time library:

```
Import time
Time.sleep(1)
```

**3.3 Disguised as a browser, or anti-theft chain

Some websites will check if you are actually accessing the browser or if the machine is automatically accessed. In this case, plus User-Agent, you are allowed to access the browser. Sometimes it will check if the Referer information is also checked if your Referer is legal, usually with Referer.

```
Headers = {'User-Agent':'XXXXX'} # Disguised as a browser access, suitable for sites that refuse crawlers
Headers = {'Referer':'XXXXX'}
Headers = {'User-Agent': 'XXXXX', 'Referer': 'XXXXX'}
Requests:
Response = requests.get(url=url, headers=headers)
Urllib2:
Import urllib, urllib2
Req = urllib2.Request(url=url, headers=headers)
Response = urllib2.urlopen(req)
```

### 4. Reconnect for disconnection

Not much to say.

```
Def multi_session(session, *arg):
retryTimes = 20
While retryTimes>0:
Try:
Return session.post(*arg)
Except:
Print '.',
retryTimes -= 1
```

Or

```
Def multi_open(opener, *arg):
retryTimes = 20
While retryTimes>0:
Try:
Return opener.open(*arg)
Except:
Print '.',
retryTimes -= 1
```

This way we can use multi_session or multi_open to keep the session or opener crawled by the crawler.

### 5. Multi-process crawling

Here's an experimental comparison of [Wall Street Insights] (http://live.wallstreetcn.com/) for parallel crawling: [Python Multi-Process Crawl] (https://github.com/lining0806/PythonSpiderNotes/blob/master/ Spider_Python) and [Java single-threaded and multi-threaded crawling] (https://github.com/lining0806/PythonSpiderNotes/blob/master/Spider_Java)

Related reference: [Comparison of multi-process multi-threaded computing methods for Python and Java] (http://www.lining0806.com/%E5%85%B3%E4%BA%8Epython%E5%92%8Cjava%E7%9A %84%E5%A4%9A%E8%BF%9B%E7%A8%8B%E5%A4%9A%E7%BA%BF%E7%A8%8B%E8%AE%A1%E7%AE%97 %E6%96%B9%E6%B3%95%E5%AF%B9%E6%AF%94/)

### 6. Processing of Ajax requests

For the "load more" case, Ajax is used to transfer a lot of data.

It works by: after loading the source code of the web page from the url of the web page, the JavaScript program is executed in the browser. These programs will load more content and "fill" it into the web page. This is why if you go directly to the url of the page itself, you will not find the actual content of the page.

Here, if you use Google Chrome to analyze the "request" corresponding link (method: right click → review element → Network → empty, click "load more", the corresponding GET link appears to find Type as text / html, click, view get parameter Or copy the Request URL), the loop process.

* If there is a page before "Request", the first page will be derived based on the analysis of the URL of the previous step. And so on, grab the data of the Ajax address.
* Regularly match the returned json format data (str). In the json format data, the unicode_escape encoding of the form '\\uxxxx' is converted to the unicode encoding of u'\uxxxx'.

### 7. Automated testing tool Selenium

Selenium is an automated testing tool. It can implement a series of operations such as character padding, mouse clicks, get elements, page switching, etc. In short, Selenium can do whatever the browser can do.

Here is a list of the code for using the selenium to dynamically crawl the fare information of [Zonai] (http://flight.qunar.com/) after a given list of cities.

Reference project: [Web crawler Selenium uses proxy login: crawl to where to go] (https://github.com/lining0806/PythonSpiderNotes/blob/master/QunarSpider)

### 8. Verification code recognition

For the case where the website has a verification code, we have three options:

* Use the proxy to update the IP.
* Log in using cookies.
* Identification codes.

I have already talked about using the proxy and logging in with a cookie. Let's talk about the verification code identification.

The open source Tesseract-OCR system can be used to download and identify the captcha image, and the recognized characters are transmitted to the crawler system for simulated landing. Of course, the verification code picture can also be uploaded to the coding platform for identification. If it is unsuccessful, you can update the verification code identification again until it is successful.

Reference project: [Verification Code Recognition Project First Edition: Captcha1] (https://github.com/lining0806/PythonSpiderNotes/blob/master/Captcha1)

**Crawling has two issues to be aware of: **

* How to monitor the update of a series of websites, that is, how to carry out incremental crawling?
* How to implement distributed crawling for massive data?

## Analysis

After the crawl is to analyze the content of the crawl, what content you need, from which to extract the relevant content.

Common analysis tools are [regular expressions] (http://deerchao.net/tutorials/regex/regex.htm), [BeautifulSoup](http://www.crummy.com/software/BeautifulSoup/), [lxml ](http://lxml.de/) and so on.

## storage

After analyzing what we need, the next step is to store it.

We can choose to save the text file, or you can choose to save it to [MySQL] (http://www.mysql.com/) or [MongoDB] (https://www.mongodb.org/) database.

**Storage has two issues to be aware of: **

* How to carry out web page weighting?
* In what form is the content stored?


## Scrapy

Scrapy is an open source Python crawler framework based on Twisted and is widely used in industry.

For related content, please refer to [Building based on Scrapy web crawler] (http://www.lining0806.com/%E5%9F%BA%E4%BA%8Escrapy%E7%BD%91%E7%BB%9C%E7% 88%AC%E8%99%AB%E7%9A%84%E6%90%AD%E5%BB%BA/), also given the [WeChat Search] described in this article (http://weixin.sogou .com/weixin) Climb the project code for everyone to learn as a reference.

Reference project: [Retrieving WeChat search results using Scrapy or Requests recursively] (https://github.com/lining0806/PythonSpiderNotes/blob/master/WechatSearchProjects)

## Robots Agreement

A good web crawler, first of all, must comply with the **Robots Agreement**. The full name of the Robots protocol (also known as crawler protocol, robot protocol, etc.) is the "Robots Exclusion Protocol". The website uses the Robots protocol to tell search engines which pages can be crawled and which pages cannot be crawled.

Place a robots.txt text file (such as https://www.taobao.com/robots.txt) in the root directory of the website, which can specify the pages that different web crawlers can access and the pages that are forbidden to access. The specified pages are regular. Expression representation. Before collecting the website, the web crawler first obtains the text file of the robots.txt, then parses the rules into it, and then collects the data of the website according to the rules.

### 1. Robots Protocol Rules

User-agent: Specify which crawlers to take effect
Disallow: Specify a URL that is not allowed to be accessed
Allow: Specify the URLs that are allowed to be accessed

Note: One English should be capitalized, the colon is in English, there is a space after the colon, and "/" stands for the entire site.

### 2. Robots protocol example

Prohibit all robot access
User-agent: *
Disallow: /
Allow all robots to access
User-agent: *
Disallow:
Prohibit specific robot access
User-agent: BadBot
Disallow: /
Allow specific robot access
User-agent: GoodBot
Disallow:
Block access to specific directories
User-agent: *
Disallow: /images/
Allow access to specific directories only
User-agent: *
Allow: /images/
Disallow: /
Block access to specific files
User-agent: *
Disallow: /*.html$
Allow access to specific files only
User-agent: *
Allow: /*.html$
Disallow: /
Send feedback
History
Saved
Community
