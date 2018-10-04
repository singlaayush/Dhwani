# Dhwani (Alpha)
### Concept

https://dhwani.xyz

### About this version

This is an experimental version of the Dhwani App made by Ayush Singla, which now implements the Retrofit API for networking in lieu of a modified version of the HTTP Library made by Ananay Arora. This version also implements the Settings pages and sports some UI changes. Only name detection is currently supported in this version - the pre-trained doorbell model was removed. 

The main reason I shifted to Retrofit was so I could clean up the code, since using it allowed me to take a more modular approach with networking calls. This made the app significantly more lightweight and I recorded a substantial increase in the training of the model. There were even fewer crashes while training the model, which was a welcome addition.

### Compatibility

Works for Android arm-architecture devices with Oreo 8.0 and below. Does not work on x86 devices yet.

To access the main demo seen sporting Doorbell detection and name detection, head to this link for the guide and app: [Dhwani Demo - Google Drive](https://drive.google.com/drive/folders/0B0XtwKSifjladmpmX0p1bXlWQTg)

To try this version out, download the app here: [Dhwani-Retrofit.apk - Google Drive](https://drive.google.com/open?id=1mnachFoIawXC6QD_SeyEBC5DEVVhaYCC).

Have a good day!
