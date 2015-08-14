# Aware
<p align="center">
  <img src="http://s29.postimg.org/wiu6dik2v/github_cover.png"/>
</p>

Aware detects motion gestures and lets you customize reactions to them. To say that in a more visually compelling way:

![Tutorial](http://s11.postimg.org/xjohk9x5f/github_tutorial.png)

##Detecting Actions
###Hand wave
Every time the proximity sensor's value changes, it's saved with a timestamp. If the proximity sensor now detects that there's nothing above it and the time since it was covered is within a certain range, it decides that is a hand wave. For more accuracy, the device also has to be facing up to be a hand wave.
###Shake
Every 100ms, the accelerometer's value is saved. If the difference in acceleration is greater than a certain threshold, it counts as a shake. Multiple shakes like this are needed during a short period of time to trigger the shake action.
###Pulled out of pocket
Remember that every proximity sensor value change is stored with a timestamp. If the sensor detects that there's nothing above it and it's been covered up for longer than a certain value, it determines that it was pulled out of a pocket. This can also be triggered if there was something on top of the phone for a while, since orientation checking hasn't yet been implemented.

##Infinite Expandability
With the setup, adding an action is as simple as writing code to detect it, then adding that option to the enum. Adding a reaction is equally easy: just write the method that should be triggered, then add it to the enum and executeReaction method.

##Contributing
Got an action or a reaction you want to add? Go for it! Feel free to message me with any questions you have about the code structure.
