
# Settings

## Hardware settings
Changing any of the hardware settings causes a reset, as the computer must be reset to change the hardware configuration.

### Number of inserted M35FD drives
Controls the number of M35FD floppy drives plugged into the computer. Numbers between 0-2 are allowed.

### Peripheral: UNMS001 Standard Mouse
Controls whether a UNMS001 mouse is plugged into the computer. It is controlled by the host computer's mouse.

### Peripheral: UNAC810 DAC Sound Card
Controls whether a UNAC810 sound card is plugged into the computer. It is used for playback of digital PCM audio.

## UI settings

### Hide cursor on display
Whether to hide the host cursor when it is over the display. This is especially recommended when an UNMS001 is plugged into the device, as supported software will likely display its own cursor.

### Pause emulation when inactive
Whether to pause the emulated CPU and other peripherals when the emulation window goes inactive (to the background).

### Display scale
Allows choosing the size of the display. 1x is the same resolution as the actual display. Any integer scale between 1 and 4 can be chosen. A larger screen is usually more convenient. The scaling is controlled by JavaFX and therefore the scaling method depends on the platform: most likely option is bilinear scaling.

## Emulation settings

### Emulation CPU speed
Allows slowing down or speeding up the emulated CPU. This will only change the speed of the CPU and possibly some attached peripherals - display will still be rendered at the same speed.

### Sound volume
Controls the sound volume of emulated audio devices.

# Setting file
Settings are stored as `settings.json` in the same folder in the working directory, which is usually where the JAR file itself is located.
