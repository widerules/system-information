from bottle import Bottle, route, run, redirect, request, static_file, debug

import urllib2, json, gzip, StringIO, redis

app = Bottle()

@route('/')
def ping():
    s = 'pang: '
    for key in request.params.keys():
	s += key + ', ' + request.params[key] + '\n'
    return s

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

@route('/<filename:path>')
def send_static(filename):
    return static_file(filename, root='kendoui/')

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
	return '%s(%s);'%(callback, json.dumps({'results':results}))
    else:
	return {'results':results}

def getCallback():
    callback = ''
    try:
	for key in request.params.keys():
	    if key == 'callback' or key == 'jsonp' or key == 'jsonpcallback':
		callback = request.params[key]
    except:
	pass
    return callback

if (__name__ == '__main__'):
    debug(True)
    run(host='192.168.5.136', port=8080, reloader=True)
