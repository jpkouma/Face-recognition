LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
OPENCV_INCLUDE1 := /home/jipe/workspace/OpenCV/include
OPENCV_INCLUDE2 := /home/jipe/workspace/OpenCV/include/opencv

LOCAL_C_INCLUDES +=$(OPENCV_INCLUDE1)
LOCAL_C_INCLUDES +=$(OPENCV_INCLUDE2)
LOCAL_MODULE := opencv
LOCAL_SRC_FILES := libopencv.so 
include $(PREBUILT_SHARED_LIBRARY)


# Here we give our module name and source file(s)
include $(CLEAR_VARS)
OPENCV_INCLUDE1 := /home/jipe/workspace/OpenCV/include
OPENCV_INCLUDE2 := /home/jipe/workspace/OpenCV/include/opencv

LOCAL_C_INCLUDES +=$(OPENCV_INCLUDE1)
LOCAL_C_INCLUDES +=$(OPENCV_INCLUDE2)

OPENCV_LIB :=  /home/jipe/workspace/FaceRecog/jni

LOCAL_MODULE := FaceRecog
LOCAL_LDFLAGS += -L$(OPENCV_LIB)
LOCAL_LDLIBS += -llog -lopencv
LOCAL_SRC_FILES := FaceRecog.c   
include $(BUILD_SHARED_LIBRARY)
#-----------------------------------------------------------------------------------------------------------------
