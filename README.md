# Introduction
This repository contains the web application that handles two factor authentication in Bahmni. This is a plugin based application, where you install a plugin for the third party Short Message Service (SMS) service provider your want to use. [SMS plugins repository](https://github.com/Bahmni/bahmni-sms-plugins) already comes with few plugins.

# Installing a plugin
Download the `jar` file of the plugin you want to install and place it in `/home/bahmni/.bahmni-security/` directory. If the plugin requires any configuration usually the configuration file also resides in the above mentioned directory unless mentioned otherwise in the plugin's documentation.

# Developers
The build tool used in the project is gradle. The project is divided into two sub-projects. One is `sms-interface` and the other is `webapp`. 

* `sms-interface` project contains the contract for the third party SMS gateway plugin. To build the project use `./gradlew clean sms-interface:build` 
* `webapp` project contains the spring boot web application. To build it use `./gradlew clean webapp:build`. To run the web application use `java -Dloader.path=/home/bahmni/.bahmni-security/ -jar two-factor-auth-VERSION.jar`. If you do not specify a valid plugin path the web app will fail to start.
