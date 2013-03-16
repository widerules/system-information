# A simple test for the minimal standard C++ library
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := mongoose
LOCAL_SRC_FILES := mongoose.c main.c
include $(BUILD_EXECUTABLE)
