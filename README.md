<b><center><h1>OpenClassic Client</h></center></b>
==========

The Minecraft Classic client with many improvements and an implementation of the OpenClassicAPI.


<b>Features</b>
--------

 * The OpenClassicAPI and it's features, including custom blocks, custom GUIs, and registering/playing custom sounds.
 * Both the OpenClassic client and server can tell when a player has a custom client/is connected to an OpenClassic server. This can be used for various features in a server.
 * The ability to use the client without the browser.
 * A main menu.
 * A server list, level loading screen, the options screen, and quitting the game all accessible from the menu.
 * Saving online no longer works, so you can save offline.
 * OpenClassic level format.
 * Sounds work and download from a different URL.
 * Access to survival test features.
 * Mipmaping support via the smoothing option in the options.
 * A hacks menu (only a speedhack so far) with hacks that ONLY work with servers with "+hax" in the MOTD (or +ophax if you're an OP).
 * Various other features and improvements.


<b>Building the Source</b>
--------

The source has a maven script for required dependencies, so just run 'mvn clean install' in the project folder.


<b>License</b>
---------

The original Minecraft code is Copyright (C) 2009-2012 Mojang AB.
The OpenClassic Client code is licensed under the <b>[MIT license](http://www.opensource.org/licenses/mit-license.html)</b>.
