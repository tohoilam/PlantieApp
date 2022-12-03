
# PlantieApp
 
## Overview

Plantie is a flower detection application designed to be run on Android. 
With Plantie, you may:
* Point your phone to any flower and get their information immediately
* Save all the plants you captured with the info
* Register and save the plants on cloud
* View your capture history on map

The application is developed with Kotlin on Android Studio.
The backend is powered by Amazon Web Services Amplify.
The object detection model is forked from TensorFlow.

## Setup

1. Install Node and npm
2. Install latest Amplify CLI with `npm install -g @aws-amplify/cli`
3. Run `amplify pull --appId d2s9xj50g7zouc --envName dev`
    - Choose `AWS access keys`
    - Enter Access Key & Secret Key (available upon request)
    - Choose region `ap-southeast-1`
    - `Android Studio`
    - `android`
    - `app/src/main/res`
    - `Yes`
4. Open emulator and test the app

Or, alternatively, you may download the apk directly and run on your Android device.

## Functionalities

**1. Capture**
<img src="https://drive.google.com/uc?export=download&id=1qV3LWYWAWuUwqeUvgKhKwY1IpcqTPc03" width="200"/>
User can take photo of flowers here with their camera. The photo will be stored to gallery when the user clicked on the capture button in the bottom. Before taking photos, a category prediction will be displayed to indicate what type of flowers it is predicted to be.


**2. Browse**
<img src="https://drive.google.com/uc?export=download&id=1-YDTKJZGISQsv4OBf-cAX-ZHwAqoqbr_" width="200"/><img src="https://drive.google.com/uc?export=download&id=1-T-M3F5ZswcXblimmxY2sUjkkx-Ak_Wp" width="200"/><img src="https://drive.google.com/uc?export=download&id=1-RB-h77AMfqBpGQbUO1qlwUaAlpJfDNk" width="200"/>
User will be able to see the history of photos taken here. The app will synchronize the list of images with the cloud when the user is logged in and there is an internet connection. When clicked on a photo, detail information of the plant will be displayed.


**3. Map**
<img src="https://drive.google.com/uc?export=download&id=1-Imm0w8vv9CM-p8cFgNnUIPYHEWdbCbX" width="200"/>
A map-based interface will be shown with images pinned on the map, which is the exact location the user took the photo. When clicked on it the info page will also be displayed.


**4. Login/Register**
<img src="https://drive.google.com/uc?export=download&id=1-gsT-TiN178sHfZ0hfKAH1mZ4loa5r7g" width="200"/>
<img src="https://drive.google.com/uc?export=download&id=1-malcG1yrev3G1hFHWA1m5HJJcoDaXtL" width="200"/>
User will be able to login and register their account here. After logging in, user will be able to save their images to cloud to prevent lost of data.
