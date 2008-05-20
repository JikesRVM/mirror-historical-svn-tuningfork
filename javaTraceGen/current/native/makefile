# Native support for the NativeLogger tuningfork trace library

# This makefile requires:
#  ARCH to be set to win32 on a windows machine
#  ARCH to be set to gumstix when cross-compiling for gumstix.  The
#   gumstix tools must be in the path.
#  Otherwise OSTYPE should be set to either Linux or Darwin (MacOS X).
#
#  JDK to be set to a JDK that has a working javah and a jni.h

# Note that, by design, it should be possible for javah and jni to
# come from any JDK, not necessarily one that will execute on the
# target platform.

# If the above assumptions are met, no editing of anything in this
# makefile should be required.


# To use with a JVM, use -Djava.library.path=<path> where the .dll/.so lives in <path>
#

################################################################################################
# HACK: hardcode for Linux since right now that is the only meaningfully supported platform for natives
OSTYPE=Linux
JDK=/opt/ibm/java2-i386-50
################################################################################################


MYNATIVE=OSBridge

CLASS=com.ibm.tuningfork.tracegen.impl.$(MYNATIVE)
CXX_PROJECT=../../JavaTraceGenerationLibrary
CLASSFILE=../bin/$(subst .,/,$(CLASS)).class
HFILE=$(subst .,_,$(CLASS)).h


ifeq ($(ARCH),win32)
PREF=
SUF=compiledOk
else

ifeq ($(OSTYPE),Darwin)
JDK=/System/Library/Frameworks/JavaVM.framework
SUF=jnilib
else
SUF=so
endif

ifeq ($(ARCH),gumstix)
PREF=gumstix/lib
else
PREF=lib
endif

endif

TARGET=$(PREF)TuningForkNativeTraceSupport.$(SUF)

all: $(TARGET)

clean:
		-rm -f $(TARGET) $(HFILE) *.obj *.pdb *.ilk *.o *.exp *.lib *.so gumstix/*

$(CLASSFILE):

$(HFILE):       $(CLASSFILE)
		$(JDK)/bin/javah -classpath ../bin $(CLASS)
		touch $@

ifeq ($(ARCH),win32)

$(TARGET):      $(MYNATIVE).obj
		touch $(TARGET)

$(MYNATIVE).obj:        $(MYNATIVE).cpp $(HFILE)
		cl -c -D_MT -D_DLL -MD -D_WINSOCKAPI_ -DWIN32 -Gs -GF -Zm400 /Zi $(MYNATIVE).cpp /I$(JDK)/include /I$(CXX_PROJECT)

else
$(TARGET):      $(MYNATIVE).c $(HFILE)
ifeq ($(ARCH),gumstix)
		arm-linux-g++ -fPIC -shared -o$@ -I$(JDK)/include -DLINUX $(MYNATIVE).cpp -I$(CXX_PROJECT) -L$(CXX_PROJECT)/Gumstix -lTuningForkTraceGeneration
else
ifeq ($(OSTYPE),Darwin)
		gcc -c -I$(JDK)/include -DDARWIN -DSPECIALIZE_RELPROD -DSPECIALIZE_AND -DSPECIALIZE_OR -DSMALL_NODES -fomit-frame-pointer -fno-common $(MYNATIVE).c -I$(CXX_PROJECT) -L$(CXX_PROJECT)/Debug
		libtool -dynamic -framework JavaVM -o $@ -lc $(MYNATIVE).o
else
ifeq ($(OSTYPE),Linux)
		gcc -shared -m32 -o$@ -I$(JDK)/include -DLINUX -fPIC $(MYNATIVE).c -I$(CXX_PROJECT) -L$(CXX_PROJECT)/Debug 
else
		@echo "Unsupported machine/OS environment " $(OSTYPE) " on " $(ARCH)
endif
endif
endif
endif
