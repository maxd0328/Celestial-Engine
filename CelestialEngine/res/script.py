from celestial.core import CEObject
from celestial.render import CERenderPacket
from celestial.render import CEUpdatePacket
from mod.celestial.texture import FragMaterialModifier

# Available function arguments:
#    packet (CERenderPacket/CEUpdatePacket) - Current render/update packet
#    obj (CEObject) - Object containing script

# preRender()
#    Called once per root object before render
def preRender():
	# Code
	return

# render()
#    Called once per object instance during render
def render():
	# Code
	return

# postRender()
#    Called once per root object after render
def postRender():
	# Code
	return

# update0()
#    Called once per object instance before pre-render
def update0():
	return

# update1()
#    Called once per root object after post-render
def update1():
	# Code
	return
