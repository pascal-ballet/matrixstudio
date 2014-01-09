#!/bin/sh
#Creates the DMG files for MatrixStudio in 32bits and 64bits.

VERSION=`cat ../bin/matrixstudio/ui/version.properties | grep version | awk -F= '{ print $2 }'`
DATE=`cat ../bin/matrixstudio/ui/version.properties | grep date | awk -F= '{ print $2 }'`

DIST=built


# Gets the correct tag depending on distribution.
if [ "$VERSION" = "daily" ]; then
	TAG=$DATE
else
	TAG=$VERSION
fi

if ( test -d $DIST/macosx32 ) then
	echo "Creating $DIST/MatrixStudio-$TAG-macosx-32bits.dmg"
	if ( test -f $DIST/MatrixStudio-$TAG-macosx-32bits.dmg ) then
 		rm  $DIST/MatrixStudio-$TAG-macosx-32bits.dmg
 	fi
	hdiutil create -srcfolder $DIST/macosx32 -volname MatrixStudio $DIST/MatrixStudio-$TAG-macosx-32bits.dmg
fi

if ( test -d $DIST/macosx64 ) then
	echo "Creating $DIST/MatrixStudio-$TAG-macosx-64bits.dmg"
	if ( test -f $DIST/MatrixStudio-$TAG-macosx-64bits.dmg ) then
 		rm  $DIST/MatrixStudio-$TAG-macosx-64bits.dmg 
 	fi
	hdiutil create -srcfolder $DIST/macosx64 -volname MatrixStudio $DIST/MatrixStudio-$TAG-macosx-64bits.dmg
fi
