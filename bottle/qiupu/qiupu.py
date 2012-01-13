from bottle import Bottle, route, run, redirect, request, static_file, debug, post, get, urllib, template, response

import urllib2, json, gzip, StringIO, redis, hashlib, base64

app = Bottle()
   

#example: 
# http://192.168.5.136:8080/kendoui/examples/index.html
# http://192.168.5.136:8080/qiupu/login.html
@app.post('/<filename:path>')
@app.get('/<filename:path>')
def send_static(filename):
        name     = request.forms.get('username')
        password = request.forms.get('password')
	if (name != None) and (password != None):
		return login_submit()
	else:
		return static_file(filename, root='')

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

apiurl = 'http://api.borqs.com/'
def getSrc(src):
	return 'appSecret1' + src + 'appSecret1'

def md5b64(src):
	return base64.b64encode(hashlib.md5(src).digest())


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
	ip = request.environ.get('REMOTE_ADDR')
	if (data.find('error_code') > -1):
		response.set_cookie('ticket', '')
		response.set_cookie('user_id', '')
		print 'user:'+name + ' from ' + ip +' login fail'
		return 'invalid user name or password' 
	else:
		jdata = json.loads(data)
		user_id = jdata['user_id']
		response.set_cookie('ticket', jdata['ticket'])
		response.set_cookie('user_id', user_id)
		print 'user:'+user_id + ' from ' + ip + ' login'
		return static_file('index.html', root='')


def invoke_api(cmd, paras = {}, needLogin = True):
	user_id = request.cookies.get('user_id', '')
	print 'user:'+user_id + ' ' + cmd
	if needLogin:
		src = ''
		for para in paras:
			src += para + ','
		src = src.rstrip(',')
		data = paras #this is a reference, so must put after get src
		data['ticket'] = request.cookies.get('ticket', '')
		data['appid'] = '1'
		data['sign'] = md5b64(getSrc(src))
	else:
		data = paras
	return json.loads(findUrlGzip(apiurl + cmd, data))


@app.route('/show_followers')
def show_followers():
	followers = invoke_api('follower/show', {'user':request.cookies.get('user_id', '')})
	ret = []
	for follower in followers:
		jf = {'display_name':follower['display_name'], 'status':follower['status'], 'gender':follower['gender'], 'image_url':follower['image_url']}
		ret.append(jf)
	return {'results':ret}


@app.route('/show_friends')
def show_friends():
	data = show_circles('')
	circles = ''
	for circle in data:
		circles += str(circle['circle_id']) + ','
	data = invoke_api('friend/show', {'circles':circles.rstrip(',')})
	ret = []
	for follower in data:
		jf = {'display_name':follower['display_name'], 'status':follower['status'], 'gender':follower['gender'], 'image_url':follower['image_url']}
		ret.append(jf)
	return {'results':ret}


@app.route('/show_me')
def show_me():
	return {'results': show_user(request.cookies.get('user_id', ''))}

def show_user(users):
	return invoke_api('user/show', {'users':users}, False)


@app.route('/show_mycircles')
def show_mycircles():
	return {'results':show_circles('')}


def show_circles(circles):
	return invoke_api('circle/show', {'circles':circles})


def parse_data(data):
	ids = ''
	for post in data:
		ids += str(post['source']) + ','
	users = json.loads(findUrlGzip(apiurl + 'user/show', {'users':ids, 'columns':'display_name'}))
	userlist = {}
	for user in users:
		userlist[user['user_id']] = user['display_name']

	ret = []
	for post in data:
		jf = {'author':userlist[post['source']], 'message':post['message'], 'post_id':post['post_id']}
		ret.append(jf)
	return {'results':ret}


@app.route('/show_userstimeline')
def show_userstimeline():
	data = invoke_api('post/userstimeline', {'users':request.cookies.get('user_id', '')})
	return parse_data(data)


@app.route('/show_friendtimeline')
def show_userstimeline():
	data = invoke_api('post/qiupufriendtimeline')
	return parse_data(data)


@app.route('/show_publictimeline') #no login needed
def show_publictimeline():
	data = invoke_api('post/qiupupublictimeline', {'appid':'1', 'cols':'source, message, post_id'}, False)
	return parse_data(data)


if (__name__ == '__main__'):
	debug(True)
	run(app, host='192.168.5.136', port=8080, reloader=True)
