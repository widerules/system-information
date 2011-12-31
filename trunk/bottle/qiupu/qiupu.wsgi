import bottle, os, sys

os.chdir(os.path.dirname(__file__))
sys.path+=[os.path.dirname(__file__)]

import qiupu

application=qiupu.app
