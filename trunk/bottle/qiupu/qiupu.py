from bottle import Bottle, route, run, redirect, request, static_file, debug, post, get, urllib, template

import urllib2, json, gzip, StringIO, redis, hashlib, base64

app = Bottle()

@route('/')
def ping():
    s = 'pang: '
    for key in request.params.keys():
	s += key + ', ' + request.params[key] + '\n'
    return s

@route('/md5')
def md5base64():
    strid = 'emails'
    appsec = 'appSecret1'
    data = appsec + strid + appsec
    key = hashlib.md5()
    key.update(data)
    md5string = key.digest()
    b64 = base64.b64encode(md5string)
    return data + ', ' + md5string + ', ' + b64
    

@route('/test')
def test():
    something = findUrlGzip('http://api.borqs.com/user/show?users=43')
    return something
    #return simplejson.dumps(something)

def findUrlGzip(url):
    request0 = urllib2.Request(url)
    request0.add_header('Accept-encoding', 'gzip')
    opener = urllib2.build_opener()
    f = opener.open(request0)
    isGzip = f.headers.get('Content-Encoding')
    if isGzip :
        compresseddata = f.read()
        compressedstream = StringIO.StringIO(compresseddata)
        gzipper = gzip.GzipFile(fileobj=compressedstream)
        data = gzipper.read()
    else:
        data = f.read()
    return data

#example: 
# http://192.168.5.136:8080/kendoui/examples/index.html
# http://192.168.5.136:8080/qiupu/login.html
@post('/<filename:path>')
@get('/<filename:path>')
def send_static(filename):
    return static_file(filename, root='')

@route('/redis')
def redis():
    r = redis.StrictRedis(host='localhost', port=6379, db=0)
    r.set('foo','bar')
    r.set('foo1','bar1')
    print 'dbsize:%s' %r.dbsize()
    print 'key value is:%s' %r.get('fool')
    return r.get('fool')

@route('/json')
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


@get('/login') # or @route('/login')
def login_form():
    return '''<form method="POST">
                <input name="username"     type="text" />
                <input name="password" type="password" />
                <input name="login" type="submit" />
              </form>'''

@post('/qiupu/login_post.action')
@post('/login') # or @route('/login', method='POST')
def login_submit():
    name     = request.forms.get('username')
    password = request.forms.get('password')
    key = hashlib.md5()
    key.update(password)
    data = {'login_name':name,'password':key.hexdigest()}
    apiurl = 'http://api.borqs.com/account/login'
    f = urllib.urlopen(apiurl, urllib.urlencode(data))
    compresseddata = f.read()
    compressedstream = StringIO.StringIO(compresseddata)
    gzipper = gzip.GzipFile(fileobj=compressedstream)
    data = gzipper.read()
    #output = template('make_table', rows=data)
    if (data.find('error_code')):
	return data
    else:
	return data

def user_form():
    return ''


if (__name__ == '__main__'):
    debug(True)
#    run(host='192.168.5.136', port=8080, reloader=True)
