from bottle import Bottle, route, run, redirect, request, static_file

import urllib2, simplejson, json, gzip, StringIO

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

@route('/static/<filename:path>')
def send_static(filename):
    return static_file(filename, root='../simpleHome/assets/')

run(host='localhost', port=8080, reloader=True)
