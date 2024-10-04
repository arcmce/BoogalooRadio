to get it to work debug mode for now. comment out build.gradle buildtypes - release section

to build
up rev in build.gradle
android studio - Build - Generate Signed Bundle/ APK - Android App Bundle - Next - Next - 'Release' - Build

upload to internal app sharing, then download from link
https://play.google.com/console/u/3/internal-app-sharing/?pli=1

to enable on device https://www.xda-developers.com/google-play-store-developer-setting-enable-internal-app-sharing/
  


to generate apks for installing locally -
cd /Users/arcmce/Documents/git/BoogalooRadio/app/release
bundletool build-apks --bundle=app-release.aab --output=release.apks --ks=/Users/arcmce/boog_play/keystores/upload-keystore --ks-key-alias=upload
optional --overwrite
enter pwd G10_





--------
test build.gradle:

apply plugin: 'com.android.application'

apply plugin: 'kotlin-android'

apply plugin: 'kotlin-android-extensions'

android {
compileSdkVersion 30
defaultConfig {
applicationId "com.arcmce.boogaloo"
minSdkVersion 21
targetSdkVersion 30
versionCode 4
versionName "1.02"
testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}
buildTypes {
debug {
}

        release {
//            signingConfig signingConfigs.debug
//            debuggable true
applicationVariants.all { variant ->
variant.outputs.all {
outputFileName = "${applicationName}_${variant.buildType.name}_${defaultConfig.versionName}.apk"
}
}
}
}
kotlinOptions {
jvmTarget = "1.8"
}
}


dependencies {
implementation fileTree(dir: 'libs', include: ['*.jar'])
implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
implementation 'androidx.appcompat:appcompat:1.2.0'
implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
implementation 'androidx.legacy:legacy-support-v4:1.0.0'
implementation 'com.google.android.material:material:1.0.0'
testImplementation 'junit:junit:4.12'
androidTestImplementation 'androidx.test.ext:junit:1.1.1'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.1.0'
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1"
implementation "com.android.volley:volley:1.1.1"
implementation "androidx.fragment:fragment:1.3.4"
implementation("androidx.fragment:fragment-ktx:1.3.4")
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'com.google.code.gson:gson:2.8.5'
implementation 'com.github.bumptech.glide:glide:4.10.0'
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1'
}

repositories {
jcenter()
}

--------