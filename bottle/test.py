from bottle import Bottle, route, run, redirect, request, static_file

import urllib2, simplejson, json, gzip, StringIO, redis

app = Bottle()

@route('/')
@route('/test')
def test():
    something = findUrlGzip('http://api.borqs.com/user/show?users=43')
    return something
    #return json.loads(something)

def findUrlGzip(url):
    request = urllib2.Request(url)
    request.add_header('Accept-encoding', 'gzip')
    opener = urllib2.build_opener()
    f = opener.open(request)
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
def json():
    data = [{'Id': 1, 'city': 'beijing', 'area': 16800, 'population': 1600}, {'Id': 2, 'city': 'shanghai', 'area': 6400, 'population': 1800}]
    x = simplejson.dumps(data)
    return x

run(host='192.168.5.136', port=8080, reloader=True)
