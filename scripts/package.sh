#!/bin/bash
die () {
    echo >&2 "$@"
    exit 1
}

[ "$#" -eq 1 ] || die "1 argument required, $# provided"

VERSION=$1
cd ..

# clean output dir
rm -f scripts/package/win/admin/webapps/ROOT.war
rm -Rf scripts/package/win/viewer
rm -f scripts/package/win/htm-admin-$VERSION.zip
rm -f scripts/package/win/htm-viewer-$VERSION.zip

sed -i "s/val buildVersion = \"[^\"]*\"/val buildVersion = \"${VERSION}\"/" project/HtmBuild.scala 
sbt clean package
sbt "project lib" publishLocal
cp htm-admin/target/scala-2.10/htm-admin_2.10-$VERSION.war scripts/package/win/admin/webapps/ROOT.war
mkdir scripts/package/win/admin/resourcebundle
cp -r resourcebundles/socal2014/* scripts/package/win/admin/resourcebundle/
cd scripts/package/win/
zip -r htm-admin-$VERSION.zip admin "Start Admin.lnk"
cd ../../../
cd play
sed -i "s/val appVersion = \"[^\"]*\"/val appVersion = \"${VERSION}\"/" project/HtmPlayBuild.scala
sed -i "s/app.version=\"[^\"]*\"/app.version=\"${VERSION}\"/" htm-viewer-play/conf/application.conf
sbt "project htm-viewer-play" dist
cp htm-viewer-play/target/universal/htm-viewer-play-$VERSION.zip ../scripts/package/win/
cd ../scripts/package/win/
unzip htm-viewer-play-$VERSION.zip
rm htm-viewer-play-$VERSION.zip
mv htm-viewer-play-$VERSION viewer
zip -r htm-viewer-$VERSION.zip viewer "Start Viewer.lnk"
