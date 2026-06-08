export default {
  "expo": {
    "name": "EV-Go",
    "slug": "capstone-project",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/images/icon.png",
    "scheme": "com.evgo.mobile",
    "userInterfaceStyle": "automatic",
    "newArchEnabled": true,
    "ios": {
      "supportsTablet": true
    },
    "android": {
      "adaptiveIcon": {
        "backgroundColor": "#E6F4FE",
        "foregroundImage": "./assets/images/android-icon-foreground.png",
        "backgroundImage": "./assets/images/android-icon-background.png",
        "monochromeImage": "./assets/images/android-icon-monochrome.png"
      },
      "config": {
        "googleMaps": {
          "apiKey": process.env.EXPO_PUBLIC_MAP_API_KEY
        }
      },
      "edgeToEdgeEnabled": true,
      "predictiveBackGestureEnabled": false,
      "package": "com.evgoteam.evgo",
      "usesCleartextTraffic": true,
      "permissions": [
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_FINE_LOCATION"
      ],
      "googleServicesFile": "./google-services.json"
    },
    "web": {
      "output": "static",
      "favicon": "./assets/images/favicon.png",
      "bundler": "metro"
    },
    "plugins": [
      "expo-router",
      [
        "expo-splash-screen",
        {
          "image": "./assets/images/splash-icon.png",
          "resizeMode": "contain",
          "backgroundColor": "#ffffff",
          "dark": {
            "backgroundColor": "#000000"
          },
          "duration": 1000,
          "fade": true
        }
      ],
      "expo-secure-store",
      [
        "expo-location",
        {
          "locationAlwaysAndWhenInUsePermission": "Allow EV-Go to use your location to find nearby charging stations.",
          "isAndroidBackgroundLocationEnabled": false,
          "isAndroidForegroundServiceEnabled": false
        }
      ],
      [
        "expo-notifications",
        {
          "icon": "./assets/images/icon.png",
          "color": "#33404F",
          "sounds": []
        }
      ]
    ],
    "experiments": {
      "typedRoutes": true,
      "reactCompiler": true
    },
    "extra": {
      "router": {},
      "eas": {
        "projectId": "4b0cc3f5-45b9-4488-a216-533ae314478d"
      }
    },
    "runtimeVersion": {
      "policy": "appVersion"
    },
    "updates": {
      "url": "https://u.expo.dev/4b0cc3f5-45b9-4488-a216-533ae314478d"
    },
    "owner": "sherllgen"
  }
}
