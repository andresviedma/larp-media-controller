# Larp media controller

Desktop / Android application to make easier controlling media in LARPs (Live Action Role-Playing games).
The app shows scenes description and buttons to quickly trigger changes in light bulbs,
music / sound and projectors. It is intended mainly for small scripted
scene LARPs or just "scenarios", but it can potentially be used to control the media in bigger LARPs,
table role games or any other purpose, as long as the devices are connected to a single WiFi spot.

Each LARP is defined with a YAML file structured in scenes, where each scene is described by
a text. It also contains buttons for the events associated to the scene. The buttons trigger
actions in the connected devices.

This is a project intended for my personal needs in some LARPs, and suited to the devices
I own. So it's probably of not much interest for other people with current functionality, but
can be used as a base and extended for other needs and devices.

## Project structure

This is a Kotlin Multiplatform project targeting Android, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## LARP definition file

The LARP definition files are `larp.yaml` files which are searched in these directories:

* Desktop: Any subdirectory inside the user home, e.g. `$HOME/my_larp`
* Android: Any subdirectory inside the internal storage directory, e.g. `[internal storage]/my_larp`
* Any subdirectory inside the main home, in "Usr", "Usr/Larps", "Downloads", "Downloads/rol",
e.g. `Usr/Larps/my_larp`. These directories are pretty arbitrary and according to my personal
computer and phone structure.

A sample config file can be found in `src/commonMain/composeResources/files/test.yaml`

The main parts in the config file are:

* metadata: Just some information about the LARP
* devices: Configuration of the devices which will be used in the LARP, e.g.:
  * assign an internal name to the light bulbs, identified by their internal identifiers
  * set a directory for the music files, if it's different to `music` 
  * set a directory for the music files, if it's different to `sound`
  * configure the SSH connection to the Raspberry PI which will be used for the projector
* presets: Combination of devices configuration identified by a short name. This allows configuring
common actions more easily
* scenes:
  * Title, number and description of the scene
  * Initial settings when the "play" button is pressed, defined as a preset and/or individual setting
of the devices
  * Actions, which will be turned into buttons in the scene GUI, defined as the button text and the
devices settings that will be triggered when the button is pressed (set some light colors, play a
sound, etc.).

## Compatible devices

### Lights: Yeelight

Every Yeelight bulb needs to be previously synchronized with the same WiFi we intend to use. For small LARPs,
that typically involves sharing a mobile connection with a hotspot in the phone, as the bulbs need to
have an active internet connection.

The initialization of the lights must be done just once using the Yeelight official app, but it needs
to be an old version, as we need to [enable LAN control](https://home.yeelight.de/en/support/lan-control/)
in the bulbs and that option is not available in recent versions of the app.

An old version that has enabled the LAN control option is 3.2.06, which can be found
[here](https://www.apkmirror.com/apk/yeelink/yeelight/yeelight-3-2-06-release/yeelight-3-2-06-android-apk-download/).

You might need to use two different mobile phones for the initialization, one sharing the hotspot
and the other using the app.

Documentation of protocol: https://www.yeelight.com/download/Yeelight_Inter-Operation_Spec.pdf
Library we are using (copied and modified for new kotlin+ktor version):
https://github.com/omarmiatello/yeelight

#### Fast guide: How to configure a bulb

* With the "wifi" phone, share the wifi.
* With the controller phone AND Yeelight app version 3.2.06, connect to the phone wifi
* Reset the bulb (turn off and on 5 times with 1-2 seconds each) and register it in the app
* Go the bulb, open the "v" menu inside and enable Local control
* Ready! It should work with the app (once you discover the bulb id, it should not change with
a new initialization)

### Local sound and music

We are using [Korge](https://korge.org/) for the sound player. It's multiplatform, so it should
work on every platform. It has been tested with Desktop (Mac) and Android.

### Remote projector (Raspberry Pi 3+)

We will use a Raspberry PI 3+ with Raspberry OS installed. This OS has VLC player included, we will use
its HTTP interface to send commands to play the videos locally, which will be copied to the PI machine.

On installation, make sure to:
* Enable the SSH interface and set the WiFi configuration.
* Using the GUI, make sure that the sound is directed to HDMI and/or the jack output as desired, and the volume is set to maximum level.
* Using the GUI, make sure in Raspberry Pi configuration that the screen blanking is disabled (it is disabled by default)
* Set black colour with no image as desktop background

Once everything installed and set up, in the Blackberry PI machine:

1. In `$HOME/.config/autostart` create a new file `vlc.desktop` with this content:
```
[Desktop Entry]
Type=Application
Exec=/usr/bin/cvlc --extraintf http --http-password x --fullscreen --no-video-deco --no-video-title-show --image-duration=86400 &
Hidden=false
NoDisplay=false
X-GNOME-Autostart-enabled=true
Name=VLCBack
```
This will make sure that VLC is run on startup, with HTTP interface enabled (using "x" as password).

2. Create a black image with a small size, e.g. 800x600. Save it as `black.png`
```bash
sudo apt-get install imagemagick
magick -size 800x600 xc:black black.png
```
3. Create a video with this image, running:
```bash
sudo apt-get install ffmpeg
ffmpeg -loop 1 -i black.png -t 01:00:00 black.mp4
```
4. Copy `black.png` and `black.mp4` to $HOME folder in the Pi machine.
These files will be used as black background when no video is playing.
You can copy the files using `scp`, with command line or a GUI like Filezilla (port 22).
5. In the same way, copy all the video files of the LARP to a directory.

### Remote projector / music (Raspberry Pi 1/2)

We are using a Raspberry PI receiving commands from the controller device using SSH and connected
to a projector via HDMI and to some speakers if we also want sound.

For my tests I've used a Raspberry PI model B. As we want the minimal SO, install Rasp IO Buster Lite,
which does not include a graphical environment. You can find it for
[arm 64 (newer Raspberrys)](https://downloads.raspberrypi.org/raspios_arm64/images/raspios_arm64-2021-05-28/) or for
[armhf (older Raspberrys)](https://downloads.raspberrypi.org/raspios_lite_armhf/images/raspios_lite_armhf-2023-05-03/).

We want to install a version of the Raspberry OS so old because we are going to use OMXPlayer, a video
player highly optimized for old Pi models, and it only works in those versions.

Once installed, follow these instructions:
1. Start the Raspberry PI, connected to some screen and keyboard, and finish the installation.
2. Run `raspi-config` and:
   * configure the WiFi
   * enable SSH
   * enable auto-login
3. Restart and you can now remove the screen and keyboard if you like and run everything remotely with SSH. When you installed
the image you can modify it, but the default hostname is `raspberrypi.local`
4. [Install OMXPlayer](https://pimylifeup.com/raspberry-pi-omxplayer/). This is a command-line video player optimized
for old versions of the Raspberry PI. All the other video players I've tried run very slow in the Raspberry Pi B.
5. In order to have an all-black screen in the user terminal, add this to the end of `.profile`
```bash
/opt/vc/bin/tvservice -o
/opt/vc/bin/tvservice -p
```
6. Install ffmpeg, if you will also play sounds and/or music remotely using the projector speakers.
```bash
sudo apt-get install ffmpeg
```
Although a different version (still to be decided if better) is using also omxplayer for this.

7. You can try everything by connecting the Raspberry PI to a screen or projector and run commands like:
```bash
ffplay -v 0 -nodisp -autoexit <audio file>
omxplayer -o hdmi <video file>
```
You can copy the audio / video files using `scp`, with command line or a GUI like Filezilla (port 22).

