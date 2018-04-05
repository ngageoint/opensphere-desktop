Building GDAL on Mac OS for OpenSphere
======================================

1. Download gdal-1.9.2 source archive from [http://download.osgeo.org/gdal/gdal-1.9.2.tar.gz](http://)

1. Download and install supporting frameworks (available at [http://www.kyngchaos.com/software/frameworks](http://))

        GEOS_Framework-3.3.8-2.dmg
        PROJ_Framework-4.8.0-1.dmg
        SQLite3_Framework-3.7.17-2.dmg
        UnixImageIO_Framework-1.4.3.dmg

1. In the gdal-1.9.2 directory, run `autogen.sh` to create the `configure` script
1. Patch the configure script to fix the check for Java JNI headers

        --- orig/gdal-1.9.2/configure	2016-05-05 09:56:58.000000000 -0600
        +++ gdal-1.9.2/configure	2016-05-05 08:56:44.000000000 -0600
        @@ -27453,8 +27453,13 @@
                     JAVA_INC="-I$JAVA_HOME/include -I$JAVA_HOME/include/freebsd"
                     { $as_echo "$as_me:${as_lineno-$LINENO}: result: yes" >&5
         $as_echo "yes" >&6; }
        +        elif test -d "$with_java/include/darwin"; then
        +            JAVA_HOME="$with_java"
        +            JAVA_INC="-I$JAVA_HOME/include -I$JAVA_HOME/include/darwin"
        +            { $as_echo "$as_me:${as_lineno-$LINENO}: result: yes" >&5
        +$as_echo "yes" >&6; }
                 else
        -            as_fn_error $? "\"Cannot find $with_java/include/linux or solaris or freebsd directory.\"" "$LINENO" 5
        +            as_fn_error $? "\"Cannot find $with_java/include/linux or solaris or freebsd or darwin directory.\"" "$LINENO" 5
                 fi
             else
                 as_fn_error $? "\"Cannot find $with_java/include directory.\"" "$LINENO" 5

1. Configure gdal build (*change JDK directory name if necessary*)

        ./configure --with-threads --disable-static --without-grass --with-jasper=/Library/Frameworks/UnixImageIO.framework/unix --with-libtiff=/Library/Frameworks/UnixImageIO.framework/unix --with-jpeg=/Library/Frameworks/UnixImageIO.framework/unix --with-gif=/Library/Frameworks/UnixImageIO.framework/unix --with-png=/Library/Frameworks/UnixImageIO.framework/unix --with-geotiff=/Library/Frameworks/UnixImageIO.framework/unix --with-sqlite3=/Library/Frameworks/SQLite3.framework/unix --with-odbc --with-pcraster=internal --with-geos=/Library/Frameworks/GEOS.framework/unix/bin/geos-config --with-static-proj4=/Library/Frameworks/PROJ.framework/unix --with-expat=/usr/local --with-curl --with-python --with-macosx-framework --with-java=/Library/Java/JavaVirtualMachines/jdk1.8.0_74.jdk/Contents/Home CFLAGS="-Os -arch i386 -arch x86_64" CXXFLAGS="-Os -arch i386 -arch x86_64" LDFLAGS="-arch i386 -arch x86_64"

1. Make a symlink to work around a header file include path issue

        cd /Library/Frameworks/UnixImageIO.framework/unix/include
        ln -s . UnixImageIO

1. Fix the options file in the java bindings directory (`gdal-1.9.2/swig/java/java.opt`) (*change JDK directory name if necessary*):

        JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_74.jdk/Contents/Home
        JAVA_INCLUDE=-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin

1. Fix the java bindings makefile (`gdal-1.9.2/swig/java/GNUmakefile`)

        --- orig/gdal-1.9.2/swig/java/GNUmakefile	2012-10-08 18:58:27.000000000 -0600
        +++ gdal-1.9.2/swig/java/GNUmakefile	2016-05-05 13:17:43.000000000 -0600
        @@ -46,7 +46,7 @@
        
         build: generate ${JAVA_OBJECTS} ${JAVA_MODULES}
         ifeq ($(HAVE_LIBTOOL),yes)
        -	-cp ./.libs/*.so ./
        +	-cp ./.libs/*.dylib ./
         endif
         	ant
        

1. Build and install the GDAL framework

        make
        make BINDINGS=java swig-target
        make install

1. Copy the resulting libraries into the `core/lib/macosx/x86_64 directory`:

        /Library/Frameworks/GDAL.framework/Versions/1.9/GDAL (rename to libgdal.dylib)
        gdal-1.9.2/swig/java/libgdalconstjni.dylib
        gdal-1.9.2/swig/java/libgdaljni.dylib
        gdal-1.9.2/swig/java/libogrjni.dylib
        gdal-1.9.2/swig/java/libosrjni.dylib
        /Library/Frameworks/PROJ.framework/Versions/Current/PROJ (rename to libproj.dylib)
