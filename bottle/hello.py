from bottle import Bottle, route, run

app = Bottle()

@route('/')
@route('/hello/<name>')
def greet(name='Stranger'):
    return 'Hello %s, how are you?' % name

run(host='localhost', port=8080)
