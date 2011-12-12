from bottle import Bottle, route, run, redirect, request

import urllib2, simplejson, json, gzip, StringIO

app = Bottle()

@route('/')
@route('/test')
def test():
    #req = urllib2.Request('http://api.borqs.com/user/show?users=43')  
    #sthdecode = something.decode('utf-8', 'ignore')
    #print json.dumps(sthdecode.encode('utf-8'))
    something = findUrlGzip('http://api.borqs.com/user/show?users=43')
    return json.dumps(something)

def findUrlGzip(url):
    request = urllib2.Request(url)
    request.add_header('Accept-encoding', 'gzip')
    opener = urllib2.build_opener()
    f = opener.open(request)
    isGzip = f.headers.get('Content-Encoding')
    #print isGzip
    if isGzip :
        compresseddata = f.read()
        compressedstream = StringIO.StringIO(compresseddata)
        gzipper = gzip.GzipFile(fileobj=compressedstream)
        data = gzipper.read()
    else:
        data = f.read()
    return data

run(host='localhost', port=8080)
