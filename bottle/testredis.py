import redis
r = redis.StrictRedis(host='localhost', port=6379, db=0)
r.set('foo','bar')
r.set('foo1','bar1')
print 'dbsize:%s' %r.dbsize()
print 'key value is:%s' %r.get('fool')
