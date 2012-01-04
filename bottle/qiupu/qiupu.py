from bottle import Bottle, route, run, redirect, request, static_file, debug, post, get, urllib, template

import urllib2, json, gzip, StringIO, redis, hashlib, base64

app = Bottle()
   

#example: 
# http://192.168.5.136:8080/kendoui/examples/index.html
# http://192.168.5.136:8080/qiupu/login.html
@app.post('/<filename:path>')
@app.get('/<filename:path>')
def send_static(filename):
    return static_file(filename, root='')

@app.route('/redis')
def redis():
    r = redis.StrictRedis(host='localhost', port=6379, db=0)
    r.set('foo','bar')
    r.set('foo1','bar1')
    print 'dbsize:%s' %r.dbsize()
    print 'key value is:%s' %r.get('fool')
    return r.get('fool')

@app.route('/json')
def testjson():
    data = [{'Id': 1, 'city': 'beijing', 'area': 16800, 'population': 1600}, {'Id': 2, 'city': 'shanghai', 'area': 6400, 'population': 1800}]
    return wrapResults(data)

def wrapResults(results):
    callback = getCallback()
    if len(callback) > 0:
	ret = '%s(%s);'%(callback, json.dumps({'results':results}))
    else:
	ret = {'results':results}
    print ret
    return ret

def getCallback():
    callback = ''
    try:
	for key in request.params.keys():
	    if key == 'callback' or key == 'jsonp' or key == 'jsonpcallback':
		callback = request.params[key]
    except:
	pass
    return callback


@app.get('/login') # or @route('/login')
def login_form():
    return '''<form method="POST">
                <input name="username"     type="text" />
                <input name="password" type="password" />
                <input name="login" type="submit" />
              </form>'''

apiurl = 'http://api.borqs.com/'
src = 'appSecret1userappSecret1'
strSrc = base64.b64encode(hashlib.md5(src).digest())

@app.post('/login_post.action')
@app.post('/login') # or @route('/login', method='POST')
def login_submit():
    name     = request.forms.get('username')
    password = request.forms.get('password')
    key = hashlib.md5()
    key.update(password)
    data = {'login_name':name,'password':key.hexdigest()}
    loginurl = apiurl + 'account/login'
    data = findUrlGzip(loginurl, data)
    if (data.find('error_code') > -1):
	return data
    else:
	jdata = json.loads(data)
	data = {'user':jdata["user_id"], 'ticket':jdata["ticket"], 'appid':'1', 'sign':strSrc}
	followers = findUrlGzip(apiurl + 'follower/show', data)
	data = json.loads(followers)
	ret = '['
	for follower in data:
	    jf = {'user_id':follower['user_id'], 'contact_info':follower['contact_info'], 'followers_count':follower['followers_count'], 'status':follower['status'], 'friends_count':follower['friends_count'], 'display_name':follower['display_name'], 'gender':follower['gender'], 'image_url':follower['image_url']}
	    ret += json.dumps(jf) + ','
	ret = ret.rstrip(',') + ']'
	return {'results':ret}

def findUrlGzip(url):
    return findUrlGzip(url, '')

def findUrlGzip(url, data):
    f = urllib.urlopen(url, urllib.urlencode(data))
    compresseddata = f.read()
    compressedstream = StringIO.StringIO(compresseddata)
    gzipper = gzip.GzipFile(fileobj=compressedstream)
    try:
	data = gzipper.read()
	return data
    except:
	return compresseddata


if (__name__ == '__main__'):
    debug(True)
    run(app, host='192.168.5.136', port=8080, reloader=True)
